package ca.griis.speds.application.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.MsgType;
import ca.griis.js2p.gen.speds.application.api.dto.Service;
import ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive;
import ca.griis.speds.application.internal.domain.ApplicationInterface;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.mockito.ArgumentCaptor;

public class Cases {

  static SharedObjectMapper mapper = SharedObjectMapper.getInstance();

  public static void ct_01(Environment environment)
      throws JsonProcessingException, ExecutionException, InterruptedException {

    ApplicationInterface appInterface = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "source",
        "destination",
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
        MsgType.PLAN,
        "{\"key\": \"value\"}");
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(environment.getHost().submitIdu(captor.capture()))
        .thenAnswer(invocation -> {
          String request = invocation.getArgument(0);
          return CompletableFuture.completedFuture(Optional.of(request));
        });

    CompletableFuture<ApplicationInterface> result = environment.getClient().submit(appInterface);

    String capturedRequest = captor.getValue();
    InterfaceDataUnit12Dto capturedIdu =
        mapper.getMapper().readValue(capturedRequest, InterfaceDataUnit12Dto.class);

    assertEquals(appInterface.projectId(), capturedIdu.getContext().getPga());
    assertEquals(appInterface.sourceCode(), capturedIdu.getContext().getSourceCode());
    assertEquals(appInterface.destinationCode(), capturedIdu.getContext().getDestinationCode());
    assertEquals("delegate", capturedIdu.getContext().getService().toString());
    assertEquals("request", capturedIdu.getContext().getServicePrimitive().toString());

