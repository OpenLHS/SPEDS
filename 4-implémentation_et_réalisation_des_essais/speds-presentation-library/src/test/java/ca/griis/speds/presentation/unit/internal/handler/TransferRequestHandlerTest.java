package ca.griis.speds.presentation.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.speds.presentation.internal.handler.TransferRequestHandler;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransferRequestHandlerTest {

  private ObjectMapper mapper = new ObjectMapper();
  private SharedObjectMapper objectMapper = SharedObjectMapper.getInstance();
  private TransferRequestHandler transferRequestHandler;
  private VersionDto version;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private SessionHost mockSessionHost;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    version = new VersionDto("7.0.0", "presentation");
    transferRequestHandler = new TransferRequestHandler(
        version, cryptographyService, mockSessionHost);
  }

  @Test
  void testHandleSuccess() throws Exception {
    Context12Dto context = new Context12Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "TRANSFER",
        ServicePrimitive.REQUEST,
        null);

    InterfaceDataUnit12Dto inputIdu = new InterfaceDataUnit12Dto(context, "Hello SPEDS");

    SecretKey fakeKey = KeyGenerator.getInstance("AES").generateKey();

    when(cryptographyService.generateSymmetricKey(
        any(SpedsConfigItemDto.SpedsLayer.class))).thenReturn(fakeKey);

    when(cryptographyService.encryptSymmetric(
        any(SpedsConfigItemDto.SpedsLayer.class),
        any(SecretKey.class),
        any(byte[].class))).thenReturn("encryptedContent".getBytes(StandardCharsets.UTF_8));

    Context23Dto responseContext = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "destination",
        "source",
        "someSdek",
        "TRANSFER",
        ServicePrimitive.CONFIRM,
        null);

    InterfaceDataUnit23Dto responseIdu = new InterfaceDataUnit23Dto(
        responseContext, "Session confirm message");

    String sessionResponse = objectMapper.getMapper().writeValueAsString(responseIdu);

    CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.of(sessionResponse));

    when(mockSessionHost.submitIdu(any(String.class))).thenReturn(future);

    Optional<String> result = transferRequestHandler.handle(inputIdu);

    assertTrue(result.isPresent());
    verify(mockSessionHost).submitIdu(any(String.class));
    verify(cryptographyService).generateSymmetricKey(any());
    verify(cryptographyService).encryptSymmetric(any(), any(), any());

    String resultJson = result.get();
    InterfaceDataUnit12Dto confirmIdu =
        mapper.readValue(resultJson, InterfaceDataUnit12Dto.class);
    assertTrue(confirmIdu.getMessage().contains("Session confirm message"));
  }

  @Test
  void testHandleException() throws Exception {
    Context12Dto context = new Context12Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "TRANSFER",
        ServicePrimitive.REQUEST,
        null);

    InterfaceDataUnit12Dto inputIdu = new InterfaceDataUnit12Dto(context, "Hello SPEDS");

    when(cryptographyService.generateSymmetricKey(any()))
        .thenThrow(new RuntimeException("Crypto failure"));

    Optional<String> result = transferRequestHandler.handle(inputIdu);

    assertTrue(result.isPresent());
    String resultJson = result.get();
    assertTrue(resultJson.contains("FAILED"));
  }
}
