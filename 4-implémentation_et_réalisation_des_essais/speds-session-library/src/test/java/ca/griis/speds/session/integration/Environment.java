package ca.griis.speds.session.integration;

import static ca.griis.speds.session.integration.SessionInformationField.client;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.host.ImmutableSessionHost;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.jena.iri.IRIFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public final class Environment {
  public final String pga;
  public final ObjectMapper objectMapper;
  public final ProjectService projectService;

  private static final String version = "version";
  private static final String reference = "reference";

  private final CryptographyService cryptographyService;

  public final Map<HeaderDto.Msgtype, String> sidu;
  public final SessionHost initiatrice;
  public final SessionHost initiatrice2;
  public final SessionHost partenaire;
  public final SessionHost partenaire2;
  public final SessionParameters clientParameters;
  public final SessionParameters clientParameters2;
  public final SessionParameters serverParameters;
  public final SessionParameters serverParameters2;

  public Boolean expect_null;
  public Boolean delete_ses_info;
  public Class<? extends Throwable> exception_type;

  public TransportHostEvent sourceNotifier;
  public TransportHostEvent sourceNotifier2;
  public TransportHostEvent destinationNotifier;
  public TransportHostEvent destinationNotifier2;

  private final TransportHost clientTransportHost = mock(TransportHost.class);
  private final TransportHost clientTransportHost2 = mock(TransportHost.class);
  private final TransportHost serverTransportHost = mock(TransportHost.class);

  private final LinkedBlockingQueue<String> clientIdus;
  private final LinkedBlockingQueue<String> client2Idus;
  private final LinkedBlockingQueue<String> serverIdus;
  private final LinkedBlockingQueue<String> server2Idus;

  public Environment() throws JsonProcessingException, ParameterException {
    this.pga = UUID.randomUUID().toString();

    final String sourceCode = "source_code";
    final String destinationCode = "destination_code";
    final var sourceIri = IRIFactory.uriImplementation().create("https://source.iri:8080");
    final var destinationIri =
        IRIFactory.uriImplementation().create("https://destination.iri::8081");

    final String sourceCode2 = "source_code2";
    final String destinationCode2 = "destination_code2";
    final var sourceIri2 = IRIFactory.uriImplementation().create("https://source2.iri:8080");
    final var destinationIri2 =
        IRIFactory.uriImplementation().create("https://destination2.iri::8081");

    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
    this.projectService = mock(ProjectService.class);

    this.expect_null = false;
    this.delete_ses_info = false;
    this.exception_type = null;

    this.sidu = new HashMap<>();
    this.cryptographyService = SecurityUtils.createCryptographyService();

    this.clientIdus = new LinkedBlockingQueue<>();
    this.client2Idus = new LinkedBlockingQueue<>();
    this.serverIdus = new LinkedBlockingQueue<>();
    this.server2Idus = new LinkedBlockingQueue<>();

    SessionHostFactory clientSessionFactory =
        new SessionHostFactory(projectService, cryptographyService) {
          @Override
          public TransportHost initTransportHost(String parameters,
              TransportHostEvent hostEventConsumer) {
            sourceNotifier = hostEventConsumer;
            return clientTransportHost;
          }
        };

    SessionHostFactory clientSessionFactory2 =
        new SessionHostFactory(projectService, cryptographyService) {
          @Override
          public TransportHost initTransportHost(String parameters,
              TransportHostEvent hostEventConsumer) {
            sourceNotifier2 = hostEventConsumer;
            return clientTransportHost2;
          }
        };

    SessionHostFactory serverSessionFactory =
        new SessionHostFactory(projectService, cryptographyService) {
          @Override
          public TransportHost initTransportHost(String parameters,
              TransportHostEvent hostEventConsumer) {
            destinationNotifier = hostEventConsumer;
            return serverTransportHost;
          }
        };

    SessionHostFactory serverSessionFactory2 =
        new SessionHostFactory(projectService, cryptographyService) {
          @Override
          public TransportHost initTransportHost(String parameters,
              TransportHostEvent hostEventConsumer) {
            destinationNotifier2 = hostEventConsumer;
            return serverTransportHost;
          }
        };

    final String clientParamsStr;
    final OptionsDto clientOptions;
    final String serverParamsStr;
    final InitInParamsDto serverParams;

    try {
      clientParamsStr = Files.readString(
          Paths.get(getClass().getClassLoader().getResource("initClient.json").toURI()));
      final InitInParamsDto clientParams =
          objectMapper.readValue(clientParamsStr, new TypeReference<>() {});
      clientOptions = objectMapper.convertValue(clientParams.getOptions(), OptionsDto.class);

      serverParamsStr = Files.readString(
          Paths.get(getClass().getClassLoader().getResource("initServer.json").toURI()));
      serverParams = objectMapper.readValue(serverParamsStr, new TypeReference<>() {});
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }

    final OptionsDto serverOptions =
        objectMapper.convertValue(serverParams.getOptions(), OptionsDto.class);

    this.clientParameters =
        new SessionParameters(cryptographyService, clientOptions, sourceCode, sourceIri);
    this.clientParameters2 =
        new SessionParameters(cryptographyService, clientOptions, sourceCode2, sourceIri2);
    this.serverParameters =
        new SessionParameters(cryptographyService, serverOptions, destinationCode, destinationIri);
    this.serverParameters2 =
        new SessionParameters(cryptographyService, serverOptions, destinationCode2,
            destinationIri2);

    SessionHostEvent clientEvent = new SessionHostEvent() {
      @Override
      public void notifyIdu(String idu) {
        notifyClientResult(idu);
      }

      @Override
      public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyException(Exception exception) {}
    };

    SessionHostEvent clientEvent2 = new SessionHostEvent() {
      @Override
      public void notifyIdu(String idu) {
        notifyClient2Result(idu);
      }

      @Override
      public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyException(Exception exception) {}
    };

    SessionHostEvent serverEvent = new SessionHostEvent() {
      @Override
      public void notifyIdu(String idu) {
        notifyServerResult(idu);
      }

      @Override
      public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyException(Exception exception) {}
    };

    SessionHostEvent serverEvent2 = new SessionHostEvent() {
      @Override
      public void notifyIdu(String idu) {
        notifyServer2Result(idu);
      }

      @Override
      public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {}

      @Override
      public void notifyException(Exception exception) {}
    };

    this.initiatrice = clientSessionFactory.initHost(clientParamsStr, clientEvent);
    this.initiatrice2 = clientSessionFactory2.initHost(clientParamsStr, clientEvent2);
    this.partenaire = serverSessionFactory.initHost(serverParamsStr, serverEvent);
    this.partenaire2 = serverSessionFactory2.initHost(serverParamsStr, serverEvent2);

    lenient().when(projectService.getEntityIri(pga, clientParameters.code()))
        .thenReturn(clientParameters.iri());
    lenient().when(projectService.getEntityIri(pga, serverParameters.code()))
        .thenReturn(serverParameters.iri());
    lenient().when(projectService.getEntityIri(pga, clientParameters2.code()))
        .thenReturn(clientParameters2.iri());
    lenient().when(projectService.getEntityIri(pga, serverParameters2.code()))
        .thenReturn(serverParameters2.iri());

    lenient().when(projectService.getEntityPublicKey(pga, sourceCode))
        .thenReturn(clientParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey());
    lenient().when(projectService.getEntityPublicKey(pga, destinationCode))
        .thenReturn(serverParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey());
    lenient().when(projectService.getEntityPublicKey(pga, sourceCode2))
        .thenReturn(clientParameters2.certificatePrivateKeysEntry().getCertficate().getPublicKey());
    lenient().when(projectService.getEntityPublicKey(pga, destinationCode2))
        .thenReturn(serverParameters2.certificatePrivateKeysEntry().getCertficate().getPublicKey());

    lenient().when(projectService.verifyEntityLegitimacy(
        pga,
        sourceCode,
        clientParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey()))
        .thenReturn(true);
    lenient().when(projectService.verifyEntityLegitimacy(
        pga,
        destinationCode,
        serverParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey()))
        .thenReturn(true);
    lenient().when(projectService.verifyEntityLegitimacy(
        pga,
        sourceCode2,
        clientParameters2.certificatePrivateKeysEntry().getCertficate().getPublicKey()))
        .thenReturn(true);
    lenient().when(projectService.verifyEntityLegitimacy(
        pga,
        destinationCode2,
        serverParameters2.certificatePrivateKeysEntry().getCertficate().getPublicKey()))
        .thenReturn(true);
  }

  public static Map<SessionId, SessionInformation> getSessionInformation(
      SessionHost sessionHost,
      SessionInformationField sessionInformationField)
      throws NoSuchFieldException, IllegalAccessException {

    ImmutableSessionHost host = (ImmutableSessionHost) sessionHost;

    final String fieldName = sessionInformationField.equals(client)
        ? "initiatorContext"
        : "peerContext";

    Field field = host.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);

    HostStartupContext context = (HostStartupContext) field.get(host);
    return context.sessions().asMap();
  }

  public CryptographyService getCryptographyService() {
    return cryptographyService;
  }

  public String getClientResult(Long times) throws InterruptedException {
    return clientIdus.poll(times, TimeUnit.SECONDS);
  }

  public String getClient2Result(Long times) throws InterruptedException {
    return client2Idus.poll(times, TimeUnit.SECONDS);
  }

  public String getServerResult(Long times) throws InterruptedException {
    return serverIdus.poll(times, TimeUnit.SECONDS);
  }

  public String getServer2Result(Long times) throws InterruptedException {
    return server2Idus.poll(times, TimeUnit.SECONDS);
  }

  public void notifyClientResult(String clientResult) {
    clientIdus.add(clientResult);
  }

  public void notifyClient2Result(String clientResult) {
    client2Idus.add(clientResult);
  }

  public void notifyServerResult(String serverResult) {
    serverIdus.add(serverResult);
  }

  public void notifyServer2Result(String serverResult) {
    server2Idus.add(serverResult);
  }

  public String getPga() {
    return pga;
  }

  public String getVersion() {
    return version;
  }

  public String getReference() {
    return reference;
  }

  public TransportHost getClientTransportHost() {
    return clientTransportHost;
  }

  public TransportHost getServerTransportHost() {
    return serverTransportHost;
  }

  public TransportHost getClientTransportHost2() {
    return clientTransportHost2;
  }
}
