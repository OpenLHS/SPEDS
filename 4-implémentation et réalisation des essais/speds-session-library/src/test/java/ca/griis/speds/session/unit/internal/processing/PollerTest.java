package ca.griis.speds.session.unit.internal.processing;

import static org.mockito.Mockito.*;

import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.processing.MessageDispatcher;
import ca.griis.speds.session.internal.processing.Poller;
import ca.griis.speds.transport.api.TransportHost;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PollerTest {

  private TransportHost transportHost;
  private MessageDispatcher dispatcher;
  private Poller poller;

  @BeforeEach
  void setup() {
    transportHost = mock(TransportHost.class);
    dispatcher = mock(MessageDispatcher.class);
    poller = new Poller(transportHost, dispatcher, Duration.ofMillis(200));
  }

  @Test
  void testStartAndStop_doesNotCrash() throws Exception {
    when(transportHost.dataReply())
        .thenReturn(null)
        .thenAnswer(inv -> {
          Thread.sleep(100);
          return null;
        });

    poller.start();
    TimeUnit.MILLISECONDS.sleep(250); // Donne le temps au poller de s’exécuter un peu
    poller.stop();

    // Pas d’exception = succès
    verify(transportHost, atLeastOnce()).dataReply();
  }

  @Test
  void testPollLoop_dispatchesMessage() throws Exception {
    when(transportHost.dataReply())
        .thenReturn("message")
        .thenAnswer(inv -> {
          Thread.sleep(100);
          return null;
        });

    poller.start();
    TimeUnit.MILLISECONDS.sleep(300);
    poller.stop();

    verify(dispatcher, atLeastOnce()).dispatch("message");
  }

  @Test
  void testPollLoop_handlesDeserializationException() throws Exception {
    when(transportHost.dataReply()).thenThrow(new DeserializationException("bad"));

    poller.start();
    TimeUnit.MILLISECONDS.sleep(300);
    poller.stop();

    // Exception est loggée, mais le poller ne crash pas
    verify(dispatcher, never()).dispatch(any());
  }

  @Test
  void testRegisterHandlers_forwardsCall() {
    HandlerRegistry registry = mock(HandlerRegistry.class);
    poller.registerHandlers(registry);

    verify(dispatcher).registerHandlers(registry);
  }
}
