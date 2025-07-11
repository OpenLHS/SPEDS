package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.speds.session.internal.contract.MsgType;
import org.junit.jupiter.api.Test;

public class MsgTypeTest {

  @Test
  public void testFromAndToGen_areSymmetric() {
    for (HeaderDto.Msgtype original : HeaderDto.Msgtype.values()) {
      MsgType msgType = MsgType.from(original);
      HeaderDto.Msgtype convertedBack = msgType.toGen();
      assertEquals(original, convertedBack, "Conversion should be symmetric for " + original);
    }
  }

  @Test
  public void testToGenAndFrom_areSymmetric() {
    for (MsgType msgType : MsgType.values()) {
      HeaderDto.Msgtype gen = msgType.toGen();
      MsgType convertedBack = MsgType.from(gen);
      assertEquals(msgType, convertedBack, "Conversion should be symmetric for " + msgType);
    }
  }

  @Test
  public void testFrom_invalidValue_throws() {
    assertThrows(IllegalArgumentException.class, () -> {
      // Cas simple : null
      MsgType.from(null);
    });
  }

  @Test
  public void testToGen_neverReturnsNull() {
    for (MsgType msgType : MsgType.values()) {
      assertNotNull(msgType.toGen(), "toGen() should not return null for " + msgType);
    }
  }
}
