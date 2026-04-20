package com.loopy.core.planning;

import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionBinding;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionResult;
import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.ConditionEvaluator;
import com.loopy.core.condition.ConditionResult;
import com.loopy.core.condition.Precondition;
import com.loopy.core.mailbox.ImmutableMailbox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class DefaultBeliefDeriverTest {

        private final BeliefDeriver deriver = new DefaultBeliefDeriver();

        @Test
        void derivesTypePresenceFromMailbox() {
                record Person(String name) {
                }

                var mailbox = ImmutableMailbox.empty().post(new Person("Alice"));

                var action = new ActionDefinition(
                                "greet", "greet", Set.of(), Set.of(),
                                Set.of(TypeToken.of(Person.class)),
                                Set.of(TypeToken.of(String.class)),
                                0.5, false);
                var agent = new AgentDefinition("test", "test",
                                List.of(new ActionBinding(action, ctx -> ActionResult.Success.of("hi"))),
                                Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(Condition.typePresent(Person.class))).isTrue();
                assertThat(beliefs.holds(Condition.typePresent(String.class))).isFalse();
        }

        @Test
        void evaluatesCustomConditionFromPrecondition() {
                var mailbox = ImmutableMailbox.empty().post("hello");

                var hasGreeting = Condition.custom("hasGreeting");
                ConditionEvaluator evaluator = m -> m.last(TypeToken.of(String.class)).isPresent()
                                ? ConditionResult.satisfied()
                                : ConditionResult.unsatisfied();

                var action = new ActionDefinition("test", "test",
                                Set.of(Precondition.requires(hasGreeting, evaluator)),
                                Set.of(), Set.of(), Set.of(), 0.5, false);
                var agent = new AgentDefinition("test", "test",
                                List.of(new ActionBinding(action, ctx -> null)),
                                Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(hasGreeting)).isTrue();
        }

        @Test
        void undecidableConditionsAreOmitted() {
                var mailbox = ImmutableMailbox.empty();

                var mystery = Condition.custom("mystery");

                var action = new ActionDefinition("test", "test",
                                Set.of(Precondition.requires(mystery)),
                                Set.of(), Set.of(), Set.of(), 0.5, false);
                var agent = new AgentDefinition("test", "test",
                                List.of(new ActionBinding(action, ctx -> null)),
                                Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(mystery)).isFalse();
                assertThat(beliefs.satisfiedConditions()).doesNotContain(mystery);
        }

        @Test
        void emptyMailboxAndAgent_returnsEmptyBeliefs() {
                var mailbox = ImmutableMailbox.empty();
                var action = new ActionDefinition("test", "test", Set.of(), Set.of(),
                                Set.of(), Set.of(), 0.5, false);
                var agent = new AgentDefinition("test", "test",
                                List.of(new ActionBinding(action, ctx -> null)),
                                Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.satisfiedConditions()).isEmpty();
        }
}
