package ca.griis.speds.session.integration;

import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_CLE_ENV;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_CLE_REC;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_FIN_ENV;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_FIN_REC;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_MSG_ENV;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_MSG_REC;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_PUB_ENV;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_PUB_REC;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_SAK_ENV;
import static ca.griis.js2p.gen.speds.session.api.dto.HeaderDto.Msgtype.SES_SAK_REC;
import static ca.griis.speds.session.integration.Environment.getSessionInformation;
import static ca.griis.speds.session.integration.SessionInformationField.client;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.ProtocolDataUnit3SESDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.IdentityDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.speds.session.api.sync.ImmutableSessionHost;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class Cases {
  public static void ct_01(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas normal
    } else {
      final String outMsg = environment.sidu.get(SES_PUB_ENV);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final IdentityDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), IdentityDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(environment.clientParameters.iri(), sidu.getContext().getSourceIri());
      assertEquals(environment.serverParameters.iri(), sidu.getContext().getDestinationIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(SES_PUB_ENV, spdu.getHeader().getMsgtype());
      assertEquals("0", spdu.getStamp());

      // Critères de succès - Outmsg (sdu)
      assertEquals(Base64.getEncoder().encodeToString(
          environment.clientParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey()
              .getEncoded()),
          sdu.getContent());
      assertNotNull(sdu.getSession());

      // Critères de succès - Information de session
      final SessionInformation sessionInformation =
          getSessionInformation((ImmutableSessionHost) environment.initiatrice, client)
              .get(new SessionId(sdu.getSession()));
      assertEquals(sdu.getSession(), sessionInformation.sessionId.id());
      assertEquals(environment.pidu_env.getContext().getSourceCode(),
          sessionInformation.initiatorId);
      assertEquals(Base64.getEncoder().encodeToString(
          environment.clientParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey()
              .getEncoded()),
          sessionInformation.initiatorPubKey);
      assertEquals(environment.pidu_env.getContext().getDestinationCode(),
          sessionInformation.peerId);
      assertEquals(environment.pidu_env.getContext().getPga(), sessionInformation.pgaId);
      assertEquals(environment.pidu_env.getContext().getSdek(),
          Base64.getEncoder().encodeToString(sessionInformation.sdek.getEncoded()));
    }
  }

  public static void ct_02(Environment environment) throws Exception {
    // Flux d'entrée
    final InterfaceDataUnit34Dto triggerMsg =
        environment.objectMapper.readValue(environment.sidu.get(SES_PUB_ENV),
            InterfaceDataUnit34Dto.class);
    final ProtocolDataUnit3SESDto triggerMsgSpdu =
        environment.objectMapper.readValue(triggerMsg.getMessage(), ProtocolDataUnit3SESDto.class);

    // Flux de sortie - Cas d'exception
    if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_PUB_REC));
    } else {
      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_PUB_REC);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final UUID sdu = UUID.fromString((String) spdu.getContent());

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationCode(), sidu.getContext().getSourceCode());
      assertEquals(triggerMsg.getContext().getSourceCode(), sidu.getContext().getDestinationCode());
      assertEquals(triggerMsg.getContext().getDestinationIri(), sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(), sidu.getContext().getDestinationIri());
      assertEquals(triggerMsg.getContext().getTrackingNumber(),
          sidu.getContext().getTrackingNumber());

      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      final SessionInformation sessionInformation = map.get(new SessionId(sdu));

      // Critères de succès - Outmsg (spdu)
      assertEquals(triggerMsgSpdu.getHeader().getId(), spdu.getHeader().getId());
      assertEquals(SES_PUB_REC, spdu.getHeader().getMsgtype());
      assertEquals(new SealCreator().createSeal(spdu.getContent(),
          environment.serverParameters.certificatePrivateKeysEntry().getPrivateKey(),
          environment.objectMapper),
          spdu.getStamp());
      assertNotNull(sdu);

      // Critères de succès - Information de session
      assertEquals(sdu.toString(), sessionInformation.sessionId.id().toString());
      assertEquals(triggerMsg.getContext().getSourceCode(), sessionInformation.initiatorId);
      assertEquals(triggerMsg.getContext().getSourceIri(), sessionInformation.initiatorIri);
      assertEquals(environment.serverParameters.code(), sessionInformation.peerId);
      assertEquals(environment.serverParameters.iri(), sessionInformation.peerIri);
    }
  }

  public static void ct_03(Environment environment) throws Exception {
    // Flux de sortie - Cas d'exception
    if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_SAK_ENV));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_PUB_REC),
              InterfaceDataUnit34Dto.class);

      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_SAK_ENV);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesSakEnvDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesSakEnvDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationIri(), sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(), sidu.getContext().getDestinationIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(SES_SAK_ENV, spdu.getHeader().getMsgtype(),
          "ct_03 - Le msgtype du SPDU n'est pas SES.SAK.ENV");
      assertEquals(new SealCreator().createSeal(spdu.getContent(),
          environment.clientParameters.certificatePrivateKeysEntry().getPrivateKey(),
          environment.objectMapper),
          spdu.getStamp(), "ct_03 - L’estampille est invalide");

      // Critères de succès - Outmsg (sdu)
      assertFalse(sdu.getValue().isEmpty());
      assertNotNull(sdu.getSession());
    }
  }

  public static void ct_04(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_SAK_REC));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_SAK_ENV),
              InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto triggerMsgSpdu =
          environment.objectMapper.readValue(triggerMsg.getMessage(),
              ProtocolDataUnit3SESDto.class);

      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_SAK_REC);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesSakRecDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesSakRecDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationCode(),
          sidu.getContext().getSourceCode());
      assertEquals(triggerMsg.getContext().getSourceCode(),
          sidu.getContext().getDestinationCode());
      assertEquals(triggerMsg.getContext().getDestinationIri(), sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(), sidu.getContext().getDestinationIri());
      assertEquals(triggerMsg.getContext().getTrackingNumber(),
          sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(triggerMsgSpdu.getHeader().getId(), spdu.getHeader().getId());
      assertEquals(SES_SAK_REC, spdu.getHeader().getMsgtype());
      assertEquals(new SealCreator().createSeal(sdu,
          environment.serverParameters.certificatePrivateKeysEntry().getPrivateKey(),
          environment.objectMapper),
          spdu.getStamp(), "ct_04 - L’estampille est invalide");

      // Critères de succès - Outmsg (sdu)
      assertFalse(sdu.getValue().isEmpty());
      assertNotNull(sdu.getSession());

      // Critères de succès - Information de session
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      final SessionInformation sessionInformation = map.get(new SessionId(sdu.getSession()));

      assertEquals("AES", sessionInformation.skak.getAlgorithm());
    }
  }

  public static void ct_05(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_CLE_ENV));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_SAK_REC),
              InterfaceDataUnit34Dto.class);

      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_CLE_ENV);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesCleEnvDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesCleEnvDto.class);
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.initiatrice, client);
      final SessionInformation sessionInformation = map.get(new SessionId(sdu.getSession()));
      final KeyTransferDto clefTrans = environment.objectMapper.readValue(
          new AesGcmDecryptor(sessionInformation.skak).decrypt(
              Base64.getDecoder().decode(sdu.getContent())),
          KeyTransferDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationIri(),
          sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(),
          sidu.getContext().getDestinationIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(UUID.class, spdu.getHeader().getId().getClass());
      assertEquals(SES_CLE_ENV, spdu.getHeader().getMsgtype(), "Type incorrect");

      Boolean valid = new SealVerifier().verifySymmetricalSeal(sdu, sessionInformation.skak,
          spdu.getStamp(), environment.objectMapper);
      assertTrue(valid);

      // Critères de succès - Outmsg (sdu)
      assertNotNull(sdu.getSession());

      // Critères de succès - Outmsg (clef_trans)
      assertEquals(
          Base64.getEncoder().encodeToString(sessionInformation.sdek.getEncoded()),
          clefTrans.getSdek());
      assertNotNull(clefTrans.getPgaNumber());
      assertNotNull(clefTrans.getToken());

      // Critères de succès - Information de session
      assertEquals("AES", sessionInformation.skak.getAlgorithm());
    }
  }

  public static void ct_06(Environment environment, Throwable thrown)
      throws IOException, NoSuchFieldException, IllegalAccessException {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_CLE_REC));
      // Flux de sortie - Cas d'exception
    } else if (environment.delete_ses_info) {
      // Critères de succès - Cas d'exception
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire,
              SessionInformationField.server);
      assertTrue(map.isEmpty());
      assertNull(environment.sidu.get(SES_CLE_REC));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_CLE_ENV),
              InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto triggerMsgSpdu =
          environment.objectMapper.readValue(triggerMsg.getMessage(),
              ProtocolDataUnit3SESDto.class);
      final SesCleEnvDto triggerMsgSpduSdu =
          environment.objectMapper.readValue((String) triggerMsgSpdu.getContent(),
              SesCleEnvDto.class);
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      final SessionInformation sessionInformation =
          map.get(new SessionId(triggerMsgSpduSdu.getSession()));
      final KeyTransferDto clefTrans = environment.objectMapper.readValue(
          new AesGcmDecryptor(sessionInformation.skak).decrypt(
              Base64.getDecoder().decode(triggerMsgSpduSdu.getContent())),
          KeyTransferDto.class);

      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_CLE_REC);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesCleRecDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesCleRecDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationCode(),
          sidu.getContext().getSourceCode());
      assertEquals(triggerMsg.getContext().getSourceCode(),
          sidu.getContext().getDestinationCode());
      assertEquals(triggerMsg.getContext().getSourceIri(),
          sidu.getContext().getDestinationIri());
      assertEquals(triggerMsg.getContext().getDestinationIri(),
          sidu.getContext().getSourceIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(triggerMsgSpdu.getHeader().getId(), spdu.getHeader().getId());
      assertEquals(SES_CLE_REC, spdu.getHeader().getMsgtype(), "Type incorrect");

      Boolean valid = new SealVerifier().verifySymmetricalSeal(spdu.getContent(),
          sessionInformation.skak,
          spdu.getStamp(), environment.objectMapper);
      assertTrue(valid);

      // Critères de succès - Outmsg (sdu)
      assertEquals(triggerMsgSpduSdu.getSession(), sdu.getSession());

      final byte[] encryptedBytes = Base64.getDecoder().decode(sdu.getContent());
      final byte[] decryptedBytes =
          new AesGcmDecryptor(sessionInformation.sdek).decrypt(encryptedBytes);
      final String decryptedUuid = new String(decryptedBytes, StandardCharsets.UTF_8);
      assertDoesNotThrow(() -> UUID.fromString(decryptedUuid),
          "Le contenu déchiffré n'est pas un UUID valide");

      // Critères de succès - Information de session
      assertEquals(sessionInformation.pgaId, clefTrans.getPgaNumber());
      assertEquals(
          Base64.getEncoder().encodeToString(sessionInformation.sdek.getEncoded()),
          clefTrans.getSdek());
    }
  }

  public static void ct_07(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_MSG_ENV));
      // Flux de sortie - Cas d'exception
    } else if (environment.delete_ses_info) {
      // Critères de succès - Cas d'exception
      assertTrue(getSessionInformation(environment.initiatrice, client).isEmpty());
      assertNull(environment.sidu.get(SES_MSG_ENV));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_CLE_REC),
              InterfaceDataUnit34Dto.class);

      // Flux de sortie - cas normal
      final String outMsg = environment.sidu.get(SES_MSG_ENV);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(outMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesMsgEnvDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesMsgEnvDto.class);
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.initiatrice, client);
      final SessionInformation sessionInformation =
          map.get(new SessionId(sdu.getSession()));

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationIri(),
          sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(),
          sidu.getContext().getDestinationIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertNotNull(spdu.getHeader().getId());
      assertEquals(SES_MSG_ENV, spdu.getHeader().getMsgtype(), "Type incorrect");

      Boolean valid = new SealVerifier().verifySymmetricalSeal(sdu, sessionInformation.skak,
          spdu.getStamp(), environment.objectMapper);
      assertTrue(valid);

      // Critères de succès - Outmsg (sdu)
      assertEquals(environment.pidu_env.getMessage(), sdu.getContent(),
          "Le message retourné doit être le même que celui du PIDU initial");
      assertNotNull(sdu.getSession());
    }
  }

  public static void ct_08(Environment environment, Throwable thrown)
      throws IOException, NoSuchFieldException, IllegalAccessException {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.getPidu_res());
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_MSG_ENV),
              InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto triggerMsgSpdu =
          environment.objectMapper.readValue(triggerMsg.getMessage(),
              ProtocolDataUnit3SESDto.class);
      final SesMsgEnvDto triggerMsgSpduSdu =
          environment.objectMapper.readValue((String) triggerMsgSpdu.getContent(),
              SesMsgEnvDto.class);

      // Flux de sortie - cas normal
      final InterfaceDataUnit23Dto pidu = environment.getPidu_res();

      // Critères de succès - Outmsg (PIDU)
      assertEquals(triggerMsgSpdu.getHeader().getId(),
          pidu.getContext().getTrackingNumber());
      assertEquals(environment.pidu_env.getMessage(), pidu.getMessage());

      // Critères de succès - Information de session
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      final SessionInformation sessionInformation =
          map.get(new SessionId(triggerMsgSpduSdu.getSession()));
      assertEquals(triggerMsgSpdu.getHeader().getId(), sessionInformation.trackingNumber);
    }
  }

  public static void ct_09(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_MSG_REC));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit23Dto triggerMsg = environment.getPidu_res();

      // Flux de sortie - Cas normal
      // Récupération du message de sortie
      final String outMsg = environment.sidu.get(SES_MSG_REC);
      final InterfaceDataUnit34Dto sidu =
          environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu =
          environment.objectMapper.readValue(sidu.getMessage(), ProtocolDataUnit3SESDto.class);
      SesMsgRecDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesMsgRecDto.class);
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      final SessionInformation sessionInformation =
          map.get(new SessionId(sdu.getSession()));

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getSourceCode(), sidu.getContext().getSourceCode());
      assertEquals(triggerMsg.getContext().getDestinationCode(),
          sidu.getContext().getDestinationCode());
      assertEquals(environment.serverParameters.iri(), sidu.getContext().getSourceIri());
      assertEquals(environment.clientParameters.iri(), sidu.getContext().getDestinationIri());
      assertEquals(sessionInformation.trackingNumber,
          sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(triggerMsg.getContext().getTrackingNumber(), spdu.getHeader().getId());
      assertEquals(SES_MSG_REC, spdu.getHeader().getMsgtype());
      Boolean valid = new SealVerifier().verifySymmetricalSeal(sdu, sessionInformation.skak,
          spdu.getStamp(), environment.objectMapper);
      assertTrue(valid);

      // Critères de succès - Outmsg (sdu)
      assertEquals(triggerMsg.getMessage(), sdu.getContent());
      assertEquals(sessionInformation.sessionId.id(), sdu.getSession());
    }
  }

  public static void ct_10(Environment environment, Throwable thrown)
      throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_FIN_ENV));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_MSG_REC),
              InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto triggerMsgSpdu =
          environment.objectMapper.readValue(triggerMsg.getMessage(),
              ProtocolDataUnit3SESDto.class);
      final SesMsgRecDto triggerMsgSpduSdu =
          environment.objectMapper.readValue((String) triggerMsgSpdu.getContent(),
              SesMsgRecDto.class);

      // Flux de sortie piduMsg- cas normal
      final InterfaceDataUnit23Dto pidu = environment.getPidu_res();

      // Flux de sortie siduMsg- cas normal
      final String siduMsg = environment.sidu.get(SES_FIN_ENV);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(siduMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesFinEnvDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesFinEnvDto.class);

      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.initiatrice, client);
      final SessionInformation sessionInformation =
          map.get(new SessionId(sdu.getSession()));

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getDestinationIri(),
          sidu.getContext().getSourceIri());
      assertEquals(triggerMsg.getContext().getSourceIri(),
          sidu.getContext().getDestinationIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertNotNull(spdu.getHeader().getId());
      assertEquals(SES_FIN_ENV, spdu.getHeader().getMsgtype(), "Type incorrect");
      assertTrue(new SealVerifier().verifySymmetricalSeal(spdu.getContent(),
          sessionInformation.skak, spdu.getStamp(), environment.objectMapper));

      // Critères de succès - Outmsg (sdu)
      assertEquals(sessionInformation.sessionId.id(), sdu.getSession());
      assertEquals(environment.nbMsg, Integer.valueOf(sdu.getQuantity()));
      assertNotNull(sdu.getToken());

      // Critères de succès - Outmsg (pidu)
      assertEquals(triggerMsgSpdu.getHeader().getId(), pidu.getContext().getTrackingNumber());
      assertEquals(triggerMsgSpduSdu.getContent(), pidu.getMessage());

      // Critères de succès - Information de session
      assertEquals(environment.nbMsg, sessionInformation.numberOfMessage);
    }
  }

  public static void ct_11(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      assertNull(environment.sidu.get(SES_FIN_REC));
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_FIN_ENV),
              InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto triggerMsgSpdu =
          environment.objectMapper.readValue(triggerMsg.getMessage(),
              ProtocolDataUnit3SESDto.class);
      final SesFinEnvDto triggerMsgSpduSdu =
          environment.objectMapper.readValue((String) triggerMsgSpdu.getContent(),
              SesFinEnvDto.class);

      // Flux de sortie outMsg- cas normal
      final String sduMsg = environment.sidu.get(SES_FIN_REC);
      final InterfaceDataUnit34Dto sidu = environment.objectMapper.readValue(sduMsg,
          InterfaceDataUnit34Dto.class);
      final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
          ProtocolDataUnit3SESDto.class);
      final SesFinRecDto sdu =
          environment.objectMapper.readValue((String) spdu.getContent(), SesFinRecDto.class);

      // Critères de succès - Outmsg (sidu)
      assertEquals(triggerMsg.getContext().getSourceIri(),
          sidu.getContext().getDestinationIri());
      assertEquals(triggerMsg.getContext().getDestinationIri(),
          sidu.getContext().getSourceIri());
      assertEquals(spdu.getHeader().getId(), sidu.getContext().getTrackingNumber());

      // Critères de succès - Outmsg (spdu)
      assertEquals(triggerMsgSpdu.getHeader().getId(), spdu.getHeader().getId());
      assertEquals(SES_FIN_REC, spdu.getHeader().getMsgtype(), "Type incorrect");

      // Critères de succès - Outmsg (sdu)
      assertEquals(triggerMsgSpduSdu.getToken(), sdu.getToken());

      // Critères de succès - Information de session
      Map<SessionId, SessionInformation> mapServer =
          getSessionInformation(environment.partenaire, SessionInformationField.server);
      assertTrue(mapServer.isEmpty());
    }
  }

  public static void ct_12(Environment environment, Throwable thrown) throws Exception {
    // Flux de sortie - Cas d'exception
    if (thrown != null) {
      // Critères de succès - cas d'exception
      assertEquals(thrown.getClass(), environment.exception_type);
      // Flux de sortie - cas d'exception
    } else if (environment.expect_null) {
      // Critères de succès - Cas d'exception
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.initiatrice, client);
      assertNotEquals(0, map.size());
    } else {
      // Flux d'entrée
      final InterfaceDataUnit34Dto triggerMsg =
          environment.objectMapper.readValue(environment.sidu.get(SES_FIN_REC),
              InterfaceDataUnit34Dto.class);
      assertNotNull(triggerMsg);

      // Flux de sortie - Cas normal
      // informsation session supprimé
      // Critères de succès - Information de session
      Map<SessionId, SessionInformation> map =
          getSessionInformation(environment.initiatrice, client);
      assertTrue(map.isEmpty());
      // Flux de sortie - Cas d'exception
    }
  }
}
