package ca.griis.speds.presentation.integration;

import static ca.griis.speds.presentation.integration.Cases.*;

import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ProtocolDataUnit2PREDto;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestSuite {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  SessionHost mockSessionHost;

  Environment environment;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    environment = new Environment(objectMapper, mockSessionHost);
  }

  @Test
  public void e01_test() throws JsonProcessingException {
    c01_client_request_success(environment);
  }


  @Test
  public void e02_test() throws JsonProcessingException {
    c01_client_request_success(environment);
    c02_server_indication_success(environment);
  }

  @Test
  public void e03_test() throws JsonProcessingException {
    c01_client_request_success(environment);
    c02_server_indication_success(environment);
    c03_server_response_success(environment);
  }

  @Test
  public void e04_test() throws JsonProcessingException {
    c01_client_request_success(environment);
    c02_server_indication_success(environment);
    c03_server_response_success(environment);
    c04_client_confirm_success(environment);
  }

  @Test
  public void e05_test() throws JsonProcessingException {
    c05_server_indication_fail_deserialization(environment);
  }

  @Test
  public void e06_test() throws JsonProcessingException {
    environment.msgtype = HeaderDto.Msgtype.PRE_MSG_ENV;
    environment.msgId = UUID.randomUUID();
    environment.trackinNumber = environment.msgId;

    c06_server_indication_fail_decryption(environment);
  }

  @Test
  public void e07_test() throws JsonProcessingException {
    c07_client_confirm_fail_deserialization(environment);
  }

  @Test
  public void e08_test() throws JsonProcessingException {
    c01_client_request_success(environment);

    environment.msgtype = HeaderDto.Msgtype.PRE_MSG_REC;

    InterfaceDataUnit23Dto idu23Dto =
        environment.objectMapper.readValue(environment.idu23Sent,
            InterfaceDataUnit23Dto.class);

    ProtocolDataUnit2PREDto pdu2Dto =
        environment.objectMapper.readValue(idu23Dto.getMessage(), ProtocolDataUnit2PREDto.class);

    environment.msgId = pdu2Dto.getHeader().getId();

    environment.trackinNumber = environment.msgId;

    c08_client_confirm_fail_decryption(environment);
  }

  @Test
  public void e09_test() throws JsonProcessingException {
    c01_client_request_success(environment);

    environment.msgtype = HeaderDto.Msgtype.PRE_MSG_REC;

    environment.msgId = UUID.randomUUID();

    environment.trackinNumber = environment.msgId;

    c09_client_confirm_fail_message_id(environment);
  }
}
