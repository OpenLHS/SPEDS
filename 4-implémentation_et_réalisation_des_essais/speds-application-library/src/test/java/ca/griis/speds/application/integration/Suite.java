package ca.griis.speds.application.integration;

import static ca.griis.speds.application.integration.Cases.ct_01;
import static ca.griis.speds.application.integration.Cases.ct_02;
import static ca.griis.speds.application.integration.Cases.ct_03;
import static ca.griis.speds.application.integration.Cases.ct_04;
import static ca.griis.speds.application.integration.Cases.ct_05;
import static ca.griis.speds.application.integration.Cases.ct_06;
import static ca.griis.speds.application.integration.Cases.ct_07;
import static ca.griis.speds.application.integration.Cases.ct_08;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class Suite {

  @Test
  public void e_01() throws JsonProcessingException, ExecutionException, InterruptedException {
    final Environment env1 = new Environment();
    env1.initClient();
    ct_01(env1);
  }

  @Test
  public void e_02() throws Exception {
    final Environment env1 = new Environment();
    env1.initClient(content -> false);
    ct_02(env1);
  }

  @Test
  public void e_03() throws Exception {
    final Environment env2 = new Environment();
    env2.initClient();
    when(env2.getProjectService().checkPlanActivity(anyString())).thenReturn(false);
    ct_03(env2);
  }

  @Test
  public void e_04() throws Exception {
    final Environment env1 = new Environment();
    env1.initClient();
    ct_04(env1);
  }

  @Test
  public void e_05() throws Exception {
    final Environment env1 = new Environment();
    env1.initClient();
    env1.initServer();
    ct_05(env1);
  }

  @Test
  public void e_06() throws Exception {
    final Environment env1 = new Environment();
    env1.initClient();
    env1.initServer(content -> false);
    ct_06(env1);
  }

  @Test
  public void e_07() throws Exception {
    final Environment env2 = new Environment();
    env2.initClient();
    env2.initServer();
    when(env2.getProjectService().checkPlanActivity(anyString())).thenReturn(false);
    ct_07(env2);
  }

  @Test
  public void e_08() throws Exception {
    final Environment env1 = new Environment();
    env1.initClient();
    env1.initServer();
    ct_08(env1);
  }
}
