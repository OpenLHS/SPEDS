package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.speds.session.internal.contract.SpduHeader;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SpduHeaderTest {

  @Test
  void shouldCreateEmptySpduHeader() {
    SpduHeader header = new SpduHeader();
    assertNotNull(header);
  }

  @Test
  void shouldCreateSpduHeaderWithParameters() {
    HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.SES_PUB_ENV;
    UUID id = UUID.randomUUID();
    Object parameters = new Object();
    SPEDSDto speds = new SPEDSDto();
    SpduHeader header = new SpduHeader(msgtype, id, parameters, speds);

    assertNotNull(header);
    assertInstanceOf(HeaderDto.class, header);
  }

  @Test
  void shouldBeAssignableToHeaderDto() {
    SpduHeader header = new SpduHeader();
    assertNotNull(header);
    assertInstanceOf(SpduHeader.class, header);
  }
}
