package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.speds.session.internal.contract.SesPubEnvDto;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpduTest {

  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
  }

  @Test
  public void makeSpdu() {
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto();
    Spdu sdpu = new Spdu(new HeaderDto(HeaderDto.Msgtype.SES_PUB_ENV, UUID.randomUUID(), false,
        new SPEDSDto("a", "b")), "c", sesPubEnvDto);

    assertNotNull(sdpu);
  }

  @Test
  public void testSpduSerializationDeserialization() throws Exception {
    // given
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto();
    String serialSesPub = objectMapper.writeValueAsString(sesPubEnvDto);
    Spdu sdpu = new Spdu(new HeaderDto(HeaderDto.Msgtype.SES_PUB_ENV, UUID.randomUUID(), false,
        new SPEDSDto("a", "b")), "c", serialSesPub);

    // when
    String json = objectMapper.writeValueAsString(sdpu);
    Spdu restored = objectMapper.readValue(json, Spdu.class);
    SesPubEnvDto restoredSesPubEnvDto =
        objectMapper.readValue((String) restored.getContent(), SesPubEnvDto.class);

    // Then
    assertEquals(sdpu, restored);
    assertEquals(sesPubEnvDto, restoredSesPubEnvDto);
  }

  @Test
  public void uuidContentTest() throws JsonProcessingException {
    // given
    UUID sdu = UUID.randomUUID();
    String serialSesPub = objectMapper.writeValueAsString(sdu);
    Spdu spdu = new Spdu(new HeaderDto(HeaderDto.Msgtype.SES_PUB_ENV, UUID.randomUUID(), false,
        new SPEDSDto("a", "b")), "c", serialSesPub);

    // when
    String json = objectMapper.writeValueAsString(spdu);
    Spdu restored = objectMapper.readValue(json, Spdu.class);
    UUID restoredUUID = objectMapper.readValue((String) restored.getContent(), UUID.class);

    // Then
    assertEquals(spdu, restored);
    assertEquals(sdu, restoredUUID);
  }
}
