package ca.griis.speds.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.application.api.dto.CommandeEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.CommandeReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.DataReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.ExceptionReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.FinReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InArgDto;
import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.OutArgDto;
import ca.griis.js2p.gen.speds.application.api.dto.ParamDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaConfirmationDto;
import ca.griis.js2p.gen.speds.application.api.dto.PgaFinDto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.StatutReceptionDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheEnvoiDto;
import ca.griis.js2p.gen.speds.application.api.dto.TacheReceptionDto;
import ca.griis.speds.api.converter.ContentToAppIduConverter;
import ca.griis.speds.api.converter.ContentToSpedsIduConverter;
import ca.griis.speds.api.converter.InterfaceDataUnitConverter;
import ca.griis.speds.api.converter.ProtocolDataUnitConverter;
import ca.griis.speds.api.dto.SpedsInterfaceDataUnit;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestSuite {
  private ServerCases serverCases;
  private ClientCases clientCases;
  private ContentToAppIduConverter converter;
  private ContentToSpedsIduConverter spedsConverter;
  private ProtocolDataUnitConverter pduConverter;
  private ObjectMapper mapper;
  private InterfaceDataUnitConverter iduConverter;

  @BeforeEach
  public void init() throws JsonProcessingException, IllegalArgumentException,
      IllegalAccessException, NoSuchFieldException, SecurityException {
    final String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }""";
    clientCases = new ClientCases(params);

    Map<String, Object> map = new HashMap<>();
    map.put("speds.app.version", "2.0.0");
    map.put("speds.app.reference", "a reference");

    final InitInParamsDto options = new InitInParamsDto(map);
    serverCases = new ServerCases(options);
    mapper = SharedObjectMapper.getInstance().getMapper();
    converter = new ContentToAppIduConverter(mapper, "2.0.0", "a reference");
    spedsConverter = new ContentToSpedsIduConverter(converter);
    pduConverter = new ProtocolDataUnitConverter(mapper);
    iduConverter = new InterfaceDataUnitConverter(mapper);
  }

  @AfterEach
  public void close() {
    clientCases.close();
    serverCases.close();
  }

  @Test
  public void convert_test() throws Exception {
    final PgaFinDto envDto = new PgaFinDto("uwi");
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);

    SpedsInterfaceDataUnit spedsIdu = iduConverter.convertToInterfaceDataUnit(requestIdu);
    assertEquals(spedsIdu.idu(), requestIdu);
    assertEquals(spedsIdu.type(), HeaderDto.Msgtype.PGA_FIN);

    SpedsInterfaceDataUnit scdSpedsIdu =
        iduConverter.convertToInterfaceDataUnit(requestIdu, HeaderDto.Msgtype.PGA_FIN);
    assertEquals(scdSpedsIdu.idu(), requestIdu);
    assertEquals(scdSpedsIdu.type(), HeaderDto.Msgtype.PGA_FIN);

    ProtocolDataUnit1APPDto pdu = iduConverter.convertToProtocolDataUnit(requestIdu);
    ProtocolDataUnit1APPDto msg =
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class);
    assertEquals(pdu, msg);
  }

  @Test
  public void protocol_101_test() throws Exception {
    final List<InArgDto> args = Arrays.asList(
        new InArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new InArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    final TacheEnvoiDto envDto =
        new TacheEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            args,
            new ArrayList<ParamDto>(),
            TacheEnvoiDto.Command.START);
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.TACHE_ENVOI);
    assertEquals(envDto, pduConverter.convertToTacheEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final TacheReceptionDto receptionDto = new TacheReceptionDto(
        "26ad7aef-16c2-4ef8-b196-f652026d513c",
        "11666f67-9680-4903-a91c-9b8ed459962c",
        Date.valueOf("2025-03-12"));
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.TACHE_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToTacheReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_102_test() throws Exception {
    final List<OutArgDto> args = Arrays.asList(
        new OutArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new OutArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    final FinEnvoiDto envDto =
        new FinEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            args,
            Date.valueOf("2025-03-12"));
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);

    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.FIN_ENVOI);
    assertEquals(envDto, pduConverter.convertToFinEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final FinReceptionDto receptionDto =
        new FinReceptionDto("26ad7aef-16c2-4ef8-b196-f652026d513c");
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.FIN_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToFinReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_103_test() throws Exception {
    final ExceptionEnvoiDto envDto =
        new ExceptionEnvoiDto(
            "26ad7aef-16c2-4ef8-b196-f652026d513c",
            "11666f67-9680-4903-a91c-9b8ed459962c",
            "fatal",
            Date.valueOf("2025-03-12"));
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.EXCEPTION_ENVOI);
    assertEquals(envDto, pduConverter.convertToExceptionEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final ExceptionReceptionDto receptionDto =
        new ExceptionReceptionDto("26ad7aef-16c2-4ef8-b196-f652026d513c");
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.EXCEPTION_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToExceptionReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_104_test() throws Exception {
    final DataEnvoiDto envDto =
        new DataEnvoiDto(
            UUID.fromString("26ad7aef-16c2-4ef8-b196-f652026d513c"),
            "data");
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.DATA_ENVOI);
    assertEquals(envDto, pduConverter.convertToDataEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final DataReceptionDto receptionDto =
        new DataReceptionDto(UUID.fromString("26ad7aef-16c2-4ef8-b196-f652026d513c"),
            "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", "SHA256");
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.DATA_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToDataReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_105_test() throws Exception {
    final StatutEnvoiDto envDto = new StatutEnvoiDto("uwi");
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.STATUT_ENVOI);
    assertEquals(envDto, pduConverter.convertToStatutEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final StatutReceptionDto receptionDto =
        new StatutReceptionDto("c6ad7aef-16c2-4ef8-b196-f652026d513c", true);
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.STATUT_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToStatutReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_106_test() throws Exception {
    final CommandeEnvoiDto envDto = new CommandeEnvoiDto(CommandeEnvoiDto.Command.START);
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.COMMANDE_ENVOI);
    assertEquals(envDto, pduConverter.convertToCommandeEnvoiDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final CommandeReceptionDto receptionDto =
        new CommandeReceptionDto("c6ad7aef-16c2-4ef8-b196-f652026d513c");
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.COMMANDE_RECEPTION);
    assertEquals(receptionDto, pduConverter.convertToCommandeReceptionDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }

  @Test
  public void protocol_107_test() throws Exception {
    final PgaFinDto envDto = new PgaFinDto("uwi");
    final InterfaceDataUnit01Dto requestIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
        envDto);
    final SpedsInterfaceDataUnit spedsRequestIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            envDto);
    assertEquals(spedsRequestIdu.idu(), requestIdu);
    assertEquals(spedsRequestIdu.type(), HeaderDto.Msgtype.PGA_FIN);
    assertEquals(envDto, pduConverter.convertToPgaFinDto(
        mapper.readValue(requestIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    clientCases.requestSuccess(requestIdu);
    serverCases.indicationSuccess(requestIdu);

    final PgaConfirmationDto receptionDto =
        new PgaConfirmationDto("c6ad7aef-16c2-4ef8-b196-f652026d513c");
    final InterfaceDataUnit01Dto responseIdu = converter.convert(
        "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
        "e1",
        "e2",
        UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"), receptionDto);
    final SpedsInterfaceDataUnit spedsResponseIdu =
        spedsConverter.convert("736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "e1",
            "e2",
            UUID.fromString("daffd463-7b1a-424b-b26f-f8ffd1f54a77"),
            receptionDto);
    assertEquals(spedsResponseIdu.idu(), responseIdu);
    assertEquals(spedsResponseIdu.type(), HeaderDto.Msgtype.PGA_CONFIRMATION);
    assertEquals(receptionDto, pduConverter.convertToPgaConfirmationDto(
        mapper.readValue(responseIdu.getMessage(), ProtocolDataUnit1APPDto.class)));

    serverCases.responseSuccess(responseIdu);
    clientCases.confirmSuccess(responseIdu);
  }
}
