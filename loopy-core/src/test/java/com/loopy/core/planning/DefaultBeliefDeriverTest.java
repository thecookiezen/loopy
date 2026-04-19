package com.loopy.core.planning;

import com.loopy.core.TypeToken;
import com.loopy.core.action.ActionBinding;
import com.loopy.core.action.ActionDefinition;
import com.loopy.core.action.ActionResult;
import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.ConditionBinding;
import com.loopy.core.condition.ConditionResult;
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
                                Set.of(), Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(Condition.typePresent(Person.class))).isTrue();
                assertThat(beliefs.holds(Condition.typePresent(String.class))).isFalse();
        }

        @Test
        void evaluatesConditionBindings() {
                var mailbox = ImmutableMailbox.empty().post("hello");

                var hasGreeting = new Condition.Custom("hasGreeting");
                var binding = ConditionBinding.custom(hasGreeting,
                                m -> m.last(TypeToken.of(String.class)).isPresent()
                                                ? ConditionResult.satisfied()
                                                : ConditionResult.unsatisfied());

                var agent = new AgentDefinition("test", "test",
                                List.of(), Set.of(), Set.of(binding));

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(hasGreeting)).isTrue();
        }

        @Test
        void undecidableConditionsAreOmitted() {
                var mailbox = ImmutableMailbox.empty();

                var mystery = new Condition.Custom("mystery");
                var binding = ConditionBinding.custom(mystery,
                                m -> ConditionResult.undecidable("not enough data"));

                var agent = new AgentDefinition("test", "test",
                                List.of(), Set.of(), Set.of(binding));

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.holds(mystery)).isFalse();
                assertThat(beliefs.satisfiedConditions()).doesNotContain(mystery);
        }

        @Test
        void emptyMailboxAndAgent_returnsEmptyBeliefs() {
                var mailbox = ImmutableMailbox.empty();
                var agent = new AgentDefinition("test", "test",
                                List.of(), Set.of(), Set.of());

                var beliefs = deriver.derive(mailbox, agent);

                assertThat(beliefs.satisfiedConditions()).isEmpty();
        }
}
