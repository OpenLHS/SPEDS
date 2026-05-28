package ca.griis.speds.presentation.integration;

import static ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto.Msgtype.PRE_MSG_ENV;
import static ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive.CONFIRM;
import static ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive.INDICATION;
import static ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive.REQUEST;
import static ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive.RESPONSE;
import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.PRESENTATION;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ProtocolDataUnit2PreDto;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.mockito.ArgumentCaptor;

public class Cases {

  private static final String PGA = "PGA";
  private static final String SOURCE_CODE = "source_code";
  private static final String DESTINATION_CODE = "destination_code";
  private static final String DELEGATE = "delegate";
  private static final String TRANSFER = "transfer";
  private static final String MESSAGE = "Message from the application layer";
  private static final String SUCCEED = "SUCCEED";
  private static final String FAILED = "FAILED:";

  private static final String TRACKING_NUMBER_TAG = "TN";
  private static final String SERVER_TRACKING_FIELD = "serverTracking";
  private static final UUID MESSAGE_ID = UUID.randomUUID();
  private static final String SESSION_MESSAGE_ID = UUID.randomUUID().toString();

  public static void ct_01(Environment environment) throws Exception {
    final String given_e01 =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(new InterfaceDataUnit12Dto(
            new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, TRANSFER, REQUEST, false),
            MESSAGE));

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    assertDoesNotThrow(() -> environment.getPresentationHost().submitIdu(given_e01));

    verify(environment.getSessionHost()).submitIdu(iduCaptor.capture());

    final InterfaceDataUnit23Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(iduCaptor.getValue(), InterfaceDataUnit23Dto.class);
    final byte[] sdekEncoded = Base64.getDecoder().decode(actual_idu.getContext().getSdek());
    final SecretKey sdek = new SecretKeySpec(sdekEncoded, 0, sdekEncoded.length, "AES");
    final ProtocolDataUnit2PreDto actual_pdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(actual_idu.getMessage(), ProtocolDataUnit2PreDto.class);

    final byte[] actual_message = environment.getCryptographyService().decryptSymmetric(
        PRESENTATION, sdek,
        Base64.getDecoder().decode(actual_pdu.getContent()));

    assertArrayEquals(MESSAGE.getBytes(StandardCharsets.UTF_8), actual_message);

    final Context23Dto expectedContext23 = new Context23Dto(PGA, SOURCE_CODE, DESTINATION_CODE,
        actual_idu.getContext().getSdek(), DELEGATE, REQUEST, false);
    assertEquals(expectedContext23, actual_idu.getContext());

    final HeaderDto expectedHeader =
        new HeaderDto(PRE_MSG_ENV, actual_pdu.getHeader().getId(), false, environment.getVersion());
    assertEquals(expectedHeader, actual_pdu.getHeader());

