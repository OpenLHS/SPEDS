package ca.griis.security.unit.internal.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.internal.spec.SpecProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SpecProviderTest {
  private final SpecProvider specProvider = new SpecProvider();

  @Test
  public void testSpecProviderStrongest() {
    Map<String, SecuritySpec> specProviderMap =
        specProvider.getProfilSecuritySpecs(SecurityProfile.Strongest);

    List<String> keys = new ArrayList<>(specProviderMap.keySet());

    String actualSpecString = specProviderMap.keySet().stream()
        .skip(3)
        .findFirst()
        .orElseThrow();

    SecuritySpec actualSpec = specProviderMap.get(actualSpecString);

    final Map<String, String> params = actualSpec.getParameters();

    assertEquals("CSPRNG", keys.get(0));
    assertEquals("25519-DH-Gen", keys.get(1));
    assertEquals("RSA-Encipherment-Signature-Gen", keys.get(2));
    assertEquals("RSA-Signature", keys.get(3));
    assertEquals("25519-DH", keys.get(4));
    assertEquals("SHA-512", params.get("mdName"));
  }

  @Test
  public void testSpecProviderEfficient() {
    Map<String, SecuritySpec> specProviderMap =
        specProvider.getProfilSecuritySpecs(SecurityProfile.Efficient);

    String actualSpecString = specProviderMap.keySet().stream()
        .skip(3)
        .findFirst()
        .orElseThrow();

    SecuritySpec actualSpec = specProviderMap.get(actualSpecString);

    final Map<String, String> params = actualSpec.getParameters();

    List<String> keys = new ArrayList<>(specProviderMap.keySet());
    assertEquals("CSPRNG", keys.get(0));
    assertEquals("25519-DH-Gen", keys.get(1));
    assertEquals("RSA-Encipherment-Signature-Gen", keys.get(2));
    assertEquals("RSA-Signature", keys.get(3));
    assertEquals("25519-DH", keys.get(4));
    assertEquals("SHA-256", params.get("mdName"));
  }
}
