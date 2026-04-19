package com.loopy.core.action;

import com.loopy.core.TypeToken;
import com.loopy.core.mailbox.ImmutableMailbox;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ActionInputsTest {

    @Test
    void resolve_extractsFromMailbox() {
        record Data(String value) {}
        var mailbox = ImmutableMailbox.empty().post(new Data("hello"));
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(Data.class)), Set.of(), 0.5, false);

        var inputs = ActionInputs.resolve(action, mailbox);
        assertThat(inputs.size()).isEqualTo(1);
        assertThat(inputs.get(Data.class).value()).isEqualTo("hello");
    }

    @Test
    void resolve_throwsWhenMissing() {
        record MissingData() {}
        var mailbox = ImmutableMailbox.empty();
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(MissingData.class)), Set.of(), 0.5, false);

        assertThatIllegalStateException()
                .isThrownBy(() -> ActionInputs.resolve(action, mailbox))
                .withMessageContaining("MissingData");
    }

    @Test
    void get_throwsForUndeclaredType() {
        record Data(String value) {}
        var mailbox = ImmutableMailbox.empty().post(new Data("x"));
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(Data.class)), Set.of(), 0.5, false);

        var inputs = ActionInputs.resolve(action, mailbox);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> inputs.get(String.class))
                .withMessageContaining("No input of type");
    }

    @Test
    void multipleInputTypes() {
        record A(String v) {}
        record B(int n) {}
        var mailbox = ImmutableMailbox.empty().post(new A("x")).post(new B(42));
        var action = new ActionDefinition("a", "a", Set.of(), Set.of(),
                Set.of(TypeToken.of(A.class), TypeToken.of(B.class)), Set.of(), 0.5, false);

        var inputs = ActionInputs.resolve(action, mailbox);
        assertThat(inputs.size()).isEqualTo(2);
        assertThat(inputs.get(A.class).v()).isEqualTo("x");
        assertThat(inputs.get(B.class).n()).isEqualTo(42);
    }
}
