package ca.griis.speds.link.unit.internal.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.link.internal.security.JavaTrustStore;
import org.junit.jupiter.api.Test;

public class JavaTrustStoreFactoryTest {
  @Test
  public void staticDefaultTrustInitialization() {
    assertNotNull(JavaTrustStore.getDefaultTrustManager());
  }

  @Test
  public void staticDefaultTrustFactoryInitialization() {
    assertNotNull(JavaTrustStore.getDefaultTrustFactoryManager());
  }
}
