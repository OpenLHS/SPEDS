package ca.griis.speds.session.unit.internal.handler.peer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.speds.session.api.contract.IdentifierGenerator;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.peer.SesSakEnvHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SesSakEnvHandlerTest {

  private SesSakEnvHandler sesSakEnvHandler;
  private TransportHost mockTransport;
  private ConcurrentHashMap<SessionId, SessionInformation> sessionMap;
  private KeyPair hostKeyPair;
  private ObjectMapper objectMapper;

  @Mock
  private HostStartupContext hostStartupContext;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    hostKeyPair = keyGen.generateKeyPair();

    mockTransport = mock(TransportHost.class);
    when(mockTransport.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    sessionMap = new ConcurrentHashMap<>();
    Cache<SessionId, SessionInformation> mockCache = mock(Cache.class);
    when(mockCache.asMap()).thenReturn(sessionMap);

    CertificatePrivateKeysEntry mockKeys =
        mock(CertificatePrivateKeysEntry.class);
    when(mockKeys.getPrivateKey()).thenReturn(hostKeyPair.getPrivate());

    when(hostStartupContext.transportHost()).thenReturn(mockTransport);
    when(hostStartupContext.sharedMapper()).thenReturn(objectMapper);
    when(hostStartupContext.projectService()).thenReturn(mock(AuthorizationService.class));
    when(hostStartupContext.cryptographyService()).thenReturn(mock(CryptographyService.class));
    when(hostStartupContext.pendingMessage()).thenReturn(mock(Cache.class));
    when(hostStartupContext.identifierGenerator()).thenReturn(mock(IdentifierGenerator.class));
    when(hostStartupContext.version()).thenReturn(new VersionDto("7.0.0", "ref"));
    when(hostStartupContext.sessions()).thenReturn(mockCache);
    when(hostStartupContext.hostKeys()).thenReturn(mockKeys);

    sesSakEnvHandler = new SesSakEnvHandler(hostStartupContext);
  }

  @Test
  public void getHandledTypeTest() {
    assertEquals(MsgType.SES_SAK_ENV, sesSakEnvHandler.getHandledType());
  }
}
