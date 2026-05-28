package ca.griis.security.spec.env;

public enum CryptoSpecId {
  hashing("Hashing"),
  aesGcm("AES-GCM"),
  aesGen("AES-Gen"),
  rsaGen("RSA-Encipherment-Signature-Gen"),
  ed25519Gen("25519-Signature-Gen"),
  x25519Gen("25519-DH-Gen"),
  x25519("25519-DH"),
  rsaCipher("RSA-Encipherment"),
  rsaSignature("RSA-Signature"),
  ed25519Signature("25519-Signature"),
  csprng("CSPRNG");

  private final String value;

  CryptoSpecId(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
