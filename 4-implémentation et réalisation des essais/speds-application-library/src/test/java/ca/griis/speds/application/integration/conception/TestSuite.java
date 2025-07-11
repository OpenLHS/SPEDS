package ca.griis.speds.application.integration.conception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSuite {
  private ServerCases serverCases;
  private ClientCases clientCases;

  @BeforeEach
  public void setup() throws JsonProcessingException {
    clientCases = new ClientCases();
    serverCases = new ServerCases();
  }

  @Test
  public void e01_test() throws Exception {
    clientCases.ct001_init_success();
  }

  @Test
  public void e07_test() throws Exception {
    clientCases.ct001_init_success();
    clientCases.ct006_request_success();
  }

  @Test
  public void e08_test() throws Exception {
    clientCases.ct001_init_success();
    clientCases.ct006_request_success();
    clientCases.ct007_confirm_success();
  }

  @Test
  public void e09_test() throws Exception {
    clientCases.ct001_init_success();
    clientCases.ct008_confirm_deserialization_exception();
  }

  @Test
  public void e10_test() throws Exception {
    clientCases.ct001_init_success();
    clientCases.ct009_confirm_invalidPduId_exception();
  }

  @Test
  public void e11_test() throws Exception {
    serverCases.ct011_init_success();
  }

  @Test
  public void e17_test() throws Exception {
    serverCases.ct011_init_success();
    serverCases.ct016_indication_success();
  }

  @Test
  public void e18_test() throws Exception {
    serverCases.ct011_init_success();
    serverCases.ct017_indication_deserialization_exception();
  }

  @Test
  public void e19_test() throws Exception {
    serverCases.ct011_init_success();
    serverCases.ct016_indication_success();
    serverCases.ct018_response_success();
  }
}
