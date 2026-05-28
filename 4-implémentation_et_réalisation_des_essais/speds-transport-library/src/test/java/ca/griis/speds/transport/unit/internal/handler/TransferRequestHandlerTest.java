package ca.griis.speds.transport.unit.internal.handler;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.handler.TransferRequestHandler;
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

public class TransferRequestHandlerTest {

  private IdentifierGenerator identifierGenerator;
  private CryptographyService cryptographyService;
  private NetworkHost networkHost;

  private ObjectMapper objectMapper;

  private ConfirmationRegistry confirmationRegistry;
  private Map<UUID, Boolean> requestedMessages;
  private Speds45Dto speds45Dto;

  @BeforeEach
  void setUp() {
    identifierGenerator = mock(IdentifierGenerator.class);
    cryptographyService = mock(CryptographyService.class);
    networkHost = mock(NetworkHost.class);

    objectMapper = new ObjectMapper();
    confirmationRegistry = new ConfirmationRegistry();

    requestedMessages = new ConcurrentHashMap<>();
    speds45Dto = mock(Speds45Dto.class);
  }

  @Test
  void handle_ConfirmFailed() throws Exception {
    UUID traId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    when(identifierGenerator.generateId()).thenReturn(traId);

    when(cryptographyService.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    // confirm réseau IDU-4_5 avec Failed
    InterfaceDataUnit45Dto confirm45 = new InterfaceDataUnit45Dto(
        new Context45Dto("https://src", "https://dst",
            Context45Dto.Service.TRANSFER, ServicePrimitive.CONFIRM, Boolean.FALSE),
        "FAILED: network error");
    String confirm45Json = objectMapper.writeValueAsString(confirm45);

    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(confirm45Json)));

    TransferRequestHandler handler = new TransferRequestHandler(
        identifierGenerator,
        speds45Dto,
        objectMapper,
        networkHost,
        cryptographyService,
        requestedMessages,
        confirmationRegistry);

    // IDU-3_4 entrée
    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "https://dst",
        Boolean.FALSE);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "SESSION_SDU");

    Optional<String> out = handler.handle(idu34).get(1, SECONDS);

    assertTrue(out.isPresent());

    InterfaceDataUnit34Dto confirm34 =
        objectMapper.readValue(out.get(), InterfaceDataUnit34Dto.class);

    assertEquals(ServicePrimitive.CONFIRM, confirm34.getContext().getServicePrimitive());
    assertEquals(Context34Dto.Service.TRANSFER, confirm34.getContext().getService());
    assertTrue(confirm34.getMessage().startsWith("FAILED"));

    assertTrue(requestedMessages.containsKey(traId));
  }

  @Test
  void handle_ConfirmSucceed() throws Exception {
    UUID traId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    when(identifierGenerator.generateId()).thenReturn(traId);

    when(cryptographyService.hash(any(), any(byte[].class)))
        .thenReturn("hash".getBytes(StandardCharsets.UTF_8));

    // confirm réseau IDU-4_5 avec succeed
    InterfaceDataUnit45Dto confirm45 = new InterfaceDataUnit45Dto(
        new Context45Dto("https://src", "https://dst",
            Context45Dto.Service.TRANSFER, ServicePrimitive.CONFIRM, Boolean.FALSE),
        "SUCCEED");
    String confirm45Json = objectMapper.writeValueAsString(confirm45);

    when(networkHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(confirm45Json)));

    TransferRequestHandler handler = new TransferRequestHandler(
        identifierGenerator,
        speds45Dto,
        objectMapper,
        networkHost,
        cryptographyService,
        requestedMessages,
        confirmationRegistry);

    Context34Dto ctx34 = new Context34Dto(
        "SRC_CODE",
        "DST_CODE",
        "https://src",
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "https://dst",
        Boolean.FALSE);
    InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(ctx34, "SESSION_SDU");

    CompletableFuture<Optional<String>> future = handler.handle(idu34);

    assertThrows(TimeoutException.class, () -> future.get(150, TimeUnit.MILLISECONDS));

    String finalConfirm34Json = "{\"context\":{},\"message\":\"SUCCEED\"}";
    confirmationRegistry.confirm(traId, finalConfirm34Json);

    Optional<String> out = future.get(1, SECONDS);

    assertTrue(out.isPresent());
    assertEquals(finalConfirm34Json, out.get());

    assertTrue(requestedMessages.containsKey(traId));
  }
}
