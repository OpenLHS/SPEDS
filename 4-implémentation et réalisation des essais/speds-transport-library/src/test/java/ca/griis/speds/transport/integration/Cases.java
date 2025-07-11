package ca.griis.speds.transport.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.exception.ContentSealException;
import ca.griis.speds.transport.exception.DeserializationException;
import ca.griis.speds.transport.exception.HeaderSealException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

public class Cases {

  // Ce cas de test retourne le messageId nécessaire pour les messages de confirmation.
  public static String ct_pro_04_01(Environment environment) throws JsonProcessingException {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-04_01_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'git sat";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-04_01_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-04_01_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-04_01_E1", String.class);
    assertNotNull(givenMsg, h2);

    String h3 =
        "CT_PRO-04_01_ZH003 - L'implémentation fournit le fournisseur de réponse et il est disponible dans la variable \"CT_PRO-04_01_Z002_supplier\" de l'environnement";
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
    givenHost.dataRequest(givenMsg);

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

  public static void ct_pro_04_02(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-04_02_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-04_02_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-04_02_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-04_01_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    DeserializationException actual = assertThrows(DeserializationException.class, () -> {
      givenHost.dataRequest(givenMsg);
    });
    assertNotNull(actual,
        "Échec attendu; le test valide qu'un élément malformé lance une exception");
  }

  public static void ct_pro_05_01(Environment environment) {
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

    // When / Then
    assertDoesNotThrow(() -> {
      givenHost.dataConfirm();
    });
  }

  public static void ct_pro_05_03(Environment environment) {
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
    HeaderSealException actual = assertThrows(HeaderSealException.class, givenHost::dataConfirm);

    assertNotNull(actual);
  }

  public static void ct_pro_05_04(Environment environment) {
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
    ContentSealException actual = assertThrows(ContentSealException.class, givenHost::dataConfirm);

    assertNotNull(actual);
  }

  public static void ct_pro_05_05(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-05_05_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-05_05_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-05_05_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-05_05_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When / Then
    assertDoesNotThrow(() -> {
      givenHost.dataConfirm();
    });
  }

  // ct_005
  public static void ct_pro_05_06(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-05_06_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-05_06_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-05_06_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-05_06_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    DeserializationException actual =
        assertThrows(DeserializationException.class, givenHost::dataConfirm);

    assertNotNull(actual,
        "Échec attendu; le test valide un élément absent de la conception donc comportement non garanti");
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
    String c006_s1 = givenHost.dataReply();
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
        "CT_PRO-11_03_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'git sat";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_03_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_03_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_03_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    HeaderSealException actual = assertThrows(HeaderSealException.class, givenHost::dataReply);

    // Then
    assertNotNull(actual,
        "Échec attendu; le test valide un élément absent de la conception donc comportement non garanti");
  }

  // ct_008
  public static void ct_pro_11_04(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-11_04_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'git sat";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_04_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_04_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_04_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    ContentSealException actual = assertThrows(ContentSealException.class, givenHost::dataReply);

    // Then
    assertNotNull(actual,
        "Échec attendu; le test valide un élément absent de la conception donc comportement non garanti");
  }

  // ct_009
  public static void ct_pro_11_05(Environment environment) {
    // Given
    // Validation des antécédents
    String h1 =
        "CT_PRO-11_05_ZH001 - Le client est disponible dans la variable \"Z001_host\" de l'environnement";
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    assertNotNull(givenHost, h1);

    String h2 =
        "CT_PRO-11_05_ZH002 - Le message d'envoi initial est disponible dans la variable \"CT_PRO-11_05_E1\" de l'environnement";
    String givenMsg = environment.getInput("CT_PRO-11_05_E1", String.class);
    assertNotNull(givenMsg, h2);

    // When
    DeserializationException actual =
        assertThrows(DeserializationException.class, givenHost::dataConfirm);

    assertNotNull(actual,
        "Échec attendu; le test valide un élément absent de la conception donc comportement non garanti");
  }
}
