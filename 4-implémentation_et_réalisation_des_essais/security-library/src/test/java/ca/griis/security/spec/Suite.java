package ca.griis.security.spec;

import static org.mockito.MockitoAnnotations.openMocks;

import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.spec.env.CryptoSpecId;
import java.security.KeyPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class Suite {
  private Cases cases;

  @BeforeEach
  public void setup() {
    openMocks(this);

    var securityService = new DefaultSecurityService();
    this.cases = new Cases(securityService);
  }

  @Test
  public void e01() {
    cases.ct_01();
    cases.ct_02();
  }

  @Test
  public void e02() {
    var digest = cases.ct_03(SecurityProfile.Strongest);
    cases.ct_04(SecurityProfile.Strongest, digest);
  }

  @Test
  public void e03() {
    cases.ct_05(SecurityProfile.Strongest);
  }

  @Test
  public void e04() {
    var digest = cases.ct_03(SecurityProfile.Efficient);
    cases.ct_04(SecurityProfile.Efficient, digest);
  }

  @Test
  public void e05() {
    cases.ct_05(SecurityProfile.Efficient);
  }

  @Test
  public void e06() {
    var secretKey = cases.ct_06(SecurityProfile.Strongest);
    var encryptedData = cases.ct_07(SecurityProfile.Strongest, secretKey);
    cases.ct_08(SecurityProfile.Strongest, secretKey, encryptedData);
  }

  @Test
  public void e07() {
    var secretKey = cases.ct_06(SecurityProfile.Strongest);
    var encryptedData = cases.ct_07(SecurityProfile.Strongest, secretKey);
    cases.ct_09(SecurityProfile.Strongest, encryptedData);
    cases.ct_10(SecurityProfile.Strongest, secretKey);
  }

  @Test
  public void e08() {
    var secretKey = cases.ct_06(SecurityProfile.Efficient);
    var encryptedData = cases.ct_07(SecurityProfile.Efficient, secretKey);
    cases.ct_08(SecurityProfile.Efficient, secretKey, encryptedData);
  }

  @Test
  public void e09() {
    var secretKey = cases.ct_06(SecurityProfile.Efficient);
    var encryptedData = cases.ct_07(SecurityProfile.Efficient, secretKey);
    cases.ct_09(SecurityProfile.Efficient, encryptedData);
    cases.ct_10(SecurityProfile.Efficient, secretKey);
  }

  @Test
  public void e10() {
    cases.ct_11(SecurityProfile.Strongest, CryptoSpecId.ed25519Gen.value());
  }

  @Test
  public void e11() {
    cases.ct_11(SecurityProfile.Strongest, CryptoSpecId.x25519Gen.value());
  }

  @Test
  public void e12() {
    KeyPair keyPair = cases.ct_11(SecurityProfile.Strongest, CryptoSpecId.rsaGen.value());
    var encryptedData = cases.ct_12(SecurityProfile.Strongest, keyPair);
    cases.ct_13(SecurityProfile.Strongest, keyPair, encryptedData);
  }

  @Test
  public void e13() {
    KeyPair keyPair = cases.ct_11(SecurityProfile.Efficient, CryptoSpecId.rsaGen.value());
    var encryptedData = cases.ct_12(SecurityProfile.Efficient, keyPair);
    cases.ct_13(SecurityProfile.Efficient, keyPair, encryptedData);
  }

  @Test
  public void e14() {
    KeyPair keyPair = cases.ct_11(SecurityProfile.Strongest, CryptoSpecId.rsaGen.value());
    var encryptedData = cases.ct_12(SecurityProfile.Strongest, keyPair);
    cases.ct_14(SecurityProfile.Strongest, encryptedData);
    cases.ct_15(SecurityProfile.Efficient, keyPair);
  }

  @Test
  public void e15() {
    KeyPair keyPair = cases.ct_11(SecurityProfile.Efficient, CryptoSpecId.rsaGen.value());
    var encryptedData = cases.ct_12(SecurityProfile.Efficient, keyPair);
    cases.ct_14(SecurityProfile.Efficient, encryptedData);
    cases.ct_15(SecurityProfile.Efficient, keyPair);
  }

  @Test
  public void e16() {
    cases.ct_16(SecurityProfile.Strongest);
  }

  @Test
  public void e17() {
    cases.ct_16(SecurityProfile.Efficient);
  }

  @Test
  public void e18() {
    var result = cases.ct_17(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value());
    cases.ct_18(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(), result.getKey(),
        result.getValue());
  }

  @Test
  public void e19() {
    var result = cases.ct_17(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value());
    cases.ct_18(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(), result.getKey(),
        result.getValue());
  }

  @Test
  public void e20() {
    var result = cases.ct_17(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(),
        CryptoSpecId.ed25519Gen.value());
    cases.ct_18(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(), result.getKey(),
        result.getValue());
  }

  @Test
  public void e21() {
    var result = cases.ct_17(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value());
    cases.ct_19(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(), result.getKey());
    cases.ct_20(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(), result.getKey(),
        result.getValue());
    cases.ct_21(SecurityProfile.Strongest, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value(), result.getValue());
  }

  @Test
  public void e22() {
    var result = cases.ct_17(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value());
    cases.ct_19(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(), result.getKey());
    cases.ct_20(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(), result.getKey(),
        result.getValue());
    cases.ct_21(SecurityProfile.Efficient, CryptoSpecId.rsaSignature.value(),
        CryptoSpecId.rsaGen.value(), result.getValue());
  }

  @Test
  public void e23() {
    var result = cases.ct_17(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(),
        CryptoSpecId.ed25519Gen.value());
    cases.ct_19(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(), result.getKey());
    cases.ct_20(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(), result.getKey(),
        result.getValue());
    cases.ct_21(SecurityProfile.Strongest, CryptoSpecId.ed25519Signature.value(),
        CryptoSpecId.ed25519Gen.value(), result.getValue());
  }

  @Test
  public void e24() throws Exception {
    cases.ct_22();
    cases.ct_24();
    cases.ct_26();
    cases.ct_28();
    cases.ct_29();
  }

  @Test
  public void e25() throws Exception {
    cases.ct_23();
  }

  @Test
  public void e26() throws Exception {
    cases.ct_25();
  }

  @Test
  public void e27() throws Exception {
    cases.ct_27();
  }

  @Test
  public void e28() throws Exception {
    cases.ct_30();
  }

  @Test
  public void e29() throws Exception {
    cases.ct_31();
  }

  @Test
  public void e30() throws Exception {
    cases.ct_32();
  }
}
