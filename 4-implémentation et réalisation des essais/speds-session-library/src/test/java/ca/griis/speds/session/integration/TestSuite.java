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
import static ca.griis.speds.session.integration.Cases.ct_01;
import static ca.griis.speds.session.integration.Cases.ct_02;
import static ca.griis.speds.session.integration.Cases.ct_03;
import static ca.griis.speds.session.integration.Cases.ct_04;
import static ca.griis.speds.session.integration.Cases.ct_05;
import static ca.griis.speds.session.integration.Cases.ct_06;
import static ca.griis.speds.session.integration.Cases.ct_07;
import static ca.griis.speds.session.integration.Cases.ct_08;
import static ca.griis.speds.session.integration.Cases.ct_09;
import static ca.griis.speds.session.integration.Cases.ct_10;
import static ca.griis.speds.session.integration.Cases.ct_11;
import static ca.griis.speds.session.integration.Cases.ct_12;
import static ca.griis.speds.session.integration.Environment.getSessionInformation;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.ProtocolDataUnit3SESDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.exception.GetIriException;
import ca.griis.speds.session.api.exception.InvalidTokenException;
import ca.griis.speds.session.api.exception.InvalidTrackingNumberException;
import ca.griis.speds.session.api.exception.KeyAgreementException;
import ca.griis.speds.session.api.exception.NoResponseRequestException;
import ca.griis.speds.session.api.exception.SessionTerminaisonFailedException;
import ca.griis.speds.session.api.exception.VerifyException;
import ca.griis.speds.session.api.sync.ImmutableSessionHost;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSuite {
  private long executorTimeout = 20L;

  private ExecutorService executor;

  @Mock
  private TransportHost clientTransportHost;

  @Mock
  private TransportHost serverTransportHost;

  @Mock
  private PgaService pgaService;

  private BlockingQueue<String> requests = new LinkedBlockingQueue<>();

  private BlockingQueue<String> responses = new LinkedBlockingQueue<>();

  private Environment environment;

  @BeforeEach
  public void setup() throws Exception {
    environment = new Environment(clientTransportHost, serverTransportHost, pgaService);
  }

  @Test
  public void e_01_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_PUB_ENV, environment.initiatrice, false)).when(
        clientTransportHost)
        .dataRequest(anyString());

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());

    Throwable thrown = sendRequest();

    ct_01(environment, thrown);
  }

  @Test
  public void e_02_getIriException_source() throws Exception {
    doAnswer(interruptAnswer(requests, SES_PUB_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());

    // exception sur la source
    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenThrow(new GetIriException("Erreur de la source"));
    // Comportement normal pour la destination
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());

    environment.exception_type = GetIriException.class;

    Throwable thrown = sendRequest();

    ct_01(environment, thrown);
  }

  @Test
  public void e_03_getIriException_destination() throws Exception {
    doAnswer(interruptAnswer(requests, SES_PUB_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());

    // exception sur la source
    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());

    // Comportement normal pour la destination
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenThrow(new GetIriException("Erreur de la destination"));

    environment.exception_type = GetIriException.class;

    Throwable thrown = sendRequest();

    ct_01(environment, thrown);
  }

  @Test
  public void e_04_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_PUB_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_PUB_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());

    sendRequest();

    ct_02(environment);
  }

  @Test
  public void e_05_invalidStamp() throws Exception {
    doAnswer(invalidStampAnswer(requests, SES_PUB_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_PUB_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());


    sendRequest();

    environment.expect_null = true;
    ct_02(environment);
  }

  @Test
  public void e_06_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_SAK_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_PUB_REC, environment.partenaire, true))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    sendRequest();

    ct_03(environment);
  }

  @Test
  public void e_07_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    assertTrue(responses.offer(environment.createSessionMessage(SES_PUB_REC)));
    environment.expect_null = true;

    ct_03(environment);
  }

  @Test
  public void e_08_invalidStamp() throws Exception {
    doAnswer(interruptAnswer(requests, SES_SAK_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidStampAnswer(responses, SES_PUB_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    sendRequest();

    environment.expect_null = true;
    ct_03(environment);
  }

  @Test
  public void e_09_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_SAK_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_SAK_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    Throwable thrown = sendRequest();

    ct_04(environment, thrown);
  }

  @Test
  public void e_10_unknownSession() throws Exception {
    doAnswer(normalAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    assertTrue(requests.offer(environment.createSessionMessage(SES_SAK_ENV)));
    environment.expect_null = true;

    ct_04(environment, null);
  }

  @Test
  public void e_11_invalidStamp() throws Exception {
    doAnswer(invalidStampAnswer(requests, SES_SAK_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_SAK_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    environment.expect_null = true;

    Throwable thrown = sendRequest();

    ct_04(environment, thrown);
  }

  @Test
  public void e_12_invalidSkak() throws Exception {
    doAnswer(invalidSkakAnswer(requests, SES_SAK_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_SAK_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    environment.exception_type = KeyAgreementException.class;

    Throwable thrown = sendRequestWithPeerException();

    ct_04(environment, thrown);
  }

  @Test
  public void e_13_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_CLE_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_SAK_REC, environment.partenaire, true))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    Throwable thrown = sendRequest();
    ct_05(environment, thrown);
  }

  @Test
  public void e_14_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();
    assertTrue(responses.offer(environment.createSessionMessage(SES_SAK_REC)));
    environment.expect_null = true;

    ct_05(environment, null);
  }

  @Test
  public void e_15_invalidStamp() throws Exception {
    doAnswer(interruptAnswer(requests, SES_CLE_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidStampAnswer(responses, SES_SAK_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    Throwable thrown = sendRequest();
    environment.expect_null = true;

    ct_05(environment, thrown);
  }

  @Test
  public void e_16_invalidSkak() throws Exception {
    doAnswer(interruptAnswer(requests, SES_CLE_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidSkakAnswer(responses, SES_SAK_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));

    Throwable thrown = sendRequest();
    environment.expect_null = true;

    ct_05(environment, thrown);
  }

  @Test
  public void e_17_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_CLE_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_CLE_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequest();

    ct_06(environment, thrown);
  }

  @Test
  public void e_18_unknownSession() throws Exception {
    doAnswer(normalAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    assertTrue(requests.offer(environment.createSessionMessage(SES_CLE_ENV)));
    environment.expect_null = true;

    ct_06(environment, null);
  }

  @Test
  public void e_19_invalidStamp() throws Exception {
    doAnswer(invalidStampAnswer(requests, SES_CLE_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_CLE_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequest();
    environment.expect_null = true;

    ct_06(environment, thrown);
  }

  @Test
  public void e_20_invalidLegitimacy() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_CLE_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(false);

    Throwable thrown = sendRequestWithPeerException();
    environment.exception_type = VerifyException.class;

    ct_06(environment, thrown);
  }

  @Test
  public void e_21_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_MSG_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_CLE_REC, environment.partenaire, true))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequest();
    ct_07(environment, thrown);
  }

  @Test
  public void e_22_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();
    assertTrue(responses.offer(environment.createSessionMessage(SES_CLE_REC)));
    environment.expect_null = true;

    ct_07(environment, null);
  }

  @Test
  public void e_23_invalidStamp() throws Exception {
    doAnswer(interruptAnswer(requests, SES_MSG_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidStampAnswer(responses, SES_CLE_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequest();
    environment.expect_null = true;

    ct_07(environment, thrown);
  }

  @Test
  public void e_24_invalidSdek() throws Exception {
    doAnswer(interruptAnswer(requests, SES_MSG_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidSdekAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequest();
    environment.exception_type = InvalidTokenException.class;

    ct_07(environment, thrown);
  }

  @Test
  public void e_25_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_MSG_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(normalAnswer(responses)).when(serverTransportHost).dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndGetAnswer();

    ct_08(environment, thrown);
  }

  @Test
  public void e_26_unknownSession() throws Exception {
    doAnswer(normalAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    assertTrue(requests.offer(environment.createSessionMessage(SES_MSG_ENV)));
    environment.expect_null = true;

    ct_08(environment, null);
  }

  @Test
  public void e_27_invalidStamp() throws Exception {
    doAnswer(invalidStampAnswer(requests, SES_MSG_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(normalAnswer(responses)).when(serverTransportHost).dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndGetAnswer();
    environment.expect_null = true;

    ct_08(environment, thrown);
  }

  @Test
  public void e_28_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_MSG_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_MSG_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponse(true, false);

    ct_09(environment, thrown);
  }

  @Test
  public void e_29_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_MSG_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponse(true, true);
    environment.exception_type = InvalidTrackingNumberException.class;

    ct_09(environment, thrown);
  }

  @Test
  public void e_30_noResponseRequestException() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_MSG_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponse(false, false);
    environment.exception_type = NoResponseRequestException.class;

    ct_09(environment, thrown);
  }

  @Test
  public void e_31_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_FIN_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_MSG_REC, environment.partenaire, true))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(true);

    ct_10(environment, thrown);
  }

  @Test
  public void e_32_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost).dataRequest(anyString());
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    assertTrue(responses.offer(environment.createSessionMessage(SES_MSG_REC)));
    environment.expect_null = true;

    ct_10(environment, null);
  }

  @Test
  public void e_33_invalidStamp() throws Exception {
    doAnswer(interruptAnswer(requests, SES_FIN_ENV, environment.initiatrice, false))
        .when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidStampAnswer(responses, SES_MSG_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    // toimprove md - le test se rend et fait l'exception il n'est pas interrompu
    // J'ai rajouter un boolean pour getAnswer, maintenant ça marche avec timeout
    // Mais c'est potentiellement refactorisable a de quoi de plus beau
    // C'est selon
    Throwable thrown = sendRequestAndResponseAndGetAnswer(false);
    thrown = null;
    environment.expect_null = true;

    ct_10(environment, thrown);
  }

  @Test
  public void e_34_success() throws Exception {
    doAnswer(interruptAnswer(requests, SES_FIN_ENV, environment.initiatrice, true))
        .when(clientTransportHost).dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_FIN_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(true);

    ct_11(environment, thrown);
  }

  @Test
  public void e_35_unknownSession() throws Exception {
    doAnswer(normalAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();

    assertTrue(requests.offer(environment.createSessionMessage(SES_FIN_ENV)));
    environment.expect_null = true;

    ct_11(environment, null);
  }

  @Test
  public void e_36_invalidStamp() throws Exception {
    doAnswer(invalidStampAnswer(requests, SES_FIN_ENV)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_FIN_REC, environment.partenaire, false))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(true);
    environment.expect_null = true;

    ct_11(environment, thrown);
  }

  @Test
  public void e_37_success() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(interruptAnswer(responses, SES_FIN_REC, environment.partenaire, true))
        .when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(true);

    ct_12(environment, thrown);
  }

  @Test
  public void e_38_unknownSession() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidSessionAnswer(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(false);
    environment.expect_null = true;

    ct_12(environment, thrown);
  }

  @Test
  public void e_39_invalidStamp() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidStampAnswer(responses, SES_FIN_REC)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(false);
    environment.expect_null = true;

    ct_12(environment, thrown);
  }

  @Test
  public void e_40_invalidToken() throws Exception {
    doAnswer(normalAnswer(requests)).when(clientTransportHost)
        .dataRequest(anyString());
    doAnswer(invalidSessionAnswerToken(responses)).when(serverTransportHost)
        .dataRequest(anyString());
    doAnswer(invocation -> requests.poll()).when(serverTransportHost)
        .dataReply();
    doAnswer(invocation -> responses.poll()).when(clientTransportHost)
        .dataReply();

    when(pgaService.getIri(environment.pga, environment.clientParameters.code()))
        .thenReturn(environment.clientParameters.iri());
    when(pgaService.getIri(environment.pga, environment.serverParameters.code()))
        .thenReturn(environment.serverParameters.iri());
    when(pgaService.getPublicKey(environment.pga, environment.serverParameters.code())).thenReturn(
        Base64.getEncoder().encodeToString(
            environment.serverParameters.certificatePrivateKeysEntry().getCertficate()
                .getPublicKey().getEncoded()));
    when(pgaService.verifyLegitimacy(environment.pga, environment.clientParameters.code(),
        Base64.getEncoder()
            .encodeToString(
                environment.clientParameters.certificatePrivateKeysEntry().getCertficate()
                    .getPublicKey().getEncoded()))).thenReturn(true);

    Throwable thrown = sendRequestAndResponseAndGetAnswer(false);
    environment.exception_type = SessionTerminaisonFailedException.class;

    ct_12(environment, thrown);
  }

  private Throwable sendRequest() throws JsonProcessingException {
    // Flux d'entrée
    final String triggerMsgStr = environment.objectMapper.writeValueAsString(environment.pidu_env);

    Future<?> future = environment.initiatrice.requestFuture(triggerMsgStr);

    Throwable thrown = null;
    try {
      future.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
    } catch (Exception e) {
      thrown = e.getCause();
    }
    return thrown;
  }

  private Throwable sendRequestWithPeerException() throws JsonProcessingException {
    // Flux d'entrée
    final String triggerMsgStr = environment.objectMapper.writeValueAsString(environment.pidu_env);

    executor = Executors.newFixedThreadPool(2);
    environment.initiatrice.requestFuture(triggerMsgStr);
    Future<Exception> future2 =
        executor.submit(() -> ((ImmutableSessionHost) environment.partenaire).getException());

    Throwable thrown = null;
    try {
      thrown = future2.get(executorTimeout, TimeUnit.SECONDS);
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      throw new RuntimeException(e);
    } finally {
      executor.shutdown();
    }
    return thrown;
  }

  private Throwable sendRequestAndGetAnswer() throws JsonProcessingException {
    // Flux d'entrée
    final String triggerMsgStr = environment.objectMapper.writeValueAsString(environment.pidu_env);

    executor = Executors.newFixedThreadPool(2);
    Future<?> future1 = environment.initiatrice.requestFuture(triggerMsgStr);
    Future<String> future2 = executor.submit(() -> environment.partenaire.indication());

    Throwable thrown = null;
    String receivedPidu = null;
    try {
      receivedPidu = future2.get(executorTimeout, TimeUnit.SECONDS);
      future1.cancel(true);
    } catch (TimeoutException e) {
      future2.cancel(true);
    } catch (Exception e) {
      thrown = e.getCause();
    } finally {
      executor.shutdown();
    }

    InterfaceDataUnit23Dto pidu_env;
    if (receivedPidu != null) {
      pidu_env = environment.objectMapper.readValue(receivedPidu, InterfaceDataUnit23Dto.class);
    } else {
      pidu_env = null;
    }
    environment.setPidu_res(pidu_env);

    return thrown;
  }

  private Throwable sendRequestAndResponse(Boolean hasValidTrackingNumber, Boolean cleanSession)
      throws JsonProcessingException {
    // Flux d'entrée
    final String triggerMsgStr = environment.objectMapper.writeValueAsString(environment.pidu_env);

    final ExecutorService executor = Executors.newFixedThreadPool(3);
    Future<?> future1 = environment.initiatrice.requestFuture(triggerMsgStr);
    Future<String> future2 = executor.submit(() -> environment.partenaire.indication());

    String receivedPidu = "";
    try {
      receivedPidu = future2.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException | InterruptedException | ExecutionException e) {
      future2.cancel(true);
    }

    InterfaceDataUnit23Dto pidu_env = environment.objectMapper.readValue(receivedPidu,
        InterfaceDataUnit23Dto.class);
    environment.pidu_env_received = pidu_env;

    if (hasValidTrackingNumber) {
      environment.createPiduRec(pidu_env.getContext().getTrackingNumber());
    } else {
      environment.createPiduRec(UUID.randomUUID());
    }

    if (cleanSession) {
      environment.partenaire.clearSessionStates();
    }

    final String pidu_rec = environment.objectMapper.writeValueAsString(environment.getPidu_res());

    Future<?> future3 =
        executor.submit(() -> environment.partenaire.response(pidu_rec));

    Throwable thrown = null;
    try {
      future3.get(executorTimeout, TimeUnit.SECONDS);
      future1.cancel(true);
    } catch (TimeoutException e) {
      future3.cancel(true);
    } catch (Exception e) {
      thrown = e.getCause();
    }

    return thrown;
  }

  private Throwable sendRequestAndResponseAndGetAnswer(Boolean getAnswer)
      throws JsonProcessingException {
    // Flux d'entrée
    final String triggerMsgStr = environment.objectMapper.writeValueAsString(environment.pidu_env);

    final ExecutorService executor = Executors.newFixedThreadPool(4);
    Future<?> future1 = environment.initiatrice.requestFuture(triggerMsgStr);
    Future<String> future2 = executor.submit(() -> environment.partenaire.indication());

    String receivedRequestPidu = "";
    try {
      receivedRequestPidu = future2.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException | InterruptedException | ExecutionException e) {
      future2.cancel(true);
    }

    InterfaceDataUnit23Dto pidu_env = environment.objectMapper.readValue(receivedRequestPidu,
        InterfaceDataUnit23Dto.class);
    environment.pidu_env_received = pidu_env;
    environment.createPiduRec(pidu_env.getContext().getTrackingNumber());

    final String pidu_rec = environment.objectMapper.writeValueAsString(environment.getPidu_res());

    Future<?> future3 =
        executor.submit(() -> environment.partenaire.response(pidu_rec));

    try {
      future3.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException | InterruptedException | ExecutionException e) {
      future3.cancel(true);
    }

    String receivedResponsePidu = "";
    Throwable thrown = null;
    Future<String> future4 = executor.submit(() -> environment.initiatrice.confirm());
    try {
      receivedResponsePidu = future4.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future4.cancel(true);
    } catch (Exception e) {
      thrown = e.getCause();
    }

    try {
      future1.get(executorTimeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future1.cancel(true);
    } catch (Exception e) {
      thrown = e.getCause();
    }

    if (getAnswer) {
      InterfaceDataUnit23Dto pidu_res = environment.objectMapper.readValue(receivedResponsePidu,
          InterfaceDataUnit23Dto.class);
      environment.pidu_res_received = pidu_res;
      environment.nbMsg++;
    }

    return thrown;
  }

  private Answer<Void> normalAnswer(BlockingQueue<String> queue) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto idu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto sdu = environment.objectMapper.readValue(idu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(sdu.getHeader().getMsgtype(), outMsg);
        if (!queue.offer(outMsg)) {
          throw new RuntimeException("message not added to the queue.");
        }
        return null;
      }
    };
  }

  private Answer<Void> interruptAnswer(BlockingQueue<String> queue, HeaderDto.Msgtype msgType,
      SessionHost sessionHost, Boolean mustSendMsg) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto idu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto sdu = environment.objectMapper.readValue(idu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(sdu.getHeader().getMsgtype(), outMsg);

        if (!sdu.getHeader().getMsgtype().equals(msgType) || mustSendMsg) {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        } else {
          sessionHost.closePreservingSessionStates();
        }

        return null;
      }
    };
  }

  private Answer<Void> invalidStampAnswer(BlockingQueue<String> queue, HeaderDto.Msgtype msgType) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto sidu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(spdu.getHeader().getMsgtype(), outMsg);

        if (spdu.getHeader().getMsgtype().equals(msgType)) {
          final ProtocolDataUnit3SESDto invalidStamp =
              new ProtocolDataUnit3SESDto(spdu.getHeader(), "invalidStamp", spdu.getContent());
          final InterfaceDataUnit34Dto invalidStampIdu =
              new InterfaceDataUnit34Dto(sidu.getContext(),
                  environment.objectMapper.writeValueAsString(invalidStamp));
          if (!queue.offer(environment.objectMapper.writeValueAsString(invalidStampIdu))) {
            throw new RuntimeException("message not added to the queue.");
          }
        } else {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        }

        return null;
      }
    };
  }

  private Answer<Void> invalidSessionAnswer(BlockingQueue<String> queue) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto sidu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(spdu.getHeader().getMsgtype(), outMsg);

        if (spdu.getHeader().getMsgtype().equals(SES_FIN_REC)) {
          final SesFinRecDto sdu = environment.objectMapper.readValue((String) spdu.getContent(),
              SesFinRecDto.class);
          final SesFinRecDto sesFinRecDto = new SesFinRecDto(sdu.getToken(), UUID.randomUUID());

          final ProtocolDataUnit3SESDto invalid =
              new ProtocolDataUnit3SESDto(spdu.getHeader(), spdu.getStamp(),
                  environment.objectMapper.writeValueAsString(sesFinRecDto));
          final InterfaceDataUnit34Dto invalidIdu =
              new InterfaceDataUnit34Dto(sidu.getContext(),
                  environment.objectMapper.writeValueAsString(invalid));
          if (!queue.offer(environment.objectMapper.writeValueAsString(invalidIdu))) {
            throw new RuntimeException("message not added to the queue.");
          }
        } else {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        }

        return null;
      }
    };
  }

  private Answer<Void> invalidSessionAnswerToken(BlockingQueue<String> queue) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto sidu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(spdu.getHeader().getMsgtype(), outMsg);

        if (spdu.getHeader().getMsgtype().equals(SES_FIN_REC)) {
          final SesFinRecDto content =
              environment.objectMapper.readValue((String) spdu.getContent(), SesFinRecDto.class);
          final SesFinRecDto sesFinRecDto =
              new SesFinRecDto(UUID.randomUUID(), content.getSession());

          final String serialSdu = environment.objectMapper.writeValueAsString(sesFinRecDto);
          final Map<SessionId, SessionInformation> map =
              getSessionInformation(environment.initiatrice, SessionInformationField.client);
          final SessionInformation sessionInformation =
              map.get(new SessionId(content.getSession()));
          final String stamp =
              new SealCreator().createSymmetricalSeal(serialSdu, sessionInformation.skak,
                  environment.objectMapper);

          final ProtocolDataUnit3SESDto invalid =
              new ProtocolDataUnit3SESDto(spdu.getHeader(), stamp, serialSdu);
          final InterfaceDataUnit34Dto invalidIdu =
              new InterfaceDataUnit34Dto(sidu.getContext(),
                  environment.objectMapper.writeValueAsString(invalid));
          if (!queue.offer(environment.objectMapper.writeValueAsString(invalidIdu))) {
            throw new RuntimeException("message not added to the queue.");
          }
        } else {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        }

        return null;
      }
    };
  }

  private Answer<Void> invalidSkakAnswer(BlockingQueue<String> queue, HeaderDto.Msgtype msgType) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto sidu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(spdu.getHeader().getMsgtype(), outMsg);

        if (spdu.getHeader().getMsgtype().equals(msgType)) {
          if (msgType.equals(SES_SAK_ENV)) {
            final SesSakEnvDto sdu =
                environment.objectMapper.readValue((String) spdu.getContent(), SesSakEnvDto.class);
            final String choice = "invalidSkak";
            final String invalidSkakSdu = environment.objectMapper.writeValueAsString(
                new SesSakEnvDto(choice, sdu.getSession()));
            final ProtocolDataUnit3SESDto invalidSkakPdu =
                new ProtocolDataUnit3SESDto(spdu.getHeader(),
                    new SealCreator().createSeal(invalidSkakSdu,
                        environment.clientParameters.certificatePrivateKeysEntry().getPrivateKey(),
                        environment.objectMapper),
                    invalidSkakSdu);
            final InterfaceDataUnit34Dto invalidSkakIdu =
                new InterfaceDataUnit34Dto(sidu.getContext(),
                    environment.objectMapper.writeValueAsString(invalidSkakPdu));
            if (!queue.offer(environment.objectMapper.writeValueAsString(invalidSkakIdu))) {
              throw new RuntimeException("message not added to the queue.");
            }
          } else if (msgType.equals(SES_SAK_REC)) {
            final SesSakRecDto sdu =
                environment.objectMapper.readValue((String) spdu.getContent(), SesSakRecDto.class);
            final String choice = "invalidSkak";
            final String invalidSkakSdu = environment.objectMapper.writeValueAsString(
                new SesSakRecDto(choice, sdu.getSession()));
            final ProtocolDataUnit3SESDto invalidSkakPdu =
                new ProtocolDataUnit3SESDto(spdu.getHeader(),
                    new SealCreator().createSeal(invalidSkakSdu,
                        environment.serverParameters.certificatePrivateKeysEntry().getPrivateKey(),
                        environment.objectMapper),
                    invalidSkakSdu);
            final InterfaceDataUnit34Dto invalidSkakIdu =
                new InterfaceDataUnit34Dto(sidu.getContext(),
                    environment.objectMapper.writeValueAsString(invalidSkakPdu));
            if (!queue.offer(environment.objectMapper.writeValueAsString(invalidSkakIdu))) {
              throw new RuntimeException("message not added to the queue.");
            }
          }
        } else {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        }

        return null;
      }
    };
  }

  private Answer<Void> invalidSdekAnswer(BlockingQueue<String> queue) {
    return new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        final String outMsg = invocationOnMock.getArgument(0);

        final InterfaceDataUnit34Dto sidu =
            environment.objectMapper.readValue(outMsg, InterfaceDataUnit34Dto.class);

        final ProtocolDataUnit3SESDto spdu = environment.objectMapper.readValue(sidu.getMessage(),
            ProtocolDataUnit3SESDto.class);
        environment.sidu.put(spdu.getHeader().getMsgtype(), outMsg);

        if (spdu.getHeader().getMsgtype().equals(SES_CLE_REC)) {
          final SesCleRecDto sdu =
              environment.objectMapper.readValue((String) spdu.getContent(), SesCleRecDto.class);
          final String content = "invalidSdek";
          final String invalidSdekSdu = environment.objectMapper.writeValueAsString(
              new SesCleRecDto(content, sdu.getSession()));
          Map<SessionId, SessionInformation> map =
              getSessionInformation(environment.initiatrice, SessionInformationField.client);
          final SessionInformation sessionInformation =
              map.get(new SessionId(sdu.getSession()));
          final ProtocolDataUnit3SESDto invalidSdekPdu =
              new ProtocolDataUnit3SESDto(spdu.getHeader(),
                  new SealCreator().createSymmetricalSeal(invalidSdekSdu,
                      sessionInformation.skak, environment.objectMapper),
                  invalidSdekSdu);

          final InterfaceDataUnit34Dto invalidSdekIdu =
              new InterfaceDataUnit34Dto(sidu.getContext(),
                  environment.objectMapper.writeValueAsString(invalidSdekPdu));
          if (!queue.offer(environment.objectMapper.writeValueAsString(invalidSdekIdu))) {
            throw new RuntimeException("message not added to the queue.");
          }
        } else {
          if (!queue.offer(outMsg)) {
            throw new RuntimeException("message not added to the queue.");
          }
        }

        return null;
      }
    };
  }
}
