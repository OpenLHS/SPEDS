package ca.griis.speds.session.integration;


import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.speds.session.integration.Scenario.BAD_SDEK;
import static ca.griis.speds.session.integration.Scenario.BAD_STAMP;
import static ca.griis.speds.session.integration.Scenario.BAD_TOKEN;
import static ca.griis.speds.session.integration.Scenario.ILLEGITIMATE;
import static ca.griis.speds.session.integration.Scenario.NO_SESSION;
import static ca.griis.speds.session.integration.Scenario.SUCCESS;
import static ca.griis.speds.session.integration.SessionInformationField.client;
import static ca.griis.speds.session.integration.SessionInformationField.server;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.ProtocolDataUnit3SESDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.SesPubEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.crypto.SecretKey;
import org.mockito.ArgumentCaptor;


/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public class Cases {
  private static final String sourceCode = "source_code";
  private static final String sourceCode2 = "source_code2";
  private static final String destinationCode = "destination_code";
  private static final String sourceIri = "https://source.iri:8080";
  private static final String sourceIri2 = "https://source2.iri:8080";
  private static final String destinationIri = "https://destination.iri::8081";
  private static final String message = "Message from the PRE layer";

  private static final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();

  public static String ct_01(Environment environment, Scenario scenario, Initiator initiator)
      throws Exception {
    final SessionHost sessionHost = switch (initiator) {
      case INITIATOR_1 -> environment.initiatrice;
      case INITIATOR_2 -> environment.initiatrice2;
    };

    final TransportHost transportHost = switch (initiator) {
      case INITIATOR_1 -> environment.getClientTransportHost();
      case INITIATOR_2 -> environment.getClientTransportHost2();
    };

    final String sourceCodeValue = switch (initiator) {
      case INITIATOR_1 -> sourceCode;
      case INITIATOR_2 -> sourceCode2;
    };

    final String sourceIriValue = switch (initiator) {
      case INITIATOR_1 -> sourceIri;
      case INITIATOR_2 -> sourceIri2;
    };

    final String sdek = Base64.getEncoder().encodeToString(
        environment.getCryptographyService()
            .generateSymmetricKey(SESSION)
            .getEncoded());

    // ct_01_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit23Dto(
            new Context23Dto(
                environment.getPga(),
                sourceCodeValue,
                destinationCode,
                sdek,
                Context23Dto.Service.DELEGATE,
                Context23Dto.ServicePrimitive.REQUEST,
                false),
            message));

    // ct_01_e2
    final String transportConfirmMessage =
        scenario == SUCCESS ? "SUCCEED" : "FAILED";

    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCodeValue,
                destinationCode,
                sourceIriValue,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri,
                Map.of("TN", UUID.randomUUID().toString())),
            transportConfirmMessage));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    when(transportHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    final CompletableFuture<Optional<String>> future = sessionHost.submitIdu(given_e1);

    // ct_01_s1
    final Optional<String> maybeConfirm = future.get();
    assertTrue(maybeConfirm.isPresent(), "ct_01_s1 devrait être retourné");

    final InterfaceDataUnit23Dto actual_s1 =
        mapper.readValue(maybeConfirm.get(), InterfaceDataUnit23Dto.class);

    assertEquals(Context23Dto.Service.DELEGATE, actual_s1.getContext().getService());
    assertEquals(Context23Dto.ServicePrimitive.CONFIRM,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(sourceCodeValue, actual_s1.getContext().getSourceCode());
    assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
    assertEquals(environment.getPga(), actual_s1.getContext().getPga());
    assertEquals(sdek, actual_s1.getContext().getSdek());
    assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));

    // ct_01_s2
    verify(transportHost, atLeastOnce()).submitIdu(iduCaptor.capture());

    final var sentIdus = iduCaptor.getAllValues();
    assertFalse(sentIdus.isEmpty(), "ct_01_s2 devrait être envoyé vers la couche transport");

    final String actualSiduJson = sentIdus.get(sentIdus.size() - 1);

    final InterfaceDataUnit34Dto actual_s2 =
        mapper.readValue(actualSiduJson, InterfaceDataUnit34Dto.class);

    assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.REQUEST,
        actual_s2.getContext().getServicePrimitive());
    assertEquals(sourceCodeValue, actual_s2.getContext().getSourceCode());
    assertEquals(destinationCode, actual_s2.getContext().getDestinationCode());
    assertEquals(sourceIriValue, actual_s2.getContext().getSourceIri());
    assertEquals(destinationIri, actual_s2.getContext().getDestinationIri());

    final ProtocolDataUnit3SESDto actualSpdu =
        mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

    assertEquals(HeaderDto.Msgtype.SES_PUB_ENV, actualSpdu.getHeader().getMsgtype());
    assertNotNull(actualSpdu.getHeader().getId());
    assertEquals("0", actualSpdu.getStamp());

    final SesPubEnvDto actualSdu =
        mapper.readValue((String) actualSpdu.getContent(), SesPubEnvDto.class);

    final String expectedPubKey = Base64.getEncoder().encodeToString(
        environment.clientParameters
            .certificatePrivateKeysEntry()
            .getCertficate()
            .getPublicKey()
            .getEncoded());

    assertEquals(expectedPubKey, actualSdu.getContent());
    assertNotNull(actualSdu.getSession());

    final Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(sessionHost, client);

    if (scenario == SUCCESS) {
      assertEquals(1, sessions.size(), "Une session doit être créée et conservée");

      final SessionInformation currentSession = sessions.values().stream()
          .filter(s -> s.sessionId().id().equals(actualSdu.getSession()))
          .findFirst()
          .orElseThrow(() -> new AssertionError("La session créée est introuvable"));

      assertEquals(actualSdu.getSession(), currentSession.sessionId().id());
      assertEquals(sourceCodeValue, currentSession.initiatorId());
      assertEquals(destinationCode, currentSession.peerId());
      assertEquals(sourceIriValue, currentSession.initiatorIri());
      assertEquals(destinationIri, currentSession.peerIri());
      assertEquals(environment.getPga(), currentSession.pgaId());
      assertEquals(
          sdek,
          Base64.getEncoder().encodeToString(currentSession.sdek().getEncoded()));
      assertEquals(
          expectedPubKey,
          Base64.getEncoder().encodeToString(currentSession.initiatorPubKey().getEncoded()));
      assertEquals(0, currentSession.numberOfMessage());

      return actualSiduJson;
    } else {
      assertTrue(
          sessions.isEmpty()
              || sessions.values().stream()
                  .noneMatch(s -> s.sessionId().id().equals(actualSdu.getSession())),
          "Aucune session associée ne doit être conservée en cas d'échec du confirm transport");

      return null;
    }
  }

  public static String ct_02(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;

    assertNotNull(triggerMsg, "ct_02 requiert le triggerMsg de ct_01");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesPubEnvDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesPubEnvDto.class);

    final UUID sessionId = previousSdu.getSession();
    final String initiatorPubKey = previousSdu.getContent();

    final HeaderDto headerDto = new HeaderDto(
        HeaderDto.Msgtype.SES_PUB_ENV,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String inputStamp = success ? previousSpdu.getStamp() : "badstamp";

    final String spdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            headerDto,
            inputStamp,
            mapper.writeValueAsString(new SesPubEnvDto(initiatorPubKey, sessionId))));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_02_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                previousIdu.getContext().getSourceCode(),
                destinationCode,
                previousIdu.getContext().getSourceIri(),
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options),
            spdu));

    // ct_02_e2
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                previousIdu.getContext().getSourceCode(),
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                previousIdu.getContext().getSourceIri(),
                Map.of("TN", tn)),
            "SUCCEED"));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    environment.destinationNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    final var sentIdus = iduCaptor.getAllValues();
    assertFalse(sentIdus.isEmpty(), "Aucune sortie vers le transport n’a été capturée");

    InterfaceDataUnit34Dto actual_s1 = null;
    InterfaceDataUnit34Dto actual_s2 = null;

    for (String raw : sentIdus) {
      InterfaceDataUnit34Dto idu = mapper.readValue(raw, InterfaceDataUnit34Dto.class);

      if (idu.getContext().getService() == Context34Dto.Service.TRANSFER
          && idu.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE) {
        actual_s1 = idu;
        continue;
      }

      if (idu.getContext().getService() == Context34Dto.Service.TRANSFER
          && idu.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST) {
        try {
          ProtocolDataUnit3SESDto spduOut =
              mapper.readValue(idu.getMessage(), ProtocolDataUnit3SESDto.class);
          if (spduOut.getHeader().getMsgtype() == HeaderDto.Msgtype.SES_PUB_REC) {
            actual_s2 = idu;
          }
        } catch (Exception ignored) {
          assertNotNull(ignored);
        }
      }
    }

    if (success) {
      assertNotNull(actual_s1, "ct_02_s1 (TRANSFER RESPONSE) devrait être émis");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
          actual_s1.getContext().getServicePrimitive());
      assertEquals(previousIdu.getContext().getSourceCode(),
          actual_s1.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
      assertEquals(previousIdu.getContext().getSourceIri(), actual_s1.getContext().getSourceIri());
      assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));

      assertNotNull(actual_s2, "ct_02_s2 (SES.PUB.REC) devrait être émis");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
      assertEquals(previousIdu.getContext().getSourceCode(),
          actual_s2.getContext().getDestinationCode());
      assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
      assertEquals(previousIdu.getContext().getSourceIri(),
          actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_PUB_REC, actualSpdu.getHeader().getMsgtype());
      assertEquals(previousSpdu.getHeader().getId(), actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      final String actualContent = mapper.readValue(
          mapper.writeValueAsString(actualSpdu.getContent()),
          String.class);
      assertEquals(sessionId.toString(), actualContent);

      Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(environment.partenaire, server);

      assertFalse(sessions.isEmpty(),
          "Une session devrait être créée côté partenaire en cas de succès");

      SessionInformation currentSession = sessions.values().stream()
          .filter(s -> s.sessionId().id().equals(sessionId))
          .findFirst()
          .orElseThrow(() -> new AssertionError(
              "La session créée pour le sessionId attendu est introuvable"));

      assertEquals(sessionId, currentSession.sessionId().id());
      assertEquals(previousIdu.getContext().getSourceCode(), currentSession.initiatorId());
      assertEquals(previousIdu.getContext().getSourceIri(), currentSession.initiatorIri());
      assertEquals(destinationCode, currentSession.peerId());
      assertEquals(destinationIri, currentSession.peerIri());

      assertEquals(
          initiatorPubKey,
          Base64.getEncoder().encodeToString(currentSession.initiatorPubKey().getEncoded()));

      return mapper.writeValueAsString(actual_s2);

    } else {
      assertNotNull(actual_s1, "ct_02_s1 FAILED devrait être émis");
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_02_s2 ne doit pas être émis en cas d’erreur");

      Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(environment.partenaire, server);

      assertTrue(
          sessions.isEmpty()
              || sessions.values().stream().noneMatch(s -> s.sessionId().id().equals(sessionId)),
          "Aucune session ne doit être conservée en cas d’erreur");

      return null;
    }
  }

  public static String ct_03(Environment environment, Scenario scenario, String triggerMsg,
      Initiator initiator) throws Exception {
    final SessionHost sessionHost = switch (initiator) {
      case INITIATOR_1 -> environment.initiatrice;
      case INITIATOR_2 -> environment.initiatrice2;
    };

    final TransportHost transportHost = switch (initiator) {
      case INITIATOR_1 -> environment.getClientTransportHost();
      case INITIATOR_2 -> environment.getClientTransportHost2();
    };

    final TransportHostEvent transportHostEvent = switch (initiator) {
      case INITIATOR_1 -> environment.sourceNotifier;
      case INITIATOR_2 -> environment.sourceNotifier2;
    };

    final String sourceCodeValue = switch (initiator) {
      case INITIATOR_1 -> sourceCode;
      case INITIATOR_2 -> sourceCode2;
    };

    final String sourceIriValue = switch (initiator) {
      case INITIATOR_1 -> sourceIri;
      case INITIATOR_2 -> sourceIri2;
    };

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;


    SessionInformation currentSession = null;
    if (!noSession) {
      Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(sessionHost, client);

      assertEquals(1, sessions.size(), "Une session initiatrice doit exister avant ct_03");
      currentSession = sessions.values().iterator().next();
    }

    assertNotNull(triggerMsg, "ct_03 requiert le triggerMsg de ct_02");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    String content = mapper.readValue(
        mapper.writeValueAsString(previousSpdu.getContent()),
        String.class);

    if (noSession) {
      content = UUID.randomUUID().toString();
    }

    String stamp = previousSpdu.getStamp();

    if (badStamp) {
      stamp = "badstamp";
    } else if (noSession) {
      stamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().sign(
              SESSION,
              environment.serverParameters
                  .certificatePrivateKeysEntry()
                  .getPrivateKey(),
              content.getBytes(StandardCharsets.UTF_8)));
    }

    ProtocolDataUnit3SESDto inputSpdu = new ProtocolDataUnit3SESDto(
        previousSpdu.getHeader(),
        stamp,
        content);

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);
    // entrée 1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCodeValue,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                sourceIriValue,
                options),
            mapper.writeValueAsString(inputSpdu)));

    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCodeValue,
                destinationCode,
                sourceIriValue,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri,
                options),
            "SUCCEED"));

    reset(transportHost);

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    when(transportHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    transportHostEvent.notifyIdu(given_e1);

    // THEN
    verify(transportHost, atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_03_s2 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s2.getContext().getServicePrimitive());
    assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
    assertEquals(sourceCodeValue, actual_s2.getContext().getDestinationCode());
    assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
    assertEquals(sourceIriValue, actual_s2.getContext().getDestinationIri());

    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s2.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s1, "ct_03_s1 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s1.getContext().getServicePrimitive());
      assertEquals(sourceCodeValue, actual_s1.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
      assertEquals(sourceIriValue, actual_s1.getContext().getSourceIri());
      assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s1.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_SAK_ENV, actualSpdu.getHeader().getMsgtype());
      assertNotNull(actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getHeader().getVersion());
      assertNotNull(actualSpdu.getStamp());

      SesSakEnvDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesSakEnvDto.class);

      assertNotNull(actualSdu.getValue());
      assertFalse(actualSdu.getValue().isBlank());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id().toString(), actualSdu.getSession());

      return mapper.writeValueAsString(actual_s1);

    } else if (noSession) {
      assertTrue(actual_s2.getMessage().startsWith("FAILED"));
      assertNull(actual_s1, "ct_03_s1 ne doit pas être reçu");
      return null;

    } else {
      assertTrue(actual_s2.getMessage().startsWith("FAILED"));
      assertNull(actual_s1, "ct_03_s1 ne doit pas être reçu");
      return null;
    }
  }

  public static String ct_04(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;

    // Précondition
    // le partenaire doit déjà avoir une session locale

    assertNotNull(triggerMsg, "ct_04 requiert le triggerMsg de ct_03");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesSakEnvDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesSakEnvDto.class);

    SessionInformation currentSession = null;
    if (!noSession) {
      Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(environment.partenaire, server);
      assertFalse(sessions.isEmpty(), "Une session partenaire doit exister avant ct_04");
      currentSession = sessions.get(new SessionId(UUID.fromString(previousSdu.getSession())));
    }

    final String sessionValue = noSession
        ? UUID.randomUUID().toString()
        : currentSession.sessionId().id().toString();

    final String dhValue = previousSdu.getValue();

    final SesSakEnvDto inputSdu = new SesSakEnvDto(dhValue, sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_SAK_ENV,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      // Le SES.SAK.ENV
      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().sign(
              SESSION,
              environment.clientParameters
                  .certificatePrivateKeysEntry()
                  .getPrivateKey(),
              serialSdu.getBytes(StandardCharsets.UTF_8)));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_04_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                previousIdu.getContext().getSourceCode(),
                destinationCode,
                previousIdu.getContext().getSourceIri(),
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options),
            inputSpdu));

    // ct_04_e2
    // confirm pour l’envoi de SES.SAK.REC
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                previousIdu.getContext().getSourceCode(),
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                previousIdu.getContext().getSourceIri(),
                options),
            "SUCCEED"));

    reset(environment.getServerTransportHost());

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    environment.destinationNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_04_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(previousIdu.getContext().getSourceCode(), actual_s1.getContext().getSourceCode());
    assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
    assertEquals(previousIdu.getContext().getSourceIri(), actual_s1.getContext().getSourceIri());
    assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());

    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s2, "ct_04_s2 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
      assertEquals(previousIdu.getContext().getSourceCode(),
          actual_s2.getContext().getDestinationCode());
      assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
      assertEquals(previousIdu.getContext().getSourceIri(),
          actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_SAK_REC, actualSpdu.getHeader().getMsgtype());
      assertEquals(inputHeader.getId(), actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesSakRecDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesSakRecDto.class);

      assertNotNull(actualSdu.getValue());
      assertFalse(actualSdu.getValue().isBlank());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id().toString(), actualSdu.getSession().toString());

      Map<SessionId, SessionInformation> updatedSessions =
          Environment.getSessionInformation(environment.partenaire, server);

      SessionInformation updatedSession = updatedSessions.get(currentSession.sessionId());

      assertNotNull(updatedSession, "La session doit toujours exister");
      assertNotNull(updatedSession.skak(), "La clé skak doit être présente");

      return mapper.writeValueAsString(actual_s2);

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_04_s2 ne doit pas être reçu");
      return null;

    } else {
      // BAD_STAMP
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_04_s2 ne doit pas être reçu");
      return null;
    }
  }

  public static String ct_05(Environment environment, Scenario scenario, String triggerMsg,
      Initiator initiator) throws Exception {
    final SessionHost sessionHost = switch (initiator) {
      case INITIATOR_1 -> environment.initiatrice;
      case INITIATOR_2 -> environment.initiatrice2;
    };

    final TransportHost transportHost = switch (initiator) {
      case INITIATOR_1 -> environment.getClientTransportHost();
      case INITIATOR_2 -> environment.getClientTransportHost2();
    };

    final TransportHostEvent transportHostEvent = switch (initiator) {
      case INITIATOR_1 -> environment.sourceNotifier;
      case INITIATOR_2 -> environment.sourceNotifier2;
    };

    final String sourceCodeValue = switch (initiator) {
      case INITIATOR_1 -> sourceCode;
      case INITIATOR_2 -> sourceCode2;
    };

    final String sourceIriValue = switch (initiator) {
      case INITIATOR_1 -> sourceIri;
      case INITIATOR_2 -> sourceIri2;
    };

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;

    // Précondition
    // l’initiatrice doit déjà avoir une session
    SessionInformation currentSession = null;
    if (!noSession) {
      Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(sessionHost, client);

      assertEquals(1, sessions.size(), "Une session initiatrice doit exister avant ct_05");
      currentSession = sessions.values().iterator().next();
    }

    assertNotNull(triggerMsg, "ct_05 requiert le triggerMsg de ct_04");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesSakRecDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesSakRecDto.class);

    final String sessionValue = noSession
        ? UUID.randomUUID().toString()
        : currentSession.sessionId().id().toString();

    final String dhValue = previousSdu.getValue();
    final SesSakRecDto inputSdu = new SesSakRecDto(dhValue, UUID.fromString(sessionValue));

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_SAK_REC,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      // le SES.SAK.REC provient du partenaire
      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().sign(
              SESSION,
              environment.serverParameters
                  .certificatePrivateKeysEntry()
                  .getPrivateKey(),
              serialSdu.getBytes(StandardCharsets.UTF_8)));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_05_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCodeValue,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                sourceIriValue,
                options),
            inputSpdu));

    // ct_05_e2
    // confirm de l’envoi de SES.CLE.ENV
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCodeValue,
                destinationCode,
                sourceIriValue,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri,
                options),
            "SUCCEED"));

    reset(transportHost);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(transportHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    transportHostEvent.notifyIdu(given_e1);

    // THEN
    verify(transportHost, atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    // ct_05_s2
    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_05_s2 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s2.getContext().getServicePrimitive());
    assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
    assertEquals(sourceCodeValue, actual_s2.getContext().getDestinationCode());
    assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
    assertEquals(sourceIriValue, actual_s2.getContext().getDestinationIri());

    // ct_05_s1 : request SES.CLE.ENV
    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s2.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s1, "ct_05_s1 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s1.getContext().getServicePrimitive());
      assertEquals(sourceCodeValue, actual_s1.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
      assertEquals(sourceIriValue, actual_s1.getContext().getSourceIri());
      assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s1.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_CLE_ENV, actualSpdu.getHeader().getMsgtype());
      assertNotNull(actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesCleEnvDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesCleEnvDto.class);

      assertNotNull(actualSdu.getContent());
      assertFalse(actualSdu.getContent().isBlank());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id(), actualSdu.getSession());

      // validation du contenu chiffré avec la SKAK
      final Map<SessionId, SessionInformation> sessions =
          Environment.getSessionInformation(sessionHost, client);
      currentSession = sessions.values().iterator().next();
      assertNotNull(currentSession.skak(), "La clé SKAK doit avoir été établie");

      byte[] encryptedTransfer = Base64.getDecoder().decode(actualSdu.getContent());
      byte[] clearTransfer =
          environment.getCryptographyService().decryptSymmetric(
              SESSION,
              currentSession.skak(),
              encryptedTransfer);

      KeyTransferDto keyTransfer =
          mapper.readValue(clearTransfer, KeyTransferDto.class);

      String expectedSdek =
          Base64.getEncoder().encodeToString(currentSession.sdek().getEncoded());

      assertEquals(expectedSdek, keyTransfer.getSdek());
      assertEquals(currentSession.pgaId(), keyTransfer.getPgaNumber());
      assertNotNull(keyTransfer.getToken());

      Map<SessionId, SessionInformation> updatedSessions =
          Environment.getSessionInformation(sessionHost, client);

      SessionInformation updatedSession = updatedSessions.get(currentSession.sessionId());

      assertNotNull(updatedSession, "La session doit toujours exister");
      assertNotNull(updatedSession.skak(), "La clé skak doit être présente");

      return mapper.writeValueAsString(actual_s1);

    } else if (noSession) {
      assertTrue(actual_s2.getMessage().startsWith("FAILED"));
      assertNull(actual_s1, "ct_05_s1 ne doit pas être reçu");
      return null;

    } else {
      // BAD_STAMP
      assertTrue(actual_s2.getMessage().startsWith("FAILED"));
      assertNull(actual_s1, "ct_05_s1 ne doit pas être reçu");
      return null;
    }
  }

  public static String ct_06(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;
    final boolean illegitimate = scenario == ILLEGITIMATE;

    assertNotNull(triggerMsg, "ct_06 requiert le triggerMsg de ct_05");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesCleEnvDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesCleEnvDto.class);

    // Précondition
    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(environment.partenaire, server);

    SessionInformation currentSession = null;
    if (!noSession) {
      assertFalse(sessions.isEmpty(), "Une session partenaire doit exister avant ct_06");
      currentSession = sessions.get(new SessionId(previousSdu.getSession()));
      assertNotNull(currentSession.skak(), "La clé skak doit exister avant ct_06");
    }

    final UUID sessionValue = noSession
        ? UUID.randomUUID()
        : currentSession.sessionId().id();

    final String clefTrans = previousSdu.getContent();
    final SesCleEnvDto inputSdu = new SesCleEnvDto(clefTrans, sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_CLE_ENV,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else if (noSession) {
      inputStamp = "0";
    } else {
      assertNotNull(currentSession, "Une session est requise pour signer correctement");

      final byte[] serialSduHash =
          environment.getCryptographyService().hash(
              SESSION,
              serialSdu.getBytes(StandardCharsets.UTF_8));

      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().encryptSymmetric(
              SESSION,
              currentSession.skak(),
              serialSduHash));
    }
    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_06_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                previousIdu.getContext().getSourceCode(),
                destinationCode,
                previousIdu.getContext().getSourceIri(),
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options),
            inputSpdu));

    // ct_06_e2
    // confirm de l’envoi de SES.CLE.REC
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                previousIdu.getContext().getSourceCode(),
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                previousIdu.getContext().getSourceIri(),
                options),
            "SUCCEED"));

    reset(environment.getServerTransportHost());

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    environment.destinationNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_06_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(previousIdu.getContext().getSourceCode(), actual_s1.getContext().getSourceCode());
    assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
    assertEquals(previousIdu.getContext().getSourceIri(), actual_s1.getContext().getSourceIri());
    assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());

    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s2, "ct_06_s2 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
      assertEquals(previousIdu.getContext().getSourceCode(),
          actual_s2.getContext().getDestinationCode());
      assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
      assertEquals(previousIdu.getContext().getSourceIri(),
          actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_CLE_REC, actualSpdu.getHeader().getMsgtype());
      assertEquals(inputHeader.getId(), actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesCleRecDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesCleRecDto.class);

      assertNotNull(actualSdu.getContent());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id(), actualSdu.getSession());

      Map<SessionId, SessionInformation> updatedSessions =
          Environment.getSessionInformation(environment.partenaire, server);

      SessionInformation updatedSession = updatedSessions.get(currentSession.sessionId());

      assertNotNull(updatedSession, "La session doit toujours exister");
      assertEquals(environment.getPga(), updatedSession.pgaId());

      String updatedSdekBase64 =
          Base64.getEncoder().encodeToString(updatedSession.sdek().getEncoded());

      byte[] encryptedTransfer = Base64.getDecoder().decode(clefTrans);
      byte[] clearTransfer =
          environment.getCryptographyService().decryptSymmetric(
              SESSION,
              updatedSession.skak(),
              encryptedTransfer);

      KeyTransferDto keyTransfer =
          mapper.readValue(clearTransfer, KeyTransferDto.class);

      assertEquals(keyTransfer.getSdek(), updatedSdekBase64);
      assertEquals(keyTransfer.getPgaNumber(), updatedSession.pgaId());
      assertNotNull(keyTransfer.getToken());

      return mapper.writeValueAsString(actual_s2);

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_06_s2 ne doit pas être reçu");
      return null;

    } else if (badStamp) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_06_s2 ne doit pas être reçu");
      return null;

    } else if (illegitimate) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_06_s2 ne doit pas être reçu");
      return null;

    } else {
      fail("Scenario non supporté: " + scenario);
      return null;
    }
  }

  public static String ct_07(Environment environment, Scenario scenario, String triggerMsg,
      Initiator initiator) throws Exception {
    final SessionHost sessionHost = switch (initiator) {
      case INITIATOR_1 -> environment.initiatrice;
      case INITIATOR_2 -> environment.initiatrice2;
    };

    final TransportHost transportHost = switch (initiator) {
      case INITIATOR_1 -> environment.getClientTransportHost();
      case INITIATOR_2 -> environment.getClientTransportHost2();
    };

    final TransportHostEvent transportHostEvent = switch (initiator) {
      case INITIATOR_1 -> environment.sourceNotifier;
      case INITIATOR_2 -> environment.sourceNotifier2;
    };

    final String sourceCodeValue = switch (initiator) {
      case INITIATOR_1 -> sourceCode;
      case INITIATOR_2 -> sourceCode2;
    };

    final String sourceIriValue = switch (initiator) {
      case INITIATOR_1 -> sourceIri;
      case INITIATOR_2 -> sourceIri2;
    };

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;
    final boolean badSdek = scenario == BAD_SDEK;

    assertNotNull(triggerMsg, "ct_07 requiert le triggerMsg de ct_06");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    // Précondition
    // l’initiatrice doit déjà avoir une session valide
    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(sessionHost, client);

    assertEquals(1, sessions.size(), "Une session initiatrice doit exister avant ct_07");
    SessionInformation currentSession = sessions.values().iterator().next();

    assertNotNull(currentSession.skak(), "SKAK doit exister avant ct_07");
    assertNotNull(currentSession.sdek(), "SDEK doit exister avant ct_07");
    assertNotNull(currentSession.token(), "Le token doit exister avant ct_07");
    assertNotNull(currentSession.piduMessage(), "Le message initial doit exister avant ct_07");

    final SecretKey skak = currentSession.skak();
    final SecretKey sdek = currentSession.sdek();

    if (noSession) {
      sessions.remove(currentSession.sessionId());
    }

    final UUID sessionValue = currentSession.sessionId().id();
    final UUID tokenValue = currentSession.token();

    // contenu de SES.CLE.REC
    // token chiffré avec SDEK
    byte[] encryptedToken;
    if (badSdek) {
      SecretKey wrongSdek = environment.getCryptographyService().generateSymmetricKey(SESSION);
      encryptedToken = environment.getCryptographyService().encryptSymmetric(
          SESSION,
          wrongSdek,
          tokenValue.toString().getBytes(StandardCharsets.UTF_8));
    } else {
      encryptedToken = environment.getCryptographyService().encryptSymmetric(
          SESSION,
          sdek,
          tokenValue.toString().getBytes(StandardCharsets.UTF_8));
    }

    final String encryptedTokenBase64 = Base64.getEncoder().encodeToString(encryptedToken);

    final SesCleRecDto inputSdu = new SesCleRecDto(
        encryptedTokenBase64,
        sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_CLE_REC,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      final byte[] serialSduHash =
          environment.getCryptographyService().hash(
              SESSION,
              serialSdu.getBytes(StandardCharsets.UTF_8));

      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().encryptSymmetric(
              SESSION,
              skak,
              serialSduHash));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_07_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCodeValue,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                sourceIriValue,
                options),
            inputSpdu));

    // ct_07_e2
    // confirm de l’envoi de SES.MSG.ENV
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCodeValue,
                destinationCode,
                sourceIriValue,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri,
                options),
            "SUCCEED"));

    reset(transportHost);

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(transportHost.submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    transportHostEvent.notifyIdu(given_e1);

    // THEN
    verify(transportHost, atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();
    // ct_07_s1
    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_07_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(destinationCode, actual_s1.getContext().getSourceCode());
    assertEquals(sourceCodeValue, actual_s1.getContext().getDestinationCode());
    assertEquals(destinationIri, actual_s1.getContext().getSourceIri());
    assertEquals(sourceIriValue, actual_s1.getContext().getDestinationIri());

    // ct_07_s2
    // request SES.MSG.ENV
    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s2, "ct_07_s2 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(sourceCodeValue, actual_s2.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s2.getContext().getDestinationCode());
      assertEquals(sourceIriValue, actual_s2.getContext().getSourceIri());
      assertEquals(destinationIri, actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_MSG_ENV, actualSpdu.getHeader().getMsgtype());
      assertNotNull(actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesMsgEnvDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesMsgEnvDto.class);

      assertNotNull(actualSdu.getContent());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id(), actualSdu.getSession());

      assertEquals(currentSession.piduMessage(), actualSdu.getContent());

      return mapper.writeValueAsString(actual_s2);

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_07_s2 ne doit pas être reçu");
      return null;

    } else if (badStamp) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_07_s2 ne doit pas être reçu");
      return null;

    } else if (badSdek) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_07_s2 ne doit pas être reçu");
      return null;

    } else {
      fail("Scenario non supporté: " + scenario);
      return null;
    }
  }

  public static String ct_08(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;

    assertNotNull(triggerMsg, "ct_08 requiert le triggerMsg de ct_07");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesMsgEnvDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesMsgEnvDto.class);

    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(environment.partenaire, server);

    assertEquals(1, sessions.size(), "Une session partenaire doit exister avant ct_08");

    SessionInformation currentSession = sessions.values().iterator().next();

    assertNotNull(currentSession.skak(), "SKAK doit exister avant ct_08");
    assertNotNull(currentSession.sdek(), "SDEK doit exister avant ct_08");

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    final String given_e1;
    if (success) {
      InterfaceDataUnit34Dto rebuiltIdu = new InterfaceDataUnit34Dto(
          new Context34Dto(
              sourceCode,
              destinationCode,
              sourceIri,
              Context34Dto.Service.TRANSFER,
              Context34Dto.ServicePrimitive.INDICATION,
              destinationIri,
              options),
          previousIdu.getMessage());

      given_e1 = mapper.writeValueAsString(rebuiltIdu);

    } else {
      ProtocolDataUnit3SESDto badSpdu = new ProtocolDataUnit3SESDto(
          previousSpdu.getHeader(),
          "badstamp",
          previousSpdu.getContent());

      InterfaceDataUnit34Dto badIdu = new InterfaceDataUnit34Dto(
          new Context34Dto(
              sourceCode,
              destinationCode,
              sourceIri,
              Context34Dto.Service.TRANSFER,
              Context34Dto.ServicePrimitive.INDICATION,
              destinationIri,
              options),
          mapper.writeValueAsString(badSpdu));

      given_e1 = mapper.writeValueAsString(badIdu);
    }

    final String given_e3 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                sourceIri,
                options),
            "SUCCEED"));

    reset(environment.getServerTransportHost());

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e3)));

    // WHEN
    environment.destinationNotifier.notifyIdu(given_e1);

    if (success) {
      // ct_08_s2 : indication vers couche supérieure
      String serverResultJson = environment.getServerResult(30L);
      assertNotNull(serverResultJson, "ct_08_s2 doit être produit en moins de 30 secondes");

      InterfaceDataUnit23Dto actual_s2 =
          mapper.readValue(serverResultJson, InterfaceDataUnit23Dto.class);

      assertEquals(Context23Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context23Dto.ServicePrimitive.INDICATION,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(sourceCode, actual_s2.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s2.getContext().getDestinationCode());
      assertEquals(environment.getPga(), actual_s2.getContext().getPga());

      String expectedSdek =
          Base64.getEncoder().encodeToString(currentSession.sdek().getEncoded());

      assertEquals(expectedSdek, actual_s2.getContext().getSdek());
      assertEquals(previousSdu.getContent(), actual_s2.getMessage());

      // ct_08_e2 : réponse de la couche supérieure
      String given_e2 = mapper.writeValueAsString(
          new InterfaceDataUnit23Dto(
              new Context23Dto(
                  environment.getPga(),
                  sourceCode,
                  destinationCode,
                  expectedSdek,
                  Context23Dto.Service.TRANSFER,
                  Context23Dto.ServicePrimitive.RESPONSE,
                  actual_s2.getContext().getOptions()),
              "SUCCEED"));

      environment.partenaire.submitIdu(given_e2);

      // ct_08_s3 : SES.MSG.REC
      verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

      List<InterfaceDataUnit34Dto> sentAll = iduCaptor.getAllValues().stream()
          .map(json -> {
            try {
              return mapper.readValue(json, InterfaceDataUnit34Dto.class);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .toList();

      InterfaceDataUnit34Dto actual_s3 = sentAll.stream()
          .filter(
              i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
          .findFirst()
          .orElseThrow(() -> new AssertionError("ct_08_s3 non reçu"));

      assertEquals(Context34Dto.Service.TRANSFER, actual_s3.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s3.getContext().getServicePrimitive());
      assertEquals(destinationCode, actual_s3.getContext().getSourceCode());
      assertEquals(sourceCode, actual_s3.getContext().getDestinationCode());
      assertEquals(destinationIri, actual_s3.getContext().getSourceIri());
      assertEquals(sourceIri, actual_s3.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s3.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_MSG_REC, actualSpdu.getHeader().getMsgtype());
      assertEquals(previousSpdu.getHeader().getId(), actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesMsgRecDto actualMsgRec =
          mapper.readValue((String) actualSpdu.getContent(), SesMsgRecDto.class);

      assertEquals("ACK", actualMsgRec.getContent());
      assertEquals(currentSession.sessionId().id(), actualMsgRec.getSession());

      return mapper.writeValueAsString(actual_s3);

    } else {
      String serverResultJson = environment.getServerResult(1L);
      assertNull(serverResultJson, "ct_08_s2 ne doit pas être reçu en cas d’erreur");

      boolean anyTransportCall;
      try {
        verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
        anyTransportCall = !iduCaptor.getAllValues().isEmpty();
      } catch (AssertionError e) {
        anyTransportCall = false;
      }

      if (anyTransportCall) {
        List<InterfaceDataUnit34Dto> sentAll = iduCaptor.getAllValues().stream()
            .map(json -> {
              try {
                return mapper.readValue(json, InterfaceDataUnit34Dto.class);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            })
            .toList();

        boolean requestExists = sentAll.stream()
            .anyMatch(
                i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST);

        assertFalse(requestExists, "ct_08_s3 ne doit pas être reçu en cas d’erreur");
      }

      return null;
    }
  }

  public static String ct_10(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;

    assertNotNull(triggerMsg, "ct_10 requiert le triggerMsg de ct_08");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesMsgRecDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesMsgRecDto.class);

    // Précondition
    // l’initiatrice doit déjà avoir une session valide
    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(environment.initiatrice, client);

    assertEquals(1, sessions.size(), "Une session initiatrice doit exister avant ct_10");
    SessionInformation currentSession = sessions.values().iterator().next();

    assertNotNull(currentSession.skak(), "SKAK doit exister avant ct_10");
    assertNotNull(currentSession.sessionId(), "sessionId doit exister avant ct_10");
    assertNotNull(currentSession.token(), "token doit exister avant ct_10");

    final SecretKey skak = currentSession.skak();

    if (noSession) {
      sessions.remove(currentSession.sessionId());
    }

    final UUID sessionValue = currentSession.sessionId().id();

    final SesMsgRecDto inputSdu = new SesMsgRecDto(
        previousSdu.getContent(),
        sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_MSG_REC,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      final byte[] serialSduHash =
          environment.getCryptographyService().hash(
              SESSION,
              serialSdu.getBytes(StandardCharsets.UTF_8));

      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().encryptSymmetric(
              SESSION,
              skak,
              serialSduHash));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_10_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                sourceIri,
                options),
            inputSpdu));

    // ct_10_e2
    // confirm transport pour l’envoi de SES.FIN.ENV
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode,
                destinationCode,
                sourceIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri,
                options),
            "SUCCEED"));

    reset(environment.getClientTransportHost());

    ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getClientTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    environment.sourceNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getClientTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_10_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(destinationCode, actual_s1.getContext().getSourceCode());
    assertEquals(sourceCode, actual_s1.getContext().getDestinationCode());
    assertEquals(destinationIri, actual_s1.getContext().getSourceIri());
    assertEquals(sourceIri, actual_s1.getContext().getDestinationIri());

    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s2, "ct_10_s2 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(sourceCode, actual_s2.getContext().getSourceCode());
      assertEquals(destinationCode, actual_s2.getContext().getDestinationCode());
      assertEquals(sourceIri, actual_s2.getContext().getSourceIri());
      assertEquals(destinationIri, actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_FIN_ENV, actualSpdu.getHeader().getMsgtype());
      assertNotNull(actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesFinEnvDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesFinEnvDto.class);

      currentSession = sessions.values().iterator().next();

      assertNotNull(actualSdu.getToken());
      assertNotNull(actualSdu.getSession());
      assertEquals(currentSession.sessionId().id(), actualSdu.getSession());
      assertEquals(currentSession.numberOfMessage().toString(), actualSdu.getQuantity());

      return mapper.writeValueAsString(actual_s2);

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_10_s2 ne doit pas être reçu");
      return null;

    } else if (badStamp) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_10_s2 ne doit pas être reçu");
      return null;

    } else {
      fail("Scenario non supporté: " + scenario);
      return null;
    }
  }

  public static String ct_11(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;

    assertNotNull(triggerMsg, "ct_11 requiert le triggerMsg de ct_10");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesFinEnvDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesFinEnvDto.class);

    // Précondition
    // le partenaire doit déjà avoir une session active
    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(environment.partenaire, server);

    assertEquals(1, sessions.size(), "Une session partenaire doit exister");
    SessionInformation currentSession = sessions.values().iterator().next();

    assertNotNull(currentSession.skak(), "SKAK doit exister ");
    assertNotNull(currentSession.sessionId(), "sessionId doit exister");

    if (noSession) {
      sessions.remove(currentSession.sessionId());
    }

    final UUID finToken = previousSdu.getToken();
    final UUID sessionValue = currentSession.sessionId().id();

    final SesFinEnvDto inputSdu = new SesFinEnvDto(
        finToken,
        previousSdu.getQuantity(),
        sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_FIN_ENV,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      final byte[] serialSduHash =
          environment.getCryptographyService().hash(
              SESSION,
              serialSdu.getBytes(StandardCharsets.UTF_8));

      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().encryptSymmetric(
              SESSION,
              currentSession.skak(),
              serialSduHash));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_11_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode,
                destinationCode,
                sourceIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options),
            inputSpdu));

    // ct_11_e2
    // confirm transport pour l’envoi de SES.FIN.REC
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                sourceIri,
                options),
            "SUCCEED"));

    reset(environment.getServerTransportHost());

    ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    // WHEN
    environment.destinationNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getServerTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    // ct_11_s1
    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_11_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(sourceCode, actual_s1.getContext().getSourceCode());
    assertEquals(destinationCode, actual_s1.getContext().getDestinationCode());
    assertEquals(sourceIri, actual_s1.getContext().getSourceIri());
    assertEquals(destinationIri, actual_s1.getContext().getDestinationIri());

    // ct_11_s2 : request SES.FIN.REC
    InterfaceDataUnit34Dto actual_s2 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
        .findFirst()
        .orElse(null);

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));
      assertNotNull(actual_s2, "ct_11_s2 non reçu");

      assertEquals(Context34Dto.Service.TRANSFER, actual_s2.getContext().getService());
      assertEquals(Context34Dto.ServicePrimitive.REQUEST,
          actual_s2.getContext().getServicePrimitive());
      assertEquals(destinationCode, actual_s2.getContext().getSourceCode());
      assertEquals(sourceCode, actual_s2.getContext().getDestinationCode());
      assertEquals(destinationIri, actual_s2.getContext().getSourceIri());
      assertEquals(sourceIri, actual_s2.getContext().getDestinationIri());

      ProtocolDataUnit3SESDto actualSpdu =
          mapper.readValue(actual_s2.getMessage(), ProtocolDataUnit3SESDto.class);

      assertEquals(HeaderDto.Msgtype.SES_FIN_REC, actualSpdu.getHeader().getMsgtype());
      assertEquals(inputHeader.getId(), actualSpdu.getHeader().getId());
      assertNotNull(actualSpdu.getStamp());

      SesFinRecDto actualSdu =
          mapper.readValue((String) actualSpdu.getContent(), SesFinRecDto.class);

      assertNotNull(actualSdu.getToken());
      assertNotNull(actualSdu.getSession());
      assertEquals(finToken, actualSdu.getToken());
      assertEquals(currentSession.sessionId().id(), actualSdu.getSession());

      Map<SessionId, SessionInformation> updatedSessions =
          Environment.getSessionInformation(environment.partenaire, server);

      assertFalse(
          updatedSessions.containsKey(currentSession.sessionId()),
          "La session ne doit plus exister après réception réussie de SES.FIN.ENV");

      return mapper.writeValueAsString(actual_s2);

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_11_s2 ne doit pas être reçu");
      return null;

    } else if (badStamp) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));
      assertNull(actual_s2, "ct_11_s2 ne doit pas être reçu");
      return null;

    } else {
      fail("Scenario non supporté: " + scenario);
      return null;
    }
  }

  public static String ct_12(Environment environment, Scenario scenario, String triggerMsg)
      throws Exception {

    final boolean success = scenario == SUCCESS;
    final boolean noSession = scenario == NO_SESSION;
    final boolean badStamp = scenario == BAD_STAMP;
    final boolean badToken = scenario == BAD_TOKEN;

    assertNotNull(triggerMsg, "ct_12 requiert le triggerMsg de ct_11");

    InterfaceDataUnit34Dto previousIdu =
        mapper.readValue(triggerMsg, InterfaceDataUnit34Dto.class);

    ProtocolDataUnit3SESDto previousSpdu =
        mapper.readValue(previousIdu.getMessage(), ProtocolDataUnit3SESDto.class);

    SesFinRecDto previousSdu =
        mapper.readValue((String) previousSpdu.getContent(), SesFinRecDto.class);

    // Précondition
    // l’initiatrice doit déjà avoir une session active
    Map<SessionId, SessionInformation> sessionsBefore =
        Environment.getSessionInformation(environment.initiatrice, client);

    assertEquals(1, sessionsBefore.size(), "Une session initiatrice doit exister avant ct_12");
    SessionInformation currentSession = sessionsBefore.values().iterator().next();

    assertNotNull(currentSession.skak(), "SKAK doit exister avant ct_12");
    assertNotNull(currentSession.sessionId(), "sessionId doit exister avant ct_12");
    assertNotNull(currentSession.token(), "token doit exister avant ct_12");

    if (noSession) {
      sessionsBefore.remove(currentSession.sessionId());
    }

    final UUID finToken = badToken ? UUID.randomUUID() : previousSdu.getToken();
    final UUID sessionValue = currentSession.sessionId().id();

    final SesFinRecDto inputSdu = new SesFinRecDto(finToken, sessionValue);

    final HeaderDto inputHeader = new HeaderDto(
        HeaderDto.Msgtype.SES_FIN_REC,
        previousSpdu.getHeader().getId(),
        false,
        new VersionDto(environment.getVersion(), environment.getReference()));

    final String serialSdu = mapper.writeValueAsString(inputSdu);

    final String inputStamp;
    if (badStamp) {
      inputStamp = "badstamp";
    } else {
      final byte[] serialSduHash =
          environment.getCryptographyService().hash(
              SESSION,
              serialSdu.getBytes(StandardCharsets.UTF_8));

      inputStamp = Base64.getEncoder().encodeToString(
          environment.getCryptographyService().encryptSymmetric(
              SESSION,
              currentSession.skak(),
              serialSduHash));
    }

    final String inputSpdu = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(
            inputHeader,
            inputStamp,
            serialSdu));

    final String tn = UUID.randomUUID().toString();
    final Map<String, String> options = Map.of("TN", tn);

    // ct_12_e1
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                sourceIri,
                options),
            inputSpdu));

    reset(environment.getClientTransportHost());

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getClientTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    // WHEN
    environment.sourceNotifier.notifyIdu(given_e1);

    // THEN
    verify(environment.getClientTransportHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
        .map(json -> {
          try {
            return mapper.readValue(json, InterfaceDataUnit34Dto.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .toList();

    InterfaceDataUnit34Dto actual_s1 = sent.stream()
        .filter(i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
        .findFirst()
        .orElseThrow(() -> new AssertionError("ct_12_s1 non reçu"));

    assertEquals(Context34Dto.Service.TRANSFER, actual_s1.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
        actual_s1.getContext().getServicePrimitive());
    assertEquals(destinationCode, actual_s1.getContext().getSourceCode());
    assertEquals(sourceCode, actual_s1.getContext().getDestinationCode());
    assertEquals(destinationIri, actual_s1.getContext().getSourceIri());
    assertEquals(sourceIri, actual_s1.getContext().getDestinationIri());

    if (success) {
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));

      Map<SessionId, SessionInformation> sessionsAfter =
          Environment.getSessionInformation(environment.initiatrice, client);

      assertFalse(
          sessionsAfter.containsKey(currentSession.sessionId()),
          "Les informations de session doivent être effacées après réception réussie de SES.FIN.REC");

      return null;

    } else if (noSession) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));

      Map<SessionId, SessionInformation> sessionsAfter =
          Environment.getSessionInformation(environment.initiatrice, client);

      assertFalse(
          sessionsAfter.containsKey(currentSession.sessionId()),
          "La session avait été retirée avant le traitement");

      return null;

    } else if (badStamp) {
      assertTrue(actual_s1.getMessage().startsWith("FAILED"));

      Map<SessionId, SessionInformation> sessionsAfter =
          Environment.getSessionInformation(environment.initiatrice, client);

      assertTrue(
          sessionsAfter.containsKey(currentSession.sessionId()),
          "Les informations de session ne doivent pas être modifiées en cas d'échec");

      return null;

    } else if (badToken) {
      Map<SessionId, SessionInformation> sessionsAfter =
          Environment.getSessionInformation(environment.initiatrice, client);

      assertTrue(
          sessionsAfter.containsKey(currentSession.sessionId()),
          "Les informations de session ne doivent pas être modifiées quand le token de fin est invalide");

      return null;
    } else {
      fail("Scenario non supporté: " + scenario);
      return null;
    }
  }

  public static void ct_13(Environment environment) throws Exception {
    final var version = new VersionDto(environment.getVersion(), environment.getReference());

    final String sourceCode1 = environment.clientParameters.code();
    final String sourceCode2 = environment.clientParameters2.code();
    final String destinationCode = environment.serverParameters.code();

    final String sourceIri1 = environment.clientParameters.iri().toString();
    final String sourceIri2 = environment.clientParameters2.iri().toString();
    final String destinationIri = environment.serverParameters.iri().toString();

    // Précondition : 2 sessions côté partenaire
    Map<SessionId, SessionInformation> partnerSessions =
        Environment.getSessionInformation(environment.partenaire, server);

    assertEquals(2, partnerSessions.size(),
        "Deux sessions partenaires doivent exister avant ct_13");

    List<SessionInformation> sessionList = new ArrayList<>(partnerSessions.values());
    SessionInformation session1 = sessionList.stream()
        .filter(s -> sourceCode1.equals(s.initiatorId()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Session 1 introuvable"));
    SessionInformation session2 = sessionList.stream()
        .filter(s -> sourceCode2.equals(s.initiatorId()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Session 2 introuvable"));

    assertNotNull(session1.skak());
    assertNotNull(session1.sdek());
    assertNotNull(session2.skak());
    assertNotNull(session2.sdek());

    // Entrées e1 et e4 : SES.MSG.ENV concurrents
    SesMsgEnvDto inputSdu1 = new SesMsgEnvDto(session1.piduMessage(), session1.sessionId().id());
    SesMsgEnvDto inputSdu2 = new SesMsgEnvDto(session2.piduMessage(), session2.sessionId().id());

    HeaderDto inputHeader1 = new HeaderDto(
        HeaderDto.Msgtype.SES_MSG_ENV,
        UUID.randomUUID(),
        false,
        version);

    HeaderDto inputHeader2 = new HeaderDto(
        HeaderDto.Msgtype.SES_MSG_ENV,
        UUID.randomUUID(),
        false,
        version);

    String serialSdu1 = mapper.writeValueAsString(inputSdu1);
    String serialSdu2 = mapper.writeValueAsString(inputSdu2);

    byte[] serialSduHash1 = environment.getCryptographyService().hash(
        SESSION, serialSdu1.getBytes(StandardCharsets.UTF_8));
    byte[] serialSduHash2 = environment.getCryptographyService().hash(
        SESSION, serialSdu2.getBytes(StandardCharsets.UTF_8));

    String inputStamp1 = Base64.getEncoder().encodeToString(
        environment.getCryptographyService().encryptSymmetric(
            SESSION, session1.skak(), serialSduHash1));

    String inputStamp2 = Base64.getEncoder().encodeToString(
        environment.getCryptographyService().encryptSymmetric(
            SESSION, session2.skak(), serialSduHash2));

    String inputSpdu1 = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(inputHeader1, inputStamp1, serialSdu1));
    String inputSpdu2 = mapper.writeValueAsString(
        new ProtocolDataUnit3SESDto(inputHeader2, inputStamp2, serialSdu2));

    Map<String, String> options1 = Map.of("TN", UUID.randomUUID().toString());
    Map<String, String> options2 = Map.of("TN", UUID.randomUUID().toString());

    String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode1,
                destinationCode,
                sourceIri1,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options1),
            inputSpdu1));

    String given_e4 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode2,
                destinationCode,
                sourceIri2,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.INDICATION,
                destinationIri,
                options2),
            inputSpdu2));

    // confirms transport e3 et e6
    String given_e3 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode1,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                sourceIri1,
                options1),
            "SUCCEED"));

    String given_e6 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                destinationCode,
                sourceCode2,
                destinationIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                sourceIri2,
                options2),
            "SUCCEED"));

    reset(environment.getServerTransportHost());

    ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getServerTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e3)))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e6)));

    // Déclenchement simultané
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      Future<?> f1 = executor.submit(() -> environment.destinationNotifier.notifyIdu(given_e1));
      Future<?> f2 = executor.submit(() -> environment.destinationNotifier.notifyIdu(given_e4));

      f1.get();
      f2.get();

      // s2 et s5 : indications vers couche supérieure
      String client1Result = environment.getServerResult(30L);
      String client2Result = environment.getServerResult(30L);

      assertNotNull(client1Result, "ct_13_s2 doit être produit");
      assertNotNull(client2Result, "ct_13_s5 doit être produit");

      InterfaceDataUnit23Dto out1 = mapper.readValue(client1Result, InterfaceDataUnit23Dto.class);
      InterfaceDataUnit23Dto out2 = mapper.readValue(client2Result, InterfaceDataUnit23Dto.class);

      List<InterfaceDataUnit23Dto> upperOutputs = List.of(out1, out2);

      boolean foundS2 = false;
      boolean foundS5 = false;

      for (InterfaceDataUnit23Dto out : upperOutputs) {
        assertEquals(Context23Dto.Service.TRANSFER, out.getContext().getService());
        assertEquals(Context23Dto.ServicePrimitive.INDICATION,
            out.getContext().getServicePrimitive());
        assertEquals(destinationCode, out.getContext().getDestinationCode());
        assertEquals(environment.getPga(), out.getContext().getPga());

        if (sourceCode1.equals(out.getContext().getSourceCode())) {
          assertEquals(session1.piduMessage(), out.getMessage());
          String expectedSdek1 = Base64.getEncoder().encodeToString(session1.sdek().getEncoded());
          assertEquals(expectedSdek1, out.getContext().getSdek());
          foundS2 = true;
        }

        if (sourceCode2.equals(out.getContext().getSourceCode())) {
          assertEquals(session2.piduMessage(), out.getMessage());
          String expectedSdek2 = Base64.getEncoder().encodeToString(session2.sdek().getEncoded());
          assertEquals(expectedSdek2, out.getContext().getSdek());
          foundS5 = true;
        }
      }

      assertTrue(foundS2, "ct_13_s2 doit être reçu pour session1");
      assertTrue(foundS5, "ct_13_s5 doit être reçu pour session2");

      // Entrées e2 + e5 : réponses de la couche supérieure vers le partenaire
      String sdek1 = Base64.getEncoder().encodeToString(session1.sdek().getEncoded());
      String sdek2 = Base64.getEncoder().encodeToString(session2.sdek().getEncoded());

      InterfaceDataUnit23Dto upperOut1 = upperOutputs.stream()
          .filter(o -> sourceCode1.equals(o.getContext().getSourceCode()))
          .findFirst()
          .orElseThrow();

      InterfaceDataUnit23Dto upperOut2 = upperOutputs.stream()
          .filter(o -> sourceCode2.equals(o.getContext().getSourceCode()))
          .findFirst()
          .orElseThrow();

      String given_e2 = mapper.writeValueAsString(
          new InterfaceDataUnit23Dto(
              new Context23Dto(
                  environment.getPga(),
                  sourceCode1,
                  destinationCode,
                  sdek1,
                  Context23Dto.Service.TRANSFER,
                  Context23Dto.ServicePrimitive.RESPONSE,
                  upperOut1.getContext().getOptions()),
              "SUCCEED"));

      String given_e5 = mapper.writeValueAsString(
          new InterfaceDataUnit23Dto(
              new Context23Dto(
                  environment.getPga(),
                  sourceCode2,
                  destinationCode,
                  sdek2,
                  Context23Dto.Service.TRANSFER,
                  Context23Dto.ServicePrimitive.RESPONSE,
                  upperOut2.getContext().getOptions()),
              "SUCCEED"));

      environment.partenaire.submitIdu(given_e2);
      environment.partenaire.submitIdu(given_e5);

      // s1/s4 + s3/s6
      verify(environment.getServerTransportHost(), atLeast(4)).submitIdu(iduCaptor.capture());

      List<InterfaceDataUnit34Dto> allSent = iduCaptor.getAllValues().stream()
          .map(json -> {
            try {
              return mapper.readValue(json, InterfaceDataUnit34Dto.class);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .toList();

      List<InterfaceDataUnit34Dto> responses = allSent.stream()
          .filter(
              i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.RESPONSE)
          .toList();

      assertEquals(2, responses.size(), "ct_13_s1 et ct_13_s4 doivent être reçus");

      for (InterfaceDataUnit34Dto response : responses) {
        assertEquals(Context34Dto.Service.TRANSFER, response.getContext().getService());
        assertEquals(Context34Dto.ServicePrimitive.RESPONSE,
            response.getContext().getServicePrimitive());
        assertTrue(response.getMessage().startsWith("SUCCEED"));
      }

      List<InterfaceDataUnit34Dto> requests = allSent.stream()
          .filter(
              i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
          .toList();

      boolean foundSession1Ack = false;
      boolean foundSession2Ack = false;

      for (InterfaceDataUnit34Dto request : requests) {
        ProtocolDataUnit3SESDto spdu =
            mapper.readValue(request.getMessage(), ProtocolDataUnit3SESDto.class);

        if (spdu.getHeader().getMsgtype() == HeaderDto.Msgtype.SES_MSG_REC) {
          SesMsgRecDto sdu = mapper.readValue((String) spdu.getContent(), SesMsgRecDto.class);

          if (session1.sessionId().id().equals(sdu.getSession())) {
            assertEquals("ACK", sdu.getContent());
            assertEquals(inputHeader1.getId(), spdu.getHeader().getId());
            foundSession1Ack = true;
          }

          if (session2.sessionId().id().equals(sdu.getSession())) {
            assertEquals("ACK", sdu.getContent());
            assertEquals(inputHeader2.getId(), spdu.getHeader().getId());
            foundSession2Ack = true;
          }
        }
      }

      assertTrue(foundSession1Ack, "ct_13_s3 doit être reçu pour session1");
      assertTrue(foundSession2Ack, "ct_13_s6 doit être reçu pour session2");

    } finally {
      executor.shutdownNow();
    }
  }

  public static void ct_14(Environment environment) throws Exception {
    final String sourceCode = environment.clientParameters.code();
    final String destinationCode1 = environment.serverParameters.code();
    final String destinationCode2 = environment.serverParameters2.code();

    final String sourceIri = environment.clientParameters.iri().toString();
    final String destinationIri1 = environment.serverParameters.iri().toString();
    final String destinationIri2 = environment.serverParameters2.iri().toString();

    // Entrées e1 et e3
    final String sdek1 = Base64.getEncoder().encodeToString(
        environment.getCryptographyService()
            .generateSymmetricKey(SESSION)
            .getEncoded());

    final String sdek2 = Base64.getEncoder().encodeToString(
        environment.getCryptographyService()
            .generateSymmetricKey(SESSION)
            .getEncoded());

    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit23Dto(
            new Context23Dto(
                environment.getPga(),
                sourceCode,
                destinationCode1,
                sdek1,
                Context23Dto.Service.DELEGATE,
                Context23Dto.ServicePrimitive.REQUEST,
                Map.of("TN", UUID.randomUUID().toString())),
            message));

    final String given_e3 = mapper.writeValueAsString(
        new InterfaceDataUnit23Dto(
            new Context23Dto(
                environment.getPga(),
                sourceCode,
                destinationCode2,
                sdek2,
                Context23Dto.Service.DELEGATE,
                Context23Dto.ServicePrimitive.REQUEST,
                Map.of("TN", UUID.randomUUID().toString())),
            message));

    // Entrées e2 et e4 : confirms transport
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode,
                destinationCode1,
                sourceIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri1,
                Map.of("TN", UUID.randomUUID().toString())),
            "SUCCEED"));

    final String given_e4 = mapper.writeValueAsString(
        new InterfaceDataUnit34Dto(
            new Context34Dto(
                sourceCode,
                destinationCode2,
                sourceIri,
                Context34Dto.Service.TRANSFER,
                Context34Dto.ServicePrimitive.CONFIRM,
                destinationIri2,
                Map.of("TN", UUID.randomUUID().toString())),
            "SUCCEED"));

    reset(environment.getClientTransportHost());

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    when(environment.getClientTransportHost().submitIdu(anyString()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e4)));

    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      // Envoi simultané
      Future<Optional<String>> future1 =
          executor.submit(() -> environment.initiatrice.submitIdu(given_e1).get());

      Future<Optional<String>> future2 =
          executor.submit(() -> environment.initiatrice.submitIdu(given_e3).get());

      Optional<String> out1 = future1.get();
      Optional<String> out2 = future2.get();

      // Validation ct_14_s1 et ct_14_s3
      assertTrue(out1.isPresent(), "ct_14_s1 doit être reçu");
      assertTrue(out2.isPresent(), "ct_14_s3 doit être reçu");

      InterfaceDataUnit23Dto actual_s1 = mapper.readValue(out1.get(), InterfaceDataUnit23Dto.class);
      InterfaceDataUnit23Dto actual_s3 = mapper.readValue(out2.get(), InterfaceDataUnit23Dto.class);

      assertEquals(Context23Dto.Service.DELEGATE, actual_s1.getContext().getService());
      assertEquals(Context23Dto.ServicePrimitive.CONFIRM,
          actual_s1.getContext().getServicePrimitive());
      assertEquals(sourceCode, actual_s1.getContext().getSourceCode());
      assertEquals(destinationCode1, actual_s1.getContext().getDestinationCode());
      assertTrue(actual_s1.getMessage().startsWith("SUCCEED"));

      assertEquals(Context23Dto.Service.DELEGATE, actual_s3.getContext().getService());
      assertEquals(Context23Dto.ServicePrimitive.CONFIRM,
          actual_s3.getContext().getServicePrimitive());
      assertEquals(sourceCode, actual_s3.getContext().getSourceCode());
      assertEquals(destinationCode2, actual_s3.getContext().getDestinationCode());
      assertTrue(actual_s3.getMessage().startsWith("SUCCEED"));

      // Validation ct_14_s2 et ct_14_s4
      verify(environment.getClientTransportHost(), atLeast(2)).submitIdu(iduCaptor.capture());

      List<InterfaceDataUnit34Dto> sent = iduCaptor.getAllValues().stream()
          .map(json -> {
            try {
              return mapper.readValue(json, InterfaceDataUnit34Dto.class);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .filter(
              i -> i.getContext().getServicePrimitive() == Context34Dto.ServicePrimitive.REQUEST)
          .toList();

      assertEquals(2, sent.size(), "ct_14_s2 et ct_14_s4 doivent être reçus");

      InterfaceDataUnit34Dto msgToPartner1 = sent.stream()
          .filter(i -> destinationCode1.equals(i.getContext().getDestinationCode()))
          .findFirst()
          .orElseThrow(() -> new AssertionError("ct_14_s2 non reçu"));

      InterfaceDataUnit34Dto msgToPartner2 = sent.stream()
          .filter(i -> destinationCode2.equals(i.getContext().getDestinationCode()))
          .findFirst()
          .orElseThrow(() -> new AssertionError("ct_14_s4 non reçu"));

      validatePubEnvRequest(
          msgToPartner1,
          sourceCode,
          destinationCode1,
          sdek1,
          environment);

      validatePubEnvRequest(
          msgToPartner2,
          sourceCode,
          destinationCode2,
          sdek2,
          environment);

    } finally {
      executor.shutdownNow();
    }
  }

  private static void validatePubEnvRequest(
      InterfaceDataUnit34Dto actualSidu,
      String expectedSourceCode,
      String expectedDestinationCode,
      String expectedSdek,
      Environment environment) throws Exception {

    assertEquals(Context34Dto.Service.TRANSFER, actualSidu.getContext().getService());
    assertEquals(Context34Dto.ServicePrimitive.REQUEST,
        actualSidu.getContext().getServicePrimitive());
    assertEquals(expectedSourceCode, actualSidu.getContext().getSourceCode());
    assertEquals(expectedDestinationCode, actualSidu.getContext().getDestinationCode());

    ProtocolDataUnit3SESDto actualSpdu =
        mapper.readValue(actualSidu.getMessage(), ProtocolDataUnit3SESDto.class);

    assertEquals(HeaderDto.Msgtype.SES_PUB_ENV, actualSpdu.getHeader().getMsgtype());
    assertEquals("0", actualSpdu.getStamp());

    SesPubEnvDto actualSdu =
        mapper.readValue((String) actualSpdu.getContent(), SesPubEnvDto.class);

    String expectedPubKey = Base64.getEncoder().encodeToString(
        environment.clientParameters
            .certificatePrivateKeysEntry()
            .getCertficate()
            .getPublicKey()
            .getEncoded());

    assertEquals(expectedPubKey, actualSdu.getContent());
    assertNotNull(actualSdu.getSession());

    Map<SessionId, SessionInformation> sessions =
        Environment.getSessionInformation(environment.initiatrice, client);

    SessionInformation matchingSession = sessions.values().stream()
        .filter(s -> s.sessionId().id().equals(actualSdu.getSession()))
        .findFirst()
        .orElseThrow(
            () -> new AssertionError("Aucune session créée pour " + expectedDestinationCode));

    assertEquals(expectedSourceCode, matchingSession.initiatorId());
    assertEquals(expectedDestinationCode, matchingSession.peerId());
    assertEquals(environment.getPga(), matchingSession.pgaId());
    assertEquals(expectedSdek,
        Base64.getEncoder().encodeToString(matchingSession.sdek().getEncoded()));
    assertEquals(0, matchingSession.numberOfMessage());
  }
}
