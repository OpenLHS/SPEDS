package ca.griis.logger.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
public class GriisLoggerFactoryTest {
  @Test
  public void getLoggerTest() {
    ReflectionTestUtils.setField(GriisLoggerFactory.class, "hasInit", false);
    GriisLogger logger = GriisLoggerFactory.getLogger(this.getClass());
    assertNotNull(logger);
  }

  @SpringBootApplication
  static class TestConfiguration {
  }
}
