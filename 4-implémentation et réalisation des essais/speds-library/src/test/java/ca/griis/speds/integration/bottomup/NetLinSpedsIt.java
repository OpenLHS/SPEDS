package ca.griis.speds.integration.bottomup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.integration.util.NetworkDomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;

public class NetLinSpedsIt {

  private SyncNetworkFactory networkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeProperty
  public void setup() {
    networkFactory = new SyncNetworkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Property(tries = 1)
  @Domain(NetworkDomainProviderUtil.class)
  public void netLinkExchangeTest(@ForAll InterfaceDataUnit45Dto idu45Dto) throws IOException {
    try (ServerSocket originSocket = new ServerSocket(4000);
        ServerSocket targetSocket = new ServerSocket(4001)) {
      // todo
      testUtil.freePorts(originSocket, targetSocket, null);

      // Certificat RSA au lieu de Curve.
      final String originParams =
          """
              {
                "options": {
                  "test":"origin",
                  "speds.app.version":"2.0.21",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIID+jCCAuKgAwIBAgIUQZAxZRzQB1/uhJT1qD9pMzQCzDswDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEOMAwGA1UECgwFZ3JpaXMxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yNTA3MDcxNDU2MjNaFw0zNTA3MDUxNDU2MjNaMEsxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCU1oxDjAMBgNVBAoMBWdyaWlzMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAGjgdUwgdIwHQYDVR0OBBYEFIm5+1m5QbD6ISvwC50D0cuaNX3GMB8GA1UdIwQYMBaAFIm5+1m5QbD6ISvwC50D0cuaNX3GMA8GA1UdEwEB/wQFMAMBAf8wMQYIKwYBBQUHAQEEJTAjMCEGCCsGAQUFBzABhhVodHRwOi8vMTI3LjAuMC4xOjk5OTkwJwYDVR0lBCAwHgYIKwYBBQUHAwkGCCsGAQUFBwMCBggrBgEFBQcDATAjBgNVHREEHDAagglsb2NhbGhvc3SCDXd3dy5sb2NhbGhvc3QwDQYJKoZIhvcNAQELBQADggEBAGpclYsipmgvR3UtRTOya92oD7UONPyFRgbL8w6eWDI2WM8OBI/d4vhnIiNhAz84cDrWB0BDfrN+xTFP88ryLMWTLp+2MMoCQWATGza7URhMUXQX2ytobhXpZCNvoPlp4xC74euKR8PDKRyTgjxDMWK9UTImWJ0+PKSnESUu2ZXIVoP4nIGtut7l+H/QAkLsYYh0TvVmFIUPlWjP7krEzADOC0R+t5V69rqR5OmSjdkACOPDGmvVHqqtxmgww5inLi29JWAyS9Q7+ENIBRY46NUUhVEqV0XY3SGNkE9yO0klRG380+xt+m0D4Wndgompu002J//uwk/Wgy6fMgC3RBY=",
                  "speds.net.private.key": "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAECgf97Y7O1IxG+47RP+zQi03WS0Z8J3gsZRwnmhCr0FuS+cyPItW2PEipEyP+FLUxWNZnrs6BiS1qZYeQnCHg6hjDT6vWOxld/tZQKom+NNqDfPPjEx2v1uANCqoU/vaOinWr3xeATO053MRByiETxczWySg08f3IkIa7vd9gZHCbhuAvRzfz/lRwl8VHOYm04y+l00pdFm52+n7Eiphdx3hGAHUHw5IET3wVSWN5WfLFWoVEFyBILHFKyJUyliC1je5Ovs2ykweXH2sXzurxd1XX3hrbfNQ1q9Y4CgE2wsQw7Bt1rSXJNqrJcmzbwy1mgxVjlMYYUEjL7PTJn/Bv7zKECgYEA84zVblos17/2IkbG4owZGxjabkPfd8cIwU3gnNnkYLcMSBHLmlBQWqM46gnR+cn5KjcLnrS/AKW/PKp3E4o7t6scUf8LmqU7XYlLo9WpcjLCRq/wNAHgp57I/1eRtEMnmETQp5u+H4c1LQkizdZBmfrS1egXAT6F4g35Ihc3N7cCgYEAyTrUpNYbHy1Z1jAjNyhmyoR5ka3MMVkzdE4rAhjQ2A38yFFQOS/3c92sC1kflv2E092uUX7HxvkC60WEQ+0fX4p55KdVEbXrEGOhmBASSoAtpBDjwk2/1XtG2bduVIDRApYdU9R8GDpiTpHYejBmCgJZDFtaGWVsqPZTcFQayTECgYAS9NpD7Zbh8BoLTvD5/yeRGV0fCf9hxkcjLJmX+8mv0W+OSHxNdYTbmSq75KnIRHU1WPV5k93uAfihky8TgUXmpNXed/iCP+wf+qi9hsrajaLbd6Q9mAvRYCWK4lj/694wFuMauJ+SuqZj6/sehFe00XYJmjf4K9Kl5qqGgcIVfwKBgBs4QwVdFOQdvVY0wr62T1k7nDoxeTp/p28nF7g75RwkoDn0jdZ+f4mDxb7vOk+xUWNbTDYn4RwwGM38ZEzP6XuB/sUIQYCYzAg2gehQ3DiJqCLWSgjBfZ6CA2512o3BswQXjDYHuxWH6D5aih7VJyhZcvQx8zkWOCGO2HCMX+cxAoGBAOWCukJ9r7yzobFChEvFUlmB9tI0d7mTjeCdbL7U6Dpz7uaGQBwGxagGgBV3D19LJFMQLtjH8xWPIlsKpsgXn8fQbiCBTtAA1+MT0LXgKQUViB2CVxSzXrGICIG5HhYmNgZfj7+1oOz+xd8oAUIYM0hMxJ2SvXaLy5yXwmBq6hFl",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIID+jCCAuKgAwIBAgIUQZAxZRzQB1/uhJT1qD9pMzQCzDswDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEOMAwGA1UECgwFZ3JpaXMxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yNTA3MDcxNDU2MjNaFw0zNTA3MDUxNDU2MjNaMEsxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCU1oxDjAMBgNVBAoMBWdyaWlzMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAGjgdUwgdIwHQYDVR0OBBYEFIm5+1m5QbD6ISvwC50D0cuaNX3GMB8GA1UdIwQYMBaAFIm5+1m5QbD6ISvwC50D0cuaNX3GMA8GA1UdEwEB/wQFMAMBAf8wMQYIKwYBBQUHAQEEJTAjMCEGCCsGAQUFBzABhhVodHRwOi8vMTI3LjAuMC4xOjk5OTkwJwYDVR0lBCAwHgYIKwYBBQUHAwkGCCsGAQUFBwMCBggrBgEFBQcDATAjBgNVHREEHDAagglsb2NhbGhvc3SCDXd3dy5sb2NhbGhvc3QwDQYJKoZIhvcNAQELBQADggEBAGpclYsipmgvR3UtRTOya92oD7UONPyFRgbL8w6eWDI2WM8OBI/d4vhnIiNhAz84cDrWB0BDfrN+xTFP88ryLMWTLp+2MMoCQWATGza7URhMUXQX2ytobhXpZCNvoPlp4xC74euKR8PDKRyTgjxDMWK9UTImWJ0+PKSnESUu2ZXIVoP4nIGtut7l+H/QAkLsYYh0TvVmFIUPlWjP7krEzADOC0R+t5V69rqR5OmSjdkACOPDGmvVHqqtxmgww5inLi29JWAyS9Q7+ENIBRY46NUUhVEqV0XY3SGNkE9yO0klRG380+xt+m0D4Wndgompu002J//uwk/Wgy6fMgC3RBY=",
                  "speds.dl.https.server.private.key": "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAECgf97Y7O1IxG+47RP+zQi03WS0Z8J3gsZRwnmhCr0FuS+cyPItW2PEipEyP+FLUxWNZnrs6BiS1qZYeQnCHg6hjDT6vWOxld/tZQKom+NNqDfPPjEx2v1uANCqoU/vaOinWr3xeATO053MRByiETxczWySg08f3IkIa7vd9gZHCbhuAvRzfz/lRwl8VHOYm04y+l00pdFm52+n7Eiphdx3hGAHUHw5IET3wVSWN5WfLFWoVEFyBILHFKyJUyliC1je5Ovs2ykweXH2sXzurxd1XX3hrbfNQ1q9Y4CgE2wsQw7Bt1rSXJNqrJcmzbwy1mgxVjlMYYUEjL7PTJn/Bv7zKECgYEA84zVblos17/2IkbG4owZGxjabkPfd8cIwU3gnNnkYLcMSBHLmlBQWqM46gnR+cn5KjcLnrS/AKW/PKp3E4o7t6scUf8LmqU7XYlLo9WpcjLCRq/wNAHgp57I/1eRtEMnmETQp5u+H4c1LQkizdZBmfrS1egXAT6F4g35Ihc3N7cCgYEAyTrUpNYbHy1Z1jAjNyhmyoR5ka3MMVkzdE4rAhjQ2A38yFFQOS/3c92sC1kflv2E092uUX7HxvkC60WEQ+0fX4p55KdVEbXrEGOhmBASSoAtpBDjwk2/1XtG2bduVIDRApYdU9R8GDpiTpHYejBmCgJZDFtaGWVsqPZTcFQayTECgYAS9NpD7Zbh8BoLTvD5/yeRGV0fCf9hxkcjLJmX+8mv0W+OSHxNdYTbmSq75KnIRHU1WPV5k93uAfihky8TgUXmpNXed/iCP+wf+qi9hsrajaLbd6Q9mAvRYCWK4lj/694wFuMauJ+SuqZj6/sehFe00XYJmjf4K9Kl5qqGgcIVfwKBgBs4QwVdFOQdvVY0wr62T1k7nDoxeTp/p28nF7g75RwkoDn0jdZ+f4mDxb7vOk+xUWNbTDYn4RwwGM38ZEzP6XuB/sUIQYCYzAg2gehQ3DiJqCLWSgjBfZ6CA2512o3BswQXjDYHuxWH6D5aih7VJyhZcvQx8zkWOCGO2HCMX+cxAoGBAOWCukJ9r7yzobFChEvFUlmB9tI0d7mTjeCdbL7U6Dpz7uaGQBwGxagGgBV3D19LJFMQLtjH8xWPIlsKpsgXn8fQbiCBTtAA1+MT0LXgKQUViB2CVxSzXrGICIG5HhYmNgZfj7+1oOz+xd8oAUIYM0hMxJ2SvXaLy5yXwmBq6hFl",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(originSocket.getLocalPort());


      final String targetParams =
          """
              {
                "options": {
                  "test":"target",
                  "speds.app.version":"2.0.22",
                  "speds.app.reference": "a reference",
                  "speds.pre.version": "2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.tra.version":"3.0.0",
                  "speds.tra.reference": "https://reference.iri/speds",
                  "speds.net.version":"3.0.0",
                  "speds.net.reference": "https://reference.iri/speds",
                  "speds.net.cert": "MIID+jCCAuKgAwIBAgIUQZAxZRzQB1/uhJT1qD9pMzQCzDswDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEOMAwGA1UECgwFZ3JpaXMxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yNTA3MDcxNDU2MjNaFw0zNTA3MDUxNDU2MjNaMEsxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCU1oxDjAMBgNVBAoMBWdyaWlzMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAGjgdUwgdIwHQYDVR0OBBYEFIm5+1m5QbD6ISvwC50D0cuaNX3GMB8GA1UdIwQYMBaAFIm5+1m5QbD6ISvwC50D0cuaNX3GMA8GA1UdEwEB/wQFMAMBAf8wMQYIKwYBBQUHAQEEJTAjMCEGCCsGAQUFBzABhhVodHRwOi8vMTI3LjAuMC4xOjk5OTkwJwYDVR0lBCAwHgYIKwYBBQUHAwkGCCsGAQUFBwMCBggrBgEFBQcDATAjBgNVHREEHDAagglsb2NhbGhvc3SCDXd3dy5sb2NhbGhvc3QwDQYJKoZIhvcNAQELBQADggEBAGpclYsipmgvR3UtRTOya92oD7UONPyFRgbL8w6eWDI2WM8OBI/d4vhnIiNhAz84cDrWB0BDfrN+xTFP88ryLMWTLp+2MMoCQWATGza7URhMUXQX2ytobhXpZCNvoPlp4xC74euKR8PDKRyTgjxDMWK9UTImWJ0+PKSnESUu2ZXIVoP4nIGtut7l+H/QAkLsYYh0TvVmFIUPlWjP7krEzADOC0R+t5V69rqR5OmSjdkACOPDGmvVHqqtxmgww5inLi29JWAyS9Q7+ENIBRY46NUUhVEqV0XY3SGNkE9yO0klRG380+xt+m0D4Wndgompu002J//uwk/Wgy6fMgC3RBY=",
                  "speds.net.private.key": "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAECgf97Y7O1IxG+47RP+zQi03WS0Z8J3gsZRwnmhCr0FuS+cyPItW2PEipEyP+FLUxWNZnrs6BiS1qZYeQnCHg6hjDT6vWOxld/tZQKom+NNqDfPPjEx2v1uANCqoU/vaOinWr3xeATO053MRByiETxczWySg08f3IkIa7vd9gZHCbhuAvRzfz/lRwl8VHOYm04y+l00pdFm52+n7Eiphdx3hGAHUHw5IET3wVSWN5WfLFWoVEFyBILHFKyJUyliC1je5Ovs2ykweXH2sXzurxd1XX3hrbfNQ1q9Y4CgE2wsQw7Bt1rSXJNqrJcmzbwy1mgxVjlMYYUEjL7PTJn/Bv7zKECgYEA84zVblos17/2IkbG4owZGxjabkPfd8cIwU3gnNnkYLcMSBHLmlBQWqM46gnR+cn5KjcLnrS/AKW/PKp3E4o7t6scUf8LmqU7XYlLo9WpcjLCRq/wNAHgp57I/1eRtEMnmETQp5u+H4c1LQkizdZBmfrS1egXAT6F4g35Ihc3N7cCgYEAyTrUpNYbHy1Z1jAjNyhmyoR5ka3MMVkzdE4rAhjQ2A38yFFQOS/3c92sC1kflv2E092uUX7HxvkC60WEQ+0fX4p55KdVEbXrEGOhmBASSoAtpBDjwk2/1XtG2bduVIDRApYdU9R8GDpiTpHYejBmCgJZDFtaGWVsqPZTcFQayTECgYAS9NpD7Zbh8BoLTvD5/yeRGV0fCf9hxkcjLJmX+8mv0W+OSHxNdYTbmSq75KnIRHU1WPV5k93uAfihky8TgUXmpNXed/iCP+wf+qi9hsrajaLbd6Q9mAvRYCWK4lj/694wFuMauJ+SuqZj6/sehFe00XYJmjf4K9Kl5qqGgcIVfwKBgBs4QwVdFOQdvVY0wr62T1k7nDoxeTp/p28nF7g75RwkoDn0jdZ+f4mDxb7vOk+xUWNbTDYn4RwwGM38ZEzP6XuB/sUIQYCYzAg2gehQ3DiJqCLWSgjBfZ6CA2512o3BswQXjDYHuxWH6D5aih7VJyhZcvQx8zkWOCGO2HCMX+cxAoGBAOWCukJ9r7yzobFChEvFUlmB9tI0d7mTjeCdbL7U6Dpz7uaGQBwGxagGgBV3D19LJFMQLtjH8xWPIlsKpsgXn8fQbiCBTtAA1+MT0LXgKQUViB2CVxSzXrGICIG5HhYmNgZfj7+1oOz+xd8oAUIYM0hMxJ2SvXaLy5yXwmBq6hFl",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIID+jCCAuKgAwIBAgIUQZAxZRzQB1/uhJT1qD9pMzQCzDswDQYJKoZIhvcNAQELBQAwSzELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAkdEMQswCQYDVQQHDAJTWjEOMAwGA1UECgwFZ3JpaXMxEjAQBgNVBAMMCWxvY2FsaG9zdDAeFw0yNTA3MDcxNDU2MjNaFw0zNTA3MDUxNDU2MjNaMEsxCzAJBgNVBAYTAkNOMQswCQYDVQQIDAJHRDELMAkGA1UEBwwCU1oxDjAMBgNVBAoMBWdyaWlzMRIwEAYDVQQDDAlsb2NhbGhvc3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAGjgdUwgdIwHQYDVR0OBBYEFIm5+1m5QbD6ISvwC50D0cuaNX3GMB8GA1UdIwQYMBaAFIm5+1m5QbD6ISvwC50D0cuaNX3GMA8GA1UdEwEB/wQFMAMBAf8wMQYIKwYBBQUHAQEEJTAjMCEGCCsGAQUFBzABhhVodHRwOi8vMTI3LjAuMC4xOjk5OTkwJwYDVR0lBCAwHgYIKwYBBQUHAwkGCCsGAQUFBwMCBggrBgEFBQcDATAjBgNVHREEHDAagglsb2NhbGhvc3SCDXd3dy5sb2NhbGhvc3QwDQYJKoZIhvcNAQELBQADggEBAGpclYsipmgvR3UtRTOya92oD7UONPyFRgbL8w6eWDI2WM8OBI/d4vhnIiNhAz84cDrWB0BDfrN+xTFP88ryLMWTLp+2MMoCQWATGza7URhMUXQX2ytobhXpZCNvoPlp4xC74euKR8PDKRyTgjxDMWK9UTImWJ0+PKSnESUu2ZXIVoP4nIGtut7l+H/QAkLsYYh0TvVmFIUPlWjP7krEzADOC0R+t5V69rqR5OmSjdkACOPDGmvVHqqtxmgww5inLi29JWAyS9Q7+ENIBRY46NUUhVEqV0XY3SGNkE9yO0klRG380+xt+m0D4Wndgompu002J//uwk/Wgy6fMgC3RBY=",
                  "speds.dl.https.server.private.key": "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQC/cYvJcndI23Y6eERy26/w6pS5Y2yNr4jPNmiuKzR+NJI0hDUqqj0JO18dwBssOtyEWUkN10D/l0H+VL8+DRAPrv2ifxF9wIHw0KS/1/CeDCtz+hbeFa28yVtoW12snMkMtUivfLab7KcA7PE4+HrMBdC37nCzUfU+S66l8N9nwgNu/xwxKqdhwfT4yFQNSD/+qWgKzZ7P7cJseiFrqZsV/2Em1SWvaIqJOQkRQ9fg0T0nfyAG0ET+IMqGN5PaIBReW218xM9TAg1ue301vpJ/t5FiEFqT6V4F+pzYsIAchfDhXwjEL1t8PmOhM5ZBzNarx698OCDtjGtvorSA5lkHAgMBAAECgf97Y7O1IxG+47RP+zQi03WS0Z8J3gsZRwnmhCr0FuS+cyPItW2PEipEyP+FLUxWNZnrs6BiS1qZYeQnCHg6hjDT6vWOxld/tZQKom+NNqDfPPjEx2v1uANCqoU/vaOinWr3xeATO053MRByiETxczWySg08f3IkIa7vd9gZHCbhuAvRzfz/lRwl8VHOYm04y+l00pdFm52+n7Eiphdx3hGAHUHw5IET3wVSWN5WfLFWoVEFyBILHFKyJUyliC1je5Ovs2ykweXH2sXzurxd1XX3hrbfNQ1q9Y4CgE2wsQw7Bt1rSXJNqrJcmzbwy1mgxVjlMYYUEjL7PTJn/Bv7zKECgYEA84zVblos17/2IkbG4owZGxjabkPfd8cIwU3gnNnkYLcMSBHLmlBQWqM46gnR+cn5KjcLnrS/AKW/PKp3E4o7t6scUf8LmqU7XYlLo9WpcjLCRq/wNAHgp57I/1eRtEMnmETQp5u+H4c1LQkizdZBmfrS1egXAT6F4g35Ihc3N7cCgYEAyTrUpNYbHy1Z1jAjNyhmyoR5ka3MMVkzdE4rAhjQ2A38yFFQOS/3c92sC1kflv2E092uUX7HxvkC60WEQ+0fX4p55KdVEbXrEGOhmBASSoAtpBDjwk2/1XtG2bduVIDRApYdU9R8GDpiTpHYejBmCgJZDFtaGWVsqPZTcFQayTECgYAS9NpD7Zbh8BoLTvD5/yeRGV0fCf9hxkcjLJmX+8mv0W+OSHxNdYTbmSq75KnIRHU1WPV5k93uAfihky8TgUXmpNXed/iCP+wf+qi9hsrajaLbd6Q9mAvRYCWK4lj/694wFuMauJ+SuqZj6/sehFe00XYJmjf4K9Kl5qqGgcIVfwKBgBs4QwVdFOQdvVY0wr62T1k7nDoxeTp/p28nF7g75RwkoDn0jdZ+f4mDxb7vOk+xUWNbTDYn4RwwGM38ZEzP6XuB/sUIQYCYzAg2gehQ3DiJqCLWSgjBfZ6CA2512o3BswQXjDYHuxWH6D5aih7VJyhZcvQx8zkWOCGO2HCMX+cxAoGBAOWCukJ9r7yzobFChEvFUlmB9tI0d7mTjeCdbL7U6Dpz7uaGQBwGxagGgBV3D19LJFMQLtjH8xWPIlsKpsgXn8fQbiCBTtAA1+MT0LXgKQUViB2CVxSzXrGICIG5HhYmNgZfj7+1oOz+xd8oAUIYM0hMxJ2SvXaLy5yXwmBq6hFl",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(targetSocket.getLocalPort());

      // network-server-init-success
      NetworkHost targetNetHost = networkFactory.initHost(targetParams);

      // network-client-request-success
      NetworkHost originNetHost = networkFactory.initHost(originParams);
      String idu45 = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45Dto);

      // C'est indiqué 56 dans le code, mais c'est 45.
      originNetHost.request(idu45);

      String result = targetNetHost.indication();

      InterfaceDataUnit45Dto resultDto =
          objectMapper.readValue(result, InterfaceDataUnit45Dto.class);

      targetNetHost.close();
      originNetHost.close();

      assertEquals(idu45Dto.getMessage(), resultDto.getMessage());
      assertEquals(idu45Dto.getContext().getDestinationIri(),
          resultDto.getContext().getDestinationIri());
      assertNotEquals(idu45Dto.getContext().getTrackingNumber(),
          resultDto.getContext().getTrackingNumber());
      assertEquals(Boolean.FALSE, resultDto.getContext().getOptions());
    }
  }

}
