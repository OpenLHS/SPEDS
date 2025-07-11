package ca.griis.speds.session.unit.internal.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.PiduContext;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PiduTest {

  ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
  }

  @Test
  void shouldCreateEmptyPidu() {
    Pidu pidu = new Pidu();
    assertNotNull(pidu);
  }

  @Test
  public void makePidu() {
    Context23Dto context23Dto = new PiduContext("a", "a", "a", "a", UUID.randomUUID(), false);
    Pidu pidu = new Pidu(context23Dto, "asd");

    assertNotNull(pidu);
  }

  @Test
  public void testPiduSerializationDeserialization() throws Exception {
    // given
    PiduContext piduContext = new PiduContext("a", "a", "a", "a", UUID.randomUUID(), false);
    Pidu original = new Pidu(piduContext, "123");

    // when
    String json = objectMapper.writeValueAsString(original);
    Pidu restored = objectMapper.readValue(json, Pidu.class);

    // Then
    assertEquals(original, restored);
  }

  @Test
  public void testPiduSerializationDeserializationToIdu() throws Exception {
    // given
    PiduContext piduContext = new PiduContext("a", "a", "a", "a", UUID.randomUUID(), false);
    Pidu original = new Pidu(piduContext, "123");

    // when
    String json = objectMapper.writeValueAsString(original);
    InterfaceDataUnit23Dto restored = objectMapper.readValue(json, InterfaceDataUnit23Dto.class);

    // Then
    assertEquals(original, restored);
  }
}
