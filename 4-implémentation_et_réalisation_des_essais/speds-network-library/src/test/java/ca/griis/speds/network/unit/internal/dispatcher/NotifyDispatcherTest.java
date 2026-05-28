package ca.griis.speds.network.unit.internal.dispatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.internal.serializer.SharedObjectMapper;
import ca.griis.speds.network.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.network.internal.security.SealManager;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.network.util.X509CertificateCreator;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotifyDispatcherTest {
  private static KeyPair key;
  private static X509Certificate cert;

  private NotifyDispatcher dispatcher;
  private SealManager sealManager;

  @Mock
  private Host host;

  @Captor
  private ArgumentCaptor<String> messageCaptor;

  private Map<UUID, Boolean> indicatedMessages;

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
    indicatedMessages = new ConcurrentHashMap<UUID, Boolean>();
    CryptographyService service = SecurityUtils.createCryptographyService();
    dispatcher = new NotifyDispatcher(service, SharedObjectMapper.getInstance().getMapper(), host,
        indicatedMessages);
    sealManager = new SealManager(service);
  }

  @Test
  public void dispatchInvalidService() throws Exception {
    var context = new Context56Dto("https://host1.ca:4043", "delegate",
        Context56Dto.ServicePrimitive.INDICATION, false);
    var idu = new InterfaceDataUnit56Dto(context, "oh");

    var result = dispatcher.handle(idu);

    verify(host, times(0)).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchInvalidPrimtiveService() throws Exception {
    var context = new Context56Dto("https://host1.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.REQUEST, false);
    var idu = new InterfaceDataUnit56Dto(context, "oh");

    var result = dispatcher.handle(idu);

    verify(host, times(0)).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());
  }

  @Test
  public void dispatchIndicationPrimitiveServiceSuccess() throws Exception {
    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var version = new VersionDto("7.0.0", "https://reference");
    final var msgId = UUID.randomUUID();
    final var header = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, msgId, "https://host1.ca:4043",
        "https://host2.ca:4043", encodedCert, false, version);
    final var headerPdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(header);
    final var content = "yeah!";

    final var stamp = new StampDto(
        sealManager.createSeal(headerPdu, key.getPrivate()),
        sealManager.createSeal(content, key.getPrivate()));

    final var pdu = new ProtocolDataUnit5NETDto(header, stamp, content);
    final var message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final var context = new Context56Dto("https://host2.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    final var idu = new InterfaceDataUnit56Dto(context, message);

    final var result = dispatcher.handle(idu);

    verify(host, times(0)).submitIdu(messageCaptor.capture());

    assertTrue(result.isPresent());

    final var netContext =
        new Context45Dto("https://host1.ca:4043", "https://host2.ca:4043", "transfer",
            Context45Dto.ServicePrimitive.INDICATION,
            Map.of("TN", msgId, "IRI", "https://host2.ca:4043"));
    final var idu45 = new InterfaceDataUnit45Dto(netContext, "yeah!");
    final var expectedIdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    assertEquals(result.get(), expectedIdu);
  }

  @Test
  public void dispatchIndicationNoCert() throws Exception {
    final var encodedCert = Base64.getEncoder().encodeToString(new byte[0]);
    final var version = new VersionDto("7.0.0", "https://reference");
    final var msgId = UUID.randomUUID();
    final var header = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, msgId, "https://host1.ca:4043",
        "https://host2.ca:4043", encodedCert, false, version);
    final var headerPdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(header);
    final var content = "yeah!";

    final KeyPair secondKey = X509CertificateCreator.generateRsaKeyPair(4096);
    final var stamp = new StampDto(
        sealManager.createSeal(headerPdu, secondKey.getPrivate()),
        sealManager.createSeal(content, key.getPrivate()));

    final var pdu = new ProtocolDataUnit5NETDto(header, stamp, content);
    final var message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final var context = new Context56Dto("https://host2.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    final var idu = new InterfaceDataUnit56Dto(context, message);

    final var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());

    var calculatedIdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    final var expectedContext = new Context56Dto("https://host1.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.RESPONSE, false);
    final var expectedIdu = new InterfaceDataUnit56Dto(expectedContext, "FAILED: No certificate");

    assertEquals(calculatedIdu, expectedIdu);
  }

  @Test
  public void dispatchIndicationDifferentIri() throws Exception {
    final X509Certificate certIriDiff = X509CertificateCreator.createCertificate(
        "CN=host8.ca",
        "CN=host8.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host8.ca",
        "SHA256withRSA");

    final var encodedCert = Base64.getEncoder().encodeToString(certIriDiff.getEncoded());
    final var version = new VersionDto("7.0.0", "https://reference");
    final var msgId = UUID.randomUUID();
    final var header = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, msgId, "https://host1.ca:4043",
        "https://host2.ca:4043", encodedCert, false, version);
    final var headerPdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(header);
    final var content = "yeah!";

    final KeyPair secondKey = X509CertificateCreator.generateRsaKeyPair(4096);
    final var stamp = new StampDto(
        sealManager.createSeal(headerPdu, secondKey.getPrivate()),
        sealManager.createSeal(content, key.getPrivate()));

    final var pdu = new ProtocolDataUnit5NETDto(header, stamp, content);
    final var message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final var context = new Context56Dto("https://host2.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    final var idu = new InterfaceDataUnit56Dto(context, message);

    final var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());

    var calculatedIdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    final var expectedContext = new Context56Dto("https://host1.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.RESPONSE, false);
    final var expectedIdu =
        new InterfaceDataUnit56Dto(expectedContext,
            "FAILED: The speds-toolkit certificate verification returns false");

    assertEquals(calculatedIdu, expectedIdu);
  }

  @Test
  public void dispatchIndicationInvalidHeaderSeal() throws Exception {
    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var version = new VersionDto("7.0.0", "https://reference");
    final var msgId = UUID.randomUUID();
    final var header = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, msgId, "https://host1.ca:4043",
        "https://host2.ca:4043", encodedCert, false, version);
    final var headerPdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(header);
    final var content = "yeah!";

    final KeyPair secondKey = X509CertificateCreator.generateRsaKeyPair(4096);
    final var stamp = new StampDto(
        sealManager.createSeal(headerPdu, secondKey.getPrivate()),
        sealManager.createSeal(content, key.getPrivate()));

    final var pdu = new ProtocolDataUnit5NETDto(header, stamp, content);
    final var message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final var context = new Context56Dto("https://host2.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    final var idu = new InterfaceDataUnit56Dto(context, message);

    final var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());

    var calculatedIdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    final var expectedContext = new Context56Dto("https://host1.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.RESPONSE, false);
    final var expectedIdu =
        new InterfaceDataUnit56Dto(expectedContext, "FAILED: Invalid Header Seal");

    assertEquals(calculatedIdu, expectedIdu);
  }

  @Test
  public void dispatchIndicationInvalidContentSeal() throws Exception {
    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var version = new VersionDto("7.0.0", "https://reference");
    final var msgId = UUID.randomUUID();
    final var header = new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, msgId, "https://host1.ca:4043",
        "https://host2.ca:4043", encodedCert, false, version);
    final var headerPdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(header);
    final var content = "yeah!";

    final KeyPair secondKey = X509CertificateCreator.generateRsaKeyPair(4096);
    final var stamp = new StampDto(
        sealManager.createSeal(headerPdu, key.getPrivate()),
        sealManager.createSeal(content, secondKey.getPrivate()));

    final var pdu = new ProtocolDataUnit5NETDto(header, stamp, content);
    final var message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final var context = new Context56Dto("https://host2.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    final var idu = new InterfaceDataUnit56Dto(context, message);

    final var result = dispatcher.handle(idu);

    verify(host).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());

    var calculatedIdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    final var expectedContext = new Context56Dto("https://host1.ca:4043", "transfer",
        Context56Dto.ServicePrimitive.RESPONSE, false);
    final var expectedIdu =
        new InterfaceDataUnit56Dto(expectedContext, "FAILED: Invalid Content Seal");

    assertEquals(calculatedIdu, expectedIdu);
  }
}
