package ca.griis.speds.integration.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.integration.util.TestUtil;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@Disabled
public class PresLinkMemoryLeakIT {
  private PgaService pgaServiceMock;
  private SessionHost sessionHostMock;

  private SyncApplicationFactory appFactory;
  private MutablePresentationFactory presentationFactory;
  private ImmutableDataLinkFactory linkFactory;

  private ObjectMapper objectMapper;

  private TestUtil testUtil;

  @BeforeEach
  public void setup() {
    pgaServiceMock = Mockito.mock(PgaService.class);
    sessionHostMock = Mockito.mock(SessionHost.class);

    presentationFactory = new MutablePresentationFactory(pgaServiceMock) {
      @Override
      public SessionHost initSessionHost(String parameters) {
        return sessionHostMock;
      }
    };

    linkFactory = new ImmutableDataLinkFactory();

    objectMapper = new ObjectMapper();

    testUtil = new TestUtil(objectMapper);
  }

  @Test
  public void presLinkMemoryLeakTest() throws IOException {
    // INITIALISATIONS SPEDS
    try (ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {

      testUtil.freePorts(originSocket, targetSocket, null);

      final String originParams =
          """
              {
                "options": {
                  "speds.app.version":"2.0.0",
                  "speds.app.reference": "a reference",
                  "speds.pre.version":"2.0.0",
                  "speds.pre.reference": "a reference",
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
                  "speds.pre.version":"2.0.0",
                  "speds.pre.reference": "a reference",
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.host": "localhost",
                  "speds.dl.https.server.port": %1$s,
                  "speds.dl.https.client.cert.trustmanager.mode" : "insecure"
                }
              }"""
              .formatted(targetSocket.getLocalPort());

      // pre-client-init-success
      PresentationHost originPresHost = presentationFactory.init(originParams);

      // app-client-init-success
      appFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return originPresHost;
        }
      };

      // app-client-init-success
      ApplicationHost originAppHost = appFactory.init(originParams);
      // link1-init-success
      DataLinkHost originLinkHost = linkFactory.init(originParams);

      // pre-server-init-success
      PresentationHost targetPresHost = presentationFactory.init(targetParams);

      // app-server-init-success
      appFactory = new SyncApplicationFactory(pgaServiceMock) {
        @Override
        public PresentationHost initPresentationHost(String parameters) {
          return targetPresHost;
        }
      };
      // app-server-init-success
      ApplicationHost targetAppHost = appFactory.init(targetParams);
      // link2-init-success
      DataLinkHost targetLinkHost = linkFactory.init(targetParams);

      // INITIAL MEMORY
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();

      // EXCHANGES

      // Variables pour stocker les mesures de mémoire
      List<Long> memoryDifferences = new ArrayList<>();

      // BOUCLE TEST (10 itérations)
      final int ITERATIONS = 10;

      final int MESSAGES_PER_ITERATION = 500;

      for (int i = 0; i < ITERATIONS; i++) {

        // Envoyer 5000 messages différents
        for (int j = 0; j < MESSAGES_PER_ITERATION; j++) {

          // app-client-request-success
          InterfaceDataUnit01Dto idu01Dto = testUtil.buildIdu01();
          originAppHost.request(idu01Dto);

          ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
          verify(sessionHostMock, atLeastOnce()).request(captor.capture());

          String idu23 = captor.getValue();
          assertNotNull(idu23);

          InterfaceDataUnit23Dto idu23Dto =
              objectMapper.readValue(idu23, InterfaceDataUnit23Dto.class);

          // transform-idu
          InterfaceDataUnit56Dto idu56Dto =
              testUtil.transformIdu23ToIdu56(idu23Dto, targetSocket.getLocalPort());

          String idu56 = objectMapper.writeValueAsString(idu56Dto);

          // link1-request-success
          originLinkHost.request(idu56);

          // link2-indication-success
          String receivedIdu56 = targetLinkHost.indication();

          // transform-inverse-idu
          InterfaceDataUnit56Dto receivedIdu56Dto =
              objectMapper.readValue(receivedIdu56, InterfaceDataUnit56Dto.class);
          InterfaceDataUnit23Dto receivedIdu23Dto =
              testUtil.transformIdu56ToIdu23(receivedIdu56Dto, idu23Dto.getContext().getPga(),
                  idu23Dto.getContext().getSourceCode(), idu23Dto.getContext().getDestinationCode(),
                  idu23Dto.getContext().getSdek(), UUID.randomUUID());

          // app-server-indication-success
          String receivedIdu23 = objectMapper.writeValueAsString(receivedIdu23Dto);

          doReturn(receivedIdu23).when(sessionHostMock).indicateDataExchange();

          InterfaceDataUnit01Dto receivedIdu01Dto = targetAppHost.indication();

          assertNotNull(receivedIdu01Dto);
        }

        // Force garbage collection pour plus de précision
        System.gc();

        // Attendre que le GC fasse son travail
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // Noter la différence de mémoire
        long currentMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long memoryDiff = currentMemory - initialMemory;
        memoryDifferences.add(memoryDiff);
      }

      // Afficher le résumé des différences de mémoire
      System.out.println("\n===== RÉSUMÉ DES DIFFÉRENCES DE MÉMOIRE =====");
      for (int i = 0; i < memoryDifferences.size(); i++) {
        System.out.println("Itération " + (i + 1) + ": " +
            testUtil.formatMemorySize(memoryDifferences.get(i)));
      }

      if (!testUtil.hasReachedPlateau(memoryDifferences)) {

        // Créer un message d'erreur détaillé avec toutes les différences de mémoire
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(
            "Détection potentielle d'une fuite de mémoire: la mémoire n'a pas atteint un plateau après ")
            .append(ITERATIONS)
            .append(" itérations.\n\nDétail des mesures de mémoire:\n");

        for (int i = 0; i < memoryDifferences.size(); i++) {
          errorMessage.append("Itération ").append(i + 1).append(": ")
              .append(testUtil.formatMemorySize(memoryDifferences.get(i)))
              .append("\n");
        }

        fail(errorMessage.toString());
      }
    }
  }
}
