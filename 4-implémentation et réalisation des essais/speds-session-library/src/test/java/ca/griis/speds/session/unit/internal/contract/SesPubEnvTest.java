package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.pub.IdentityDto;
import ca.griis.speds.session.internal.contract.SesPubEnvDto;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SesPubEnvTest {

  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
  }

  @Test
  public void makeSesPub() {
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto("a", UUID.randomUUID());

    assertNotNull(sesPubEnvDto);
  }

  @Test
  public void testSesPubSerializationDeserialization() throws Exception {
    // given
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto("a", UUID.randomUUID());

    // when
    String json = objectMapper.writeValueAsString(sesPubEnvDto);
    SesPubEnvDto restored = objectMapper.readValue(json, SesPubEnvDto.class);

    // Then
    assertEquals(sesPubEnvDto, restored);
  }

  @Test
  public void testSesPubSerializationDeserializationToIdentity() throws Exception {
    // given
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto("a", UUID.randomUUID());

    // when
    String json = objectMapper.writeValueAsString(sesPubEnvDto);
    IdentityDto restored = objectMapper.readValue(json, IdentityDto.class);

    // Then
    assertEquals(sesPubEnvDto, restored);
  }
}
