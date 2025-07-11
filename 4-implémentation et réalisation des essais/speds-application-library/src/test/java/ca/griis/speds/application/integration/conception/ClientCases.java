package ca.griis.speds.application.integration.conception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InArgDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ParamDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheReceptionDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.DeserializationException;
import ca.griis.speds.application.api.exception.InvalidPduIdException;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ClientCases {
  private PgaService pgaServiceMock;
  private PresentationHost mockPresentationHost;
  private SyncApplicationFactory factory;
  private String taskRecContent;
  private ApplicationHost host;

  public ClientCases() throws JsonProcessingException {
    pgaServiceMock = Mockito.mock(PgaService.class);
    mockPresentationHost = Mockito.mock(ImmutablePresentationHost.class);

    factory = new SyncApplicationFactory(pgaServiceMock) {
      @Override
      public PresentationHost initPresentationHost(String parameters) {
        return mockPresentationHost;
      }
    };

    TacheReceptionDto taskRecDto = new TacheReceptionDto(
        "26ad7aef-16c2-4ef8-b196-f652026d513c",
        "11666f67-9680-4903-a91c-9b8ed459962c",
        Date.valueOf("2025-03-12"));
    taskRecContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(taskRecDto);
  }

  public void ct001_init_success() throws Exception {
    final String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }""";

    host = factory.init(params);
    assertNotNull(host);
  }

  public void ct006_request_success() throws Exception {
    // Message construction
    List<InArgDto> inArgs = Arrays.asList(
        new InArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new InArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    TacheEnvoiDto taskEnvDto =
        new TacheEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            inArgs,
            new ArrayList<ParamDto>(),
            TacheEnvoiDto.Command.START);
    String taskEnvContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(taskEnvDto);
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_ENVOI,
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskEnvContent);
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    // IDU construction
    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "executor",
        "connector_1",
        null,
        Boolean.FALSE);
    InterfaceDataUnit01Dto idu = new InterfaceDataUnit01Dto(context, message);

    // Behavior test
    host.request(idu);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockPresentationHost).request(captor.capture());

    String result = captor.getValue();
    assertNotNull(result);

    // IDU Comparaison
    InterfaceDataUnit12Dto resultIdu =
        SharedObjectMapper.getInstance().getMapper().readValue(result,
            InterfaceDataUnit12Dto.class);
    InterfaceDataUnit12Dto expectedIdu = new InterfaceDataUnit12Dto(new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "executor",
        "connector_1",
        null,
        Boolean.FALSE), message);

    assertEquals(expectedIdu, resultIdu);
  }

  public void ct007_confirm_success() throws Exception {
    // Response message construction
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_RECEPTION,
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskRecContent);
    final String message =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "executor",
        "connector_1",
        null,
        Boolean.FALSE);
    final String indicationIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(context, message));

    when(mockPresentationHost.confirm()).thenReturn(indicationIdu);

    InterfaceDataUnit01Dto confirmResult = host.confirm();
    InterfaceDataUnit01Dto confirmIdu = new InterfaceDataUnit01Dto(context, message);

    assertEquals(confirmResult, confirmIdu);
    assertEquals(confirmResult.getContext(), confirmIdu.getContext());
    assertEquals(confirmResult.getMessage(), confirmIdu.getMessage());
  }

  public void ct008_confirm_deserialization_exception() throws Exception {
    // Response message construction
    final String escapedContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(taskRecContent);
    final String pdu = """
        {
          "header":
          {
            "msgtype": "TYPE.INEXISTENT",
            "id": "a714866d-f28f-41e8-bb29-0116c2599d2a",
            "parameters": false,
            "SPEDS":
            {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": """ + escapedContent + """
        }""";
    final String message =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    // Indication IDU construction
    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "connector_1",
        "executor",
        null,
        Boolean.FALSE);
    final String responseIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(context, message));

    when(mockPresentationHost.confirm()).thenReturn(responseIdu);

    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.confirm();
    });
    assertNotNull(exception);
  }

  public void ct009_confirm_invalidPduId_exception() throws Exception {
    // Response message construction
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_RECEPTION,
            UUID.fromString("236bfe3a-3e9d-4d94-ada5-b69d051bcea3"),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskRecContent);
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    // Indication IDU construction
    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "executor",
        "connector_1",
        null,
        Boolean.FALSE);
    final String idu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(context, message));

    when(mockPresentationHost.confirm()).thenReturn(idu);

    InvalidPduIdException exception = assertThrows(InvalidPduIdException.class, () -> {
      host.confirm();
    });
    assertNotNull(exception);
  }
}
