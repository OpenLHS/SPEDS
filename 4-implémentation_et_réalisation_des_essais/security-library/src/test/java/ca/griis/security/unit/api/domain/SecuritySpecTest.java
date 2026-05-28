package ca.griis.security.unit.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.security.api.domain.spec.SecuritySpec;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SecuritySpecTest {
  private static class TestSecuritySpec extends SecuritySpec {
    private final Map<String, String> params;

    public TestSecuritySpec(String algo, Map<String, String> params) {
      super(algo);
      this.params = params;
    }

    @Override
    public Map<String, String> getParameters() {
      return params;
    }
  }

  @Test
  public void testConstructorAndGetAlgo() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    assertEquals("SHA-256", spec.getAlgo());
  }

  @Test
  public void testGetParameters() {
    Map<String, String> params = Map.of("param1", "value1", "param2", "value2");
    SecuritySpec spec = new TestSecuritySpec("AES", params);

    Map<String, String> result = spec.getParameters();

    assertEquals(2, result.size());
    assertEquals("value1", result.get("param1"));
    assertEquals("value2", result.get("param2"));
  }

  @Test
  public void testEquals_SameObject() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    assertTrue(spec.equals(spec));
  }

  @Test
  public void testEquals_IdenticalObjects() {
    Map<String, String> params1 = Map.of("param1", "value1");
    Map<String, String> params2 = Map.of("param1", "value1");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params1);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params2);

    assertTrue(spec1.equals(spec2));
    assertTrue(spec2.equals(spec1)); // Symétrie
  }

  @Test
  public void testEquals_DifferentAlgo() {
    Map<String, String> params = Map.of("param1", "value1");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-512", params);

    assertFalse(spec1.equals(spec2));
  }

  @Test
  public void testEquals_DifferentParameters() {
    Map<String, String> params1 = Map.of("param1", "value1");
    Map<String, String> params2 = Map.of("param1", "value2");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params1);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params2);

    assertFalse(spec1.equals(spec2));
  }

  @Test
  public void testEquals_Null() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    assertFalse(spec.equals(null));
  }

  @Test
  public void testEquals_DifferentClass() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);
    String otherObject = "Not a SecuritySpec";

    assertFalse(spec.equals(otherObject));
  }

  @Test
  public void testEquals_EmptyParameters() {
    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", Map.of());
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", Map.of());

    assertTrue(spec1.equals(spec2));
  }

  @Test
  public void testEquals_Properties() {
    Map<String, String> params = Map.of("param1", "value1");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params);

    assertEquals(spec1, spec2);
  }

  @Test
  public void testHashCode_EqualObjects() {
    Map<String, String> params1 = Map.of("param1", "value1");
    Map<String, String> params2 = Map.of("param1", "value1");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params1);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params2);

    assertEquals(spec1.hashCode(), spec2.hashCode());
  }

  @Test
  public void testHashCode_DifferentObjects() {
    Map<String, String> params1 = Map.of("param1", "value1");
    Map<String, String> params2 = Map.of("param1", "value2");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params1);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params2);

    assertNotEquals(spec1.hashCode(), spec2.hashCode());
  }

  @Test
  public void testHashCode_Consistency() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    int hashCode1 = spec.hashCode();
    int hashCode2 = spec.hashCode();
    int hashCode3 = spec.hashCode();

    assertEquals(hashCode1, hashCode2);
    assertEquals(hashCode2, hashCode3);
  }

  @Test
  public void testToString_ContainsAlgo() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    String result = spec.toString();

    assertNotNull(result);
    assertTrue(result.contains("SHA-256"));
    assertTrue(result.contains("algo"));
  }

  @Test
  public void testToString_ContainsParameters() {
    Map<String, String> params = Map.of("param1", "value1", "param2", "value2");
    SecuritySpec spec = new TestSecuritySpec("AES", params);

    String result = spec.toString();

    assertNotNull(result);
    assertTrue(result.contains("parameters"));
    assertTrue(result.contains("param1"));
    assertTrue(result.contains("value1"));
  }

  @Test
  public void testToString_EmptyParameters() {
    SecuritySpec spec = new TestSecuritySpec("SHA-256", Map.of());

    String result = spec.toString();

    assertNotNull(result);
    assertTrue(result.contains("algo"));
    assertTrue(result.contains("SHA-256"));
  }

  @Test
  public void testToString_NoClassName() {
    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec = new TestSecuritySpec("SHA-256", params);

    String result = spec.toString();

    assertNotNull(result);
    assertFalse(result.contains("TestSecuritySpec"));
    assertFalse(result.contains("SecuritySpec"));
  }

  @Test
  public void testDifferentSubclasses() {
    class AnotherSecuritySpec extends SecuritySpec {
      private final Map<String, String> params;

      public AnotherSecuritySpec(String algo, Map<String, String> params) {
        super(algo);
        this.params = params;
      }

      @Override
      public Map<String, String> getParameters() {
        return params;
      }
    }

    Map<String, String> params = Map.of("param1", "value1");
    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params);
    SecuritySpec spec2 = new AnotherSecuritySpec("SHA-256", params);

    assertTrue(spec1.equals(spec2));
    assertEquals(spec1.hashCode(), spec2.hashCode());
  }

  @Test
  public void testComplexParameters() {
    Map<String, String> params = new HashMap<>();
    params.put("algorithm", "AES/GCM/NoPadding");
    params.put("keySize", "256");
    params.put("ivSize", "12");
    params.put("tagSize", "128");

    SecuritySpec spec = new TestSecuritySpec("AES", params);

    assertEquals("AES", spec.getAlgo());
    assertEquals(4, spec.getParameters().size());
    assertEquals("AES/GCM/NoPadding", spec.getParameters().get("algorithm"));
    assertEquals("256", spec.getParameters().get("keySize"));
  }

  @Test
  public void testEqualsHashCodeContract() {
    Map<String, String> params1 = Map.of("param1", "value1");
    Map<String, String> params2 = Map.of("param1", "value1");
    Map<String, String> params3 = Map.of("param1", "value2");

    SecuritySpec spec1 = new TestSecuritySpec("SHA-256", params1);
    SecuritySpec spec2 = new TestSecuritySpec("SHA-256", params2);
    SecuritySpec spec3 = new TestSecuritySpec("SHA-256", params3);

    // Si equals, alors même hashCode
    if (spec1.equals(spec2)) {
      assertEquals(spec1.hashCode(), spec2.hashCode());
    }

    // Pas equals, probablement hashCode différent
    if (!spec1.equals(spec3)) {
      // Note: ce n'est pas garanti, mais probable
      assertNotEquals(spec1.hashCode(), spec3.hashCode());
    }
  }

  @Test
  public void testRealWorldScenarios() {
    // Scénario 1: SHA-256 hash spec
    SecuritySpec sha256Spec = new TestSecuritySpec("SHA-256",
        Map.of("mdName", "SHA-256", "provider", "SUN"));

    assertEquals("SHA-256", sha256Spec.getAlgo());
    assertEquals("SHA-256", sha256Spec.getParameters().get("mdName"));

    // Scénario 2: AES encryption spec
    SecuritySpec aesSpec = new TestSecuritySpec("AES/GCM/NoPadding",
        Map.of("keySize", "256", "ivSize", "12", "tagSize", "128"));

    assertEquals("AES/GCM/NoPadding", aesSpec.getAlgo());

    // Scénario 3: DRBG spec
    SecuritySpec drbgSpec = new TestSecuritySpec("DRBG",
        Map.of("strength", "256", "capability", "PR_AND_RESEED"));

    assertEquals("DRBG", drbgSpec.getAlgo());

    // Tous différents
    assertNotEquals(sha256Spec, aesSpec);
    assertNotEquals(aesSpec, drbgSpec);
    assertNotEquals(sha256Spec, drbgSpec);
  }
}
