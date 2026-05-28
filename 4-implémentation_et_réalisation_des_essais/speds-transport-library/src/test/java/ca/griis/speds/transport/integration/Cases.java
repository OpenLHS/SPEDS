package ca.griis.speds.transport.integration;

import static ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto.Msgtype.TRA_MSG_ENV;
import static ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto.Msgtype.TRA_MSG_REC;
import static ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive.CONFIRM;
import static ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive.INDICATION;
import static ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive.REQUEST;
import static ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive.RESPONSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.mockito.ArgumentCaptor;

public class Cases {
  private static final Long tmax = 5000L;
  private static final String sourceCode = "source_code";
  private static final String destinationCode = "destination_code";
  private static final String sourceIri = "https://source.iri:8080";
  private static final String destinationIri = "https://destination.iri::8081";
  private static final Context45Dto.Service transfer = Context45Dto.Service.TRANSFER;
  private static final String message = "Message from the session layer";
  private static final String succeed = "SUCCEED";
  private static final String failed = "FAILED: cause of failure";
  private static final String failedHeader = "FAILED: Header seal are not the same";
  private static final String failedContent = "FAILED: Content seal are not the same";
  private static final String traMsgRecContent = "";
  private static final ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();

  public static void ct_01(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final String given_e1 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, REQUEST, destinationIri, false),
        message));

    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            CONFIRM, environment.getOptions()), succeed));

    final Header45Dto header_e3 = new Header45Dto(TRA_MSG_REC, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_e3 = mapper.writeValueAsBytes(header_e3);
    final String headerSeal_e3 = hash(environment, serializedHeader_e3);
    final String contentSeal_e3 =
        hash(environment, traMsgRecContent.getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_e3,
        new StampDto(headerSeal_e3, contentSeal_e3), traMsgRecContent));
    final String given_e3 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgRec));

    // Sorties
    final Header45Dto header_s1 = new Header45Dto(TRA_MSG_ENV, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_s1 = mapper.writeValueAsBytes(header_s1);
    final String headerSeal_s1 = hash(environment, serializedHeader_s1);
    final String contentSeal_s1 = hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgEnv = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_s1,
        new StampDto(headerSeal_s1, contentSeal_s1), message));
    final String expected_s1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            REQUEST, false), traMsgEnv));

    final String expected_s2 =
        mapper.writeValueAsString(new InterfaceDataUnit34Dto(
            new Context34Dto(sourceCode, destinationCode, sourceIri,
                Context34Dto.Service.TRANSFER,
                CONFIRM, destinationIri, false),
            succeed));


    final String expected_s3 =
        mapper.writeValueAsString(new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri,
                Context45Dto.Service.TRANSFER,
                RESPONSE, environment.getOptions()),
            succeed));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Mock de la confirmation de réseau lors de l'envoi du request
    when(environment.getNetworkHost().submitIdu(expected_s1))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    final var future = environment.getClient().submitIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification du TIDU de requête
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);

    // Réception d'une indication - message TRA.MSG.REC
    environment.networkHostNotifyIdu(given_e3);

    final String actual_s2 = future.get(tmax, TimeUnit.MILLISECONDS).get();
    assertEquals(expected_s2, actual_s2);

    // Vérification de la réponse à la couche réseau
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s3 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s3, actual_s3);
  }

  public static void ct_02(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final String given_e1 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, REQUEST, destinationIri, false),
        message));

    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            CONFIRM, false), failed));

    // Sorties
    final Header45Dto header_s1 = new Header45Dto(
        TRA_MSG_ENV, environment.getMessageId(), sourceCode, destinationCode, version);
    final var serializedHeader_s1 = mapper.writeValueAsBytes(header_s1);
    final String headerSeal_s1 = hash(environment, serializedHeader_s1);
    final String contentSeal_s1 = hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgEnv = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_s1,
        new StampDto(headerSeal_s1, contentSeal_s1), message));
    final String expected_s1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            REQUEST, false), traMsgEnv));

    final String expected_s2 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, CONFIRM, destinationIri, false),
        failed));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Mock de la confirmation de réseau lors de l'envoi du request
    when(environment.getNetworkHost().submitIdu(expected_s1))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    final var future = environment.getClient().submitIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification du TIDU de requête
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);

    // Vérification du TSDU de confirmation
    final String actual_s2 = future.get(tmax, TimeUnit.MILLISECONDS).get();
    assertEquals(expected_s2, actual_s2);
  }

  public static void ct_03(Environment environment) throws JsonProcessingException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final Header45Dto header_e1 = new Header45Dto(TRA_MSG_REC, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_e1 = mapper.writeValueAsBytes(header_e1);
    final String headerSeal_e1 = hash(environment, serializedHeader_e1);
    final String contentSeal_e1 =
        hash(environment, traMsgRecContent.getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_e1,
        new StampDto(headerSeal_e1, contentSeal_e1), traMsgRecContent));
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgRec));

    final String expected_s1 =
        mapper.writeValueAsString(new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, Context45Dto.Service.TRANSFER, RESPONSE,
                environment.getOptions()),
            "FAILED: Unknown message id"));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Réception d'une indication - message TRA.MSG.REC
    environment.networkHostNotifyIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification de la réponse à la couche réseau
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);
  }

  public static void ct_04(Environment environment) throws JsonProcessingException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final String given_e1 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, REQUEST, destinationIri, false),
        message));

    final Header45Dto header_e2 = new Header45Dto(TRA_MSG_REC, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final String headerSeal_e2 = hash(environment, "invalid".getBytes(StandardCharsets.UTF_8));
    final String contentSeal_e2 =
        hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_e2,
        new StampDto(headerSeal_e2, contentSeal_e2), traMsgRecContent));
    final String given_e2 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgRec));

    // Sorties
    final Header45Dto header_s1 = new Header45Dto(TRA_MSG_ENV, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_s1 = mapper.writeValueAsBytes(header_s1);
    final String headerSeal_s1 = hash(environment, serializedHeader_s1);
    final String contentSeal_s1 = hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgEnv = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_s1,
        new StampDto(headerSeal_s1, contentSeal_s1), message));
    final String expected_s1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            REQUEST, false), traMsgEnv));

    final String expected_s2 = mapper.writeValueAsString(new InterfaceDataUnit45Dto(
        new Context45Dto(sourceIri, destinationIri, Context45Dto.Service.TRANSFER, RESPONSE,
            environment.getOptions()),
        failedHeader));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Mock de la confirmation de réseau lors de l'envoi du request
    when(environment.getNetworkHost().submitIdu(expected_s1))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e2)));

    environment.getClient().submitIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification du TIDU de requête
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);

    // Réception d'une indication - message TRA.MSG.REC
    environment.networkHostNotifyIdu(given_e2);

    // Vérification de la réponse à la couche réseau
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s2 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s2, actual_s2);
  }

  public static void ct_05(Environment environment) throws JsonProcessingException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final Header45Dto header_e1 = new Header45Dto(TRA_MSG_ENV, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final String headerSeal_e1 = hash(environment, "invalid".getBytes(StandardCharsets.UTF_8));
    final String contentSeal_e1 =
        hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_e1,
        new StampDto(headerSeal_e1, contentSeal_e1), traMsgRecContent));
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgRec));

    // Sorties
    final String expected_s1 = mapper.writeValueAsString(new InterfaceDataUnit45Dto(
        new Context45Dto(sourceIri, destinationIri, Context45Dto.Service.TRANSFER, RESPONSE,
            environment.getOptions()),
        failedHeader));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Réception d'une indication - message TRA.MSG.ENV
    environment.networkHostNotifyIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification de la réponse à la couche réseau
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);
  }

  public static void ct_06(Environment environment) throws JsonProcessingException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final Header45Dto header_s1 = new Header45Dto(TRA_MSG_ENV, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_s1 = mapper.writeValueAsBytes(header_s1);
    final String headerSeal_e1 = hash(environment, serializedHeader_s1);
    final String contentSeal_e1 =
        hash(environment, "invalid".getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_s1,
        new StampDto(headerSeal_e1, contentSeal_e1), traMsgRecContent));
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgRec));

    // Sorties
    final String expected_s1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, Context45Dto.Service.TRANSFER, RESPONSE,
                environment.getOptions()),
            failedContent));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Réception d'une indication - message TRA.MSG.ENV
    environment.networkHostNotifyIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification de la réponse à la couche réseau
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);
  }

  public static void ct_07(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    final var version = new Speds45Dto(environment.getVersion(), environment.getReference());

    // Entrées
    final Header45Dto header_e1 = new Header45Dto(TRA_MSG_ENV, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_e1 = mapper.writeValueAsBytes(header_e1);
    final String headerSeal_e1 = hash(environment, serializedHeader_e1);
    final String contentSeal_e1 =
        hash(environment, message.getBytes(StandardCharsets.UTF_8));
    final String traMsgEnv = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_e1,
        new StampDto(headerSeal_e1, contentSeal_e1), message));
    final String given_e1 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, transfer, INDICATION,
                environment.getOptions()),
            traMsgEnv));

    final String given_e2 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, RESPONSE, destinationIri, environment.getOptions()),
        message));

    final String given_e3 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(sourceIri, destinationIri, transfer,
            CONFIRM, false), succeed));

    // Sorties
    final String expected_s1 =
        mapper.writeValueAsString(new InterfaceDataUnit45Dto(
            new Context45Dto(sourceIri, destinationIri, Context45Dto.Service.TRANSFER, RESPONSE,
                environment.getOptions()),
            succeed));

    final String expected_s2 =
        mapper.writeValueAsString(new InterfaceDataUnit34Dto(
            new Context34Dto(sourceCode, destinationCode, sourceIri,
                Context34Dto.Service.TRANSFER, INDICATION, destinationIri,
                environment.getOptions()),
            message));

    final Header45Dto header_s3 = new Header45Dto(TRA_MSG_REC, environment.getMessageId(),
        sourceCode, destinationCode, version);
    final var serializedHeader_s3 = mapper.writeValueAsBytes(header_s3);
    final String headerSeal_s3 = hash(environment, serializedHeader_s3);
    final String contentSeal_s3 =
        hash(environment, traMsgRecContent.getBytes(StandardCharsets.UTF_8));
    final String traMsgRec = mapper.writeValueAsString(new ProtocolDataUnit4TraDto(header_s3,
        new StampDto(headerSeal_s3, contentSeal_s3), traMsgRecContent));
    final String expected_s3 = mapper.writeValueAsString(
        new InterfaceDataUnit45Dto(new Context45Dto(destinationIri, sourceIri, transfer,
            REQUEST, false), traMsgRec));

    final var iduCaptor = ArgumentCaptor.forClass(String.class);

    // Réception d'une indication - message TRA.MSG.ENV
    environment.networkHostNotifyIdu(given_e1);
    final Instant start = Instant.now();

    // Vérification du TIDU de réponse

    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());
    final String actual_s1 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s1, actual_s1);

    // Vérification du TSDU de réponse
    String actual_s2 = environment.getServerResult(tmax);
    assertEquals(expected_s2, actual_s2);

    // Mock de la confirmation de réseau lors de l'envoi du request
    when(environment.getNetworkHost().submitIdu(expected_s1))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(given_e3)));

    // Envoi de réponse TSDU
    final var future = environment.getServer().submitIdu(given_e2);
    future.get(tmax, TimeUnit.MILLISECONDS);

    // Vérification du TIDU de requête
    verify(environment.getNetworkHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    final String actual_s3 = iduCaptor.getValue();
    assertDuration(start);
    assertEquals(expected_s3, actual_s3);
  }

  public static void ct_08(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException, TimeoutException {
    // Entrées
    final String given_e1 = mapper.writeValueAsString(new InterfaceDataUnit34Dto(
        new Context34Dto(sourceCode, destinationCode, sourceIri,
            Context34Dto.Service.TRANSFER, RESPONSE, destinationIri, environment.getOptions()),
        message));

    // Envoi de la réponse
    var result = environment.getServer().submitIdu(given_e1).get(tmax, TimeUnit.MILLISECONDS);
    verify(environment.getNetworkHost(), timeout(1000).times(0)).submitIdu(any());

    assertTrue(result.isEmpty());
  }

  private static String hash(Environment environment, byte[] bytes) {
    return Base64.getEncoder().encodeToString(
        environment.getCryptographyService().hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT, bytes));
  }

  private static void assertDuration(Instant start) {
    final Instant end = Instant.now();
    final Duration duration = Duration.between(start, end);
    assertTrue(duration.toMillis() <= tmax);
  }
}
