package ca.griis.speds.transport.integration;

import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;

public class TestInputs {
  ///
  /// Attention
  ///
  /// La définition des messages est volontairement explicite et répétitive.
  ///

  public static InterfaceDataUnit34Dto make_ct_pro_04_01_e1() {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    UUID trackingNumber = UUID.randomUUID();

    Boolean options = false;

    Context34Dto context = new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
        destinationIri, options);

    int messageLength = ThreadLocalRandom.current().nextInt(500);
    String message = "{" + RandomStringUtils.randomAlphanumeric(messageLength) + "}";

    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit34Dto(context, message);

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_01_e1(Speds45Dto speds45Dto,
      String messageId, ObjectMapper obm) throws JsonProcessingException {
    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);
    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        trackingNumber,
        options);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        messageId,
        context45Dto.getSourceIri(),
        context45Dto.getSourceIri(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto ProtocolDataUnit4TraDto =
        new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(ProtocolDataUnit4TraDto);

    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(context45Dto, message);

    return idu45;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_02_e1(Speds45Dto speds45Dto,
      ObjectMapper obm) throws JsonProcessingException {
    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);
    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        trackingNumber,
        options);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID().toString(),
        context45Dto.getSourceIri(),
        context45Dto.getDestinationIri(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = RandomStringUtils.randomAlphanumeric(100);
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto ProtocolDataUnit4TraDto =
        new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(ProtocolDataUnit4TraDto);

    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(context45Dto, message);

    return idu45;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_02_e2(
      Speds45Dto speds45Dto, String messageId, ObjectMapper obm) throws JsonProcessingException {
    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);
    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        trackingNumber,
        options);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        messageId,
        context45Dto.getSourceIri(),
        context45Dto.getDestinationIri(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto ProtocolDataUnit4TraDto =
        new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(ProtocolDataUnit4TraDto);

    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(context45Dto, message);

    return idu45;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_03_e1(Speds45Dto speds45Dto,
      String messageId, ObjectMapper obm) throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);

    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        messageId,
        sourceCode,
        destinationCode,
        speds45Dto);

    // invalid seal
    int sealLength = ThreadLocalRandom.current().nextInt(100);
    String headerSeal = RandomStringUtils.randomAlphanumeric(sealLength);

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_04_e1(Speds45Dto speds45Dto,
      String messageId, ObjectMapper obm) throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);

    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        messageId,
        sourceCode,
        destinationCode,
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    // invalid seal
    String content = "ACK";
    int sealLength = ThreadLocalRandom.current().nextInt(100);
    String contentSeal = RandomStringUtils.randomAlphanumeric(sealLength);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_05_05_e1(Speds45Dto speds45Dto,
      ObjectMapper obm) throws JsonProcessingException {
    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);
    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        trackingNumber,
        options);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        UUID.randomUUID().toString(),
        context45Dto.getSourceIri(),
        context45Dto.getSourceIri(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto ProtocolDataUnit4TraDto =
        new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(ProtocolDataUnit4TraDto);

    InterfaceDataUnit45Dto idu45 = new InterfaceDataUnit45Dto(context45Dto, message);

    return idu45;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_11_01_e1(Speds45Dto speds45Dto,
      ObjectMapper obm) throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID().toString(),
        sourceCode,
        destinationCode,
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = RandomStringUtils.randomAlphanumeric(100);
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit45Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit45Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_11_02_e1(Speds45Dto speds45Dto,
      String messageId, ObjectMapper obm) throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        messageId,
        sourceCode,
        destinationCode,
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit45Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit45Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_11_02_e2(InterfaceDataUnit45Dto ct_pro_11_02_e1,
      Speds45Dto speds45Dto,
      ObjectMapper obm) throws JsonProcessingException {
    String sourceIri = ct_pro_11_02_e1.getContext().getSourceIri();
    String destinationIri = ct_pro_11_02_e1.getContext().getDestinationIri();

    final ProtocolDataUnit4TraDto pdu;
    pdu = SharedObjectMapper.getInstance().getMapper().readValue(ct_pro_11_02_e1.getMessage(),
        ProtocolDataUnit4TraDto.class);
    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        pdu.getHeader().getId(),
        pdu.getHeader().getSourceCode(),
        pdu.getHeader().getDestinationCode(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = RandomStringUtils.randomAlphanumeric(100);
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit45Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit45Dto;
  }

  public static String make_ct_pro_11_03_e1(Speds45Dto speds45Dto, ObjectMapper obm)
      throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID().toString(),
        sourceCode,
        destinationCode,
        speds45Dto);
    int headerSealLength = ThreadLocalRandom.current().nextInt(100);
    String headerSeal = RandomStringUtils.randomAlphanumeric(headerSealLength);

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);
    String c007_e1 = obm.writeValueAsString(InterfaceDataUnit34Dto);
    return c007_e1;
  }

  public static String make_ct_pro_11_04_e1(Speds45Dto speds45Dto, ObjectMapper obm)
      throws JsonProcessingException {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    Context45Dto context45Dto = new Context45Dto(
        sourceIri,
        destinationIri,
        UUID.randomUUID(),
        false);
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID().toString(),
        sourceCode,
        destinationCode,
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);
    int contentSealLength = ThreadLocalRandom.current().nextInt(100);
    String contentSeal = RandomStringUtils.randomAlphanumeric(contentSealLength);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu34 = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(pdu34);
    InterfaceDataUnit45Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);
    String c007_e1 = obm.writeValueAsString(InterfaceDataUnit34Dto);
    return c007_e1;
  }

  private static String hash(String content) {
    Sha512Hashing sha512Hashing = new Sha512Hashing();
    Hash hashSdu = sha512Hashing.hash(content.getBytes(StandardCharsets.UTF_8));
    return hashSdu.asBase64();
  }

  public static InterfaceDataUnit34Dto make_ct_gen_01_e1() {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    UUID trackingNumber = UUID.randomUUID();

    Boolean options = false;

    Context34Dto context = new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
        destinationIri, options);

    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit34Dto(context, "Alpha request Beta");

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit34Dto make_ct_gen_01_e2() {
    int sourceCodeLength = ThreadLocalRandom.current().nextInt(100);
    String sourceCode = RandomStringUtils.randomAlphanumeric(sourceCodeLength);

    int destinationCodeLength = ThreadLocalRandom.current().nextInt(100);
    String destinationCode = RandomStringUtils.randomAlphanumeric(destinationCodeLength);

    int sourceIriLength = ThreadLocalRandom.current().nextInt(100);
    String sourceIri = RandomStringUtils.randomAlphanumeric(sourceIriLength);

    int destinationIriLength = ThreadLocalRandom.current().nextInt(100);
    String destinationIri = RandomStringUtils.randomAlphanumeric(destinationIriLength);

    UUID trackingNumber = UUID.randomUUID();

    Boolean options = false;

    Context34Dto context = new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
        destinationIri, options);

    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit34Dto(context, "Beta request alpha");

    return InterfaceDataUnit34Dto;
  }
}
