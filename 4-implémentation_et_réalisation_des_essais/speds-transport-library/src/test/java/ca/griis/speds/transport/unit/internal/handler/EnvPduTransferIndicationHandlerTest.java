package ca.griis.speds.transport.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import ca.griis.speds.transport.internal.handler.EnvPduTransferIndicationHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
public class EnvPduTransferIndicationHandlerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void handle_whenValidPdu() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("OK")));

    CryptographyService crypto = mock(CryptographyService.class);

    byte[] fixedHash = "hash".getBytes(StandardCharsets.UTF_8);
    when(crypto.hash(any(), any(byte[].class))).thenReturn(fixedHash);

    // stamp attendu
    String seal = Base64.getEncoder().encodeToString(fixedHash);

    var indicatedMessages = new ConcurrentHashMap<UUID, Boolean>();

    EnvPduTransferIndicationHandler handler =
        new EnvPduTransferIndicationHandler(crypto, objectMapper, networkHost, indicatedMessages);

    // IDU45 reçu
    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx45 = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(ctx45, "IGNORED");

    // PDU TRA.MSG.ENV
    UUID msgId = UUID.randomUUID();
    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));

    //
    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto(seal, seal),
        "PAYLOAD");

    Optional<String> out = handler.handle(idu45, pdu);

    // assert return IDU34
    assertTrue(out.isPresent());
    InterfaceDataUnit34Dto idu34 = objectMapper.readValue(out.get(), InterfaceDataUnit34Dto.class);

    assertEquals(Context34Dto.Service.TRANSFER, idu34.getContext().getService());
    assertEquals(ServicePrimitive.INDICATION, idu34.getContext().getServicePrimitive());
    assertEquals("PAYLOAD", idu34.getMessage());

    // options TN
    Map<String, Object> opts = objectMapper.convertValue(
        idu34.getContext().getOptions(),
        new TypeReference<Map<String, Object>>() {});
    assertEquals(msgId.toString(), opts.get("TN").toString());

    assertTrue(indicatedMessages.containsKey(msgId));

    //
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto sentResp45 =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.RESPONSE, sentResp45.getContext().getServicePrimitive());
    assertEquals("SUCCEED", sentResp45.getMessage());
    assertEquals("https://src-iri", sentResp45.getContext().getSourceIri());
    assertEquals("https://dst-iri", sentResp45.getContext().getDestinationIri());
  }

  @Test
  void handle_whenInvalidPdu() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("OK")));

    CryptographyService crypto = mock(CryptographyService.class);


    when(crypto.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    var indicatedMessages = new ConcurrentHashMap<UUID, Boolean>();

    EnvPduTransferIndicationHandler handler =
        new EnvPduTransferIndicationHandler(crypto, objectMapper, networkHost, indicatedMessages);

    // IDU45 reçu
    final var options = Map.of("TN", UUID.randomUUID().toString());
    Context45Dto ctx45 = new Context45Dto(
        "https://src-iri",
        "https://dst-iri",
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.INDICATION,
        options);
    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(ctx45, "IGNORED_HERE");

    UUID msgId = UUID.randomUUID();
    Header45Dto header = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        msgId,
        "SRC_CODE",
        "DEST_CODE",
        new Speds45Dto("7.1.1", "https://reference.iri/speds"));

    ProtocolDataUnit4TraDto invalidPdu = new ProtocolDataUnit4TraDto(
        header,
        new StampDto("bad-header-seal", "bad-content-seal"),
        "PAYLOAD");

    Optional<String> out = handler.handle(idu45, invalidPdu);

    // aucun IDU34 retourné
    assertTrue(out.isEmpty());
    assertTrue(indicatedMessages.isEmpty());

    // vérifier qu'une reponse a été envoyée avec un message d'erreur
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto sentResp45 =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.RESPONSE, sentResp45.getContext().getServicePrimitive());
    assertNotNull(sentResp45.getMessage());

    assertTrue(
        sentResp45.getMessage().startsWith("FAILED")
            || sentResp45.getMessage().toUpperCase().contains("FAILED"),
        "Doit répondre FAILED");
  }
}
