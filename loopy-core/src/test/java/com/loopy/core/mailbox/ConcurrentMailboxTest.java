package com.loopy.core.mailbox;

import com.loopy.core.TypeToken;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ConcurrentMailboxTest {

    @Test
    void empty_hasNoMessages() {
        assertThat(ConcurrentMailbox.empty().messages()).isEmpty();
    }

    @Test
    void post_addsMessage() {
        var mb = ConcurrentMailbox.empty();
        mb.post("hello");
        assertThat(mb.messages()).containsExactly("hello");
    }

    @Test
    void last_returnsLatestOfType() {
        var mb = ConcurrentMailbox.empty();
        mb.post("first");
        mb.post(42);
        mb.post("second");
        assertThat(mb.last(TypeToken.of(String.class))).contains("first");
        assertThat(mb.last(TypeToken.of(Integer.class))).contains(42);
    }

    @Test
    void last_emptyWhenTypeNotFound() {
        var mb = ConcurrentMailbox.empty();
        mb.post("hello");
        assertThat(mb.last(TypeToken.of(Integer.class))).isEmpty();
    }

    @Test
    void allOfType_returnsAllMatching() {
        var mb = ConcurrentMailbox.empty();
        mb.post("a");
        mb.post(1);
        mb.post("b");
        assertThat(mb.allOfType(TypeToken.of(String.class))).containsExactly("a", "b");
    }

    @Test
    void bind_makesNamedRetrievable() {
        var mb = ConcurrentMailbox.empty();
        mb.bind("key", "value");
        assertThat(mb.get("key", TypeToken.of(String.class))).contains("value");
        assertThat(mb.messages()).contains("value");
    }

    @Test
    void from_copiesFromOtherMailbox() {
        var immutable = ImmutableMailbox.empty().post("hello").post(42);
        var concurrent = ConcurrentMailbox.from(immutable);
        assertThat(concurrent.messages()).containsExactly("hello", 42);
    }

    @Test
    void messages_returnsSnapshot() {
        var mb = ConcurrentMailbox.empty();
        mb.post("a");
        var snapshot = mb.messages();
        mb.post("b");
        assertThat(snapshot).containsExactly("a");
        assertThat(mb.messages()).hasSize(2);
    }
}
