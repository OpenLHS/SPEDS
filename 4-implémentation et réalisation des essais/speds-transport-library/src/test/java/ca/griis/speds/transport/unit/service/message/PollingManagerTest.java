package ca.griis.speds.transport.unit.service.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.service.message.PollingManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


public class PollingManagerTest {
  @Mock
  private NetworkHost mockNetworkHost;

  private PollingManager pollingManager;

  @BeforeEach
  public void setUp() {
    mockNetworkHost = Mockito.mock(NetworkHost.class);
    pollingManager = new PollingManager(mockNetworkHost);
  }

  @AfterEach
  public void cleanUp() {
    pollingManager.close();
  }

  @Test
  public void testPoolIdu_addIdu() throws Exception {

    String firstValidIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"zF408Mz7qwCkWIjNYblUw+Pyx0iDybUg5UByB6WG3hb1Eb4AfxVE9rE0E00JvbhdrOARvRs4JNqF0UFMC+aFFg\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": false, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    String secondValidIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"4e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"zF408Mz7qwCkWIjNYblUw+Pyx0iDybUg5UByB6WG3hb1Eb4AfxVE9rE0E00JvbhdrOARvRs4JNqF0UFMC+aFFg\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": false, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    String thirdValidIndicationIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.REC\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"zF408Mz7qwCkWIjNYblUw+Pyx0iDybUg5UByB6WG3hb1Eb4AfxVE9rE0E00JvbhdrOARvRs4JNqF0UFMC+aFFg\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": {}, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;


    when(mockNetworkHost.indication())
        .thenReturn(firstValidIndicationIdu4_5)
        .thenReturn(secondValidIndicationIdu4_5)
        .thenReturn(thirdValidIndicationIdu4_5);

    InterfaceDataUnit45Dto idu45 = pollingManager.pollResponse();

    assertNotNull(idu45);
    assertEquals("https://www.example.com/executor", idu45.getContext().getSourceIri());
    assertEquals("https://www.example.com/connector", idu45.getContext().getDestinationIri());
    InterfaceDataUnit45Dto idu452 = pollingManager.pollRequest();

    assertNotNull(idu452);
    InterfaceDataUnit45Dto idu453 = pollingManager.pollRequest();

    assertNotNull(idu453);
  }
}
