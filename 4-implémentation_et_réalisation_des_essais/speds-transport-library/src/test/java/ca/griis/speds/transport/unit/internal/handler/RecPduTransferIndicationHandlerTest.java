package ca.griis.speds.transport.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import ca.griis.speds.transport.internal.handler.RecPduTransferIndicationHandler;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
public class RecPduTransferIndicationHandlerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void handle_whenUnknownMessageId() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("OK")));

    CryptographyService crypto = mock(CryptographyService.class);
    // pas obligatoire ici (checker non appelé), mais safe
    when(crypto.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    Map<UUID, Boolean> indicatedMessages = new ConcurrentHashMap<>();
    ConfirmationRegistry registry = mock(ConfirmationRegistry.class);
    when(registry.remove(any(UUID.class))).thenReturn(null);
    RecPduTransferIndicationHandler handler =
        new RecPduTransferIndicationHandler(crypto, objectMapper, networkHost, indicatedMessages,
            registry);

    // input idu + pdu
    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx45 = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(ctx45, "IGNORED");

    UUID msgId = UUID.randomUUID();
    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));

    // stamp NON NULL
    ProtocolDataUnit4TraDto pdu =
        new ProtocolDataUnit4TraDto(header, new StampDto("h", "c"), "");

    // run
    Optional<String> out = handler.handle(idu45, pdu);

    assertTrue(out.isEmpty());

    verify(registry, times(1)).remove(eq(msgId));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto sentResp =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.RESPONSE, sentResp.getContext().getServicePrimitive());
    assertEquals("FAILED: Unknown message id", sentResp.getMessage());
  }

  @Test
  void handle_ValidPdu() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("OK")));

    CryptographyService crypto = mock(CryptographyService.class);

    // IMPORTANT: éviter NPE + rendre le seal "constant"
    byte[] hashBytes = "hash".getBytes(StandardCharsets.UTF_8);
    when(crypto.hash(any(), any(byte[].class))).thenReturn(hashBytes);

    // seal attendu (comme le checker fait Base64(hash))
    String expectedSeal = Base64.getEncoder().encodeToString(hashBytes);

    Map<UUID, Boolean> indicatedMessages = new ConcurrentHashMap<>();
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    RecPduTransferIndicationHandler handler =
        new RecPduTransferIndicationHandler(crypto, objectMapper, networkHost, indicatedMessages,
            registry);

    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx45 = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(ctx45, "IGNORED");

    UUID msgId = UUID.randomUUID();
    indicatedMessages.put(msgId, true); // message attendu

    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));

    // Stamp VALIDE : match le seal attendu
    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto(expectedSeal, expectedSeal),
        "" // REC content peut être vide
    );

    // register avant confirm
    CompletableFuture<String> pending = registry.register(msgId);

    Optional<String> out = handler.handle(idu45, pdu);
    assertTrue(out.isEmpty());

    assertFalse(indicatedMessages.containsKey(msgId));

    // SUCCEED envoyé au réseau
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto resp45 =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.RESPONSE, resp45.getContext().getServicePrimitive());
    assertEquals("SUCCEED", resp45.getMessage());

    // registry.confirm => future complétée avec IDU34 CONFIRM
    String confirm34Json = pending.get(1, TimeUnit.SECONDS);
    InterfaceDataUnit34Dto confirm34 =
        objectMapper.readValue(confirm34Json, InterfaceDataUnit34Dto.class);

    assertEquals(ServicePrimitive.CONFIRM, confirm34.getContext().getServicePrimitive());
    assertEquals(Context34Dto.Service.TRANSFER, confirm34.getContext().getService());
    assertEquals("SUCCEED", confirm34.getMessage());
    assertEquals("SRC_CODE", confirm34.getContext().getSourceCode());
    assertEquals("DEST_CODE", confirm34.getContext().getDestinationCode());
  }

  @Test
  void handle_InvalidPdu() throws Exception {
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("")));

    CryptographyService crypto = mock(CryptographyService.class);

    byte[] hashBytes = "hash".getBytes(StandardCharsets.UTF_8);
    when(crypto.hash(any(), any(byte[].class))).thenReturn(hashBytes);

    String expectedSeal = Base64.getEncoder().encodeToString(hashBytes);

    Map<UUID, Boolean> indicatedMessages = new ConcurrentHashMap<>();
    ConfirmationRegistry registry = spy(new ConfirmationRegistry());

    RecPduTransferIndicationHandler handler =
        new RecPduTransferIndicationHandler(crypto, objectMapper, networkHost, indicatedMessages,
            registry);

    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx45 = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(ctx45, "IGNORED");

    UUID msgId = UUID.randomUUID();
    indicatedMessages.put(msgId, true);

    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));

    ProtocolDataUnit4TraDto invalidPdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto("BAD_SEAL_HEADER", expectedSeal),
        "");

    CompletableFuture<String> pending = registry.register(msgId);

    Optional<String> out = handler.handle(idu45, invalidPdu);
    assertTrue(out.isEmpty());

    // checker invalid
    // on envoie response avec message d'erreur
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto resp45 =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.RESPONSE, resp45.getContext().getServicePrimitive());
    assertNotNull(resp45.getMessage());
    assertTrue(resp45.getMessage().startsWith("FAILED"), "Doit répondre FAILED");

    // pas de confirmation registry
    assertFalse(pending.isDone(), "Ne doit pas confirmer le registry si PDU invalide");
  }
}
