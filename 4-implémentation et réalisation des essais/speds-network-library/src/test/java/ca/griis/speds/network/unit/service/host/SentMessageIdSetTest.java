package ca.griis.speds.network.unit.service.host;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.speds.network.service.host.SentMessageIdSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SentMessageIdSetTest {

  private SentMessageIdSet messageIds = new SentMessageIdSet();

  @BeforeEach
  public void setUp() throws Exception {
    messageIds.clearMessageIds();
  }

  @Test
  public void testAddMessageIdAndContains() {
    String msgId = UUID.randomUUID().toString();
    messageIds.addMessageId(msgId);
    assertTrue(messageIds.containsMessageId(msgId));
  }

  @Test
  public void testRemoveMessageId() {
    String msgId = UUID.randomUUID().toString();
    messageIds.addMessageId(msgId);

    messageIds.removeMessageId(msgId);
    assertFalse(messageIds.containsMessageId(msgId));
  }

  @Test
  public void testClearMessageIds() {
    String msgId = UUID.randomUUID().toString();
    messageIds.addMessageId(msgId);

    messageIds.clearMessageIds();

    assertFalse(messageIds.containsMessageId(msgId));
  }
}
