package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.speds.session.internal.contract.PiduContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PiduContextTest {
  @Test
  void shouldCreateEmptyPiduContext() {
    PiduContext context = new PiduContext();
    assertNotNull(context);
  }

  @Test
  void shouldCreatePiduContextWithParameters() {
    String pga = "PGA";
    String sourceCode = "SRC";
    String destinationCode = "DST";
    String sdek = "SDEK";
    UUID trackingNumber = UUID.randomUUID();
    Object options = new Object();

    PiduContext context = new PiduContext(
        pga, sourceCode, destinationCode, sdek,
        trackingNumber, options);

    assertNotNull(context);
    assertInstanceOf(Context23Dto.class, context);
  }

  @Test
  void shouldBeAssignableToContext23Dto() {
    PiduContext context = new PiduContext();
    Context23Dto parent = (Context23Dto) context;
    assertNotNull(parent);
    assertInstanceOf(PiduContext.class, parent);
  }
}
