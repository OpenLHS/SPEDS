package ca.griis.speds.application.unit.api;

import static ca.griis.js2p.gen.speds.application.api.dto.MsgType.PLAN;
import static ca.griis.js2p.gen.speds.application.api.dto.Service.DELEGATE;
import static ca.griis.js2p.gen.speds.application.api.dto.Service.TRANSFER;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.CONFIRM;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.INDICATION;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.REQUEST;
import static ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive.RESPONSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.VersionDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.internal.ApplicationHostFactory;
import ca.griis.speds.application.internal.domain.ApplicationInterface;
import ca.griis.speds.application.internal.verification.DefaultInterfaceChecker;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableApplicationHostTest {
  private static final String SOURCE_CODE = "source_code";
  private static final String DESTINATION_CODE = "destination_code";
  private static final String PROJECT_ID = UUID.randomUUID().toString();
  private static final String MESSAGE_ID = UUID.randomUUID().toString();
  private static final String SUCCEED = "SUCCEED";
  private static final String FAILED_SYNTAX = "FAILED: syntax";
  private static final String FAILED_PLAN = "FAILED: ended plan";
  private static final Map<String, String> TN = Map.of("TN", UUID.randomUUID().toString());

  @Mock
  private PresentationHost presentationHost;

  @Mock
  private CryptographyService cryptographyService;

  @Mock
  private ProjectService projectService;

  @Mock
  private ApplicationHostEvent applicationHostEvent;

  private PresentationHostEvent presentationHostEvent;

  private String params;

  private ApplicationHostFactory applicationHostFactory;

  private ApplicationInterface actualIndication;

  @BeforeEach
  public void setUp() {
    this.params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }
        """;
    this.applicationHostFactory =
        new ApplicationHostFactory(cryptographyService, projectService) {
          @Override
          public PresentationHost initPresentationHost(String parameters,
              PresentationHostEvent consumer) {
            presentationHostEvent = consumer;
            return presentationHost;
          }
        };
    this.applicationHostEvent = new ApplicationHostEvent() {
      @Override
      public CompletableFuture<ApplicationInterface> notify(
          ApplicationInterface applicationInterface) {
        actualIndication = applicationInterface;

        final ApplicationInterface response =
            new ApplicationInterface(TRANSFER, RESPONSE, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
                MESSAGE_ID, PLAN, SUCCEED);
        return CompletableFuture.completedFuture(response);
      }

      @Override
      public void notifyException(Exception exception) {}
    };
  }

  @Test
  public void submitTest() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, new DefaultInterfaceChecker());

    final Map<String, Object> requestMap = Map.of("request", "content");
    final String requestContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(
        requestMap);
    final ApplicationInterface request =
        new ApplicationInterface(DELEGATE, REQUEST, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, requestContent);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(projectService.checkPlanActivity(eq(PROJECT_ID))).thenReturn(true);

    final InterfaceDataUnit12Dto iduConfirm = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, DELEGATE, CONFIRM, false),
        SUCCEED);
    final String presentationConfirm =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduConfirm);
    when(presentationHost.submitIdu(iduCaptor.capture())).thenReturn(
        CompletableFuture.completedFuture(Optional.of(presentationConfirm)));

    final CompletableFuture<ApplicationInterface> actualConfirm = applicationHost.submit(request);

    final ApplicationInterface expectedConfirm =
        new ApplicationInterface(DELEGATE, CONFIRM, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, SUCCEED);
    assertEquals(expectedConfirm, actualConfirm.get());

    final String actualRequest = iduCaptor.getValue();

    final ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(PLAN, UUID.fromString(MESSAGE_ID), false,
            new VersionDto("2.0.0", "a reference")),
        requestMap);
    final InterfaceDataUnit12Dto iduRequest = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, DELEGATE, REQUEST, false),
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu));
    final String expectedRequest =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduRequest);

    assertEquals(expectedRequest, actualRequest);
  }

  @Test
  public void submitTestInvalidPayload() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, content -> false);

    final Map<String, Object> requestMap = Map.of("request", "content");
    final String requestContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(
        requestMap);
    final ApplicationInterface request =
        new ApplicationInterface(DELEGATE, REQUEST, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, requestContent);

    verifyNoInteractions(presentationHost);

    final CompletableFuture<ApplicationInterface> actualConfirm = applicationHost.submit(request);

    final ApplicationInterface expectedConfirm =
        new ApplicationInterface(DELEGATE, CONFIRM, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, FAILED_SYNTAX);
    assertEquals(expectedConfirm, actualConfirm.get());
  }

  @Test
  public void submitTestNoPlan() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, new DefaultInterfaceChecker());

    final Map<String, Object> requestMap = Map.of("request", "content");
    final String requestContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(
        requestMap);
    final ApplicationInterface request =
        new ApplicationInterface(DELEGATE, REQUEST, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, requestContent);

    when(projectService.checkPlanActivity(eq(PROJECT_ID))).thenReturn(false);

    verifyNoInteractions(presentationHost);

    final CompletableFuture<ApplicationInterface> actualConfirm = applicationHost.submit(request);

    final ApplicationInterface expectedConfirm =
        new ApplicationInterface(DELEGATE, CONFIRM, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, FAILED_PLAN);
    assertEquals(expectedConfirm, actualConfirm.get());
  }

  @Test
  public void submitTestDeserializationFailure() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, content -> true);

    final ApplicationInterface request =
        new ApplicationInterface(DELEGATE, REQUEST, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, "wrongPayloadFormat");

    when(projectService.checkPlanActivity(eq(PROJECT_ID))).thenReturn(true);
    verifyNoInteractions(presentationHost);

    final CompletableFuture<ApplicationInterface> actualConfirmFuture =
        applicationHost.submit(request);
    final ApplicationInterface actualConfirm = actualConfirmFuture.get();

    final ApplicationInterface expectedConfirm =
        new ApplicationInterface(DELEGATE, CONFIRM, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, "FAILED: <information>");
    assertEquals(expectedConfirm.service(), actualConfirm.service());
    assertEquals(expectedConfirm.servicePrimitive(), actualConfirm.servicePrimitive());
    assertEquals(expectedConfirm.sourceCode(), actualConfirm.sourceCode());
    assertEquals(expectedConfirm.destinationCode(), actualConfirm.destinationCode());
    assertEquals(expectedConfirm.projectId(), actualConfirm.projectId());
    assertEquals(expectedConfirm.msgId(), actualConfirm.msgId());
    assertEquals(expectedConfirm.msgType(), actualConfirm.msgType());
    assertTrue(actualConfirm.content().startsWith("FAILED: "));
  }

  @Test
  public void notifyTest() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, new DefaultInterfaceChecker());
    assertNotNull(applicationHost);

    final Map<String, Object> requestMap = Map.of("request", "content");
    final String requestContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(
        requestMap);

    final ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(PLAN, UUID.fromString(MESSAGE_ID), false,
            new VersionDto("2.0.0", "a reference")),
        requestMap);
    final InterfaceDataUnit12Dto iduRequest = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, DELEGATE, CONFIRM, TN),
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu));
    final String serializedRequest =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduRequest);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(projectService.checkPlanActivity(eq(PROJECT_ID))).thenReturn(true);
    when(presentationHost.submitIdu(iduCaptor.capture())).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    presentationHostEvent.notifyIdu(serializedRequest);

    final ApplicationInterface expectedIndication =
        new ApplicationInterface(TRANSFER, INDICATION, SOURCE_CODE, DESTINATION_CODE, PROJECT_ID,
            MESSAGE_ID, PLAN, requestContent);
    assertEquals(expectedIndication, actualIndication);

    final String actualResponse = iduCaptor.getValue();
    final InterfaceDataUnit12Dto iduResponse = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, TRANSFER, RESPONSE, TN),
        SUCCEED);
    final String expectedResponse =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduResponse);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void notifyTestInvalidPayload() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, content -> false);
    assertNotNull(applicationHost);

    final Map<String, Object> requestMap = Map.of("request", "content");

    final ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(PLAN, UUID.fromString(MESSAGE_ID), false,
            new VersionDto("2.0.0", "a reference")),
        requestMap);
    final InterfaceDataUnit12Dto iduRequest = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, DELEGATE, CONFIRM, TN),
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu));
    final String serializedRequest =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduRequest);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(presentationHost.submitIdu(iduCaptor.capture())).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    presentationHostEvent.notifyIdu(serializedRequest);

    final String actualResponse = iduCaptor.getValue();
    final InterfaceDataUnit12Dto iduResponse = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, TRANSFER, RESPONSE, TN),
        FAILED_SYNTAX);
    final String expectedResponse =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduResponse);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void notifyTestNoPlan() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, new DefaultInterfaceChecker());
    assertNotNull(applicationHost);

    final Map<String, Object> requestMap = Map.of("request", "content");

    final ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(PLAN, UUID.fromString(MESSAGE_ID), false,
            new VersionDto("2.0.0", "a reference")),
        requestMap);
    final InterfaceDataUnit12Dto iduRequest = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, DELEGATE, CONFIRM, TN),
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu));
    final String serializedRequest =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduRequest);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(projectService.checkPlanActivity(eq(PROJECT_ID))).thenReturn(false);
    when(presentationHost.submitIdu(iduCaptor.capture())).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    presentationHostEvent.notifyIdu(serializedRequest);

    final String actualResponse = iduCaptor.getValue();
    final InterfaceDataUnit12Dto iduResponse = new InterfaceDataUnit12Dto(
        new Context12Dto(PROJECT_ID, SOURCE_CODE, DESTINATION_CODE, TRANSFER, RESPONSE, TN),
        FAILED_PLAN);
    final String expectedResponse =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(iduResponse);
    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void notifyWrongIdu() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, new DefaultInterfaceChecker());
    assertNotNull(applicationHost);

    presentationHostEvent.notifyIdu("wrongIdu");
  }

  @Test
  public void closeTest() throws Exception {
    final ApplicationHost applicationHost =
        applicationHostFactory.init(params, applicationHostEvent, content -> true);

    applicationHost.close();

    verify(presentationHost).close();
  }
}