    assertEquals(REQUEST, actual_idu.getContext().getServicePrimitive());
  }

  @SuppressWarnings("unchecked")
  public static void ct_02(Environment environment) throws Exception {
    final SecretKey givenSdek =
        environment.getCryptographyService().generateSymmetricKey(PRESENTATION);
    final String givenEncrypted = Base64.getEncoder().encodeToString(
        environment.getCryptographyService()
            .encryptSymmetric(PRESENTATION, givenSdek, MESSAGE.getBytes(
                StandardCharsets.UTF_8)));
    final String given_pdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new ProtocolDataUnit2PreDto(new HeaderDto(PRE_MSG_ENV,
            MESSAGE_ID, false, environment.getVersion()), givenEncrypted));
    final Object options = Map.of(TRACKING_NUMBER_TAG, SESSION_MESSAGE_ID);
    final String given_e01 = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit23Dto(
            new Context23Dto(PGA, SOURCE_CODE, DESTINATION_CODE,
                Base64.getEncoder().encodeToString(givenSdek.getEncoded()), TRANSFER, INDICATION,
                options),
            given_pdu));

    assertDoesNotThrow(() -> environment.getSessionHostEvent().notifyIdu(given_e01));

    final String actual_s01 = environment.getHostResult();
    final InterfaceDataUnit12Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(actual_s01, InterfaceDataUnit12Dto.class);

    final Context12Dto expectedContext12 =
        new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, TRANSFER, INDICATION,
            Map.of(TRACKING_NUMBER_TAG, MESSAGE_ID.toString()));
    assertEquals(expectedContext12, actual_idu.getContext());

    assertEquals(MESSAGE, actual_idu.getMessage());

    assertEquals(INDICATION, actual_idu.getContext().getServicePrimitive());

    final Field field =
        environment.getPresentationHost().getClass().getDeclaredField(SERVER_TRACKING_FIELD);
    field.setAccessible(true);
    final Cache<PresentationTracking, TrackingInformation> serverTracking =
        (Cache<PresentationTracking, TrackingInformation>) field.get(
            environment.getPresentationHost());
    final TrackingInformation trackingInformation =
        serverTracking.asMap().get(new PresentationTracking(MESSAGE_ID));

    assertEquals(givenSdek, trackingInformation.sdek());
  }

  @SuppressWarnings("unchecked")
  public static void ct_03(Environment environment) throws Exception {
    final String given_e01 =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(new InterfaceDataUnit12Dto(
            new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, TRANSFER, RESPONSE,
                Map.of(TRACKING_NUMBER_TAG, MESSAGE_ID.toString())),
            SUCCEED));

    final Field field =
        environment.getPresentationHost().getClass().getDeclaredField(SERVER_TRACKING_FIELD);
    field.setAccessible(true);
    final Cache<PresentationTracking, TrackingInformation> serverTracking =
        (Cache<PresentationTracking, TrackingInformation>) field.get(
            environment.getPresentationHost());
    final TrackingInformation trackingInformation =
        serverTracking.asMap().get(new PresentationTracking(MESSAGE_ID));
    final SecretKey sdek = trackingInformation.sdek();

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    assertDoesNotThrow(() -> environment.getPresentationHost().submitIdu(given_e01));

    verify(environment.getSessionHost(), atLeastOnce()).submitIdu(iduCaptor.capture());

    final InterfaceDataUnit23Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(iduCaptor.getValue(), InterfaceDataUnit23Dto.class);

    final String serializedSdek = Base64.getEncoder().encodeToString(sdek.getEncoded());
    final Context23Dto expectedContext23 = new Context23Dto(PGA, SOURCE_CODE, DESTINATION_CODE,
        serializedSdek, TRANSFER, RESPONSE, Map.of(TRACKING_NUMBER_TAG, SESSION_MESSAGE_ID));
    assertEquals(expectedContext23, actual_idu.getContext());
    assertEquals(SUCCEED, actual_idu.getMessage());

    assertEquals(RESPONSE, actual_idu.getContext().getServicePrimitive());

    assertEquals(serializedSdek, actual_idu.getContext().getSdek());
  }

  public static void ct_04(Environment environment) throws Exception {
    final String request =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(new InterfaceDataUnit12Dto(
            new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, TRANSFER, REQUEST, false),
            MESSAGE));

    final SecretKey givenSdek =
        environment.getCryptographyService().generateSymmetricKey(PRESENTATION);
    final String given_e01 = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit23Dto(
            new Context23Dto(SOURCE_CODE, DESTINATION_CODE,
                Base64.getEncoder().encodeToString(givenSdek.getEncoded()), DELEGATE, CONFIRM,
                false),
            SUCCEED));

    when(environment.getSessionHost().submitIdu(anyString())).thenReturn(
        CompletableFuture.completedFuture(Optional.of(given_e01)));

    CompletableFuture<Optional<String>> result =
        assertDoesNotThrow(() -> environment.getPresentationHost().submitIdu(request));

    final String actual_s01 = result.get().orElseThrow(AssertionError::new);
    final InterfaceDataUnit12Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(actual_s01, InterfaceDataUnit12Dto.class);

    final Context12Dto expectedContext12 =
        new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, DELEGATE, CONFIRM, false);
    assertEquals(expectedContext12, actual_idu.getContext());
    assertEquals(SUCCEED, actual_idu.getMessage());

    assertEquals(CONFIRM, actual_idu.getContext().getServicePrimitive());
  }

  public static void ct_05(Environment environment) throws Exception {
    final SecretKey givenSdek =
        environment.getCryptographyService().generateSymmetricKey(PRESENTATION);
    final String givenEncrypted = Base64.getEncoder().encodeToString(
        environment.getCryptographyService()
            .encryptSymmetric(PRESENTATION, givenSdek, MESSAGE.getBytes(
                StandardCharsets.UTF_8)));
    final SecretKey wrongSdek =
        environment.getCryptographyService().generateSymmetricKey(PRESENTATION);
    final String given_pdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new ProtocolDataUnit2PreDto(new HeaderDto(PRE_MSG_ENV,
            MESSAGE_ID, false, environment.getVersion()), givenEncrypted));
    final Object options = Map.of(TRACKING_NUMBER_TAG, SESSION_MESSAGE_ID);
    final String given_e01 = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit23Dto(
            new Context23Dto(PGA, SOURCE_CODE, DESTINATION_CODE,
                Base64.getEncoder().encodeToString(wrongSdek.getEncoded()), TRANSFER, INDICATION,
                options),
            given_pdu));

    final ArgumentCaptor<String> iduCaptor = ArgumentCaptor.forClass(String.class);

    assertDoesNotThrow(() -> environment.getSessionHostEvent().notifyIdu(given_e01));

    verify(environment.getSessionHost()).submitIdu(iduCaptor.capture());

    final InterfaceDataUnit23Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(iduCaptor.getValue(), InterfaceDataUnit23Dto.class);

    assertTrue(actual_idu.getMessage().startsWith(FAILED));

    assertEquals(RESPONSE, actual_idu.getContext().getServicePrimitive());
  }

  public static void ct_06(Environment environment) throws Exception {
    final String given_e01 =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(new InterfaceDataUnit12Dto(
            new Context12Dto(PGA, SOURCE_CODE, DESTINATION_CODE, TRANSFER, REQUEST, false),
            null));

    CompletableFuture<Optional<String>> result =
        assertDoesNotThrow(() -> environment.getPresentationHost().submitIdu(given_e01));

    final String actual_s01 = result.get().orElseThrow(AssertionError::new);

    final InterfaceDataUnit12Dto actual_idu = SharedObjectMapper.getInstance().getMapper()
        .readValue(actual_s01, InterfaceDataUnit12Dto.class);

    assertTrue(actual_idu.getMessage().startsWith(FAILED));

    assertEquals(CONFIRM, actual_idu.getContext().getServicePrimitive());
  }
}
