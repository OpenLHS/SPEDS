package ca.griis.speds.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.*;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.DomainProviderUtil;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class AppLinkSpedsIT {
  private PgaService pgaServiceMock;
  private PresentationHost presentationHostMock;

  private SyncApplicationFactory appFactory;
  private ImmutableDataLinkFactory linkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  // Test jquik
  @BeforeProperty
  public void setupProperty() {
    pgaServiceMock = Mockito.mock(PgaService.class);
    presentationHostMock = Mockito.mock(ImmutablePresentationHost.class);

    appFactory = new SyncApplicationFactory(pgaServiceMock) {
      @Override
      public PresentationHost initPresentationHost(String parameters) {
        return presentationHostMock;
      }
    };

    linkFactory = new ImmutableDataLinkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  // On a tester avec le défault de 1000 propriétés, mais c'est configurer à 10 pour l'efficacité
  @Property(tries = 10)
  @Domain(DomainProviderUtil.class)
  public void appLinkExchangeJquikTest(@ForAll InterfaceDataUnit01Dto idu01Dto) throws IOException {
    try (ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {

      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
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
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(targetSocket.getLocalPort());

      // app-client-init-success
      ApplicationHost originAppHost = appFactory.init(originParams);
      // link1-init-success
      DataLinkHost originLinkHost = linkFactory.init(originParams);
      // app-server-init-success
      ApplicationHost targetAppHost = appFactory.init(targetParams);
      // link2-init-success
      DataLinkHost targetLinkHost = linkFactory.init(targetParams);

      // app-client-request-success
      originAppHost.request(idu01Dto);

      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(presentationHostMock, atLeastOnce()).request(captor.capture());

      String idu12 = captor.getValue();
      assertNotNull(idu12);

      InterfaceDataUnit12Dto idu12Dto =
          objectMapper.readValue(idu12, InterfaceDataUnit12Dto.class);

      // transform-idu
      InterfaceDataUnit56Dto idu56Dto =
          testUtil.transformIdu12ToIdu56(idu12Dto, targetSocket.getLocalPort());

      String idu56 = objectMapper.writeValueAsString(idu56Dto);

      // link1-request-success
      originLinkHost.request(idu56);

      // link2-indication-success
      String receivedIdu56 = targetLinkHost.indication();

      // transform-inverse-idu
      InterfaceDataUnit56Dto receivedIdu56Dto =
          objectMapper.readValue(receivedIdu56, InterfaceDataUnit56Dto.class);
      InterfaceDataUnit12Dto receivedIdu12Dto =
          testUtil.transformIdu56ToIdu12(receivedIdu56Dto, idu12Dto.getContext().getPga(),
              idu12Dto.getContext().getSourceCode(), idu12Dto.getContext().getDestinationCode());

      // app-server-indication-success
      String receivedIdu12 = objectMapper.writeValueAsString(receivedIdu12Dto);
      when(presentationHostMock.indication()).thenReturn(receivedIdu12);

      InterfaceDataUnit01Dto receivedIdu01Dto = targetAppHost.indication();

      assertNotNull(receivedIdu01Dto);

      assertEquals(idu01Dto.getMessage(), receivedIdu01Dto.getMessage());
      assertEquals(idu01Dto.getContext().getPga(), receivedIdu01Dto.getContext().getPga());
      assertEquals(idu01Dto.getContext().getSourceCode(),
          receivedIdu01Dto.getContext().getSourceCode());
      assertEquals(idu01Dto.getContext().getDestinationCode(),
          receivedIdu01Dto.getContext().getDestinationCode());


      // TODO 06/05/2025-BD : Ajouter les appels aux APIs response et confirm
      // app-client-response-success
      // originAppHost.response(idu01Dto);
      //
      // ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
      // verify(presentationHostMock, atLeastOnce()).response(responseCaptor.capture());
      //
      // String respIdu12 = captor.getValue();
      // assertNotNull(respIdu12);
      //
      // InterfaceDataUnit12Dto respIdu12Dto =
      // objectMapper.readValue(respIdu12, InterfaceDataUnit12Dto.class);
      //
      // // transform-idu
      // InterfaceDataUnit56Dto respIdu56Dto =
      // testUtil.transformIdu12ToIdu56(respIdu12Dto, targetSocket.getLocalPort());
      //
      // String respIdu56 = objectMapper.writeValueAsString(respIdu56Dto);
      //
      // // link1-response-success
      // originLinkHost.response(respIdu56);
      //
      // // link2-confirm-success
      // String confReceivedIdu56 = targetLinkHost.confirm();
      //
      // // transform-inverse-idu
      // InterfaceDataUnit56Dto confReceivedIdu56Dto =
      // objectMapper.readValue(confReceivedIdu56, InterfaceDataUnit56Dto.class);
      // InterfaceDataUnit12Dto confReceivedIdu12Dto =
      // testUtil.transformIdu56ToIdu12(confReceivedIdu56Dto, idu12Dto.getContext().getPga(),
      // idu12Dto.getContext().getSourceCode(), idu12Dto.getContext().getDestinationCode());
      //
      // // app-server-confirm-success
      // String confReceivedIdu12 = objectMapper.writeValueAsString(confReceivedIdu12Dto);
      // when(presentationHostMock.confirm()).thenReturn(confReceivedIdu12);
      //
      // InterfaceDataUnit01Dto confReceivedIdu01Dto = targetAppHost.confirm();
      //
      // assertNotNull(confReceivedIdu01Dto);
      //
      // assertEquals(idu01Dto.getMessage(), confReceivedIdu01Dto.getMessage());
      // assertEquals(idu01Dto.getContext().getPga(), confReceivedIdu01Dto.getContext().getPga());
      // assertEquals(idu01Dto.getContext().getSourceCode(),
      // confReceivedIdu01Dto.getContext().getSourceCode());
      // assertEquals(idu01Dto.getContext().getDestinationCode(),
      // confReceivedIdu01Dto.getContext().getDestinationCode());
    }
  }
}
