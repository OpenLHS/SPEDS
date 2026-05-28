package ca.griis.speds.presentation.unit.internal.handler;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.handler.TransferIndicationHandler;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransferIndicationHandlerTest {

  private ObjectMapper mapper = new ObjectMapper();

  private TransferIndicationHandler transferIndicationHandler;

  private ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private SessionHost mockSessionHost;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    serverTracking = new ConcurrentHashMap<>();
    transferIndicationHandler = new TransferIndicationHandler(
        cryptographyService, mockSessionHost, serverTracking);
  }

  @Test
  public void with_result_test() throws JsonProcessingException {
    String innerPduJson = """
        {
          "header": {
            "msgtype": "PRE.MSG.ENV",
            "id": "11111111-1111-1111-1111-111111111111",
            "parameters": {},
            "version": {
              "number": "7.0.0",
              "reference": "presentation"
            }
          },
          "content": "base64EncryptedContent"
        }
        """;

    String json = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "request",
            "options": {
              "TN": "22222222-2222-2222-2222-222222222222"
            }
          },
          "message": """ + mapper.writeValueAsString(innerPduJson) + """
        }
        """;

    when(cryptographyService.getAlgorithm(
        eq(SpedsConfigItemDto.SpedsLayer.PRESENTATION), any()))
            .thenReturn("AES");

    when(cryptographyService.decryptSymmetric(
        eq(SpedsConfigItemDto.SpedsLayer.PRESENTATION),
        any(SecretKey.class),
        any(byte[].class)))
            .thenReturn("decrypted message content".getBytes(StandardCharsets.UTF_8));

    InterfaceDataUnit23Dto idu = mapper.readValue(json, InterfaceDataUnit23Dto.class);
    Optional<String> result = transferIndicationHandler.handle(idu);

    assertTrue(result.isPresent());
  }

  @Test
  public void failed_msgtype_test() throws JsonProcessingException {
    // Le "content" ici est ce que decryptSymmetric recevra en entrée
    String innerPduJson = """
        {
          "header": {
            "msgtype": "error",
            "id": "11111111-1111-1111-1111-111111111111",
            "parameters": {},
            "version": {
              "number": "7.0.0",
              "reference": "presentation"
            }
          },
          "content": "base64EncryptedContent"
        }
        """;

    String json = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "request",
            "options": {
              "TN": "22222222-2222-2222-2222-222222222222"
            }
          },
          "message": """ + mapper.writeValueAsString(innerPduJson) + """
        }
        """;


    when(cryptographyService.decryptSymmetric(
        eq(SpedsConfigItemDto.SpedsLayer.PRESENTATION),
        any(SecretKey.class),
        any(byte[].class)))
            .thenReturn("decrypted message content".getBytes(StandardCharsets.UTF_8));

    InterfaceDataUnit23Dto idu = mapper.readValue(json, InterfaceDataUnit23Dto.class);
    Optional<String> result = transferIndicationHandler.handle(idu);

    assertTrue(result.isEmpty());
  }

  @Test
  void testHandleOptionsNotMap() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    serverTracking = new ConcurrentHashMap<>();
    transferIndicationHandler = new TransferIndicationHandler(
        cryptographyService, mockSessionHost, serverTracking);

    when(cryptographyService.decryptSymmetric(
        any(), any(SecretKey.class), any(byte[].class)))
            .thenReturn("decrypted content".getBytes(StandardCharsets.UTF_8));

    String innerPduJson = """
        {
          "header": {
            "msgtype": "PRE.MSG.ENV",
            "id": "11111111-1111-1111-1111-111111111111",
            "parameters": {},
            "version": {
              "number": "7.0.0",
              "reference": "presentation"
            }
          },
          "content": "base64EncryptedContent"
        }
        """;

    String json = """
        {
          "context": {
            "PGA": "550e8400-e29b-41d4-a716-446655440000",
            "source_code": "source",
            "destination_code": "destination",
            "SDEK": "YWVzLTI1Ni1nY20tZW5jcnlwdGVkLWtleQ==",
            "service": "transfer",
            "service_primitive": "request",
            "options": null
          },
          "message": %s
        }
        """.formatted(mapper.writeValueAsString(innerPduJson));

    InterfaceDataUnit23Dto idu = mapper.readValue(json, InterfaceDataUnit23Dto.class);
    Optional<String> result = transferIndicationHandler.handle(idu);

    assertTrue(result.isEmpty());
    verify(mockSessionHost).submitIdu(argThat(s -> s.contains("FAILED")));
  }
}
