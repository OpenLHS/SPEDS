package ca.griis.speds.network.unit.serialization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.network.serialization.SharedObjectMapper;
import org.junit.jupiter.api.Test;

public class SharedObjectMapperTest {
  @Test
  public void mapperTest() throws Exception {
    assertNotNull(SharedObjectMapper.getInstance().getMapper());
  }
}
