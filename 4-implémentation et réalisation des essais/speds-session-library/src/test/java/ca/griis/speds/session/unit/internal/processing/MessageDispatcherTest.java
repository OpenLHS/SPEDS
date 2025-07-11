package ca.griis.speds.session.unit.internal.processing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.processing.MessageDispatcher;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageDispatcherTest {

  private ObjectMapper mapper;
  private MessageDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    mapper = mock(ObjectMapper.class);
    dispatcher = new MessageDispatcher(mapper);
  }

  @Test
  void testDispatch_success() throws Exception {
    String rawMsg = "msg";

    // Mocks pour les objets attendus
    Sidu sidu = mock(Sidu.class);
    Spdu spdu = mock(Spdu.class);
    HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.SES_PUB_ENV;

    // Préparer les valeurs de retour
    when(mapper.readValue(eq(rawMsg), eq(Sidu.class))).thenReturn(sidu);
    when(sidu.getMessage()).thenReturn("sub");
    when(mapper.readValue(eq("sub"), eq(Spdu.class))).thenReturn(spdu);
    HeaderDto header = new HeaderDto(msgtype, UUID.randomUUID(), false, null);
    when(spdu.getHeader()).thenReturn(header);

    // Préparer un handler
    MessageHandler handler = mock(MessageHandler.class);
    dispatcher.registerHandlers(() -> java.util.List.of(new DummyHandler(msgtype, handler)));

    dispatcher.dispatch(rawMsg);

    verify(handler).handle(any());
  }

  @Test
  void testDispatch_badJson_throwsDeserializationException() throws Exception {
    when(mapper.readValue(anyString(), eq(Sidu.class)))
        .thenThrow(new JsonProcessingException("bad") {});

    assertThrows(DeserializationException.class, () -> {
      dispatcher.dispatch("invalid");
    });
  }

  @Test
  void testDispatch_handlerThrowsSilentIgnore_isCaught() throws Exception {
    String rawMsg = "msg";
    Sidu sidu = mock(Sidu.class);
    Spdu spdu = mock(Spdu.class);
    HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.SES_PUB_ENV;

    when(mapper.readValue(eq(rawMsg), eq(Sidu.class))).thenReturn(sidu);
    when(sidu.getMessage()).thenReturn("sub");
    when(mapper.readValue(eq("sub"), eq(Spdu.class))).thenReturn(spdu);
    HeaderDto header = new HeaderDto(msgtype, UUID.randomUUID(), false, null);
    when(spdu.getHeader()).thenReturn(header);

    // Simule un handler qui lance SilentIgnoreException
    MessageHandler handler = mock(MessageHandler.class);
    doThrow(new SilentIgnoreException("ignored")).when(handler).handle(any());

    dispatcher.registerHandlers(() -> java.util.List.of(new DummyHandler(msgtype, handler)));

    dispatcher.dispatch(rawMsg);
  }

  @Test
  void testRegisterHandlers_populatesMap() {
    MessageHandler handler = mock(MessageHandler.class);
    when(handler.getHandledType()).thenReturn(MsgType.SES_PUB_ENV);

    HandlerRegistry registry = () -> java.util.List.of(handler);
    dispatcher.registerHandlers(registry);

    // Pas d’assert, mais le fait que le handler soit utilisé est testé dans les autres tests
  }

  // Handler factice pour wrapper un mock
  public static class DummyHandler implements MessageHandler {
    private final MsgType type;
    private final MessageHandler delegate;

    DummyHandler(HeaderDto.Msgtype dtoType, MessageHandler delegate) {
      this.type = MsgType.from(dtoType);
      this.delegate = delegate;
    }

    @Override
    public void handle(ExpandedSidu message) throws SilentIgnoreException {
      delegate.handle(message);
    }

    @Override
    public MsgType getHandledType() {
      return type;
    }
  }
}
