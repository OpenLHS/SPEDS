package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit34Dto;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SiduTest {

  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
  }

  @Test
  public void makeSidu() {
    Context34Dto context34Dto = new SiduContext("a", "b", "c", UUID.randomUUID(), "d", false);
    Sidu sidu = new Sidu(context34Dto, "asd");

    assertNotNull(sidu);
  }

  @Test
  public void testSiduSerializationDeserialization() throws Exception {
    // given
    Context34Dto context34Dto = new SiduContext("a", "b", "c", UUID.randomUUID(), "d", false);
    Sidu given = new Sidu(context34Dto, "asd");

    // when
    String json = objectMapper.writeValueAsString(given);
    Sidu restored = objectMapper.readValue(json, Sidu.class);

    // Then
    assertEquals(given, restored);
  }

  @Test
  public void testPiduSerializationDeserializationToIdu() throws Exception {
    // given
    Context34Dto context34Dto = new SiduContext("a", "b", "c", UUID.randomUUID(), "d", false);
    Sidu given = new Sidu(context34Dto, "asd");

    // when
    String json = objectMapper.writeValueAsString(given);
    InterfaceDataUnit34Dto restored = objectMapper.readValue(json, InterfaceDataUnit34Dto.class);

    // Then
    assertEquals(given, restored);
  }
}
