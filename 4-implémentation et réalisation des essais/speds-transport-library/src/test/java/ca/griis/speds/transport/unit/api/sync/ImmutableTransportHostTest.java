package ca.griis.speds.transport.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableTransportHostTest {
  private static final String TEST_UUID = "e841c74d-16d3-4612-abd9-1fc38e1010ee";

  private TransportHost transportHost;

  @Mock
  private NetworkHost networkHost;

  @Captor
  private ArgumentCaptor<String> messageCaptor;

  @BeforeEach
  public void setUp() {
    final String params = """
        {
          "options": {
            "speds.tra.version": "2.0.0",
            "speds.tra.reference": "a reference"
          }
        }
        """;

    final TransportFactory transportFactory =
        new SyncTransportFactory(() -> TEST_UUID) {
          @Override
          public NetworkHost initNetworkHost(String parameters) {
            return networkHost;
          }
        };

    this.transportHost = transportFactory.init(params);
  }

  @AfterEach
  public void cleanUp() {
    transportHost.close();
  }

  @Test
  public void test_dataRequest() {
    final String validIdu3_4 =
        """
              {
                    "context": {
                      "source_code": "executor",
                      "destination_code": "connector",
                      "source_iri": "https://www.example.com/executor",
                      "tracking_number":"870107df-f90e-4bda-862b-da4f1f787799",
                      "destination_iri": "https://www.example.com/connector",
                      "options": []
                    },
                    "message": "{\\"header\\": {\\"msgtype\\": \\"SES.MSG.ENV\\", \\"id\\": \\"870107df-f90e-4bda-862b-da4f1f787799\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"pfKauKELG1wu6LzpQ/ctBcOJCOkxPyIKNVueYAqfV8UmEKx2Ns9a6IQ09yMU0884WKP8b3r13YGAvCRuzCxCQA\\", \\"content_seal\\": \\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\"}, \\"content\\": \\"Contenu du message Session\\"}"
                  }
            """;

    final String expectedIdu4_5 =
        "{\"context\":{\"source_iri\":\"https://www.example.com/executor\",\"destination_iri\":\"https://www.example.com/connector\",\"tracking_number\":\"e841c74d-16d3-4612-abd9-1fc38e1010ee\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"TRA.MSG.ENV\\\",\\\"id\\\":\\\"e841c74d-16d3-4612-abd9-1fc38e1010ee\\\",\\\"source_code\\\":\\\"executor\\\",\\\"destination_code\\\":\\\"connector\\\",\\\"SPEDS\\\":{\\\"version\\\":\\\"2.0.0\\\",\\\"reference\\\":\\\"a reference\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"YrtcCYaEiKlAgzLxpsiFcwOJ2DjOobkmlyRC3mE33s73gJLX9YUNWKBXLl5GldJq++UBLOHaQIHrGtvpAUyRMQ\\\",\\\"content_seal\\\":\\\"Wy1nmeeZSJ1H3C1ZOwId4/EwMD9MguARH9eBZuGnUXz5ndX8pa9f0W4cF34CvB+FitN8MZk38yAfaKNOhEY4JA\\\"},\\\"content\\\":\\\"{\\\\\\\"header\\\\\\\": {\\\\\\\"msgtype\\\\\\\": \\\\\\\"SES.MSG.ENV\\\\\\\", \\\\\\\"id\\\\\\\": \\\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\\", \\\\\\\"source_code\\\\\\\": \\\\\\\"executor\\\\\\\", \\\\\\\"destination_code\\\\\\\": \\\\\\\"connector\\\\\\\", \\\\\\\"SPEDS\\\\\\\": {\\\\\\\"version\\\\\\\": \\\\\\\"2.0.0\\\\\\\", \\\\\\\"reference\\\\\\\": \\\\\\\"a reference\\\\\\\"}}, \\\\\\\"stamp\\\\\\\": {\\\\\\\"header_seal\\\\\\\": \\\\\\\"pfKauKELG1wu6LzpQ/ctBcOJCOkxPyIKNVueYAqfV8UmEKx2Ns9a6IQ09yMU0884WKP8b3r13YGAvCRuzCxCQA\\\\\\\", \\\\\\\"content_seal\\\\\\\": \\\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\\"}, \\\\\\\"content\\\\\\\": \\\\\\\"Contenu du message Session\\\\\\\"}\\\"}\"}";

    transportHost.dataRequest(validIdu3_4);

    verify(networkHost).request(messageCaptor.capture());
    verify(networkHost).confirm();
    assertEquals(expectedIdu4_5, messageCaptor.getValue());
  }

  @Test
  public void test_closeNetworkhost() {
    transportHost.close();

    verify(networkHost).close();
  }

  @Test
  public void test_dataConfirm() {
    final String validIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number":"dc72894d-a8cc-4e2d-a7c7-a54a585627d8",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.REC\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"z/OnkyrLncbPReAxWhvalSKNSdr4r98unTEbMV1ILf+BL2WBGTwZkS08atN6psPeWHgNS3NEY+/1Tt/J1tCv8g\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": false, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    when(networkHost.indication()).thenReturn(validIdu4_5);
    transportHost.dataConfirm();
  }

  @Test
  public void test_dataReply() {
    final String validIdu4_5 =
        """
            {
              "context": {
                "source_iri": "https://www.example.com/executor",
                "destination_iri": "https://www.example.com/connector",
                "tracking_number":"dc72894d-a8cc-4e2d-a7c7-a54a585627d8",
                "options": false
              },
              "message": "{\\"header\\": {\\"msgtype\\": \\"TRA.MSG.ENV\\", \\"id\\": \\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\", \\"source_code\\": \\"executor\\", \\"destination_code\\": \\"connector\\", \\"SPEDS\\": {\\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\"}}, \\"stamp\\": {\\"header_seal\\": \\"Q+o+bgH88887j+fk0XS/rg5SqIvah//BwueDoA9p78yk2caHSTnMpktRC46k/wqeLd1VLGEaePOYRqPGeHHyAg\\", \\"content_seal\\": \\"CZAd4Gbe8cwiHoy4LAG8nN1IJzfyvcT039Jd5fWxF26k+MJSxOithzrhRKzuF1ldq0IMFpAe7kq7sIgoG4v5Zw\\"}, \\"content\\": \\"{\\\\\\"header\\\\\\": {\\\\\\"msgtype\\\\\\": \\\\\\"SES.MSG.ENV\\\\\\", \\\\\\"id\\\\\\": \\\\\\"870107df-f90e-4bda-862b-da4f1f787799\\\\\\", \\\\\\"source_iri\\\\\\": \\\\\\"executor\\\\\\", \\\\\\"destination_iri\\\\\\": \\\\\\"connector\\\\\\", \\\\\\"parameters\\\\\\": false, \\\\\\"SPEDS\\\\\\": {\\\\\\"version\\\\\\": \\\\\\"2.0.0\\\\\\", \\\\\\"reference\\\\\\": \\\\\\"a reference\\\\\\"}}, \\\\\\"stamp\\\\\\": {\\\\\\"header_seal\\\\\\": \\\\\\"9260a7da13c082a5f65bc83bf3b1a6510bb6c4b5c066adf8371e8faff035dda9ed3f925480ad50fc9014605f379c853916276d6e989c2c21811c596bdff85fed\\\\\\", \\\\\\"content_seal\\\\\\": \\\\\\"c034ef4eef3bda6457840ec60b4e875a8cff30c5fe2f121c85613ddd0069f5ad58a78f86c3ba5a5495e2a893f1d12309057db2e728acb735c2aef6fafc7f17cf\\\\\\"}, \\\\\\"content\\\\\\": \\\\\\"Contenu du message Session\\\\\\"}\\"}"
            }
            """;

    final String validResponseIdu4_5 =
        """
            {"context":{"source_iri":"https://www.example.com/connector","destination_iri":"https://www.example.com/executor","tracking_number":"dc72894d-a8cc-4e2d-a7c7-a54a585627d8","options":false},"message":"{\\"header\\":{\\"msgtype\\":\\"TRA.MSG.REC\\",\\"id\\":\\"3e538ba4-91ae-4e8c-8a94-d5b25f9f7b5c\\",\\"source_code\\":\\"executor\\",\\"destination_code\\":\\"connector\\",\\"SPEDS\\":{\\"version\\":\\"2.0.0\\",\\"reference\\":\\"a reference\\"}},\\"stamp\\":{\\"header_seal\\":\\"z/OnkyrLncbPReAxWhvalSKNSdr4r98unTEbMV1ILf+BL2WBGTwZkS08atN6psPeWHgNS3NEY+/1Tt/J1tCv8g\\",\\"content_seal\\":\\"f3k1zpFft5sTnP1Da3bpgJP+hgBDA0WR+GHrwHnX84Pw+Db0cY8Da1W5TPsLs2UAtxz+azknzxlC0HC3O2gNmA\\"},\\"content\\":\\"ACK\\"}"}""";


    when(networkHost.indication()).thenReturn(validIdu4_5);

    final String actualIdu34 = transportHost.dataReply();
    verify(networkHost).confirm();

    assertNotNull(actualIdu34);
    verify(networkHost).request(messageCaptor.capture());
    assertEquals(validResponseIdu4_5, messageCaptor.getValue());
  }
}
