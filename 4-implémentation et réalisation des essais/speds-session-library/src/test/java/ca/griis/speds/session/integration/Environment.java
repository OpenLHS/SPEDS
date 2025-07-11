package ca.griis.speds.session.integration;

import static ca.griis.speds.session.integration.SessionInformationField.client;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.js2p.gen.speds.session.api.dto.ProtocolDataUnit3SESDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.IdentityDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.ImmutableSessionClient;
import ca.griis.speds.session.api.sync.ImmutableSessionServer;
import ca.griis.speds.session.api.sync.SyncSessionFactory;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.service.crypto.KeyAgreement;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.util.KeyMapping;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public final class Environment {
  public final String pga;
  public final ObjectMapper objectMapper;
  public final PgaService pgaService;

  public final InterfaceDataUnit23Dto pidu_env;
  public final Map<HeaderDto.Msgtype, String> sidu;
  public final SessionHost initiatrice;
  public final SessionHost partenaire;
  public final SessionParameters clientParameters;
  public final SessionParameters serverParameters;
  public InterfaceDataUnit23Dto pidu_env_received;
  private InterfaceDataUnit23Dto pidu_res;
  public InterfaceDataUnit23Dto pidu_res_received;
  public int nbMsg;
  public Boolean expect_null;
  public Boolean delete_ses_info;
  public Class<? extends Throwable> exception_type;

  public Environment(TransportHost clientTransportHost, TransportHost serverTransportHost,
      PgaService pgaService) {
    this.pga = UUID.randomUUID().toString();
    final String sourceCode = "sourceCode";
    final String destinationCode = "destinationCode";
    final String sourceIri = "https://source.iri";
    final String destinationIri = "https://destination.iri";
    final UUID tracking_number = UUID.randomUUID();
    final String sdek =
        Base64.getEncoder().encodeToString(
            SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256)
                .getEncoded());
    this.objectMapper = SharedObjectMapper.getInstance().getMapper();
    this.pgaService = pgaService;
    this.expect_null = false;
    this.delete_ses_info = false;
    this.exception_type = null;

    this.pidu_env =
        new InterfaceDataUnit23Dto(new Context23Dto(pga, sourceCode, destinationCode, sdek,
            tracking_number, false),
            "Presentation request");
    this.pidu_res = null;
    this.sidu = new HashMap<>();

    SyncSessionFactory clientSessionFactory = new SyncSessionFactory(pgaService) {
      @Override
      public TransportHost initTransportHost(String parameters) {
        return clientTransportHost;
      }
    };

    SyncSessionFactory serverSessionFactory = new SyncSessionFactory(pgaService) {
      @Override
      public TransportHost initTransportHost(String parameters) {
        return serverTransportHost;
      }
    };

    final String clientParamsStr;
    final OptionsDto clientOptions;
    final String serverParamsStr;
    final InitInParamsDto serverParams;
    try {
      clientParamsStr = Files.readString(
          Paths.get(getClass().getClassLoader().getResource("initClient.json").toURI()));
      final InitInParamsDto clientParams = objectMapper.readValue(clientParamsStr,
          new TypeReference<>() {});
      clientOptions = objectMapper.convertValue(clientParams.getOptions(),
          OptionsDto.class);
      serverParamsStr = Files.readString(
          Paths.get(getClass().getClassLoader().getResource("initServer.json").toURI()));
      serverParams = objectMapper.readValue(serverParamsStr, new TypeReference<>() {});
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
    final OptionsDto serverOptions = objectMapper.convertValue(serverParams.getOptions(),
        OptionsDto.class);
    this.clientParameters =
        new SessionParameters(clientOptions, sourceCode, sourceIri);
    this.serverParameters =
        new SessionParameters(serverOptions, destinationCode, destinationIri);
    this.initiatrice = clientSessionFactory.init(clientParamsStr);
    this.partenaire = serverSessionFactory.init(serverParamsStr);
  }

  public static Map<SessionId, SessionInformation> getSessionInformation(
      SessionHost sessionHost, SessionInformationField sessionInformationField)
      throws NoSuchFieldException, IllegalAccessException {

    final Field field =
        sessionHost.getClass().getDeclaredField(sessionInformationField.name());
    field.setAccessible(true);
    return sessionInformationField.equals(client)
        ? ((ImmutableSessionClient) field.get(sessionHost)).getClientInfo()
        : ((ImmutableSessionServer) field.get(sessionHost)).getServerInfo();
  }

  public void createPiduRec(UUID trackingNumber) {
    this.pidu_res =
        new InterfaceDataUnit23Dto(new Context23Dto(pga, serverParameters.code(),
            clientParameters.code(), null, trackingNumber, false), "Presentation response");
  }

  public String createSessionMessage(HeaderDto.Msgtype msgType) throws JsonProcessingException {
    final HeaderDto header = new HeaderDto(msgType, UUID.randomUUID(), false,
        clientParameters.spedsDto());
    final KeyAgreement keyAgreement = new KeyAgreement();
    final KeyPair choiceX = keyAgreement.generateChoicePointKey();
    final KeyPair choiceY = keyAgreement.generateChoicePointKey();

    final byte[] skakByte =
        keyAgreement.completeKeyAgreementNegotiation(choiceX, choiceY.getPublic());
    final SecretKey skak = KeyMapping.getAesSecretKeyFromByte(skakByte);
    final SecretKey sdek =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    final String sessionToken = UUID.randomUUID().toString();
    final String endSessionToken = UUID.randomUUID().toString();

    final ProtocolDataUnit3SESDto pdu = msgType.toString().endsWith("ENV")
        ? createRequestMessage(msgType, header, choiceX, sdek, sessionToken, skak,
            endSessionToken)
        : createResponseMessage(msgType, header, choiceY, sessionToken, skak,
            endSessionToken);

    Context34Dto context =
        new Context34Dto(clientParameters.code(), serverParameters.code(), clientParameters.iri(),
            pdu.getHeader().getId(),
            serverParameters.iri(), false);
    return objectMapper.writeValueAsString(
        new InterfaceDataUnit34Dto(context, objectMapper.writeValueAsString(pdu)));
  }

  private ProtocolDataUnit3SESDto createRequestMessage(HeaderDto.Msgtype msgType,
      HeaderDto header, KeyPair choiceX, SecretKey sdek, String sessionToken,
      SecretKey skak, String endSessionToken) throws JsonProcessingException {
    final ProtocolDataUnit3SESDto pdu = switch (msgType) {
      case SES_PUB_ENV -> {
        final IdentityDto sdu = new IdentityDto(
            Base64.getEncoder().encodeToString(
                clientParameters.certificatePrivateKeysEntry().getCertficate().getPublicKey()
                    .getEncoded()),
            UUID.randomUUID());
        yield new ProtocolDataUnit3SESDto(header, "0", sdu);
      }
      case SES_SAK_ENV -> {
        final String choice = Base64.getEncoder().encodeToString(choiceX.getPublic().getEncoded());
        final SesSakEnvDto sdu = new SesSakEnvDto(choice, UUID.randomUUID().toString());
        final String serialString = objectMapper.writeValueAsString(sdu);
        yield new ProtocolDataUnit3SESDto(header, new SealCreator().createSeal(sdu,
            clientParameters.certificatePrivateKeysEntry().getPrivateKey(), objectMapper),
            serialString);
      }
      case SES_CLE_ENV -> {
        final String serialKey = Base64.getEncoder().encodeToString(sdek.getEncoded());
        final KeyTransferDto keyTransferDto =
            new KeyTransferDto(serialKey, pga, UUID.fromString(sessionToken));
        final String content = Base64.getEncoder().encodeToString(new AesGcmEncryptor(skak).encrypt(
            objectMapper.writeValueAsBytes(keyTransferDto)));
        final SesCleEnvDto sdu = new SesCleEnvDto(content, UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      case SES_MSG_ENV -> {
        final SesMsgEnvDto sdu = new SesMsgEnvDto(Base64.getEncoder().encodeToString(
            objectMapper.writeValueAsString(pidu_env).getBytes(StandardCharsets.UTF_8)),
            UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      case SES_FIN_ENV -> {
        final SesFinEnvDto sdu =
            new SesFinEnvDto(UUID.fromString(endSessionToken), "1", UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      default -> throw new IllegalStateException("Unknown message type: " + msgType);
    };
    return pdu;
  }

  private ProtocolDataUnit3SESDto createResponseMessage(HeaderDto.Msgtype msgType,
      HeaderDto header, KeyPair choiceY, String sessionToken,
      SecretKey skak, String endSessionToken) throws JsonProcessingException {
    final ProtocolDataUnit3SESDto pdu = switch (msgType) {
      case SES_PUB_REC -> {
        final String sessionId = UUID.randomUUID().toString();
        yield new ProtocolDataUnit3SESDto(header, new SealCreator().createSeal(sessionId,
            serverParameters.certificatePrivateKeysEntry().getPrivateKey(), objectMapper),
            sessionId);
      }
      case SES_SAK_REC -> {
        final String choice = Base64.getEncoder().encodeToString(choiceY.getPublic().getEncoded());
        final SesSakRecDto sdu = new SesSakRecDto(choice, UUID.randomUUID());
        yield new ProtocolDataUnit3SESDto(header, new SealCreator().createSeal(sdu,
            serverParameters.certificatePrivateKeysEntry().getPrivateKey(), objectMapper), sdu);
      }
      case SES_CLE_REC -> {
        final String value = Base64.getEncoder()
            .encodeToString(new AesGcmEncryptor(skak).encrypt(sessionToken.getBytes(
                StandardCharsets.UTF_8)));
        final SesSakRecDto sdu = new SesSakRecDto(value, UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      case SES_MSG_REC -> {
        final SesMsgRecDto sdu = new SesMsgRecDto(Base64.getEncoder().encodeToString(
            objectMapper.writeValueAsString(pidu_res).getBytes(StandardCharsets.UTF_8)),
            UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      case SES_FIN_REC -> {
        final SesFinRecDto sdu =
            new SesFinRecDto(UUID.fromString(endSessionToken), UUID.randomUUID());
        final String stamp = new SealCreator().createSymmetricalSeal(sdu, skak, objectMapper);
        yield new ProtocolDataUnit3SESDto(header, stamp, sdu);
      }
      default -> throw new IllegalStateException("Unknown message type: " + msgType);
    };
    return pdu;
  }

  public InterfaceDataUnit23Dto getPidu_res() {
    return pidu_res;
  }

  public void setPidu_res(InterfaceDataUnit23Dto pidu_res) {
    this.pidu_res = pidu_res;
  }
}
