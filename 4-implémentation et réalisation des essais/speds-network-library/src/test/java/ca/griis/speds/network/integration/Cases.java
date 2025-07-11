package ca.griis.speds.network.integration;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.ArgumentCaptor;

public class Cases {

  static TestCryptoUtils cryptoUtils = new TestCryptoUtils();

  public static void ct_pro_03_01(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte client réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte client liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_03_01_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);

    // when
    // Une demande d’échange de données valide provoque un envoi réussi.
    environment.networkClient.request(idu45_e1);


    // then
    // Le message idu_5-6_ENV a bien été envoyé.
    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    assertEquals(idu45Dto_e1.getContext().getDestinationIri(),
        idu56SentDto.getContext().getDestinationIri());

    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    assertEquals(pdu5Sent.getHeader().getId(),
        idu56SentDto.getContext().getTrackingNumber().toString());


    assertEquals(HeaderDto.Msgtype.RES_ENV, pdu5Sent.getHeader().getMsgtype());
    assertEquals(idu45Dto_e1.getContext().getSourceIri(), pdu5Sent.getHeader().getSourceIri());
    assertEquals(idu45Dto_e1.getContext().getDestinationIri(),
        pdu5Sent.getHeader().getDestinationIri());

    assertNotNull(pdu5Sent.getHeader().getAuthentification());
    assertTrue(cryptoUtils.verifyCertificate((String) pdu5Sent.getHeader().getAuthentification(),
        idu45Dto_e1.getContext().getSourceIri()));

    assertEquals(false, pdu5Sent.getHeader().getParameters());

    assertEquals(environment.spedsVersion,
        pdu5Sent.getHeader().getSpeds().getVersion());
    assertEquals(environment.spedsReference,
        pdu5Sent.getHeader().getSpeds().getReference());

    assertTrue(cryptoUtils.verifySeal(pdu5Sent.getHeader(), "header",
        (String) pdu5Sent.getHeader().getAuthentification(), pdu5Sent.getStamp().getHeaderSeal()));

    assertTrue(cryptoUtils.verifySeal(pdu5Sent.getContent(), "content",
        (String) pdu5Sent.getHeader().getAuthentification(), pdu5Sent.getStamp().getContentSeal()));

