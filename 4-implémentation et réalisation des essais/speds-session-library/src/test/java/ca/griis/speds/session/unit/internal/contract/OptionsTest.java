package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.speds.session.internal.contract.Options;
import org.junit.jupiter.api.Test;

public class OptionsTest {


  @Test
  void shouldCreateOptionsInstance() {
    Options options = new Options();
    assertNotNull(options);
  }

  @Test
  void shouldBeAssignableToOptionsDto() {
    Options options = new Options();
    OptionsDto dto = options;
    assertNotNull(dto);
    assertInstanceOf(Options.class, dto);
  }
}
