package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.speds.session.internal.contract.SiduContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SiduContextTest {
  @Test
  void shouldCreateEmptySiduContext() {
    SiduContext context = new SiduContext();
    assertNotNull(context);
  }

  @Test
  void shouldCreateSiduContextWithParameters() {
    String sourceCode = "SRC";
    String destinationCode = "DST";
    String sourceIri = "http://source";
    String destinationIri = "http://destination";
    UUID trackingNumber = UUID.randomUUID();
    Object options = new Object();

    SiduContext context = new SiduContext(
        sourceCode, destinationCode, sourceIri,
        trackingNumber, destinationIri, options);

    assertNotNull(context);
    assertInstanceOf(Context34Dto.class, context);
  }

  @Test
  void shouldBeAssignableToContext34Dto() {
    SiduContext context = new SiduContext();
    assertNotNull(context);
    assertInstanceOf(SiduContext.class, context);
  }
}
