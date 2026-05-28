package ca.griis.logger.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.logger.GriisLoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class GriisLoggerDummyTest {
  private ListAppender<ILoggingEvent> listAppender;
  private Dummy dummy;

  @BeforeAll
  public static void setLocale() {
    GriisLoggerFactory.initWithLocal("code");
  }

  @BeforeEach
  public void setup() {
    listAppender = new ListAppender<>();
    listAppender.setName("LIST");
    listAppender.start();

    Logger logger = (Logger) LoggerFactory.getLogger(Dummy.class);
    logger.addAppender(listAppender);
  }

  @AfterEach
  public void cleanUp() {
    listAppender.list.clear();
  }

  @Test
  public void constructorTest() {
    // When I call the default constructor for the dummy class
    dummy = new Dummy();

    // Then I expect to find an entryMethod in the list appender and nothing else
    assertEquals(1, listAppender.list.size());

    String expected = "EA000";
    String expectedLevel = "TRACE";
    String expectedFunction = "<init>";
    validateEvent(listAppender.list.get(0), expected, expectedLevel, expectedFunction);
  }

  @Test
  public void doSomeDebugTest() throws Exception {
    // Given a dummy
    dummy = new Dummy();
    listAppender.list.clear();

    // When I call doSomeDebug
    dummy.doSomeDebug(0, false);

    // Then I expect to find the correct events in the list appender.
    // Correct number of events
    assertEquals(3, listAppender.list.size());

    // Enter function message
    String expectedMessage = "EA002 - dummyInt=0 dummyBool=false";
    String expectedLevel = "TRACE";
    String expectedFunction = "doSomeDebug";
    validateEvent(listAppender.list.get(0), expectedMessage, expectedLevel, expectedFunction);

    // FuncitonSuccess message
    expectedMessage = "DA000";
    expectedLevel = "DEBUG";
    validateEvent(listAppender.list.get(1), expectedMessage, expectedLevel, expectedFunction);

    // Exit Function message
    expectedMessage = "EB000";
    expectedLevel = "TRACE";
    validateEvent(listAppender.list.get(2), expectedMessage, expectedLevel, expectedFunction);
  }

  @Test
  public void logsAnExceptionTest() {
    // Given a dummy
    dummy = new Dummy();
    listAppender.list.clear();

    // When I call logsAnException
    dummy.logsAnException();

    // Then I expect to find the correct events in the list appender.
    // Correct number of events
    assertEquals(3, listAppender.list.size());

    // Enter function message
    String expectedMessage = "EA000";
    String expectedLevel = "TRACE";
    String expectedFunction = "logsAnException";
    validateEvent(listAppender.list.get(0), expectedMessage, expectedLevel, expectedFunction);

    // Error message
    expectedMessage = "AD001 - exception=java.lang.Exception: exception";
    expectedLevel = "ERROR";
    validateEvent(listAppender.list.get(1), expectedMessage, expectedLevel, expectedFunction);

    // Exit Function message
    expectedMessage = "EB000";
    expectedLevel = "TRACE";
    validateEvent(listAppender.list.get(2), expectedMessage, expectedLevel, expectedFunction);

  }

  @Test
  public void warnsWhenLessThanFiveTest() {
    // Given a dummy
    dummy = new Dummy();
    listAppender.list.clear();

    // When I call warnsWhenLessThanFive
    dummy.warnsWhenLessThanFive(1);

    // Then I expect to find the correct events in the list appender.
    // Correct number of events
    assertEquals(3, listAppender.list.size());

    // Enter function message
    String expectedMessage = "EA001 - dummyInt=1";
    String expectedLevel = "TRACE";
    String expectedFunction = "warnsWhenLessThanFive";
    validateEvent(listAppender.list.get(0), expectedMessage, expectedLevel, expectedFunction);

    // Error message
    expectedMessage = "BB000 - dummyInt<5";
    expectedLevel = "WARN";
    validateEvent(listAppender.list.get(1), expectedMessage, expectedLevel, expectedFunction);

    // Exit Function message
    expectedMessage = "EB001 - dummyInt=1";
    expectedLevel = "TRACE";
    validateEvent(listAppender.list.get(2), expectedMessage, expectedLevel, expectedFunction);
  }

  public void validateEvent(ILoggingEvent event, String expectedMessage, String expectedLevel,
      String expectedFunction) {
    assertNotNull(event);
    assertEquals(expectedMessage, event.getMessage());
    assertEquals(event.getLevel().toString(), expectedLevel);
    String actualMethodName = event.getCallerData()[0].getMethodName();
    assertEquals(expectedFunction, actualMethodName);
  }

  @SpringBootApplication
  static class TestConfiguration {
  }
}