    assertEquals(idu45Dto_e1.getMessage(), pdu5Sent.getContent());
  }


  public static void ct_pro_04_01(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    // Attendre que le message idu_5-6_ENV soit envoyé à la couche inférieure
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_04_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);


    environment.networkClient.request(idu45_e1);


    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient, atLeastOnce()).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    // Conserver la valeur de l’identifiant de idu_5-6_ENV
    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    String idOfPdu5Sent = pdu5Sent.getHeader().getId();

    // Envoyer par la couche inférieure idu_5-6_REC
    InterfaceDataUnit56Dto idu56Dto_e2 =
        TestInputs.make_ct_pro_04_01_e2(idOfPdu5Sent, environment.objectMapper);

    String idu56_e2 = environment.objectMapper.writeValueAsString(idu56Dto_e2);

    doReturn(idu56_e2).when(environment.dataLinkClient).confirm();


    // when
    // Une confirmation d’un envoi valide est traité correctement.
    environment.networkClient.confirm();


    // then
    // L’identifiant de idu_5-6_ENV n’est pas dans la Table des identifiants de message en attente
    // et aucune erreur ne survient au bout de trente secondes.
    assertFalse(environment.clientMessageIdSet.containsMessageId(idOfPdu5Sent));
  }


  public static void ct_pro_04_02(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    // Attendre que le message idu_5-6_ENV soit envoyé à la couche inférieure
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_04_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);


    environment.networkClient.request(idu45_e1);


    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient, atLeastOnce()).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    // Conserver la valeur de l’identifiant de idu_5-6_ENV
    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    String idOfPdu5Sent = pdu5Sent.getHeader().getId();

    // Envoyer par la couche inférieure idu_5-6_REC
    InterfaceDataUnit56Dto idu56Dto_e2 =
        TestInputs.make_ct_pro_04_02_e2(idOfPdu5Sent, environment.objectMapper);

    String idu56_e2 = environment.objectMapper.writeValueAsString(idu56Dto_e2);

    doReturn(idu56_e2).when(environment.dataLinkClient).confirm();


    // when
    // Une confirmation erronée non attendue est ignorée.
    environment.networkClient.confirm();


    // then
    // L’identifiant de idu_5-6_ENV est dans la Table des identifiants de message en attente
    // et aucune erreur ne survient au bout de trente secondes.
    assertTrue(environment.clientMessageIdSet.containsMessageId(idOfPdu5Sent));
  }


  public static void ct_pro_04_03(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    // Attendre que le message idu_5-6_ENV soit envoyé à la couche inférieure
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_04_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);


    environment.networkClient.request(idu45_e1);


    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient, atLeastOnce()).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    // Conserver la valeur de l’identifiant de idu_5-6_ENV
    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    String idOfPdu5Sent = pdu5Sent.getHeader().getId();

    // Envoyer par la couche inférieure idu_5-6_REC
    InterfaceDataUnit56Dto idu56Dto_e2 =
        TestInputs.make_ct_pro_04_03_e2(idOfPdu5Sent, environment.objectMapper);

    String idu56_e2 = environment.objectMapper.writeValueAsString(idu56Dto_e2);

    doReturn(idu56_e2).when(environment.dataLinkClient).confirm();


    // when
    // Une confirmation sans le type de message attendu est ignorée.
    environment.networkClient.confirm();


    // then
    // L’identifiant de idu_5-6_ENV est dans la Table des identifiants de message en attente
    // et aucune erreur ne survient au bout de trente secondes.
    assertTrue(environment.clientMessageIdSet.containsMessageId(idOfPdu5Sent));
  }


  public static void ct_pro_04_04(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    // Attendre que le message idu_5-6_ENV soit envoyé à la couche inférieure
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_04_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);


    environment.networkClient.request(idu45_e1);


    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient, atLeastOnce()).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    // Conserver la valeur de l’identifiant de idu_5-6_ENV
    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    String idOfPdu5Sent = pdu5Sent.getHeader().getId();

    // Envoyer par la couche inférieure idu_5-6_REC
    InterfaceDataUnit56Dto idu56Dto_e2 =
        TestInputs.make_ct_pro_04_04_e2(idOfPdu5Sent, environment.objectMapper);

    String idu56_e2 = environment.objectMapper.writeValueAsString(idu56Dto_e2);

    doReturn(idu56_e2).when(environment.dataLinkClient).confirm();


    // when
    // Une confirmation sans certificat valide de l’envoyeur de la confirmation est ignorée.
    environment.networkClient.confirm();


    // then
    // L’identifiant de idu_5-6_ENV est dans la Table des identifiants de message en attente
    // et aucune erreur ne survient au bout de trente secondes.
    assertTrue(environment.clientMessageIdSet.containsMessageId(idOfPdu5Sent));
  }



  public static void ct_pro_04_05(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkClient,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche supérieure idu_4-5
    // Attendre que le message idu_5-6_ENV soit envoyé à la couche inférieure
    InterfaceDataUnit45Dto idu45Dto_e1 = TestInputs.make_ct_pro_04_e1();

    String idu45_e1 = environment.objectMapper.writeValueAsString(idu45Dto_e1);


    environment.networkClient.request(idu45_e1);


    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkClient, atLeastOnce()).request(idu56Captor.capture());

    String idu56Sent = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56SentDto =
        environment.objectMapper.readValue(idu56Sent, InterfaceDataUnit56Dto.class);

    // Conserver la valeur de l’identifiant de idu_5-6_ENV
    ProtocolDataUnit5Dto pdu5Sent =
        environment.objectMapper.readValue(idu56SentDto.getMessage(), ProtocolDataUnit5Dto.class);

    String idOfPdu5Sent = pdu5Sent.getHeader().getId();

    // Envoyer par la couche inférieure idu_5-6_REC
    InterfaceDataUnit56Dto idu56Dto_e2 =
        TestInputs.make_ct_pro_04_05_e2(idOfPdu5Sent, environment.objectMapper);

    String idu56_e2 = environment.objectMapper.writeValueAsString(idu56Dto_e2);

    doReturn(idu56_e2).when(environment.dataLinkClient).confirm();


    // when
    // Une confirmation sans signature valide est ignorée.
    environment.networkClient.confirm();


    // then
    // L’identifiant de idu_5-6_ENV est dans la Table des identifiants de message en attente
    // et aucune erreur ne survient au bout de trente secondes.
    assertTrue(environment.clientMessageIdSet.containsMessageId(idOfPdu5Sent));
  }


  public static void ct_pro_05_01(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche inférieure idu_5-6_ENV
    InterfaceDataUnit56Dto idu56Dto_e1 = TestInputs.make_ct_pro_05_01_e1(environment.objectMapper);

    String idu56_e1 = environment.objectMapper.writeValueAsString(idu56Dto_e1);

    doReturn(idu56_e1).when(environment.dataLinkServer).indication();


    // when
    // Un envoi valide provoque une réponse valide de la part du destinataire et
    // une transmission du message vers la couche supérieure
    String idu45_s2 = environment.networkServer.indication();


    // then
    // Le message idu_5-6_REC a bien été envoyé.
    // Le message idu_5-6_REC est valide.
    ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    verify(environment.dataLinkServer, atLeastOnce()).response(idu56Captor.capture());

    String idu56_s1 = idu56Captor.getValue();

    InterfaceDataUnit56Dto idu56Dto_s1 =
        environment.objectMapper.readValue(idu56_s1, InterfaceDataUnit56Dto.class);

    ProtocolDataUnit5Dto pdu5Dto_e1 =
        environment.objectMapper.readValue(idu56Dto_e1.getMessage(), ProtocolDataUnit5Dto.class);

    assertEquals(pdu5Dto_e1.getHeader().getSourceIri(),
        idu56Dto_s1.getContext().getDestinationIri());

    assertEquals(idu56Dto_e1.getContext().getTrackingNumber(),
        idu56Dto_s1.getContext().getTrackingNumber());


    ProtocolDataUnit5Dto pdu5Dto_s1 =
        environment.objectMapper.readValue(idu56Dto_s1.getMessage(), ProtocolDataUnit5Dto.class);

    assertEquals(HeaderDto.Msgtype.RES_REC, pdu5Dto_s1.getHeader().getMsgtype());

    assertEquals(pdu5Dto_e1.getHeader().getSourceIri(), pdu5Dto_s1.getHeader().getSourceIri());

    assertEquals(pdu5Dto_e1.getHeader().getDestinationIri(),
        pdu5Dto_s1.getHeader().getDestinationIri());

    assertNotNull(pdu5Dto_s1.getHeader().getAuthentification());
    assertTrue(cryptoUtils.verifyCertificate((String) pdu5Dto_s1.getHeader().getAuthentification(),
        pdu5Dto_e1.getHeader().getDestinationIri()));

    assertFalse((Boolean) pdu5Dto_s1.getHeader().getParameters());

    assertEquals(environment.spedsVersion, pdu5Dto_s1.getHeader().getSpeds().getVersion());

    assertEquals(environment.spedsReference, pdu5Dto_s1.getHeader().getSpeds().getReference());

    assertTrue(cryptoUtils.verifySeal(pdu5Dto_s1.getHeader(), "header",
        (String) pdu5Dto_s1.getHeader().getAuthentification(),
        pdu5Dto_s1.getStamp().getHeaderSeal()));

    assertEquals("", pdu5Dto_s1.getContent());


    // Le message idu_4-5 a bien été envoyé
    // Le message idu_4-5 est valide
    InterfaceDataUnit45Dto idu45Dto_s2 =
        environment.objectMapper.readValue(idu45_s2, InterfaceDataUnit45Dto.class);

    assertEquals(pdu5Dto_e1.getHeader().getSourceIri(), idu45Dto_s2.getContext().getSourceIri());

    assertEquals(pdu5Dto_e1.getHeader().getDestinationIri(),
        idu45Dto_s2.getContext().getDestinationIri());

    assertEquals(pdu5Dto_e1.getHeader().getId(),
        idu45Dto_s2.getContext().getTrackingNumber().toString());

    assertEquals(pdu5Dto_e1.getContent(), idu45Dto_s2.getMessage());
  }


  public static void ct_pro_05_02(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche inférieure idu_5-6_ENV
    InterfaceDataUnit56Dto idu56Dto_e1 = TestInputs.make_ct_pro_05_02_e1(environment.objectMapper);

    String idu56_e1 = environment.objectMapper.writeValueAsString(idu56Dto_e1);

    doReturn(idu56_e1).when(environment.dataLinkServer).indication();


    // when
    // Un envoi sans le type de message attendu est ignoré
    String idu45_s2 = environment.networkServer.indication();


    // then
    // Au bout de trente secondes, aucun message n’est transmis et aucune erreur ne survient.
    assertEquals("", idu45_s2);
  }


  public static void ct_pro_05_03(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche inférieure idu_5-6_ENV
    InterfaceDataUnit56Dto idu56Dto_e1 = TestInputs.make_ct_pro_05_03_e1(environment.objectMapper);

    String idu56_e1 = environment.objectMapper.writeValueAsString(idu56Dto_e1);

    doReturn(idu56_e1).when(environment.dataLinkServer).indication();


    // when
    // Un envoi sans certificat valide est ignoré
    String idu45_s2 = environment.networkServer.indication();


    // then
    // Au bout de trente secondes, aucun message n’est transmis et aucune erreur ne survient.
    assertEquals("", idu45_s2);
  }


  public static void ct_pro_05_04(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche inférieure idu_5-6_ENV
    InterfaceDataUnit56Dto idu56Dto_e1 = TestInputs.make_ct_pro_05_04_e1(environment.objectMapper);

    String idu56_e1 = environment.objectMapper.writeValueAsString(idu56Dto_e1);

    doReturn(idu56_e1).when(environment.dataLinkServer).indication();


    // when
    // Un envoi sans signature d’entête valide est ignoré
    String idu45_s2 = environment.networkServer.indication();


    // then
    // Au bout de trente secondes, aucun message n’est transmis et aucune erreur ne survient.
    assertEquals("", idu45_s2);
  }


  public static void ct_pro_05_05(Environment environment) throws JsonProcessingException {
    // antécédent
    assertNotNull(environment.networkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte réseau' non null");

    assertNotNull(environment.dataLinkServer,
        "ct_pro_03_01 - l'antécédent requiert un 'hôte liaison' non null");

    // given
    // Envoyer par la couche inférieure idu_5-6_ENV
    InterfaceDataUnit56Dto idu56Dto_e1 = TestInputs.make_ct_pro_05_05_e1(environment.objectMapper);

    String idu56_e1 = environment.objectMapper.writeValueAsString(idu56Dto_e1);

    doReturn(idu56_e1).when(environment.dataLinkServer).indication();


    // when
    // Un envoi sans signature de contenu valide est ignoré
    String idu45_s2 = environment.networkServer.indication();


    // then
    // Au bout de trente secondes, aucun message n’est transmis et aucune erreur ne survient.
    assertEquals("", idu45_s2);
  }

}
