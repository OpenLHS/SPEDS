package ca.griis.speds.application.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.internal.ApplicationHostFactory;
import ca.griis.speds.application.internal.verification.InterfaceChecker;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class Environment {
  @Mock
  private ApplicationHostEvent serverConsumer;
  private final PresentationHost preHostServer = mock(PresentationHost.class);

  private ApplicationHost client;
  private ApplicationHost server;
  private final PresentationHost preHost = mock(PresentationHost.class);

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private ProjectService projectService;

  @Mock
  private ApplicationHostEvent clientConsumer;

  public Environment() {
    MockitoAnnotations.openMocks(this);
  }

  public void initClient(InterfaceChecker interfaceChecker) {
    when(projectService.checkPlanActivity(anyString())).thenReturn(true);

    ApplicationHostFactory factory = new ApplicationHostFactory(
        cryptographyService, projectService);

    ApplicationHostFactory spyFactory = spy(factory);
    doReturn(preHost).when(spyFactory)
        .initPresentationHost(anyString(), any(PresentationHostEvent.class));

    String parameters = """
        {
          "options": {
            "speds.app.version": "1.0.0",
            "speds.app.reference": "app-ref",
            "speds.pre.version": "1.0.0",
            "speds.pre.reference": "pre-ref"
          }
        }
        """;

    this.client = spyFactory.init(parameters, clientConsumer, interfaceChecker);
  }


  public void initServer() {
    initServer(content -> true);
  }

  public void initServer(InterfaceChecker interfaceChecker) {
    when(projectService.checkPlanActivity(anyString())).thenReturn(true);

    ApplicationHostFactory factory = new ApplicationHostFactory(
        cryptographyService, projectService);

    ApplicationHostFactory spyFactory = spy(factory);
    doReturn(preHostServer).when(spyFactory)
        .initPresentationHost(anyString(), any(PresentationHostEvent.class));

    String parameters = """
        {
          "options": {
            "speds.app.version": "1.0.0",
            "speds.app.reference": "app-ref",
            "speds.pre.version": "1.0.0",
            "speds.pre.reference": "pre-ref"
          }
        }
        """;

    this.server = spyFactory.init(parameters, serverConsumer, interfaceChecker);
  }

  public void initClient() {
    initClient(content -> true);
  }

  public ApplicationHostEvent getServerConsumer() {
    return serverConsumer;
  }

  public PresentationHost getHostServer() {
    return preHostServer;
  }

  public ApplicationHost getClient() {
    return client;
  }

  public ApplicationHost getServer() {
    return server;
  }

  public PresentationHost getHost() {
    return preHost;
  }

  public ProjectService getProjectService() {
    return projectService;
  }
}
