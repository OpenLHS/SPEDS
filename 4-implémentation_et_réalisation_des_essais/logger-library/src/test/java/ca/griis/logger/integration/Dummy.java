package ca.griis.logger.integration;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.logger.statuscode.Warn;

public class Dummy {
  private static final GriisLogger logger = getLogger(Dummy.class);

  private Integer dummyInt;
  private Boolean dummyBool;

  public Dummy() {
    logger.trace(Trace.ENTER_METHOD_0);
    dummyInt = 0;
    dummyBool = false;
  }

  public void doSomeDebug(Integer dummyInt, Boolean dummyBool) {
    logger.trace(Trace.ENTER_METHOD_2, "dummyInt", dummyInt, "dummyBool", dummyBool);

    for (int i = 0; i < 5; i++) {
      if (i == 4) {
        logger.debug(Debug.FUNCTION_SUCCESS_0);
      }
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void logsAnException() {
    logger.trace(Trace.ENTER_METHOD_0);

    logger.error(Error.IGNORED_ERROR, new Exception("exception"));

    logger.trace(Trace.EXIT_METHOD_0);
  }


  public Integer warnsWhenLessThanFive(Integer dummyInt) {
    logger.trace(Trace.ENTER_METHOD_1, "dummyInt", dummyInt);

    if (dummyInt < 5) {
      logger.warn(Warn.UNMET_CONDITION_0, "dummyInt<5", dummyInt);
    }

    logger.trace(Trace.EXIT_METHOD_1, "dummyInt", dummyInt);
    return dummyInt;
  }

  public Integer getDummyInt() {
    return dummyInt;
  }

  public void setDummyInt(Integer dummyInt) {
    this.dummyInt = dummyInt;
  }

  public boolean isDummyBool() {
    return dummyBool;
  }

  public void setDummyBool(boolean dummyBool) {
    this.dummyBool = dummyBool;
  }
}
