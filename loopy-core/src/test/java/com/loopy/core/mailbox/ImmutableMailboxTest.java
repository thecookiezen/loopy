package com.loopy.core.mailbox;

import com.loopy.core.TypeToken;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ImmutableMailboxTest {

    @Test
    void empty_hasNoMessages() {
        var mailbox = ImmutableMailbox.empty();
        assertThat(mailbox.messages()).isEmpty();
    }

    @Test
    void post_addsMessage() {
        var mailbox = ImmutableMailbox.empty().post("hello");
        assertThat(mailbox.messages()).containsExactly("hello");
    }

    @Test
    void post_returnsNewInstance() {
        var original = ImmutableMailbox.empty();
        var updated = original.post("hello");
        assertThat(original.messages()).isEmpty();
        assertThat(updated.messages()).containsExactly("hello");
    }

    @Test
    void last_returnsLatestOfType() {
        var mailbox = ImmutableMailbox.empty()
                .post("first")
                .post(42)
                .post("second");
        assertThat(mailbox.last(TypeToken.of(String.class))).contains("first");
        assertThat(mailbox.last(TypeToken.of(Integer.class))).contains(42);
    }

    @Test
    void last_returnsEmptyIfTypeNotFound() {
        var mailbox = ImmutableMailbox.empty().post("hello");
        assertThat(mailbox.last(TypeToken.of(Integer.class))).isEmpty();
    }

    @Test
    void allOfType_returnsAllMatching() {
        var mailbox = ImmutableMailbox.empty()
                .post("a").post(1).post("b").post(2).post("c");
        assertThat(mailbox.allOfType(TypeToken.of(String.class))).containsExactly("a", "b", "c");
        assertThat(mailbox.allOfType(TypeToken.of(Integer.class))).containsExactly(1, 2);
    }

    @Test
    void bind_makesRetrievableByName() {
        var mailbox = ImmutableMailbox.empty().bind("key", "value");
        assertThat(mailbox.get("key", TypeToken.of(String.class))).contains("value");
        assertThat(mailbox.messages()).containsExactly("value");
    }
}
