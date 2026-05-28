package ca.griis.speds.network.unit.internal.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.network.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.security.CertificatePrivateKeyPair;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.network.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubmitDispatcherTest {
  private static KeyPair key;
  private static X509Certificate cert;

  private SubmitDispatcher dispatcher;

  @Mock
  private Host host;

  @Captor
  private ArgumentCaptor<String> messageCaptor;

  private Map<UUID, Boolean> indicatedMessages;

  private UUID msgId;

  @BeforeAll
  public static void setupAll() throws Exception {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    key = X509CertificateCreator.generateRsaKeyPair(4096);
    cert = X509CertificateCreator.createCertificate(
        "CN=host1",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");
  }

  @BeforeEach
  public void setUp() throws Exception {
    final var service = SecurityUtils.createCryptographyService();
    final var version = new VersionDto("3.0.0", "https://reference.iri/speds");
    final IdentifierGenerator gen = () -> UUID.randomUUID();
    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var encodedKey = Base64.getEncoder().encodeToString(key.getPrivate().getEncoded());
    final var cpkp = CertificatePrivateKeyPair.importFromPem(encodedCert, encodedKey);

    msgId = UUID.randomUUID();
    indicatedMessages = new ConcurrentHashMap<UUID, Boolean>();
    indicatedMessages.put(msgId, true);

    dispatcher = new SubmitDispatcher(gen, version, cpkp, service,
        SharedObjectMapper.getInstance().getMapper(), host, indicatedMessages);
  }

  @Test
  public void dispatchInvalidService() throws Exception {
    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "delegate",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "data");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host, times(0)).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchInvalidPrimitiveService() throws Exception {
    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.INDICATION, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "data");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host, times(0)).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchRequestPrimitiveService() throws Exception {
    final var confirmLinkContext =
        new Context56Dto("https://host2.ca:4044", "transfer",
            Context56Dto.ServicePrimitive.REQUEST, false);
    var confirmIdu56 = new InterfaceDataUnit56Dto(confirmLinkContext, "SUCCEED");
    var confirmLinkIdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu56);

    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.of(confirmLinkIdu));
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "DATA");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    var confirmContext =
        new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
            Context45Dto.ServicePrimitive.CONFIRM, false);
    var confirmIdu45 = new InterfaceDataUnit45Dto(confirmContext, "SUCCEED");
    var confirm = SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu45);

    assertFalse(result.isEmpty());
    assertEquals(result.get(), confirm);
  }

  @Test
  public void dispatchNoIriOptionResponsePrimitiveService() throws Exception {
    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.empty());
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE, Map.of("TN", msgId));
    var idu45 = new InterfaceDataUnit45Dto(context, "SUCCEED");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchResponsePrimitiveService() throws Exception {
    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.empty());
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE,
        Map.of("TN", msgId, "IRI", "https://host2.ca:4044"));
    var idu45 = new InterfaceDataUnit45Dto(context, "SUCCEED");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchNoOptionResponsePrimitiveService() throws Exception {
    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.empty());
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "SUCCEED");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(messageCaptor.getValue().contains("FAILED:"));
    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchNoMsgIdResponsePrimitiveService() throws Exception {
    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.empty());
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE, Map.of("TN", UUID.randomUUID()));
    var idu45 = new InterfaceDataUnit45Dto(context, "SUCCEED");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(messageCaptor.getValue().contains("FAILED:"));
    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchInvalidMsgIdResponsePrimitiveService() throws Exception {
    final CompletableFuture<Optional<String>> future =
        CompletableFuture.completedFuture(Optional.empty());
    when(host.submitIdu(Mockito.anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE, Map.of("TN", "T"));
    var idu45 = new InterfaceDataUnit45Dto(context, "SUCCEED");

    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(messageCaptor.getValue().contains("FAILED:"));
    assertTrue(result.isEmpty());
  }
}
