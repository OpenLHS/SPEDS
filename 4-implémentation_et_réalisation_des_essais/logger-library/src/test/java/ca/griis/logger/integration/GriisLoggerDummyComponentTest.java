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
public class GriisLoggerDummyComponentTest {
  private ListAppender<ILoggingEvent> listAppender;
  private DummyComponent dummyComponent;

  @BeforeAll
  public static void setLocale() {
    GriisLoggerFactory.initWithLocal("en_ca");
  }

  @BeforeEach
  public void setup() {
    listAppender = new ListAppender<>();
    listAppender.setName("LIST");
    listAppender.start();

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.addAppender(listAppender);
  }

  @AfterEach
  public void cleanUp() {
    listAppender.list.clear();
  }

  @Test
  public void startComponentTest() {
    // Given a dummy
    dummyComponent = new DummyComponent();
    listAppender.list.clear();

    // When I call start component
    dummyComponent.startComponent();

    // Then I expect to find the correct events in the list appender.
    // Correct number of events
    assertEquals(4, listAppender.list.size());

    // Enter function message
    String expectedMessage = "EA000 Enter method";
    String expectedLevel = "TRACE";
    String expectedFunction = "startComponent";
    validateEvent(listAppender.list.get(0), expectedMessage, expectedLevel, expectedFunction);

    // Start component message
    expectedMessage = "CB000 The component is starting - component=componentName";
    expectedLevel = "INFO";
    validateEvent(listAppender.list.get(1), expectedMessage, expectedLevel, expectedFunction);

    // Component ready message
    expectedMessage = "CB001 The component is ready - component=componentName";
    expectedLevel = "INFO";
    validateEvent(listAppender.list.get(2), expectedMessage, expectedLevel, expectedFunction);

    // Exit Function message
    expectedMessage = "EB000 Exit method";
    expectedLevel = "TRACE";
    validateEvent(listAppender.list.get(3), expectedMessage, expectedLevel, expectedFunction);
  }

  @Test
  public void stopComponentTest() {
    // Given a dummy that was started
    dummyComponent = new DummyComponent();
    dummyComponent.startComponent();
    listAppender.list.clear();

    // When I call stop component
    dummyComponent.stopComponent();

    // Then I expect to find the correct events in the list appender.
    // Correct number of events
    assertEquals(4, listAppender.list.size());

    // Enter function message
    String expectedMessage = "EA000 Enter method";
    String expectedLevel = "TRACE";
    String expectedFunction = "stopComponent";
    validateEvent(listAppender.list.get(0), expectedMessage, expectedLevel, expectedFunction);

    // Start component message
    expectedMessage = "CB002 The component is preparing to close - component=componentName";
    expectedLevel = "INFO";
    validateEvent(listAppender.list.get(1), expectedMessage, expectedLevel, expectedFunction);

    // Component ready message
    expectedMessage = "CB003 The component is closing - component=componentName";
    expectedLevel = "INFO";
    validateEvent(listAppender.list.get(2), expectedMessage, expectedLevel, expectedFunction);

    // Exit Function message
    expectedMessage = "EB000 Exit method";
    expectedLevel = "TRACE";
    validateEvent(listAppender.list.get(3), expectedMessage, expectedLevel, expectedFunction);
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
