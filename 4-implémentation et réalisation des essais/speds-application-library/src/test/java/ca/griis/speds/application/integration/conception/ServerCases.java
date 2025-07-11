package ca.griis.speds.application.integration.conception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.OutArgDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.DeserializationException;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ServerCases {
  private PgaService pgaServiceMock;
  private PresentationHost mockPresentationHost;
  private SyncApplicationFactory factory;
  private String endEnvContent;
  private ApplicationHost host;

  public ServerCases() throws JsonProcessingException {
    pgaServiceMock = Mockito.mock(PgaService.class);
    mockPresentationHost = Mockito.mock(ImmutablePresentationHost.class);

    factory = new SyncApplicationFactory(pgaServiceMock) {
      @Override
      public PresentationHost initPresentationHost(String parameters) {
        return mockPresentationHost;
      }
    };

    FinEnvoiDto finEnvoiDto =
        new FinEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            new ArrayList<OutArgDto>(),
            Date.valueOf("2025-03-12"));
    endEnvContent = SharedObjectMapper.getInstance().getMapper().writeValueAsString(finEnvoiDto);
  }

  public void ct011_init_success() throws Exception {
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

  public void ct016_indication_success() throws Exception {
    // Message construction
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.FIN_ENVOI,
            UUID.fromString("a714866d-f28f-41e8-bb29-0116c2599d2a"),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        endEnvContent);
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    // IDU construction
    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "connector_1",
        "executor",
        UUID.fromString("a714866d-f28f-41e8-bb29-0116c2599d2a"),
        Boolean.FALSE);
    final String idu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(context, message));

    when(mockPresentationHost.indication()).thenReturn(idu);
    InterfaceDataUnit01Dto resultIdu = host.indication();

    // Compares context
    final ContextDto expectedContext =
        new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
            "executor", null, Boolean.FALSE);
    InterfaceDataUnit01Dto expectedIdu = new InterfaceDataUnit01Dto(expectedContext, message);
    assertEquals(expectedIdu.getContext(), resultIdu.getContext());

    // Compares messages
    ProtocolDataUnit1APPDto resultPdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(resultIdu.getMessage(), ProtocolDataUnit1APPDto.class);
    ProtocolDataUnit1APPDto expectedPdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(message, ProtocolDataUnit1APPDto.class);
    assertEquals(resultPdu, expectedPdu);

    // Compares messages content
    FinEnvoiDto resultContent = SharedObjectMapper.getInstance().getMapper()
        .readValue(resultPdu.getContent(), FinEnvoiDto.class);
    FinEnvoiDto expectedContent = SharedObjectMapper.getInstance().getMapper()
        .readValue(endEnvContent, FinEnvoiDto.class);

    assertEquals(expectedContent, resultContent);
  }

  public void ct017_indication_deserialization_exception() throws Exception {
    // Message construction
    final String escapedContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(endEnvContent);
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
    final String message = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu);

    // IDU construction
    final ContextDto context = new ContextDto(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "connector_1",
        "executor",
        UUID.fromString("a714866d-f28f-41e8-bb29-0116c2599d2a"),
        Boolean.FALSE);
    final String idu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(context, message));

    when(mockPresentationHost.indication()).thenReturn(idu);

    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      host.indication();
    });
    assertNotNull(exception);
  }

  public void ct018_response_success() throws Exception {
    // Response message construction
    FinReceptionDto finReceptionDto = new FinReceptionDto("26ad7aef-16c2-4ef8-b196-f652026d513c");
    String endRecContent =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(finReceptionDto);
    ProtocolDataUnit1APPDto responsePdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.FIN_RECEPTION,
            UUID.fromString("a714866d-f28f-41e8-bb29-0116c2599d2a"),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        endRecContent);
    final String responseMessage =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(responsePdu);

    InterfaceDataUnit01Dto responseIdu =
        new InterfaceDataUnit01Dto(
            new ContextDto(
                "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
                "connector_1",
                "executor",
                null,
                Boolean.FALSE),
            responseMessage);

    // Behavior test
    host.response(responseIdu);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockPresentationHost, times(1)).response(captor.capture());

    String result = captor.getValue();
    assertNotNull(result);

    // IDU Comparaison
    InterfaceDataUnit12Dto resultIdu =
        SharedObjectMapper.getInstance().getMapper().readValue(result,
            InterfaceDataUnit12Dto.class);
    InterfaceDataUnit12Dto expectedIdu =
        new InterfaceDataUnit12Dto(new ContextDto(
            "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "connector_1",
            "executor",
            UUID.fromString("a714866d-f28f-41e8-bb29-0116c2599d2a"),
            Boolean.FALSE), responseMessage);

    assertEquals(expectedIdu, resultIdu);
  }
}
