package com.loopy.core.planning;

import com.loopy.core.agent.AgentDefinition;
import com.loopy.core.condition.Condition;
import com.loopy.core.condition.ConditionResult;
import com.loopy.core.mailbox.Mailbox;

import java.util.HashSet;

/**
 * Default belief derivation strategy.
 *
 * Derives beliefs from two sources:
 * Type presence - for each input/output type declared by the agent's
 * actions, adds a {@link Condition.TypePresent} belief if the mailbox
 * contains a message of that type.
 * Condition evaluators evaluates each {@link ConditionBinding}
 * registered on the agent and adds the condition if satisfied
 * (undecidable conditions are omitted).
 */
public final class DefaultBeliefDeriver implements BeliefDeriver {

    @Override
    public Beliefs derive(Mailbox mailbox, AgentDefinition agent) {
        var satisfied = new HashSet<Condition>();

        for (var action : agent.actions()) {
            for (var outputType : action.definition().outputTypes()) {
                var condition = Condition.typePresent(outputType);
                if (mailbox.last(outputType).isPresent()) {
                    satisfied.add(condition);
                }
            }
            for (var inputType : action.definition().inputTypes()) {
                var condition = Condition.typePresent(inputType);
                if (mailbox.last(inputType).isPresent()) {
                    satisfied.add(condition);
                }
            }
        }

        for (var binding : agent.conditions()) {
            switch (binding.evaluator().evaluate(mailbox)) {
                case ConditionResult.Satisfied _ -> satisfied.add(binding.condition());
                case ConditionResult.Unsatisfied _ -> satisfied.remove(binding.condition());
                case ConditionResult.Undecidable _ -> {
                }
            }
        }

        return new Beliefs(satisfied);
    }
}
