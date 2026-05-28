package ca.griis.speds.network.integration;

import static java.lang.Boolean.FALSE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.network.util.X509CertificateCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.mockito.ArgumentCaptor;

public class Cases {
  private static final String SOURCE_IRI = "https://source.iri";
  private static final String DESTINATION_IRI = "https://destination.iri";
  private static final String MESSAGE = "Message from transport layer";

  public static void ct_01(Environment environment) throws JsonProcessingException {
    // Given
    final String e1 =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(new InterfaceDataUnit56Dto(
            new Context56Dto(DESTINATION_IRI, "transfer", Context56Dto.ServicePrimitive.CONFIRM,
                false),
            "SUCCEED"));

    // When
    environment.getLinkHost().submitIdu(e1);

    // Then: aucun message vers la couche supérieure
    assertNull(environment.getClientResult());
    assertNull(environment.getServerResult());

  }

  public static void ct_02(Environment environment)
      throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

    final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
    // entrées
    final String e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(
                SOURCE_IRI,
                DESTINATION_IRI,
                "transfer",
                Context45Dto.ServicePrimitive.REQUEST,
                false),
            MESSAGE));

    final String e2 = mapper.writeValueAsString(
        new InterfaceDataUnit56Dto(
            new Context56Dto(
                DESTINATION_IRI,
                "transfer",
                Context56Dto.ServicePrimitive.CONFIRM,
                false),
            "SUCCEED"));

    // liaison : renvoie e2
    when(environment.getLinkHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(e2)));

    final ArgumentCaptor<String> idu56Captor = ArgumentCaptor.forClass(String.class);

    // appel pour retourner s2
    final CompletableFuture<Optional<String>> future = environment.getClient().submitIdu(e1);

    // S1
    await()
        .atMost(2, SECONDS)
        .untilAsserted(() -> verify(environment.getLinkHost(), atLeastOnce())
            .submitIdu(idu56Captor.capture()));

    final String s1 = idu56Captor.getValue();

    // S2
    final String s2 = future.get(2, SECONDS).orElseThrow();

    // verification sortie 1
    JsonNode actualIdu56 = mapper.readTree(s1);
    JsonNode actualPdu = mapper.readTree(actualIdu56.get("message").asText());

    assertEquals("transfer", actualIdu56.get("context").get("service").asText());
    assertEquals("request", actualIdu56.get("context").get("service_primitive").asText());
    assertEquals(DESTINATION_IRI, actualIdu56.get("context").get("destination_iri").asText());

    assertEquals(MESSAGE, actualPdu.get("content").asText());
    assertEquals("RES.MSG.ENV", actualPdu.get("header").get("msgtype").asText());
    assertEquals(SOURCE_IRI, actualPdu.get("header").get("source_iri").asText());
    assertEquals(DESTINATION_IRI, actualPdu.get("header").get("destination_iri").asText());

    // verification sortie 2
    final InterfaceDataUnit45Dto actualS2 = mapper.readValue(s2, InterfaceDataUnit45Dto.class);

    assertEquals("transfer", actualS2.getContext().getService());
    assertEquals(Context45Dto.ServicePrimitive.CONFIRM,
        actualS2.getContext().getServicePrimitive());
    assertEquals(SOURCE_IRI, actualS2.getContext().getSourceIri());
    assertEquals(DESTINATION_IRI, actualS2.getContext().getDestinationIri());

    assertEquals("SUCCEED", actualS2.getMessage());
  }

  public static void ct_03(Environment environment) throws Exception {
    final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
    // entrées IDU(5-6)
    KeyPair key = X509CertificateCreator.generateRsaKeyPair(4096);
    X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=host1.ca",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");

    final String certB64 = Base64.getEncoder().encodeToString(cert.getEncoded());
    final HeaderDto header_e1 =
        new HeaderDto(HeaderDto.Msgtype.RES_MSG_ENV, environment.getMessageId(),
            "https://host1.ca", DESTINATION_IRI, certB64, FALSE,
            new VersionDto(environment.getVersion(), environment.getReference()));
    final String serializedHeader_e1 =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(header_e1);

    final String headerSeal_e1 = Base64.getEncoder()
        .encodeToString(environment.getCryptographyService().hash(
            SpedsConfigItemDto.SpedsLayer.NETWORK,
            serializedHeader_e1.getBytes(StandardCharsets.UTF_8)));

    final String content_e1 = "Some";
    final String contentSeal_e1 = Base64.getEncoder()
        .encodeToString(environment.getCryptographyService().hash(
            SpedsConfigItemDto.SpedsLayer.NETWORK,
            content_e1.getBytes(StandardCharsets.UTF_8)));

    final String msgRec = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new ProtocolDataUnit5NETDto(header_e1,
            new StampDto(headerSeal_e1, contentSeal_e1), content_e1));

    final String given_e1 = SharedObjectMapper.getInstance().getMapper().writeValueAsString(
        new InterfaceDataUnit56Dto(new Context56Dto(DESTINATION_IRI, "transfer",
            Context56Dto.ServicePrimitive.INDICATION, false), msgRec));

    // la liaison accepte la réponse envoyée par PRO-3
    when(environment.getLinkHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    final ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);


    // When: PRO-3 (notify IDU56 indication)
    assertDoesNotThrow(() -> environment.getHostEventConsumer().notifyIdu(given_e1));

    // Then: pas d’IDU45 vers la couche sup
    assertNull(environment.getClientResult());
    assertNull(environment.getServerResult());

    // Then: IDU56 RESPONSE vers la couche liaison
    await().atMost(2, SECONDS)
        .untilAsserted(
            () -> verify(environment.getLinkHost(), atLeastOnce()).submitIdu(cap.capture()));

    InterfaceDataUnit56Dto resp = mapper.readValue(cap.getValue(), InterfaceDataUnit56Dto.class);

    assertEquals("transfer", resp.getContext().getService());
    assertEquals(Context56Dto.ServicePrimitive.RESPONSE, resp.getContext().getServicePrimitive());
    assertEquals("https://host1.ca", resp.getContext().getDestinationIri());
    assertEquals("FAILED: Invalid Header Seal", resp.getMessage());
  }

  public static void ct_04(Environment environment) throws Exception {
    final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
    final String sourceIri = "https://host1.ca";

    KeyPair key = X509CertificateCreator.generateRsaKeyPair(4096);
    X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=host1.ca",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");

    final String certB64 = Base64.getEncoder().encodeToString(cert.getEncoded());

    final HeaderDto header = new HeaderDto(
        HeaderDto.Msgtype.RES_MSG_ENV,
        environment.getMessageId(),
        sourceIri,
        DESTINATION_IRI,
        certB64,
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serializedHeader = mapper.writeValueAsString(header);

    // header_seal valide
    byte[] headerHash = environment.getCryptographyService().hash(
        SpedsConfigItemDto.SpedsLayer.NETWORK,
        serializedHeader.getBytes(StandardCharsets.UTF_8));

    String headerSealB64 = Base64.getEncoder().encodeToString(
        signHash(environment, headerHash, key.getPrivate()));

    // content_seal invalide
    final String content = "Some";
    byte[] otherHash = environment.getCryptographyService().hash(
        SpedsConfigItemDto.SpedsLayer.NETWORK,
        "OtherContent".getBytes(StandardCharsets.UTF_8));

    String contentSealB64 = Base64.getEncoder().encodeToString(
        signHash(environment, otherHash, key.getPrivate()));

    // PDU + IDU56
    final String msgRec = mapper.writeValueAsString(
        new ProtocolDataUnit5NETDto(
            header,
            new StampDto(headerSealB64, contentSealB64),
            content));

    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit56Dto(
            new Context56Dto(DESTINATION_IRI, "transfer", Context56Dto.ServicePrimitive.INDICATION,
                false),
            msgRec));

    // la liaison accepte la réponse
    reset(environment.getLinkHost());
    when(environment.getLinkHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    environment.setClientResult(null);
    environment.setServerResult(null);

    final ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);

    // When: PRO-3
    assertDoesNotThrow(() -> environment.getHostEventConsumer().notifyIdu(given_e1));

    // Then
    await().atMost(2, SECONDS)
        .untilAsserted(() -> verify(environment.getLinkHost()).submitIdu(cap.capture()));

    InterfaceDataUnit56Dto resp = mapper.readValue(cap.getValue(), InterfaceDataUnit56Dto.class);

    assertEquals("transfer", resp.getContext().getService());
    assertEquals(Context56Dto.ServicePrimitive.RESPONSE, resp.getContext().getServicePrimitive());
    assertEquals(sourceIri, resp.getContext().getDestinationIri());
    assertEquals("FAILED: Invalid Content Seal", resp.getMessage());
  }


  public static void ct_05(Environment environment) throws Exception {
    final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();

    final Map<String, String> options = Map.of("TN", UUID.randomUUID().toString());
    final String e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(
                "https://host1.ca",
                DESTINATION_IRI,
                "transfer",
                Context45Dto.ServicePrimitive.RESPONSE,
                options),
            "FAILED"));

    reset(environment.getLinkHost());
    when(environment.getLinkHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    assertDoesNotThrow(() -> environment.getClient().submitIdu(e1).get(2, SECONDS));

    assertNull(environment.getClientResult());
    assertNull(environment.getServerResult());
  }

  public static void ct_06(Environment environment) throws Exception {
    final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();

    // entrée 1 - IDU56 indiction
    final String sourceIri = "https://host1.ca";

    KeyPair key = X509CertificateCreator.generateRsaKeyPair(4096);
    X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=host1.ca",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");

    final String certB64 = Base64.getEncoder().encodeToString(cert.getEncoded());
    final UUID msgId = UUID.randomUUID();
    final HeaderDto header = new HeaderDto(
        HeaderDto.Msgtype.RES_MSG_ENV,
        msgId,
        sourceIri,
        DESTINATION_IRI,
        certB64,
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serializedHeader = mapper.writeValueAsString(header);

    // header_seal valide
    byte[] headerHash = environment.getCryptographyService().hash(
        SpedsConfigItemDto.SpedsLayer.NETWORK,
        serializedHeader.getBytes(StandardCharsets.UTF_8));

    String headerSealB64 = Base64.getEncoder().encodeToString(
        signHash(environment, headerHash, key.getPrivate()));

    // content_seal valide
    final String content = "Some";
    byte[] contentHash = environment.getCryptographyService().hash(
        SpedsConfigItemDto.SpedsLayer.NETWORK,
        content.getBytes(StandardCharsets.UTF_8));

    String contentSealB64 = Base64.getEncoder().encodeToString(
        signHash(environment, contentHash, key.getPrivate()));

    // PDU + IDU56
    final String msgRec = mapper.writeValueAsString(
        new ProtocolDataUnit5NETDto(
            header,
            new StampDto(headerSealB64, contentSealB64),
            content));

    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit56Dto(
            new Context56Dto(DESTINATION_IRI, "transfer", Context56Dto.ServicePrimitive.INDICATION,
                false),
            msgRec));
    // clean

    environment.setClientResult(null);
    environment.setServerResult(null);
    reset(environment.getLinkHost());
    when(environment.getLinkHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));


    final ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);

    // When: PRO-3
    assertDoesNotThrow(() -> environment.getHostEventConsumer().notifyIdu(given_e1));
    // sortie 1
    await().atMost(2, SECONDS).untilAsserted(() -> {
      assertTrue(
          environment.getClientResult() != null || environment.getServerResult() != null,
          "Sortie 1 (IDU45) non capturée");
    });

    final String out1 = (environment.getClientResult() != null)
        ? environment.getClientResult()
        : environment.getServerResult();
    // s1
    final InterfaceDataUnit45Dto s1 = mapper.readValue(out1, InterfaceDataUnit45Dto.class);

    assertEquals("transfer", s1.getContext().getService());
    assertEquals(Context45Dto.ServicePrimitive.INDICATION, s1.getContext().getServicePrimitive());
    assertEquals(sourceIri, s1.getContext().getSourceIri());
    assertEquals(DESTINATION_IRI, s1.getContext().getDestinationIri());
    assertEquals(content, s1.getMessage());

    // entreé 2 - IDU45 response
    final Map<String, String> options = Map.of("TN", msgId.toString(), "IRI", DESTINATION_IRI);
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(
                sourceIri,
                DESTINATION_IRI,
                "transfer",
                Context45Dto.ServicePrimitive.RESPONSE,
                options),
            "SUCCED"));
    // when PRO-04
    assertDoesNotThrow(() -> environment.getServer().submitIdu(given_e2).get(2, SECONDS));

    // sorite 2 -IDU56 response
    await().atMost(2, SECONDS)
        .untilAsserted(() -> verify(environment.getLinkHost()).submitIdu(cap.capture()));
    // s2
    final InterfaceDataUnit56Dto s2 =
        mapper.readValue(cap.getValue(), InterfaceDataUnit56Dto.class);

    assertEquals("transfer", s2.getContext().getService());
    assertEquals(Context56Dto.ServicePrimitive.RESPONSE, s2.getContext().getServicePrimitive());
    assertEquals(sourceIri, s2.getContext().getDestinationIri());
    assertEquals("SUCCED", s2.getMessage());
  }

  private static byte[] signHash(Environment env, byte[] hash, PrivateKey privateKey) {
    return env.getCryptographyService().sign(SpedsConfigItemDto.SpedsLayer.NETWORK, privateKey,
        hash);
  }

}
