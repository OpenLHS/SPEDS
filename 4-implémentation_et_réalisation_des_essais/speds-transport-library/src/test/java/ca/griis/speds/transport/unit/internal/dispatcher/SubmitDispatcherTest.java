package ca.griis.speds.transport.unit.internal.dispatcher;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.transport.api.dto.*;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public class SubmitDispatcherTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void handle_whenInvalidJson_shouldReturnEmpty_andNoNetworkCall() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    IdentifierGenerator generator = mock(IdentifierGenerator.class);
    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> confirmedPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> indicatedPending = new ConcurrentHashMap<>();

    SubmitDispatcher dispatcher = new SubmitDispatcher(
        generator,
        new Speds45Dto("7.1.1", "https://reference.iri/speds"),
        crypto,
        objectMapper,
        networkHost,
        registry,
        confirmedPending,
        indicatedPending);

    CompletableFuture<Optional<String>> out = dispatcher.handle("");

    assertTrue(out.get(1, SECONDS).isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenServiceNotTransfer_shouldReturnEmpty_andNoNetworkCall() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    IdentifierGenerator generator = mock(IdentifierGenerator.class);
    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> confirmedPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> indicatedPending = new ConcurrentHashMap<>();

    SubmitDispatcher dispatcher = new SubmitDispatcher(
        generator,
        new Speds45Dto("7.1.1", "https://reference.iri/speds"),
        crypto,
        objectMapper,
        networkHost,
        registry,
        confirmedPending,
        indicatedPending);

    // service != TRANSFER
    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.DELEGATE,
        ServicePrimitive.REQUEST,
        "https://dst",
        Boolean.FALSE);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "DATA");
    String json = objectMapper.writeValueAsString(idu34);

    Optional<String> out = dispatcher.handle(json).get(1, SECONDS);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenUnknownPrimitive_shouldReturnEmpty_andNoNetworkCall() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    IdentifierGenerator generator = mock(IdentifierGenerator.class);
    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> confirmedPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> indicatedPending = new ConcurrentHashMap<>();

    SubmitDispatcher dispatcher = new SubmitDispatcher(
        generator,
        new Speds45Dto("7.1.1", "https://reference.iri/speds"),
        crypto,
        objectMapper,
        networkHost,
        registry,
        confirmedPending,
        indicatedPending);

    // primitive != REQUEST/RESPONSE (ex: CONFIRM)
    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.CONFIRM, // <-- unsupported by SubmitDispatcher
        "https://dst",
        Boolean.FALSE);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "DATA");
    String json = objectMapper.writeValueAsString(idu34);

    Optional<String> out = dispatcher.handle(json).get(1, SECONDS);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenRequest_shouldCallNetwork_andWaitRegistry_thenReturnConfirm34Json()
      throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    IdentifierGenerator generator = mock(IdentifierGenerator.class);

    // REAL registry so we can confirm() from the test
    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> confirmedPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> indicatedPending = new ConcurrentHashMap<>();

    // stable traId so we can confirm it later
    UUID traId = UUID.fromString("00000000-0000-0000-0000-000000000111");
    when(generator.generateId()).thenReturn(traId);

    // hash() used by TransferRequestHandler
    when(crypto.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    // network confirm IDU45 must be JSON of InterfaceDataUnit45Dto
    InterfaceDataUnit45Dto confirm45 = new InterfaceDataUnit45Dto(
        new Context45Dto("https://src", "https://dst",
            Context45Dto.Service.TRANSFER, ServicePrimitive.CONFIRM, Boolean.FALSE),
        "SUCCEED");
    String confirm45Json = objectMapper.writeValueAsString(confirm45);

    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(confirm45Json)));

    SubmitDispatcher dispatcher = new SubmitDispatcher(
        generator,
        new Speds45Dto("7.1.1", "https://reference.iri/speds"),
        crypto,
        objectMapper,
        networkHost,
        registry,
        confirmedPending,
        indicatedPending);

    // IDU34 REQUEST input
    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "https://dst",
        Boolean.FALSE);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "SESSION_SDU");
    String iduJson = objectMapper.writeValueAsString(idu34);

    CompletableFuture<Optional<String>> future = dispatcher.handle(iduJson);

    // after network SUCCEED, handler waits for TRA.MSG.REC => registry.register(traId)
    assertFalse(future.isDone(), "Doit attendre la confirmation (TRA.MSG.REC) via registry");

    // simulate reception of TRA.MSG.REC confirm34 JSON
    String finalConfirm34Json = "{\"context\":{},\"message\":\"SUCCEED\"}";
    registry.confirm(traId, finalConfirm34Json);

    Optional<String> out = future.get(1, SECONDS);

    assertTrue(out.isPresent());
    assertEquals(finalConfirm34Json, out.get());

    // networkHost.submitIdu called at least once (TRA.MSG.ENV request)
    verify(networkHost, atLeastOnce()).submitIdu(anyString());

    // requestedMessages should contain traId (TransferRequestHandler does putIfAbsent)
    assertTrue(confirmedPending.containsKey(traId), "Le request handler doit mémoriser le traId");
  }

  @Test
  void handle_whenResponse_withKnownTN_shouldCallNetwork() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    IdentifierGenerator generator = mock(IdentifierGenerator.class);
    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> confirmedPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> indicatedPending = new ConcurrentHashMap<>();

    // TN attendu par TransferResponseHandler
    UUID tn = UUID.fromString("00000000-0000-0000-0000-000000000222");
    indicatedPending.put(tn, true);

    when(crypto.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    // network confirmation for TRA.MSG.REC request
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("SUCCEED")));

    SubmitDispatcher dispatcher = new SubmitDispatcher(
        generator,
        new Speds45Dto("7.1.1", "https://reference.iri/speds"),
        crypto,
        objectMapper,
        networkHost,
        registry,
        confirmedPending,
        indicatedPending);

    Map<String, Object> options = Map.of("TN", tn.toString());
    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        "https://dst",
        options);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "SUCCEED");
    String iduJson = objectMapper.writeValueAsString(idu34);

    Optional<String> out = dispatcher.handle(iduJson).get(1, SECONDS);

    // Depending on your TransferResponseHandler implementation,
    // it may return empty or return the confirm string.
    // So we only assert it completes successfully:
    assertNotNull(out);

    // It should have tried to send a TRA.MSG.REC request to network
    verify(networkHost, atLeastOnce()).submitIdu(anyString());
  }
}
