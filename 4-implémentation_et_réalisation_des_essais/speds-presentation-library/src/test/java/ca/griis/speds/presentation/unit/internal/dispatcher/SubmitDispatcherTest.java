package ca.griis.speds.presentation.unit.internal.dispatcher;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SubmitDispatcherTest {

  private ObjectMapper mapper = new ObjectMapper();
  private ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking;
  private SubmitDispatcher dispatcher;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private SessionHost mockSessionHost;

  private VersionDto version;
  private SecretKey fakeKey;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    serverTracking = new ConcurrentHashMap<>();
    version = new VersionDto("7.0.0", "presentation");
    fakeKey = KeyGenerator.getInstance("AES").generateKey();

    dispatcher = new SubmitDispatcher(
        version, cryptographyService, mockSessionHost, serverTracking);
  }

  @Test
  void testHandleRequest() throws Exception {
    when(cryptographyService.generateSymmetricKey(any()))
        .thenReturn(fakeKey);
    when(cryptographyService.encryptSymmetric(any(), any(SecretKey.class), any(byte[].class)))
        .thenReturn("encrypted".getBytes(StandardCharsets.UTF_8));

    Context23Dto responseContext = new Context23Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "someSdek",
        "TRANSFER",
        ServicePrimitive.CONFIRM,
        null);
    InterfaceDataUnit23Dto responseIdu = new InterfaceDataUnit23Dto(responseContext, "confirm msg");
    String sessionResponse =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(responseIdu);

    when(mockSessionHost.submitIdu(any(String.class)))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(sessionResponse)));

    Context12Dto context = new Context12Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "TRANSFER",
        ServicePrimitive.REQUEST,
        null);
    InterfaceDataUnit12Dto inputIdu = new InterfaceDataUnit12Dto(context, "Hello");
    String json = mapper.writeValueAsString(inputIdu);

    Optional<String> result = dispatcher.handle(json);

    assertTrue(result.isPresent());
  }

  @Test
  void testHandleUnknownPrimitive() throws Exception {
    Context12Dto context = new Context12Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "TRANSFER",
        ServicePrimitive.INDICATION,
        null);
    InterfaceDataUnit12Dto inputIdu = new InterfaceDataUnit12Dto(context, "test");
    String json = mapper.writeValueAsString(inputIdu);

    Optional<String> result = dispatcher.handle(json);

    assertTrue(result.isEmpty());
  }
}
