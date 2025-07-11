package ca.griis.speds.network.unit.serialization;

import static ca.griis.js2p.gen.speds.network.api.dto.HeaderDto.Msgtype.RES_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkMarshallerTest {
  private static final String TRANPORT_IDU =
      "{\"context\":{\"source_iri\":\"https://source.iri\",\"destination_iri\":\"https://destination.iri\",\"tracking_number\":\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\",\"options\":false},\"message\":\"SDU\"}";
  private static final String DATA_LINK_IDU =
      "{\"context\":{\"destination_iri\":\"https://destination.iri\",\"tracking_number\":\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\",\"options\":false},\"message\":\"SDU\"}";
  private static final String PDU =
      "{\"header\":{\"msgtype\":\"RES.ENV\",\"id\":\"id\",\"source_iri\":\"https://source.iri\",\"destination_iri\":\"https://destination.iri\",\"authentification\":\"authentification\",\"parameters\":false,\"SPEDS\":{\"version\":\"version\",\"reference\":\"reference\"}},\"stamp\":{\"header_seal\":\"headerSeal\",\"content_seal\":\"contentSeal\"},\"content\":\"content\"}";

  private final NetworkMarshaller networkMarshaller =
      new NetworkMarshaller(SharedObjectMapper.getInstance()
          .getMapper());

  @Test
  public void marshallToTransportSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final String actual =
        networkMarshaller
            .marshallToTransport(new Context45Dto("https://source.iri", "https://destination.iri",
                UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false), "SDU");
    assertEquals(TRANPORT_IDU, actual);
  }

  @Test
  public void marshallToTransportException() throws Exception {
    final ObjectMapper objectMapper = mock(ObjectMapper.class);
    final NetworkMarshaller networkMarshaller = new NetworkMarshaller(objectMapper);

    doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(any(
        InterfaceDataUnit45Dto.class));
    assertThrows(SerializationException.class, () -> networkMarshaller
        .marshallToTransport(new Context45Dto("https://source.iri", "https://destination.iri",
            UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false), "SDU"));
  }

  @Test
  public void unmarshallFromTransportSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final Context45Dto ici = new Context45Dto("https://source.iri", "https://destination.iri",
        UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false);
    final String sdu = "SDU";
    InterfaceDataUnit45Dto idu45Dto = networkMarshaller.unmarshallFromTransport(TRANPORT_IDU);

    assertEquals(idu45Dto.getContext(), ici);
    assertEquals(idu45Dto.getMessage(), sdu);
  }

  @Test
  public void unmarshallFromTransportException() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    assertThrows(DeserializationException.class,
        () -> networkMarshaller.unmarshallFromTransport("wrong json"));
  }

  @Test
  public void marshallToDataLinkSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final String actual =
        networkMarshaller.marshallToDataLink(new Context56Dto("https://destination.iri",
            UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false), "SDU");
    assertEquals(DATA_LINK_IDU, actual);
  }

  @Test
  public void marshallToDataLinkException() throws Exception {
    final ObjectMapper objectMapper = mock(ObjectMapper.class);
    final NetworkMarshaller networkMarshaller = new NetworkMarshaller(objectMapper);

    doThrow(JsonProcessingException.class).when(objectMapper)
        .writeValueAsString(any(InterfaceDataUnit56Dto.class));
    assertThrows(SerializationException.class,
        () -> networkMarshaller.marshallToDataLink(new Context56Dto("https://destination.iri",
            UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false), "SDU"));
  }

  @Test
  public void unmarshallFromDataLinkSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final Context56Dto ici = new Context56Dto("https://destination.iri",
        UUID.fromString("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"), false);
    final String sdu = "SDU";
    InterfaceDataUnit56Dto idu56Dto = networkMarshaller.unmarshallFromDataLink(DATA_LINK_IDU);
    assertEquals(idu56Dto.getContext(), ici);
    assertEquals(idu56Dto.getMessage(), sdu);
  }

  @Test
  public void unmarshallFromDataLinkException() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());

    assertThrows(DeserializationException.class,
        () -> networkMarshaller.unmarshallFromDataLink("wrong json"));
  }

  @Test
  public void marshallNetworkPduSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final String actual = networkMarshaller.marshallNetworkPdu(
        new ProtocolDataUnit5Dto(
            new HeaderDto(RES_ENV, "id", "https://source.iri", "https://destination.iri",
                "authentification", false, new SPEDSDto("version", "reference")),
            new StampDto("headerSeal", "contentSeal"),
            "content"));
    assertEquals(PDU, actual);
  }

  @Test
  public void marshallNetworkPduException() throws Exception {
    final ObjectMapper objectMapper = mock(ObjectMapper.class);
    final NetworkMarshaller networkMarshaller = new NetworkMarshaller(objectMapper);

    doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(any(
        ProtocolDataUnit5Dto.class));
    assertThrows(SerializationException.class, () -> networkMarshaller.marshallNetworkPdu(
        new ProtocolDataUnit5Dto(
            new HeaderDto(RES_ENV, "id", "https://source.iri", "https://destination.iri",
                "authentification", false, new SPEDSDto("version", "reference")),
            new StampDto("headerSeal", "contentSeal"),
            "content")));
  }

  @Test
  public void unmarshallNetworkPduSuccess() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());
    final ProtocolDataUnit5Dto expected = new ProtocolDataUnit5Dto(
        new HeaderDto(RES_ENV, "id", "https://source.iri", "https://destination.iri",
            "authentification", false, new SPEDSDto("version", "reference")),
        new StampDto("headerSeal", "contentSeal"),
        "content");
    final ProtocolDataUnit5Dto actual = networkMarshaller.unmarshallNetworkPdu(PDU);
    assertEquals(expected, actual);
  }

  @Test
  public void unmarshallNetworkPduException() throws Exception {
    final NetworkMarshaller networkMarshaller =
        new NetworkMarshaller(SharedObjectMapper.getInstance()
            .getMapper());

    assertThrows(DeserializationException.class,
        () -> networkMarshaller.unmarshallNetworkPdu("wrong json"));
  }
}
