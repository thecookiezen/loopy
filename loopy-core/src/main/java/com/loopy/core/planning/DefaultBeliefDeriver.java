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
 * Preconditions - evaluates each precondition's evaluator against the mailbox.
 * {@link Condition.TypePresent} preconditions (auto-generated from input types)
 * check mailbox presence; {@link Condition.Custom} preconditions use their
 * provided evaluator (or default to undecidable).
 * Output types - checks whether output types declared by the agent's actions
 * are already present in the mailbox (e.g. from a previous action's execution).
 */
public final class DefaultBeliefDeriver implements BeliefDeriver {

    @Override
    public Beliefs derive(Mailbox mailbox, AgentDefinition agent) {
        var satisfied = new HashSet<Condition>();

        for (var action : agent.actions()) {
            for (var precondition : action.definition().preconditions()) {
                switch (precondition.evaluator().evaluate(mailbox)) {
                    case ConditionResult.Satisfied _ -> satisfied.add(precondition.condition());
                    case ConditionResult.Unsatisfied _ -> satisfied.remove(precondition.condition());
                    case ConditionResult.Undecidable _ -> {
                    }
                }
            }

            for (var outputType : action.definition().outputTypes()) {
                if (mailbox.last(outputType).isPresent()) {
                    satisfied.add(Condition.typePresent(outputType));
                }
            }

            for (var inputType : action.definition().inputTypes()) {
                if (mailbox.last(inputType).isPresent()) {
                    satisfied.add(Condition.typePresent(inputType));
                }
            }
        }

        return new Beliefs(satisfied);
    }
}
