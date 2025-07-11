package ca.griis.speds.session.unit.internal.handler.initiator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.speds.session.internal.contract.SesPubEnvDto;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.initiator.SesCleRecHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SesCleRecHandlerTest {
  private ObjectMapper mapper;
  private BlockingQueue<ExpandedSidu> queue;
  private Map<SessionId, SessionInformation> sessionMap;
  private SesCleRecHandler handler;

  @BeforeEach
  void setUp() {
    mapper = mock(ObjectMapper.class);
    queue = spy(new ArrayBlockingQueue<>(10));
    sessionMap = new HashMap<>();
    handler = new SesCleRecHandler(mapper, queue, sessionMap, new SealVerifier());
  }

  @Test
  void testHandle_validMessage_putsInQueue() throws Exception {
    mapper = SharedObjectMapper.getInstance().getMapper();
    UUID sessionIdStr = UUID.randomUUID();
    SessionId sessionId = new SessionId(sessionIdStr);

    // Spdu
    Spdu spdu = new Spdu(null, "stamp", "{\"session\":\"" + sessionIdStr + "\"}");

    // ExpandedSidu
    ExpandedSidu sidu = mock(ExpandedSidu.class);
    when(sidu.spdu()).thenReturn(spdu);

    // Mock static verifier
    SealVerifier verifier = mock(SealVerifier.class);
    when(verifier.verifySymmetricalSeal(any(), any(), anyString(), any())).thenAnswer(x -> true);
    sessionMap.put(sessionId, new SessionInformation());
    SesCleRecHandler handler = new SesCleRecHandler(mapper, queue, sessionMap, verifier);

    // when
    handler.handle(sidu);

    // then
    verify(queue).put(sidu);
  }

  @Test
  void testHandle_missingSession_throwsSilentIgnore() throws Exception {
    Spdu spdu = mock(Spdu.class);
    when(spdu.getContent()).thenReturn("{\"session\":\"missing\"}");

    ExpandedSidu sidu = mock(ExpandedSidu.class);
    when(sidu.spdu()).thenReturn(spdu);

    SesCleRecDto dto = new SesCleRecDto("", UUID.randomUUID());

    when(mapper.readValue(anyString(), eq(SesCleRecDto.class))).thenReturn(dto);

    assertThrows(SilentIgnoreException.class, () -> handler.handle(sidu));
  }

  @Test
  void testHandle_invalidSeal_throwsSilentIgnore() throws Exception {
    UUID sessionIdStr = UUID.randomUUID();
    SessionId sessionId = new SessionId(sessionIdStr);
    SessionInformation sessionInformation = new SessionInformation();

    Spdu spdu = mock(Spdu.class);
    when(spdu.getContent()).thenReturn("{\"session\":\"" + sessionIdStr + "\"}");
    when(spdu.getStamp()).thenReturn("bad-stamp");

    ExpandedSidu sidu = mock(ExpandedSidu.class);
    when(sidu.spdu()).thenReturn(spdu);

    SesCleRecDto dto = new SesCleRecDto("any", sessionIdStr);
    when(mapper.readValue(anyString(), eq(SesCleRecDto.class))).thenReturn(dto);
    sessionMap.put(sessionId, sessionInformation);

    SealVerifier verifier = mock(SealVerifier.class);
    when(verifier.verifySymmetricalSeal(any(), any(), anyString(), any())).thenAnswer(x -> false);
    SesCleRecHandler handler = new SesCleRecHandler(mapper, queue, sessionMap, verifier);
    assertThrows(SilentIgnoreException.class, () -> handler.handle(sidu));
  }

  @Test
  void testHandle_invalidJson_throwsRuntime() throws Exception {
    Spdu spdu = mock(Spdu.class);
    when(spdu.getContent()).thenReturn("bad-json");

    ExpandedSidu sidu = mock(ExpandedSidu.class);
    when(sidu.spdu()).thenReturn(spdu);

    when(mapper.readValue(anyString(), eq(SesPubEnvDto.class))).thenThrow(
        new JsonProcessingException("bad") {});

    assertThrows(RuntimeException.class, () -> handler.handle(sidu));
  }
}
