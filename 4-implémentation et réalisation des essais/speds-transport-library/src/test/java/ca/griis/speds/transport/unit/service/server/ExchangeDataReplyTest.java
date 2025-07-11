package ca.griis.speds.transport.unit.service.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.transport.exception.ContentSealException;
import ca.griis.speds.transport.exception.HeaderSealException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.server.ExchangeDataReply;
import ca.griis.speds.transport.service.server.datatype.DataReplyMessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class ExchangeDataReplyTest {

  @Test
  public void testDataIndication() throws JsonProcessingException {
    final String spedsVersion = "2.0.0";
    final String spedsReference = "a reference";
    final String validIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number": "d00834a8-a126-4524-a49c-6afcf4aaa1fd",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"Q+o+bgH88887j+fk0XS/rg5SqIvah//BwueDoA9p78yk2caHSTnMpktRC46k/wqeLd1VLGEaePOYRqPGeHHyAg\\", \\"content_seal\\": \\"oZrhyzM5IU81vpModKOnaUp7LZgBE9Jiz0jOAne1pp31VRO51z9VALwgWYMOQfprBTbEBL6jbfFZwGu1da2sqA\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": {}, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    final String validResponseIdu4_5 =
        """
            {"context":{"source_iri":"https://www.example.com/connector","destination_iri":"https://www.example.com/executor","tracking_number":"d00834a8-a126-4524-a49c-6afcf4aaa1fd","options":false},"message":"{\\"header\\":{\\"msgtype\\":\\"TRA.MSG.REC\\",\\"id\\":\\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\",\\"source_code\\":\\"executor\\",\\"destination_code\\":\\"connector\\",\\"SPEDS\\":{\\"version\\":\\"2.0.0\\",\\"reference\\":\\"a reference\\"}},\\"stamp\\":{\\"header_seal\\":\\"z/OnkyrLncbPReAxWhvalSKNSdr4r98unTEbMV1ILf+BL2WBGTwZkS08atN6psPeWHgNS3NEY+/1Tt/J1tCv8g\\",\\"content_seal\\":\\"f3k1zpFft5sTnP1Da3bpgJP+hgBDA0WR+GHrwHnX84Pw+Db0cY8Da1W5TPsLs2UAtxz+azknzxlC0HC3O2gNmA\\"},\\"content\\":\\"ACK\\"}"}""";

    final InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
        .readValue(validIndicationIdu4_5, InterfaceDataUnit45Dto.class);

    final DataReplyMessages dataReplyMessages = ExchangeDataReply.dataReplyProcess(iduDto,
        spedsVersion, spedsReference);

    final String actualIdu34 = dataReplyMessages.response34();
    final String actualIdu45 = dataReplyMessages.response45();

    assertNotNull(actualIdu34);
    assertEquals(validResponseIdu4_5, actualIdu45);
  }

  @Test
  public void testDataIndication_headerSealException() {
    final String spedsVersion = "2.0.0";
    final String spedsReference = "a reference";
    final String invalidHeaderIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"InvalidHeader\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": {}, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    assertThrows(HeaderSealException.class, () -> {
      InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
          .readValue(invalidHeaderIndicationIdu4_5, InterfaceDataUnit45Dto.class);
      ExchangeDataReply.dataReplyProcess(iduDto, spedsVersion, spedsReference);
    });
  }

  @Test
  public void testDataIndication_contentSealException() {
    final String spedsVersion = "2.0.0";
    final String spedsReference = "a reference";
    final String invalidContentIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"Q+o+bgH88887j+fk0XS/rg5SqIvah//BwueDoA9p78yk2caHSTnMpktRC46k/wqeLd1VLGEaePOYRqPGeHHyAg\\", \\"content_seal\\": \\"Invalid content seal\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": {}, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
              """;

    assertThrows(ContentSealException.class, () -> {
      InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
          .readValue(invalidContentIndicationIdu4_5, InterfaceDataUnit45Dto.class);
      ExchangeDataReply.dataReplyProcess(iduDto, spedsVersion, spedsReference);
    });
  }
}
