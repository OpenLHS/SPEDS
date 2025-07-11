package ca.griis.speds.link.integration.conception;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import ca.griis.js2p.gen.speds.link.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.api.exception.VerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Cases {
  // MD - The sleep factor for which we consider the timeout to be suffisent to garante
  // no messages it received on the other end. Increasing this value increase the confidence
  // in the test at the cost of executiuon time. A full second is considered to be enough.
  private static final int SLEEP_FACTOR = 1000;

  public static void c05_request_invalidIri_fail(Environment environment)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");

    String destination_iri = "garbage iri ";
    UUID trackingNumber = UUID.randomUUID();
    InterfaceDataUnit56Dto interfaceDataUnit56Dto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false), "This is the message");

    String dg_idu5_6_invalid_iri = environment.objMap.writeValueAsString(interfaceDataUnit56Dto);

    Thread serverThread = new Thread(() -> {
      try {
        String received = environment.targetHost.indication();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    serverThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.originHost.request(dg_idu5_6_invalid_iri);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // target host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    serverThread.interrupt();
    serverThread.join();
  }

  public static void c06_request_missingIri_fail(Environment environment)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");

    UUID trackingNumber = UUID.randomUUID();
    InterfaceDataUnit56Dto interfaceDataUnit56Dto = new InterfaceDataUnit56Dto(
        new Context56Dto(null, trackingNumber, false), "This it the message");

    String dg_idu5_6_absent_iri = environment.objMap.writeValueAsString(interfaceDataUnit56Dto);

    Thread serverThread = new Thread(() -> {
      try {
        String received = environment.targetHost.indication();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    serverThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.originHost.request(dg_idu5_6_absent_iri);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // target host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    serverThread.interrupt();
    serverThread.join();
  }

  public static void c07_request_missingSdu_fail(Environment environment)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");

    UUID trackingNumber = UUID.randomUUID();
    String destination_iri = "https://localhost:" + environment.targetPort;
    String nullMessage = null;
    InterfaceDataUnit56Dto interfaceDataUnit56Dto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false), nullMessage);

    String dg_idu5_6_absent_sdu = environment.objMap.writeValueAsString(interfaceDataUnit56Dto);

    Thread serverThread = new Thread(() -> {
      try {
        String received = environment.targetHost.indication();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    serverThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.originHost.request(dg_idu5_6_absent_sdu);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // target host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    serverThread.interrupt();
    serverThread.join();
  }

  public static InterfaceDataUnit56Dto c08_request_success(Environment environment)
      throws JsonProcessingException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");

    UUID trackingNumber = UUID.randomUUID();
    String destination_iri = "https://localhost:" + environment.targetPort;
    InterfaceDataUnit56Dto expectedDto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false), "This is the message");

    String dg_valid_idu5_6 = environment.objMap.writeValueAsString(expectedDto);

    // when
    environment.originHost.request(dg_valid_idu5_6);

    // then
    // target host receives the message
    String result = environment.targetHost.indication();

    // The test doesn't specify to transform into java object, but I'm afraid the string won't
    // quite match characters for characters
    InterfaceDataUnit56Dto actualDto =
        environment.objMap.readValue(result, InterfaceDataUnit56Dto.class);
    assertEquals(expectedDto.getContext().getDestinationIri(),
        actualDto.getContext().getDestinationIri());
    assertEquals(expectedDto.getMessage(), actualDto.getMessage());

    return actualDto;
  }

  public static void c09_response_invalidIri_fail(Environment environment,
      InterfaceDataUnit56Dto incomingServerMessage)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");
    assertNotNull(incomingServerMessage,
        "precondition targetHost must have received a previous message");

    UUID trackingNumber = incomingServerMessage.getContext().getTrackingNumber();
    String destination_iri = "garbage iri";
    InterfaceDataUnit56Dto expectedDto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false),
        incomingServerMessage.getMessage());

    String dg_idu5_6_invalid_iri = environment.objMap.writeValueAsString(expectedDto);

    Thread originThread = new Thread(() -> {
      try {
        String received = environment.originHost.confirm();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    originThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.targetHost.response(dg_idu5_6_invalid_iri);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // origin host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    originThread.interrupt();
    originThread.join();
  }

  public static void c010_response_missingIri_fail(Environment environment,
      InterfaceDataUnit56Dto incomingServerMessage)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");

    UUID trackingNumber = incomingServerMessage.getContext().getTrackingNumber();
    String destination_iri = null;
    InterfaceDataUnit56Dto expectedDto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false),
        incomingServerMessage.getMessage());

    String dg_idu5_6_absent_iri = environment.objMap.writeValueAsString(expectedDto);

    Thread originThread = new Thread(() -> {
      try {
        String received = environment.originHost.confirm();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    originThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.targetHost.response(dg_idu5_6_absent_iri);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // origin host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    originThread.interrupt();
    originThread.join();
  }

  public static void c011_response_missingSdu_fail(Environment environment,
      InterfaceDataUnit56Dto incomingServerMessage)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");
    assertNotNull(incomingServerMessage,
        "precondition targetHost must have received a previous message");

    UUID trackingNumber = incomingServerMessage.getContext().getTrackingNumber();
    String destination_iri = environment.originAddress + ":" + environment.originPort;
    InterfaceDataUnit56Dto expectedDto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false), null);

    String dg_idu5_6_absent_sdu = environment.objMap.writeValueAsString(expectedDto);

    Thread originThread = new Thread(() -> {
      try {
        String received = environment.originHost.confirm();
        fail("Aucun message ne doit être reçu message= " + received);
      } catch (ProtocolException e) {
        assertInstanceOf(InterruptedException.class, e.getCause(),
            "La cause de ProtocolException devrait être une InterruptedException - " +
                "Interrupt nécessaire pour les tests");
      }
    });
    originThread.start();

    // when
    VerificationException exception = assertThrows(VerificationException.class, () -> {
      environment.targetHost.response(dg_idu5_6_absent_sdu);
    });

    // then, we captured the correct exception
    assertNotNull(exception);

    // origin host did not receive anything
    sleep(SLEEP_FACTOR);
    // We posit 5 second is enough to determine target host won't get the message
    originThread.interrupt();
    originThread.join();
  }

  public static InterfaceDataUnit56Dto c012_confirmation_success(Environment environment,
      InterfaceDataUnit56Dto incomingServerMessage)
      throws JsonProcessingException, InterruptedException {
    // implied hypothesis inferred from env description
    // given
    assertNotNull(environment.originHost, "precondition originHost should not be null");
    assertNotNull(environment.targetHost, "precondition targetHost should not be null");
    assertNotNull(incomingServerMessage,
        "precondition targetHost must have received a previous message");

    UUID trackingNumber = incomingServerMessage.getContext().getTrackingNumber();
    String destination_iri = environment.originAddress + ":" + environment.originPort;
    InterfaceDataUnit56Dto expectedDto = new InterfaceDataUnit56Dto(
        new Context56Dto(destination_iri, trackingNumber, false),
        incomingServerMessage.getMessage());

    String dg_valid_idu5_6 = environment.objMap.writeValueAsString(expectedDto);

    AtomicReference<String> received = new AtomicReference<>();
    Thread clientThread = new Thread(() -> {
      received.set(environment.originHost.confirm());
    });
    clientThread.start();

    // when
    environment.targetHost.response(dg_valid_idu5_6);

    // then
    // origin host does receive the message
    clientThread.join();
    String result = received.get();

    // The test doesn't specify to transform into java object, but I'm afraid the string won't
    // quite match characters for characters
    InterfaceDataUnit56Dto actualDto =
        environment.objMap.readValue(result, InterfaceDataUnit56Dto.class);
    assertEquals(expectedDto.getContext().getDestinationIri(),
        actualDto.getContext().getDestinationIri());
    assertEquals(expectedDto.getMessage(), actualDto.getMessage());
    return actualDto;
  }
}
