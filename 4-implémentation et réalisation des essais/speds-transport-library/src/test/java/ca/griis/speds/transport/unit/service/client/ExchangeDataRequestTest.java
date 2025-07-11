package ca.griis.speds.transport.unit.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.client.ExchangeDataRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

public class ExchangeDataRequestTest {

  @Test
  public void testDataRequestProcess() throws Exception {
    Set<String> sentMessagesId = ConcurrentHashMap.newKeySet();
    final String validIdu3_4 =
        """
              {
                    "context": {
                      "source_code": "executor",
                      "destination_code": "connector",
                      "source_iri": "https://www.example.com/executor",
                      "destination_iri": "https://www.example.com/connector",
                      "options": []
                    },
                    "message": "{\\"header\\": {\\"msgtype\\": \\"SES.MSG.ENV\\", \\"id\\": \\"870107df-f90e-4bda-862b-da4f1f787799\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"pfKauKELG1wu6LzpQ/ctBcOJCOkxPyIKNVueYAqfV8UmEKx2Ns9a6IQ09yMU0884WKP8b3r13YGAvCRuzCxCQA\\", \\"content_seal\\": \\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\"}, \\"content\\": \\"Contenu du message Session\\"}"
                  }
            """;

    final String spedsVersion = "2.0.0";
    final String spedsReference = "a reference";

    final String expectedIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"parameters\\": false, \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"2c33b14dfe12c8a02eaad7b9cf3434949bf3942c424e6bea67644c7e8c6c3bc001ed7650d3f76067be62341757b2625ac36edb66130dcda1b14d7682ba9f49c8\\", \\"content_seal\\": \\"Wy1nmeeZSJ1H3C1ZOwId4/EwMD9MguARH9eBZuGnUXz5ndX8pa9f0W4cF34CvB+FitN8MZk38yAfaKNOhEY4JA\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": false, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    final String actualIdu4_5 =
        new ExchangeDataRequest(() -> UUID.randomUUID().toString()).dataRequestProcess(validIdu3_4,
            spedsVersion, spedsReference, sentMessagesId);
    assertNotNull(actualIdu4_5);

    // Comparer champs par champs car le header_seal change vu que le message id est géréré
    // aléatoirement
    final JsonNode actualNode = SharedObjectMapper.getInstance().getMapper().readTree(actualIdu4_5);
    final JsonNode expectedNode =
        SharedObjectMapper.getInstance().getMapper().readTree(expectedIdu4_5);
    final String actualMessageJson = actualNode.get("message").asText();
    final String expectedMessageJson = expectedNode.get("message").asText();
    final JsonNode actualMessageNode =
        SharedObjectMapper.getInstance().getMapper().readTree(actualMessageJson);
    final JsonNode expectedMessageNode =
        SharedObjectMapper.getInstance().getMapper().readTree(expectedMessageJson);
    final String actualContentJson = actualMessageNode.get("content").asText();
    final String expectedContentJson = expectedMessageNode.get("content").asText();
    final JsonNode actualContentNode =
        SharedObjectMapper.getInstance().getMapper().readTree(actualContentJson);
    final JsonNode expectedContentNode =
        SharedObjectMapper.getInstance().getMapper().readTree(expectedContentJson);

    assertEquals(expectedNode.path("context").path("source_iri").asText(),
        actualNode.path("context").path("source_iri").asText());
    assertEquals(expectedNode.path("context").path("destination_iri").asText(),
        actualNode.path("context").path("destination_iri").asText());

    assertEquals(expectedMessageNode.path("header").path("msgtype").asText(),
        actualMessageNode.path("header").path("msgtype").asText());
    assertEquals(expectedMessageNode.path("header").path("source_code").asText(),
        actualMessageNode.path("header").path("source_code").asText());
    assertEquals(expectedMessageNode.path("stamp").path("content_seal").asText(),
        actualMessageNode.path("stamp").path("content_seal").asText());
    assertEquals(expectedContentNode.path("content").asText(),
        actualContentNode.path("content").asText());
  }
}
