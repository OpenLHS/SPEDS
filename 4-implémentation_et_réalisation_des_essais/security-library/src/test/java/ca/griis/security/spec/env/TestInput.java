package ca.griis.security.spec.env;

import java.nio.charset.StandardCharsets;

public class TestInput {
  public static byte[] createData() {
    return """
        Ceci est un exemple de chaine de caracteres.
        """.getBytes(StandardCharsets.UTF_8);
  }
}
