package ca.griis.speds.network.integration;

import static ca.griis.speds.network.integration.Cases.*;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Suites {
  private Environment env;

  @BeforeAll
  public static void setupAll() throws Exception {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @BeforeEach
  void setup() throws Exception {
    env = new Environment().en1();
  }

  @Test
  void E_01() throws Exception {
    ct_01(env);
  }

  @Test
  void E_02() throws Exception {
    ct_02(env);
    ct_01(env);
  }

  @Test
  void E_03() throws Exception {
    ct_03(env);
    ct_04(env);
  }

  @Test
  void E_04() throws Exception {
    ct_05(env);
  }

  @Test
  void E_05() throws Exception {
    ct_05(env);
    ct_06(env);
  }
}
