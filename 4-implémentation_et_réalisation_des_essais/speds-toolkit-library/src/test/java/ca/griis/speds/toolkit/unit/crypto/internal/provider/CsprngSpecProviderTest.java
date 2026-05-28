package ca.griis.speds.toolkit.unit.crypto.internal.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.speds.toolkit.crypto.internal.provider.CsprngSpecProvider;
import org.junit.jupiter.api.Test;

public class CsprngSpecProviderTest {
  @Test
  void getSpec() {
    var provider = new CsprngSpecProvider();
    var spec = provider.getSpec();
    assertEquals(spec.getAlgo(), "DRBG");
  }
}
