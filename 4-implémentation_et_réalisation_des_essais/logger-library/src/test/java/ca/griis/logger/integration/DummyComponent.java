package ca.griis.logger.integration;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;

public class DummyComponent {
  private static final GriisLogger logger = getLogger(DummyComponent.class);

  public void startComponent() {
    logger.trace(Trace.ENTER_METHOD_0);

    logger.info(Info.COMPONENT_STARTING, "componentName");
    logger.info(Info.COMPONENT_READY, "componentName");

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void stopComponent() {
    logger.trace(Trace.ENTER_METHOD_0);

    logger.info(Info.COMPONENT_PREPARING_TO_CLOSE, "componentName");
    logger.info(Info.COMPONENT_CLOSING, "componentName");

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
