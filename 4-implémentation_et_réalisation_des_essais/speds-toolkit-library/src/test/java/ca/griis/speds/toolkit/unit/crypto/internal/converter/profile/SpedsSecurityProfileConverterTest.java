package ca.griis.speds.toolkit.unit.crypto.internal.converter.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.profile.SpedsSecurityProfileConverter;
import org.junit.jupiter.api.Test;

public class SpedsSecurityProfileConverterTest {
  @Test
  void apply() {
    SpedsSecurityProfileConverter converter = new SpedsSecurityProfileConverter();

    assertEquals(converter.apply(SpedsConfigItemDto.SecurityProfile.STRONG),
        ca.griis.security.api.domain.SecurityProfile.Strongest);
    assertEquals(converter.apply(SpedsConfigItemDto.SecurityProfile.EFFICIENT),
        ca.griis.security.api.domain.SecurityProfile.Efficient);
  }
}
