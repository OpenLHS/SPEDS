package ca.griis.speds.transport.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Suite {
  private Environment env1;
  private Environment env2;

  @BeforeEach
  void setup() throws Exception {
    env1 = new Environment("env1");
    env2 = new Environment("env2");
  }

  @AfterEach
  void cleanUp() throws Exception {
    env1.cleanUp();
    env2.cleanUp();
  }

  @Test
  public void e_01()
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    Cases.ct_01(env1);
  }

  @Test
  public void e_02()
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    Cases.ct_06(env2);
    Cases.ct_07(env2);
  }

  @Test
  public void e_03()
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    Cases.ct_01(env1);
    Cases.ct_03(env1);
  }

  @Test
  public void e_04()
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    Cases.ct_02(env1);
  }

  @Test
  public void e_05() throws JsonProcessingException {
    Cases.ct_04(env1);
  }

  @Test
  public void e_06() throws JsonProcessingException {
    Cases.ct_05(env2);
  }

  @Test
  public void e_07() throws JsonProcessingException {
    Cases.ct_06(env2);
  }

  @Test
  public void e_08()
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    Cases.ct_08(env2);
  }
}
