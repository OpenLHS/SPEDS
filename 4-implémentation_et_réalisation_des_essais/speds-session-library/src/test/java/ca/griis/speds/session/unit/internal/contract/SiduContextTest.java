package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.speds.session.internal.contract.SiduContext;
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
    Object options = new Object();

    SiduContext context = new SiduContext(
        sourceCode, destinationCode, sourceIri,
        Context34Dto.Service.DELEGATE,
        Context34Dto.ServicePrimitive.CONFIRM,
        destinationIri, options);

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
