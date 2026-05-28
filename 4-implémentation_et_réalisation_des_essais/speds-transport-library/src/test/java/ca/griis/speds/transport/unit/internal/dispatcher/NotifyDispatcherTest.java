package ca.griis.speds.transport.unit.internal.dispatcher;

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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.transport.internal.handler.RecPduTransferIndicationHandler;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class NotifyDispatcherTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void handle_whenNotTransfer() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    // IDU45 with service != TRANSFER
    Context45Dto ctx = new Context45Dto(
        "https://src",
        "https://dst",
        Context45Dto.Service.DELEGATE,
        ServicePrimitive.INDICATION,
        false);
    InterfaceDataUnit45Dto idu = new InterfaceDataUnit45Dto(ctx, "message");

    Optional<String> out = dispatcher.handle(idu);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenNotIndication() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    // primitive != INDICATION
    Context45Dto ctx = new Context45Dto(
        "https://src",
        "https://dst",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST, // <-- not INDICATION
        false);
    InterfaceDataUnit45Dto idu = new InterfaceDataUnit45Dto(ctx, "whatever");

    Optional<String> out = dispatcher.handle(idu);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenInvalidPdu() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    Context45Dto ctx = new Context45Dto(
        "https://src",
        "https://dst",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    // invalid JSON (readValue will throw)
    InterfaceDataUnit45Dto idu = new InterfaceDataUnit45Dto(ctx, "{not-json");

    Optional<String> out = dispatcher.handle(idu);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void handle_whenMsgTypeError() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    CryptographyService crypto = mock(CryptographyService.class);
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    UUID msgId = UUID.randomUUID();
    Header45Dto header = new Header45Dto(
        null,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://ref"));
    ProtocolDataUnit4TraDto pdu =
        new ProtocolDataUnit4TraDto(header, new StampDto("h", "c"), "PAYLOAD");
    String pduJson = objectMapper.writeValueAsString(pdu);

    Context45Dto ctx = new Context45Dto(
        "https://src",
        "https://dst",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);
    InterfaceDataUnit45Dto idu = new InterfaceDataUnit45Dto(ctx, pduJson);

    Optional<String> out = dispatcher.handle(idu);

    assertTrue(out.isEmpty());
    verifyNoInteractions(networkHost);
  }

  @Test
  void indicationTraMsgRecWith_unknownMsg() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("OK")));

    CryptographyService crypto = mock(CryptographyService.class);
    ConfirmationRegistry registry = mock(ConfirmationRegistry.class);


    when(registry.remove(any(UUID.class))).thenReturn(null);

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    UUID msgId = UUID.randomUUID();
    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://ref"));
    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto("sealHeader", "sealContent"),
        "");
    String pduJson = objectMapper.writeValueAsString(pdu);

    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu = new InterfaceDataUnit45Dto(ctx, pduJson);

    Optional<String> out = dispatcher.handle(idu);
    assertTrue(out.isEmpty());

    verify(registry, times(1)).remove(eq(msgId));

    // envoyer Failed avec unknown message id
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto sent =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);
    assertEquals(ServicePrimitive.RESPONSE, sent.getContext().getServicePrimitive());
    assertEquals("FAILED: Unknown message id", sent.getMessage());
  }

  @Test
  void indicationTraMsgRecValidPdu() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("")));

    CryptographyService crypto = mock(CryptographyService.class);

    ConfirmationRegistry registry = new ConfirmationRegistry();

    Map<UUID, Boolean> clientPending = new ConcurrentHashMap<>();
    Map<UUID, Boolean> serverPending = new ConcurrentHashMap<>();

    NotifyDispatcher dispatcher =
        new NotifyDispatcher(crypto, objectMapper, networkHost, registry, clientPending,
            serverPending);

    UUID msgId = UUID.randomUUID();
    clientPending.put(msgId, true);

    // future qui doit être complétée par confirm()
    CompletableFuture<String> pending = registry.register(msgId);

    // IDU45
    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://ref"));
    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto("sealHeader", "sealContent"),
        "");

    Context45Dto ctx = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    InterfaceDataUnit45Dto idu =
        new InterfaceDataUnit45Dto(ctx, objectMapper.writeValueAsString(pdu));

    RecPduTransferIndicationHandler recHandlerMock = mock(RecPduTransferIndicationHandler.class);

    when(recHandlerMock.handle(any(InterfaceDataUnit45Dto.class),
        any(ProtocolDataUnit4TraDto.class)))
            .thenAnswer(inv -> {
              InterfaceDataUnit45Dto inIdu = inv.getArgument(0);
              ProtocolDataUnit4TraDto inPdu = inv.getArgument(1);

              UUID id = UUID.fromString(inPdu.getHeader().getId().toString());

              clientPending.remove(id);

              // envoyer succeed à réseau
              Context45Dto ticiResp = new Context45Dto(
                  inIdu.getContext().getSourceIri(),
                  inIdu.getContext().getDestinationIri(),
                  Context45Dto.Service.TRANSFER,
                  ServicePrimitive.RESPONSE,
                  false);
              InterfaceDataUnit45Dto resp45 = new InterfaceDataUnit45Dto(ticiResp, "SUCCEED");
              networkHost.submitIdu(objectMapper.writeValueAsString(resp45));

              Context34Dto dto = new Context34Dto(
                  inPdu.getHeader().getSourceCode(),
                  inPdu.getHeader().getDestinationCode(),
                  inIdu.getContext().getSourceIri(),
                  Context34Dto.Service.TRANSFER,
                  ServicePrimitive.CONFIRM,
                  inIdu.getContext().getDestinationIri(),
                  Boolean.FALSE);
              InterfaceDataUnit34Dto confirm34 = new InterfaceDataUnit34Dto(dto, "SUCCEED");
              registry.confirm(id, objectMapper.writeValueAsString(confirm34));

              return Optional.empty();
            });

    Field f = NotifyDispatcher.class.getDeclaredField("indicationRecHandler");
    f.setAccessible(true);
    f.set(dispatcher, recHandlerMock);

    Optional<String> out = dispatcher.handle(idu);
    assertTrue(out.isEmpty());

    assertFalse(clientPending.containsKey(msgId));

    // vérifier SUCCEED envoyé
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, atLeastOnce()).submitIdu(captor.capture());

    boolean foundSucceed = captor.getAllValues().stream().anyMatch(json -> {
      try {
        InterfaceDataUnit45Dto sent = objectMapper.readValue(json, InterfaceDataUnit45Dto.class);
        return "SUCCEED".equals(sent.getMessage())
            && sent.getContext().getServicePrimitive() == ServicePrimitive.RESPONSE;
      } catch (Exception e) {
        return false;
      }
    });
    assertTrue(foundSucceed, "Doit envoyer une RESPONSE SUCCEED au réseau");

    // registry confirm doit compléter la future
    String confirm34Json = pending.get(1, SECONDS);
    InterfaceDataUnit34Dto confirm34 =
        objectMapper.readValue(confirm34Json, InterfaceDataUnit34Dto.class);

    assertEquals(Context34Dto.Service.TRANSFER, confirm34.getContext().getService());
    assertEquals(ServicePrimitive.CONFIRM, confirm34.getContext().getServicePrimitive());
    assertEquals("SUCCEED", confirm34.getMessage());
  }
}