    assertEquals(appInterface.projectId(), result.get().projectId());
    assertEquals(appInterface.sourceCode(), result.get().sourceCode());
    assertEquals(appInterface.destinationCode(), result.get().destinationCode());
    assertEquals("delegate", result.get().service().toString());
    assertEquals("confirm", result.get().servicePrimitive().toString());
  }

  public static void ct_02(Environment environment)
      throws ExecutionException, InterruptedException {
    ApplicationInterface appInterface = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "source",
        "destination",
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
        MsgType.PLAN,
        "{}");

    CompletableFuture<ApplicationInterface> result = environment.getClient().submit(appInterface);

    assertEquals("delegate", result.get().service().toString());
    assertEquals("confirm", result.get().servicePrimitive().toString());
    assertEquals("source", result.get().sourceCode());
    assertEquals("destination", result.get().destinationCode());
    assertEquals("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", result.get().projectId());
    assertEquals("daffd463-7b1a-424b-b26f-f8ffd1f54a77", result.get().msgId());
    assertEquals(MsgType.PLAN, result.get().msgType());
    assertEquals("FAILED: syntax", result.get().content());
  }

  public static void ct_03(Environment environment)
      throws ExecutionException, InterruptedException {
    String content = """
        {
          "action": "INSTRUCTION.REQUEST",
          "task_ref": "550e8400-e29b-41d4-a716-446655440000",
          "task_def": "aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4",
          "inArgs": [
            {
              "Name": "validNumber",
              "Value": "(Value (Number (Token \\"1\\")))"
            }
          ],
          "params": [],
          "command": "START"
        }
        """;

    ApplicationInterface appInterface = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "source",
        "destination",
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
        MsgType.PLAN,
        content);

    CompletableFuture<ApplicationInterface> result = environment.getClient().submit(appInterface);

    assertEquals("delegate", result.get().service().toString());
    assertEquals("confirm", result.get().servicePrimitive().toString());
    assertEquals("source", result.get().sourceCode());
    assertEquals("destination", result.get().destinationCode());
    assertEquals("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", result.get().projectId());
    assertEquals("daffd463-7b1a-424b-b26f-f8ffd1f54a77", result.get().msgId());
    assertEquals(MsgType.PLAN, result.get().msgType());
    assertEquals("FAILED: ended plan", result.get().content());
  }

  public static void ct_04(Environment environment) throws Exception {
    String content = """
        {
          "action": "INSTRUCTION.REQUEST",
          "task_ref": "550e8400-e29b-41d4-a716-446655440000",
          "task_def": "aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4",
          "inArgs": [],
          "params": [],
          "command": "START"
        }
        """;

    ApplicationInterface appInterface = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.REQUEST,
        "source",
        "destination",
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
        MsgType.PLAN,
        content);

    when(environment.getHost().submitIdu(any(String.class)))
        .thenReturn(new CompletableFuture<>());

    CompletableFuture<ApplicationInterface> result =
        CompletableFuture.supplyAsync(() -> environment.getClient().submit(appInterface).join());

    assertThrows(java.util.concurrent.TimeoutException.class, () -> {
      result.get(3, java.util.concurrent.TimeUnit.SECONDS);
    });
  }

  public static void ct_05(Environment environment) throws Exception {
    String pduJson = """
        {
          "header": {
            "msgtype": "PLAN",
            "id": "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
            "parameters": false,
            "version": {
              "number": "1.0.0",
              "reference": "app-ref"
            }
          },
          "content": {
            "action": "INSTRUCTION.REQUEST",
            "task_ref": "550e8400-e29b-41d4-a716-446655440000",
            "task_def": "aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4",
            "inArgs": [],
            "params": [],
            "command": "START"
          }
        }
        """;

    Context12Dto indicationContext = new Context12Dto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "source",
        "destination",
        Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    InterfaceDataUnit12Dto indicationIdu = new InterfaceDataUnit12Dto(
        indicationContext,
        pduJson);

    ArgumentCaptor<ApplicationInterface> notifyCaptor =
        ArgumentCaptor.forClass(ApplicationInterface.class);

    ApplicationInterface consumerResponse = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        "destination",
        "source",
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
        MsgType.PLAN,
        "{\"result\": \"SUCCESS\"}");

    when(environment.getServerConsumer().notify(notifyCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(consumerResponse));

    ArgumentCaptor<String> submitCaptor = ArgumentCaptor.forClass(String.class);
    when(environment.getHostServer().submitIdu(submitCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    String stringIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(indicationIdu);
    environment.getServer().notifyIdu(stringIdu);

    ApplicationInterface capturedIndication = notifyCaptor.getValue();
    assertEquals(Service.TRANSFER, capturedIndication.service());
    assertEquals(ServicePrimitive.INDICATION, capturedIndication.servicePrimitive());
    assertEquals("source", capturedIndication.sourceCode());
    assertEquals("destination", capturedIndication.destinationCode());
    assertEquals("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", capturedIndication.projectId());
    assertEquals("daffd463-7b1a-424b-b26f-f8ffd1f54a77", capturedIndication.msgId());
    assertEquals(MsgType.PLAN, capturedIndication.msgType());
    assertEquals(
        "{\"action\":\"INSTRUCTION.REQUEST\",\"task_ref\":\"550e8400-e29b-41d4-a716-446655440000\",\"task_def\":\"aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4\",\"inArgs\":[],\"params\":[],\"command\":\"START\"}",
        capturedIndication.content());

    String capturedSubmit = submitCaptor.getValue();
    InterfaceDataUnit12Dto capturedResponse =
        mapper.getMapper().readValue(capturedSubmit, InterfaceDataUnit12Dto.class);

    assertEquals("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        capturedResponse.getContext().getPga());
    assertEquals("destination", capturedResponse.getContext().getSourceCode());
    assertEquals("source", capturedResponse.getContext().getDestinationCode());
    assertEquals(Service.TRANSFER, capturedResponse.getContext().getService());
    assertEquals(ServicePrimitive.RESPONSE,
        capturedResponse.getContext().getServicePrimitive());
    assertEquals("{\"result\": \"SUCCESS\"}", capturedResponse.getMessage());
  }

  public static void ct_06(Environment environment) throws Exception {
    String pduJson = """
        {
          "header": {
            "msgtype": "PLAN",
            "id": "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
            "parameters": false,
            "version": {
              "number": "1.0.0",
              "reference": "app-ref"
            }
          },
          "content": {}
        }
        """;

    Context12Dto indicationContext = new Context12Dto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "source",
        "destination",
        Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    InterfaceDataUnit12Dto indicationIdu = new InterfaceDataUnit12Dto(
        indicationContext,
        pduJson);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(environment.getHostServer().submitIdu(captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    String stringIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(indicationIdu);
    environment.getServer().notifyIdu(stringIdu);

    String capturedSubmit = captor.getValue();
    ObjectMapper mapper = new ObjectMapper();
    InterfaceDataUnit12Dto capturedResponse =
        mapper.readValue(capturedSubmit, InterfaceDataUnit12Dto.class);

    assertEquals("FAILED: syntax", capturedResponse.getMessage());
    assertEquals(ServicePrimitive.RESPONSE,
        capturedResponse.getContext().getServicePrimitive());
  }

  public static void ct_07(Environment environment) throws Exception {
    String pduJson = """
        {
          "header": {
            "msgtype": "PLAN",
            "id": "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
            "parameters": false,
            "version": {
              "number": "1.0.0",
              "reference": "app-ref"
            }
          },
          "content": {
            "action": "INSTRUCTION.REQUEST",
            "task_ref": "550e8400-e29b-41d4-a716-446655440000",
            "task_def": "aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4",
            "inArgs": [],
            "params": [],
            "command": "START"
          }
        }
        """;

    Context12Dto indicationContext = new Context12Dto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "source",
        "destination",
        Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    InterfaceDataUnit12Dto indicationIdu = new InterfaceDataUnit12Dto(
        indicationContext,
        pduJson);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(environment.getHostServer().submitIdu(captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    String stringIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(indicationIdu);
    environment.getServer().notifyIdu(stringIdu);

    ObjectMapper mapper = new ObjectMapper();
    InterfaceDataUnit12Dto capturedResponse =
        mapper.readValue(captor.getValue(), InterfaceDataUnit12Dto.class);

    assertEquals("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", capturedResponse.getContext().getPga());
    assertEquals("source", capturedResponse.getContext().getSourceCode());
    assertEquals("destination", capturedResponse.getContext().getDestinationCode());
    assertEquals(Service.TRANSFER, capturedResponse.getContext().getService());
    assertEquals(ServicePrimitive.RESPONSE, capturedResponse.getContext().getServicePrimitive());
    assertEquals("FAILED: ended plan", capturedResponse.getMessage());
  }

  public static void ct_08(Environment environment) throws Exception {
    String pduJson = """
        {
          "header": {
            "msgtype": "PLAN",
            "id": "daffd463-7b1a-424b-b26f-f8ffd1f54a77",
            "parameters": false,
            "version": {
              "number": "1.0.0",
              "reference": "app-ref"
            }
          },
          "content": {
            "action": "INSTRUCTION.REQUEST",
            "task_ref": "550e8400-e29b-41d4-a716-446655440000",
            "task_def": "aa30bc32-6d3f-4e0d-a6d7-29659bf37fe4",
            "inArgs": [],
            "params": [],
            "command": "START"
          }
        }
        """;

    Context12Dto indicationContext = new Context12Dto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "source",
        "destination",
        Service.TRANSFER,
        ServicePrimitive.INDICATION,
        false);

    InterfaceDataUnit12Dto indicationIdu = new InterfaceDataUnit12Dto(
        indicationContext,
        pduJson);

    when(environment.getServerConsumer().notify(any(ApplicationInterface.class)))
        .thenReturn(new CompletableFuture<>());

    String stringIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(indicationIdu);

    CompletableFuture<Void> result =
        CompletableFuture.runAsync(() -> environment.getServer().notifyIdu(stringIdu));

    assertThrows(java.util.concurrent.TimeoutException.class, () -> {
      result.get(3, java.util.concurrent.TimeUnit.SECONDS);
    });
  }
}
