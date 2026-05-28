package ca.griis.speds.session.unit.internal.handler.request;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.SesPubEnvDto;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PendingMessage;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.request.SesDelegateRequestHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.session.internal.serializer.SharedObjectMapper;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SesDelegateRequestHandlerTest {
  private static final String PROJECT_ID = UUID.randomUUID().toString();
  private static final UUID MESSAGE_ID = UUID.randomUUID();
  private static final VersionDto VERSION = new VersionDto("number", "reference");
  private static final String SOURCE_CODE = "source_code";
  private static final String DESTINATION_CODE = "destination_code";
  private static final IRI SOURCE_IRI = IRIFactory.iriImplementation().create("source_iri");
  private static final IRI DESTINATION_IRI =
      IRIFactory.iriImplementation().create("destination_iri");

  private String requestResult = null;

  @Mock
  private AuthorizationService projectService;

  private CryptographyService cryptographyService;

  @Mock
  private CertificatePrivateKeysEntry certificatePrivateKeysEntry;

  @Captor
  private ArgumentCaptor<String> iduCaptor;

  private KeyPair keyPair;

  private SesDelegateRequestHandler sesDelegateRequestHandler;

  private Cache<SessionId, SessionInformation> sessions;

  private SecretKey sdek;

  @Mock
  private SessionHostEvent sessionHostEvent;

  @BeforeEach
  public void setUp() throws Exception {
    final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(4096);
    this.keyPair = keyGen.generateKeyPair();

    this.sessions = Caffeine.newBuilder()
        .expireAfterWrite(10L, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .build();

    final Cache<UUID, PendingMessage> pendingConfirmations = Caffeine.newBuilder()
        .expireAfterWrite(10L, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .build();

    final String configJson =
        """
            {
              "spedsProfile": [
                 {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "SYMM",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "ASYM",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "HASH",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "SIGN",
                  "securityProfile": "STRONG"
                },
                {
                  "spedsLayer": "SESSION",
                  "algorithmCategory": "DH",
                  "securityProfile": "STRONG"
                }
              ]
            }
            """;

    this.cryptographyService =
        new DefaultCryptographyFactory().initCipherSuite(configJson);

    TransportHost transportHost = new TransportHost() {
      @Override
      public CompletableFuture<Optional<String>> submitIdu(String s) {
        requestResult = s;
        final Sidu confirmSidu =
            new Sidu(new Context34Dto(SOURCE_CODE, DESTINATION_CODE, SOURCE_IRI.toString(),
                Context34Dto.Service.DELEGATE, Context34Dto.ServicePrimitive.CONFIRM,
                DESTINATION_IRI.toString(), false),
                "SUCCEED");
        final String serializedConfirmSidu;
        try {
          serializedConfirmSidu =
              SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmSidu);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(Optional.of(serializedConfirmSidu));
      }

      @Override
      public void close() {

      }
    };

    final HostStartupContext context =
        new HostStartupContext(
            transportHost,
            SharedObjectMapper.getInstance().getMapper(),
            projectService,
            VERSION,
            certificatePrivateKeysEntry,
            MESSAGE_ID::toString,
            cryptographyService,
            sessions,
            pendingConfirmations,
            sessionHostEvent,
            Executors.newFixedThreadPool(1),
            60);

    this.sesDelegateRequestHandler = new SesDelegateRequestHandler(context);
    this.sdek = cryptographyService.generateSymmetricKey(SESSION);
  }

  @Test
  public void handleTest() throws Exception {
    final Pidu requestPidu =
        new Pidu(new Context23Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE,
            Base64.getEncoder().encodeToString(sdek.getEncoded()), Context23Dto.Service.DELEGATE,
            Context23Dto.ServicePrimitive.REQUEST, false),
            "Presentation payload");

    final Pidu confirmPidu =
        new Pidu(new Context23Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE,
            Base64.getEncoder().encodeToString(sdek.getEncoded()), Context23Dto.Service.DELEGATE,
            Context23Dto.ServicePrimitive.CONFIRM, false),
            "SUCCEED");
    final String serializedConfirmPidu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmPidu);

    final Certificate cert = mock(Certificate.class);
    when(certificatePrivateKeysEntry.getCertficate()).thenReturn(cert);
    when(cert.getPublicKey()).thenReturn(keyPair.getPublic());
    when(projectService.getEntityIri(eq(PROJECT_ID), eq(SOURCE_CODE))).thenReturn(SOURCE_IRI);
    when(projectService.getEntityIri(eq(PROJECT_ID), eq(DESTINATION_CODE))).thenReturn(
        DESTINATION_IRI);

    Optional<String> actualConfirmPidu = this.sesDelegateRequestHandler.handle(requestPidu);

    int i = 0;
    while (requestResult == null && i < 50) {
      Thread.sleep(100);
      i++;
    }

    final Sidu actualRequestSidu =
        SharedObjectMapper.getInstance().getMapper().readValue(requestResult, Sidu.class);
    final Spdu actualRequestSpdu =
        SharedObjectMapper.getInstance().getMapper()
            .readValue(actualRequestSidu.getMessage(), Spdu.class);
    final SesPubEnvDto actualRequestPubEnv =
        SharedObjectMapper.getInstance().getMapper()
            .readValue((String) actualRequestSpdu.getContent(), SesPubEnvDto.class);

    final SessionId sessionId = this.sessions.asMap().keySet().stream().findAny().orElseThrow();

    final SesPubEnvDto expectedRequestPubEnv =
        new SesPubEnvDto(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
            sessionId.id());
    assertEquals(expectedRequestPubEnv, actualRequestPubEnv);
    assertEquals(HeaderDto.Msgtype.SES_PUB_ENV, actualRequestSpdu.getHeader().getMsgtype());
    assertEquals(false, actualRequestSpdu.getHeader().getParameters());
    assertEquals(VERSION, actualRequestSpdu.getHeader().getVersion());
    assertEquals("0", actualRequestSpdu.getStamp());

    final SiduContext expectedSiduContext = new SiduContext(
        requestPidu.getContext().getSourceCode(),
        requestPidu.getContext().getDestinationCode(),
        SOURCE_IRI.toString(),
        Context34Dto.Service.TRANSFER,
        Context34Dto.ServicePrimitive.REQUEST,
        DESTINATION_IRI.toString(),
        false);
    assertEquals(expectedSiduContext, actualRequestSidu.getContext());

    assertEquals(serializedConfirmPidu, actualConfirmPidu.orElseThrow());
  }

  @Test
  public void notifyPubRecTest() throws Exception {}
}
