package ca.griis.speds.transport.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.transport.api.dto.*;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.handler.TransferResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
public class TransferResponseHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void handle_SendTraMsgRecRequest_andReturnConfirm() throws Exception {
    // mocks
    NetworkHost networkHost = mock(NetworkHost.class);
    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("SUCCEED")));

    CryptographyService crypto = mock(CryptographyService.class);
    when(crypto.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    Speds45Dto version = new Speds45Dto("7.1.1", "https://reference.iri/speds");

    Map<UUID, Boolean> indicated = new ConcurrentHashMap<>();
    UUID tn = UUID.randomUUID();
    indicated.put(tn, true);

    TransferResponseHandler handler =
        new TransferResponseHandler(objectMapper, networkHost, indicated, version, crypto);

    // IDU34 RESPONSE de session
    Map<String, Object> options = Map.of("TN", tn.toString());

    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DEST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        "https://dst",
        options);

    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "SUCCEED");

    // when
    Optional<String> out = handler.handle(idu34).get();

    assertTrue(out.isEmpty());

    // TN doit être retiré
    assertFalse(indicated.containsKey(tn));

    // TIDU45 a été envoyé au réseau
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(networkHost, times(1)).submitIdu(captor.capture());

    InterfaceDataUnit45Dto sent45 =
        objectMapper.readValue(captor.getValue(), InterfaceDataUnit45Dto.class);

    assertEquals(ServicePrimitive.REQUEST, sent45.getContext().getServicePrimitive());
    assertEquals(Context45Dto.Service.TRANSFER, sent45.getContext().getService());

    ProtocolDataUnit4TraDto sentPdu =
        objectMapper.readValue(sent45.getMessage(), ProtocolDataUnit4TraDto.class);

    assertEquals(Header45Dto.Msgtype.TRA_MSG_REC, sentPdu.getHeader().getMsgtype());
    assertEquals(tn.toString(), sentPdu.getHeader().getId().toString());
    assertEquals("", sentPdu.getContent());
    assertNotNull(sentPdu.getStamp());
    assertNotNull(sentPdu.getStamp().getHeaderSeal());
    assertNotNull(sentPdu.getStamp().getContentSeal());
  }
}
