package ca.griis.speds.presentation.integration;

import static ca.griis.speds.presentation.integration.Cases.ct_01;
import static ca.griis.speds.presentation.integration.Cases.ct_02;
import static ca.griis.speds.presentation.integration.Cases.ct_03;
import static ca.griis.speds.presentation.integration.Cases.ct_04;
import static ca.griis.speds.presentation.integration.Cases.ct_05;
import static ca.griis.speds.presentation.integration.Cases.ct_06;

import org.junit.jupiter.api.Test;

public class TestSuite {
  @Test
  public void e_04() throws Exception {
    final Environment env1 = new Environment();
    ct_01(env1);
    ct_02(env1);
    ct_03(env1);
    ct_04(env1);
  }

  @Test
  public void e_05() throws Exception {
    final Environment env1 = new Environment();
    ct_05(env1);
  }

  @Test
  public void e_06() throws Exception {
    final Environment env1 = new Environment();
    ct_06(env1);
  }
}
