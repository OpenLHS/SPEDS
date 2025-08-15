package ca.griis.speds.transport.integration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.ImmutableTransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Cases {
  // Ce cas de test retourne le messageId nécessaire pour les messages de confirmation.
  public static String ct_pro_04_01(Environment environment) throws JsonProcessingException {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-04_01_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-04_01_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-04_01_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-04_01_E1", String.class);
    assertNotNull(givenMsg, h2);

    String h3 =
        "CT_PRO-04_01_ZH003 - L'implémentation fournit le fournisseur de réponse et il est disponible dans la variable \"CT_PRO-04_01_Z002_supplier\" de l'environnement";
    @SuppressWarnings("unchecked")
    Supplier<String> givenSupplier =
        environment.getInput("CT_PRO-04_01_Z002_supplier", Supplier.class);
    assertNotNull(givenSupplier, h3);

    String h4 =
        "CT_PRO-04_01_ZH004 - La version de speds est disponible dans la variable \"CT_PRO-04_01_Z003_speds\"";
    Speds45Dto speds45Dto = environment.getInput("CT_PRO-04_01_Z003_speds", Speds45Dto.class);
    assertNotNull(speds45Dto, h4);

    String h5 =
        "CT_PRO-04_01_ZH004 - L'objet de mise-en-correspondance est disponible dans la variable \"CT_PRO-04_01_Z003_mapper\"";
    ObjectMapper mapper = environment.getInput("CT_PRO-04_01_Z003_mapper", ObjectMapper.class);
    assertNotNull(mapper, h5);

    InterfaceDataUnit34Dto InterfaceDataUnit34Dto =
        mapper.readValue(givenMsg, InterfaceDataUnit34Dto.class);

    // When
    givenHost.request(givenMsg);

    // Then
    String actualIduReceived = givenSupplier.get();
    InterfaceDataUnit45Dto actualIdu =
        mapper.readValue(actualIduReceived, InterfaceDataUnit45Dto.class);

    InterfaceDataUnit45Dto expectedIdu =
        TestOutputs.make_ct_pro_04_01_s1(InterfaceDataUnit34Dto, speds45Dto, mapper);
    ProtocolDataUnit4TraDto expectedPdu =
        mapper.readValue(expectedIdu.getMessage(), ProtocolDataUnit4TraDto.class);

    assertEquals(expectedIdu.getContext().getSourceIri(), actualIdu.getContext().getSourceIri());
    assertEquals(expectedIdu.getContext().getDestinationIri(),
        actualIdu.getContext().getDestinationIri());
    assertNotNull(actualIdu.getContext().getTrackingNumber());
    assertFalse((Boolean) actualIdu.getContext().getOptions());

    ProtocolDataUnit4TraDto actualPdu =
        mapper.readValue(actualIdu.getMessage(), ProtocolDataUnit4TraDto.class);

    assertEquals(expectedPdu.getHeader().getMsgtype(), actualPdu.getHeader().getMsgtype());
    assertNotNull(actualPdu.getHeader().getId());
    assertEquals(expectedPdu.getHeader().getSourceCode(), actualPdu.getHeader().getSourceCode());
    assertEquals(expectedPdu.getHeader().getSpeds(), actualPdu.getHeader().getSpeds());

    String header = mapper.writeValueAsString(actualPdu.getHeader());
    Sha512Hashing hashing = new Sha512Hashing();
    String expectedHeaderSeal = hashing.hash(header.getBytes(StandardCharsets.UTF_8)).asBase64();
    assertEquals(expectedHeaderSeal, actualPdu.getStamp().getHeaderSeal());

    String expectedContentSeal =
        hashing.hash(actualPdu.getContent().getBytes(StandardCharsets.UTF_8)).asBase64();
    assertEquals(expectedContentSeal, actualPdu.getStamp().getContentSeal());

    assertEquals(expectedPdu.getContent(), actualPdu.getContent());

    environment.addOutput(Map.entry("C001_S1", actualIdu));

    return String.valueOf(actualPdu.getHeader().getId());
  }

  public static void ct_pro_05_01(String messageId, Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-05_01_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-05_01_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-05_01_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-05_01_E1", String.class);
    assertNotNull(givenMsg, h2);

    // Non vérifier
    String h3 =
        "CT_PRO-05_01_ZH002 - Un message TRA.MSG.ENV a été envoyé précédemment par l'entité testé";
    assertNotNull(h3);

    ImmutableTransportHost h = (ImmutableTransportHost) givenHost;
    assertTrue(h.isPending(messageId));

    givenHost.listen();

    await()
        .atMost(2, TimeUnit.SECONDS)
        .until(() -> !h.isPending(messageId));
  }

  public static void ct_pro_05_03(String messageId, Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-05_03_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-05_03_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-05_03_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-05_03_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When

    givenHost.listen();

    ImmutableTransportHost h = (ImmutableTransportHost) givenHost;
    try {
      await()
          .atMost(2, TimeUnit.SECONDS)
          .until(() -> !h.isPending(messageId));
    } catch (org.awaitility.core.ConditionTimeoutException ignored) {
    }

    assertTrue(h.isPending(messageId));
  }

  public static void ct_pro_05_04(String messageId, Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-05_04_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-05_04_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-05_04_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-05_04_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When

    givenHost.listen();

    ImmutableTransportHost h = (ImmutableTransportHost) givenHost;
    try {
      await()
          .atMost(2, TimeUnit.SECONDS)
          .until(() -> !h.isPending(messageId));
    } catch (org.awaitility.core.ConditionTimeoutException ignored) {
    }

    assertTrue(h.isPending(messageId));
  }

  // ct_006
  public static void ct_pro_11_01(Environment environment) throws JsonProcessingException {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-11_01_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_01_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_01_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_01_E1", String.class);
    assertNotNull(givenMsg, h2);

    String h3 =
        "CT_PRO-11_01_ZH003 - L'implémentation fournit le fournisseur de réponse et il est disponible dans la variable \"CT_PRO-11_01_Z002_supplier\" de l'environnement";
    @SuppressWarnings("unchecked")
    Supplier<String> givenSupplier =
        environment.getInput("CT_PRO-11_01_Z002_supplier", Supplier.class);
    assertNotNull(givenSupplier, h3);

    String h4 =
        "CT_PRO-11_01_ZH003 - L'objet de mise-en-correspondance est disponible dans la variable \"CT_PRO-11_01_Z003_mapper\"";
    ObjectMapper mapper = environment.getInput("CT_PRO-11_01_Z003_mapper", ObjectMapper.class);
    assertNotNull(mapper, h4);

    InterfaceDataUnit45Dto givenIn = mapper.readValue(givenMsg, InterfaceDataUnit45Dto.class);
    ProtocolDataUnit4TraDto pdu =
        mapper.readValue(givenIn.getMessage(), ProtocolDataUnit4TraDto.class);
    InterfaceDataUnit34Dto expectedS1 = TestOutputs.make_ct_pro_11_01_s1(givenIn, mapper);
    InterfaceDataUnit45Dto expectedS2 =
        TestOutputs.make_ct_pro_11_01_s2(givenIn, pdu.getHeader().getSpeds(), mapper);
    ProtocolDataUnit4TraDto expectedS2Pdu =
        mapper.readValue(expectedS2.getMessage(), ProtocolDataUnit4TraDto.class);

    // When
    String c006_s1 = givenHost.indication();
    String c006_s2 = givenSupplier.get();

    // Then

    // s1
    InterfaceDataUnit34Dto actualS1 = mapper.readValue(c006_s1, InterfaceDataUnit34Dto.class);
    assertEquals(expectedS1.getContext().getSourceIri(), actualS1.getContext().getSourceIri());
    assertEquals(expectedS1.getContext().getDestinationIri(),
        actualS1.getContext().getDestinationIri());
    assertNotNull(actualS1.getContext().getTrackingNumber());
    assertFalse((Boolean) actualS1.getContext().getOptions());
    assertEquals(expectedS1.getMessage(), actualS1.getMessage());

    // s2
    InterfaceDataUnit45Dto actualS2 = mapper.readValue(c006_s2, InterfaceDataUnit45Dto.class);
    assertEquals(expectedS2.getContext().getSourceIri(), actualS2.getContext().getSourceIri());
    assertEquals(expectedS2.getContext().getDestinationIri(),
        actualS2.getContext().getDestinationIri());
    assertNotNull(actualS2.getContext().getTrackingNumber());
    assertFalse((Boolean) actualS2.getContext().getOptions());

    ProtocolDataUnit4TraDto actualS2Pdu =
        mapper.readValue(actualS2.getMessage(), ProtocolDataUnit4TraDto.class);
    assertEquals(expectedS2Pdu.getHeader().getMsgtype(), actualS2Pdu.getHeader().getMsgtype());
    assertNotNull(actualS2Pdu.getHeader().getId());
    assertEquals(expectedS2Pdu.getHeader().getSourceCode(),
        actualS2Pdu.getHeader().getSourceCode());
    assertEquals(expectedS2Pdu.getHeader().getSpeds(), actualS2Pdu.getHeader().getSpeds());

    String header = mapper.writeValueAsString(actualS2Pdu.getHeader());
    Sha512Hashing hashing = new Sha512Hashing();
    String expectedHeaderSeal = hashing.hash(header.getBytes(StandardCharsets.UTF_8)).asBase64();
    assertEquals(expectedHeaderSeal, actualS2Pdu.getStamp().getHeaderSeal());

    String expectedContentSeal =
        hashing.hash(actualS2Pdu.getContent().getBytes(StandardCharsets.UTF_8)).asBase64();
    assertEquals(expectedContentSeal, actualS2Pdu.getStamp().getContentSeal());

    assertEquals(expectedS2Pdu.getContent(), actualS2Pdu.getContent());

    environment.addOutput(Map.entry("C001_S1", actualS2));
  }

  // ct_007
  public static void ct_pro_11_03(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-11_03_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_03_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_03_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_03_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    boolean isNull = false;
    try {
      await()
          .atMost(2, TimeUnit.SECONDS)
          .until(() -> givenHost.indication() != null);
    } catch (org.awaitility.core.ConditionTimeoutException ignored) {
      isNull = true;
    }

    assertTrue(isNull);
  }

  // ct_008
  public static void ct_pro_11_04(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-11_04_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_04_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_04_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_04_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    boolean isNull = false;
    try {
      await()
          .atMost(2, TimeUnit.SECONDS)
          .until(() -> givenHost.indication() != null);
    } catch (org.awaitility.core.ConditionTimeoutException ignored) {
      isNull = true;
    }

    assertTrue(isNull);
  }

  public static void ct_gen_01(Environment environment)
      throws ExecutionException, InterruptedException, JsonProcessingException {
    // Given
    ExecutorService executor = Executors.newFixedThreadPool(10);

    // Validation des antécédents
    String h1 =
        "CT_GEN_01_ZH001 - L'hôte transport A est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenAlphaHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenAlphaHost, h1);

    String h2 =
        "CT_GEN_01_ZH002 - L'hôte transport B est disponible dans la variable \"Z002_host\" de l'environnement";
    TransportHost givenBetaHost = environment.getInput("Z002_host", TransportHost.class);
    assertNotNull(givenBetaHost, h2);

    String h3 =
        "CT_GEN_01_ZH003 - Le message d'envoi initial de l'hôte A est disponible dans la variable \"CT_GEN_01_E1\" de l'environnement";
    String givenAlphaMsg = environment.getInput("CT_GEN_01_E1", String.class);
    assertNotNull(givenAlphaMsg, h3);

    String h4 =
        "CT_GEN_01_ZH004 - Le message d'envoi initial de l'hôte B est disponible dans la variable \"CT_GEN_01_E2\" de l'environnement";
    String givenBetaMsg = environment.getInput("CT_GEN_01_E2", String.class);
    assertNotNull(givenBetaMsg, h4);

    String h5 =
        "CT_GEN_01_ZH005 - L'implémentation fournit le fournisseur de réponse de l'hôte A et il est disponible dans la variable \"CT_GEN_01_Z003_supplier\" de l'environnement";
    @SuppressWarnings("unchecked")
    Supplier<String> givenAlphaSupplier =
        environment.getInput("CT_GEN_01_Z003_supplier", Supplier.class);
    assertNotNull(givenAlphaSupplier, h5);

    String h6 =
        "CT_GEN_01_ZH006 - L'implémentation fournit le fournisseur de réponse de l'hôte A et il est disponible dans la variable \"CT_GEN_01_Z004_supplier\" de l'environnement";
    @SuppressWarnings("unchecked")
    Supplier<String> givenBetaSupplier =
        environment.getInput("CT_GEN_01_Z004_supplier", Supplier.class);
    assertNotNull(givenBetaSupplier, h6);

    String h7 =
        "CT_GEN_01_ZH007 - L'objet de mise-en-correspondance est disponible dans la variable \"CT_GEN_01_Z005_mapper\"";
    ObjectMapper mapper = environment.getInput("CT_GEN_01_Z005_mapper", ObjectMapper.class);
    assertNotNull(mapper, h7);

    String h8 =
        "CT_GEN_01_ZH008 - L'hôte réseau A est disponible dans la variable \"Z006_Net_host\" de l'environnement";
    NetworkHost givenAlphaNetHost = environment.getInput("Z006_Net_host", NetworkHost.class);
    assertNotNull(givenAlphaNetHost, h8);

    String h9 =
        "CT_GEN_01_ZH009 - L'hôte réseau B est disponible dans la variable \"Z007_Net_host\" de l'environnement";
    NetworkHost givenBetaNetHost = environment.getInput("Z007_Net_host", NetworkHost.class);
    assertNotNull(givenBetaNetHost, h9);


    for (int i = 0; i < 10; i++) {
      // When
      Callable<String> callableTask = givenBetaHost::indication;

      Callable<String> callableTask2 = givenAlphaHost::indication;

      Callable<Void> callableTask3 = () -> {
        givenAlphaHost.dataConfirm();
        return null;
      };

      Callable<Void> callableTask4 = () -> {
        givenBetaHost.dataConfirm();
        return null;
      };

      Future<String> future = executor.submit(callableTask);
      Future<String> future2 = executor.submit(callableTask2);
      Future<Void> future3 = executor.submit(callableTask3);
      Future<Void> future4 = executor.submit(callableTask4);


      givenAlphaHost.request(givenAlphaMsg);
      String alphaIdu45ToSend = givenAlphaSupplier.get();

      when(givenBetaNetHost.indication()).thenReturn(alphaIdu45ToSend);


      givenBetaHost.request(givenBetaMsg);
      String betaIdu45ToSend = givenBetaSupplier.get();

      when(givenAlphaNetHost.indication()).thenReturn(betaIdu45ToSend);



      String betaOutMsg = future.get();

      String alphaOutMsg = future2.get();

      future3.get();

      future4.get();


      // Then
      InterfaceDataUnit34Dto expectedOutAlpha =
          mapper.readValue(givenBetaMsg, InterfaceDataUnit34Dto.class);

      InterfaceDataUnit34Dto expectedOutBeta =
          mapper.readValue(givenAlphaMsg, InterfaceDataUnit34Dto.class);

      InterfaceDataUnit34Dto actualOutAlpha =
          mapper.readValue(alphaOutMsg, InterfaceDataUnit34Dto.class);

      InterfaceDataUnit34Dto actualOutBeta =
          mapper.readValue(betaOutMsg, InterfaceDataUnit34Dto.class);

      assertEquals(expectedOutAlpha.getContext().getSourceIri(),
          actualOutAlpha.getContext().getSourceIri());
      assertEquals(expectedOutAlpha.getContext().getDestinationIri(),
          actualOutAlpha.getContext().getDestinationIri());
      assertEquals(expectedOutAlpha.getContext().getSourceCode(),
          actualOutAlpha.getContext().getSourceCode());
      assertEquals(expectedOutAlpha.getContext().getDestinationCode(),
          actualOutAlpha.getContext().getDestinationCode());
      assertEquals(expectedOutAlpha.getMessage(), actualOutAlpha.getMessage());

      assertEquals(expectedOutBeta.getContext().getSourceIri(),
          actualOutBeta.getContext().getSourceIri());
      assertEquals(expectedOutBeta.getContext().getDestinationIri(),
          actualOutBeta.getContext().getDestinationIri());
      assertEquals(expectedOutBeta.getContext().getSourceCode(),
          actualOutBeta.getContext().getSourceCode());
      assertEquals(expectedOutBeta.getContext().getDestinationCode(),
          actualOutBeta.getContext().getDestinationCode());
      assertEquals(expectedOutBeta.getMessage(), actualOutBeta.getMessage());
    }
  }
}
