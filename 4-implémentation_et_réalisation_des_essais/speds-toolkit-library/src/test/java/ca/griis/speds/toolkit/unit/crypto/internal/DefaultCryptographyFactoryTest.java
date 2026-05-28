package ca.griis.speds.toolkit.unit.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.toolkit.unit.utilities.ConfigCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class DefaultCryptographyFactoryTest {

  @Test
  void initCipherSuiteAlgo() throws JsonProcessingException {
    final String json = ConfigCreator.createAlgoConfig();
    final DefaultCryptographyFactory factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    assertNotNull(service);
  }

  @Test
  void initCipherSuiteProfile() throws JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final DefaultCryptographyFactory factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    assertNotNull(service);
  }
}
