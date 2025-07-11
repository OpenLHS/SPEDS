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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TestOutputs {
  ///
  /// Attention
  ///
  /// La définition des messages est volontairement explicite et répétitive.
  ///

  public static InterfaceDataUnit45Dto make_ct_pro_04_01_s1(InterfaceDataUnit34Dto c001_e1,
      Speds45Dto speds45Dto, ObjectMapper obm) throws JsonProcessingException {
    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        UUID.randomUUID().toString(),
        c001_e1.getContext().getSourceCode(),
        c001_e1.getContext().getDestinationCode(),
        speds45Dto);
    String headerSeal = hash(obm.writeValueAsString(header45Dto));

    String content = c001_e1.getMessage();
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto ProtocolDataUnit4TraDto =
        new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = obm.writeValueAsString(ProtocolDataUnit4TraDto);

    Context45Dto context45Dto = new Context45Dto(
        c001_e1.getContext().getSourceIri(),
        c001_e1.getContext().getDestinationIri(),
        UUID.randomUUID(),
        false);

    InterfaceDataUnit45Dto InterfaceDataUnit45Dto =
        new InterfaceDataUnit45Dto(context45Dto, message);

    return InterfaceDataUnit45Dto;
  }

  public static InterfaceDataUnit34Dto make_ct_pro_11_01_s1(
      InterfaceDataUnit45Dto ct_pro_11_01_e1, ObjectMapper mapper) throws JsonProcessingException {
    String sourceCode = ct_pro_11_01_e1.getContext().getSourceIri();
    String destinationCode = ct_pro_11_01_e1.getContext().getDestinationIri();
    String sourceIri = ct_pro_11_01_e1.getContext().getSourceIri();
    String destinationIri = ct_pro_11_01_e1.getContext().getDestinationIri();
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context34Dto context = new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
        destinationIri, options);
    ProtocolDataUnit4TraDto traDto =
        mapper.readValue(ct_pro_11_01_e1.getMessage(), ProtocolDataUnit4TraDto.class);
    String message = traDto.getContent();

    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit34Dto(context, message);

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_11_01_s2(
      InterfaceDataUnit45Dto ct_pro_11_01_e1,
      Speds45Dto speds45Dto, ObjectMapper mapper) throws JsonProcessingException {
    String sourceIri = ct_pro_11_01_e1.getContext().getSourceIri();
    String destinationIri = ct_pro_11_01_e1.getContext().getDestinationIri();
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context = new Context45Dto(destinationIri, sourceIri, trackingNumber, options);

    ProtocolDataUnit4TraDto traDto =
        mapper.readValue(ct_pro_11_01_e1.getMessage(), ProtocolDataUnit4TraDto.class);

    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        UUID.randomUUID().toString(),
        traDto.getHeader().getSourceCode(),
        traDto.getHeader().getDestinationCode(),
        speds45Dto);
    String headerSeal = hash(mapper.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = mapper.writeValueAsString(pdu);

    InterfaceDataUnit45Dto ct_pro_11_01_s2 = new InterfaceDataUnit45Dto(context, message);
    return ct_pro_11_01_s2;
  }

  public static InterfaceDataUnit34Dto make_ct_pro_11_02_s1(
      InterfaceDataUnit45Dto ct_pro_11_02_e2,
      ObjectMapper mapper) throws JsonProcessingException {
    String sourceCode = ct_pro_11_02_e2.getContext().getSourceIri();
    String destinationCode = ct_pro_11_02_e2.getContext().getDestinationIri();
    String sourceIri = ct_pro_11_02_e2.getContext().getSourceIri();
    String destinationIri = ct_pro_11_02_e2.getContext().getDestinationIri();
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context34Dto context = new Context34Dto(sourceCode, destinationCode, sourceIri, trackingNumber,
        destinationIri, options);
    ProtocolDataUnit4TraDto traDto =
        mapper.readValue(ct_pro_11_02_e2.getMessage(), ProtocolDataUnit4TraDto.class);

    String message = mapper.writeValueAsString(traDto);
    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        new InterfaceDataUnit34Dto(context, message);

    return InterfaceDataUnit34Dto;
  }

  public static InterfaceDataUnit45Dto make_ct_pro_11_02_s2(
      InterfaceDataUnit45Dto ct_pro_11_02_e1,
      Speds45Dto speds45Dto, ObjectMapper mapper) throws JsonProcessingException {
    String sourceIri = ct_pro_11_02_e1.getContext().getSourceIri();
    String destinationIri = ct_pro_11_02_e1.getContext().getDestinationIri();
    UUID trackingNumber = UUID.randomUUID();
    Boolean options = false;
    Context45Dto context = new Context45Dto(destinationIri, sourceIri, trackingNumber, options);

    ProtocolDataUnit4TraDto traDto =
        mapper.readValue(ct_pro_11_02_e1.getMessage(), ProtocolDataUnit4TraDto.class);

    Header45Dto header45Dto = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_REC,
        UUID.randomUUID().toString(),
        traDto.getHeader().getSourceCode(),
        traDto.getHeader().getDestinationCode(),
        speds45Dto);
    String headerSeal = hash(mapper.writeValueAsString(header45Dto));

    String content = "ACK";
    String contentSeal = hash(content);
    StampDto stamp = new StampDto(headerSeal, contentSeal);

    ProtocolDataUnit4TraDto pdu = new ProtocolDataUnit4TraDto(header45Dto, stamp, content);
    String message = mapper.writeValueAsString(pdu);

    InterfaceDataUnit45Dto ct_pro_11_02_s2 = new InterfaceDataUnit45Dto(context, message);
    return ct_pro_11_02_s2;
  }

  private static String hash(String content) {
    Sha512Hashing sha512Hashing = new Sha512Hashing();
    Hash hashSdu = sha512Hashing.hash(content.getBytes(StandardCharsets.UTF_8));
    return hashSdu.asBase64();
  }
}
