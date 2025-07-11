package ca.griis.speds.transport.unit.service.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.transport.exception.ContentSealException;
import ca.griis.speds.transport.exception.HeaderSealException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.client.ExchangeDataConfirmation;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

public class ExchangeDataConfirmationTest {

  @Test
  public void testDataConfirmProcess() throws Exception {
    Set<String> sentMessagesId = ConcurrentHashMap.newKeySet();
    sentMessagesId.add("870107df-f90e-4bda-862b-da4f1f787799");
    final String validConfirmIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number": "870107df-f90e-4bda-862b-da4f1f787799",
                "options": []
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.REC\\", \\"id\\": \\"870107df-f90e-4bda-862b-da4f1f787799\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"4.14.3\\", \\"reference\\": \\"ABCDEFGHIJKLMNOP\\"}}, \\"stamp\\": {\\"header_seal\\": \\"gGT4wg7uJZQ/fuRXSs8+Th6PI8VE5SjIErTLZKMU70n1duLVftWuA3/6tTpz+xBlNjUWDrd4EwcuWItuUKU7kA\\", \\"content_seal\\": \\"f3k1zpFft5sTnP1Da3bpgJP+hgBDA0WR+GHrwHnX84Pw+Db0cY8Da1W5TPsLs2UAtxz+azknzxlC0HC3O2gNmA\\"}, \\"content\\": \\"ACK\\"}"
            }
            """;
    final InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
        .readValue(validConfirmIdu4_5, InterfaceDataUnit45Dto.class);

    ExchangeDataConfirmation.dataConfirmationProcess(iduDto, sentMessagesId);
  }

  @Test
  public void testDataConfirm_headerSealException() {
    Set<String> sentMessagesId = ConcurrentHashMap.newKeySet();
    sentMessagesId.add("870107df-f90e-4bda-862b-da4f1f787799");
    String invalidConfirmHeaderSealIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number": "870107df-f90e-4bda-862b-da4f1f787799",
                "options": []
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.REC\\", \\"id\\": \\"870107df-f90e-4bda-862b-da4f1f787799\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"4.14.3\\", \\"reference\\": \\"ABCDEFGHIJKLMNOP\\"}}, \\"stamp\\": {\\"header_seal\\": \\"WRONGqqD+uPCd5+D1uonDbDMfKBenU2SKBvWS/6wyjZeMaQc4SWOMatNXx+uEmdcDXSB9l+E5dB6/H+bj6T2mMUiQ\\", \\"content_seal\\": \\"f3k1zpFft5sTnP1Da3bpgJP+hgBDA0WR+GHrwHnX84Pw+Db0cY8Da1W5TPsLs2UAtxz+azknzxlC0HC3O2gNmA\\"}, \\"content\\": \\"ACK\\"}"
            }
            """;

    assertThrows(HeaderSealException.class, () -> {
      InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
          .readValue(invalidConfirmHeaderSealIdu4_5, InterfaceDataUnit45Dto.class);
      ExchangeDataConfirmation.dataConfirmationProcess(iduDto, sentMessagesId);
    });
  }

  @Test
  public void testDataConfirm_contentSealException() {
    Set<String> sentMessagesId = ConcurrentHashMap.newKeySet();
    sentMessagesId.add("870107df-f90e-4bda-862b-da4f1f787799");
    String invalidConfirmContentSealIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number": "870107df-f90e-4bda-862b-da4f1f787799",
                "options": []
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.REC\\", \\"id\\": \\"870107df-f90e-4bda-862b-da4f1f787799\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"4.14.3\\", \\"reference\\": \\"ABCDEFGHIJKLMNOP\\"}}, \\"stamp\\": {\\"header_seal\\": \\"gGT4wg7uJZQ/fuRXSs8+Th6PI8VE5SjIErTLZKMU70n1duLVftWuA3/6tTpz+xBlNjUWDrd4EwcuWItuUKU7kA\\", \\"content_seal\\": \\"WRONGDnZ+3SAb7hxbETQ5UFtHYgI+rBU+U5Xf10eL0GpcB49/YfCybHOTzx5AmPwrjh1XTIIIHXBOHSywofOI1o931g\\"}, \\"content\\": \\"Wrong content\\"}"
            }
            """;

    assertThrows(ContentSealException.class, () -> {
      InterfaceDataUnit45Dto iduDto = SharedObjectMapper.getInstance().getMapper()
          .readValue(invalidConfirmContentSealIdu4_5, InterfaceDataUnit45Dto.class);
      ExchangeDataConfirmation.dataConfirmationProcess(iduDto, sentMessagesId);
    });
  }
}
