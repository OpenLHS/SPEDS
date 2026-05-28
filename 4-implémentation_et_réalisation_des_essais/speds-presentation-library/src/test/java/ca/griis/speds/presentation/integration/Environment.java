package ca.griis.speds.presentation.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostFactory;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.toolkit.project.ProjectService;
import org.mockito.MockedConstruction;

public class Environment {

  private final String version = "version";

  private final String reference = "reference";

  private final PresentationHost presentationHost;

  private final SessionHost sessionHost = mock(SessionHost.class);

  private final ProjectService projectService = mock(ProjectService.class);

  private CryptographyService cryptographyService;

  private SessionHostEvent sessionHostEvent;

  private PreEvent event = new PreEvent();

  public Environment() throws Exception {
    final String parameters =
        "{\"options\":{" + "\"speds.pre.version\":\"" + version + "\","
            + "\"speds.pre.reference\":\"" + reference + "\"}}";

    this.presentationHost = getPresentationHostFactory().initHost(parameters, event);
  }

  public String getHostResult() {
    return event.getResult();
  }

  public void setHostResult(String hostResult) {
    this.event.notifyIdu(hostResult);
  }

  public SessionHostEvent getSessionHostEvent() {
    return sessionHostEvent;
  }

  public void setSessionHostEvent(SessionHostEvent sessionHostEvent) {
    this.sessionHostEvent = sessionHostEvent;
  }

  public PresentationHost getPresentationHost() {
    return presentationHost;
  }

  public CryptographyService getCryptographyService() {
    return cryptographyService;
  }

  public SessionHost getSessionHost() {
    return sessionHost;
  }

  public VersionDto getVersion() {
    return new VersionDto(version, reference);
  }

  private PresentationHostFactory getPresentationHostFactory() throws Exception {
    final String securityConfig =
        "{\"spedsProfile\":[{\"spedsLayer\":\"PRESENTATION\",\"algorithmCategory\":\"SYMM\",\"securityProfile\":\"STRONG\"}]}";

    this.cryptographyService = new DefaultCryptographyFactory().initCipherSuite(securityConfig);

    try (MockedConstruction<SessionHostFactory> ignored =
        mockConstruction(SessionHostFactory.class)) {

      return new PresentationHostFactory(projectService, this.cryptographyService) {
        @Override
        public SessionHost initSessionHost(String parameters, SessionHostEvent hostEventConsumer) {
          setSessionHostEvent(hostEventConsumer);
          return sessionHost;
        }
      };
    }
  }
}
