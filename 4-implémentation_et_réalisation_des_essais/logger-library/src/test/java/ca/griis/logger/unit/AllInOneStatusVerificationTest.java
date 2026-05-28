package ca.griis.logger.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.logger.statuscode.Warn;
import ch.qos.cal10n.verifier.Cal10nError;
import ch.qos.cal10n.verifier.IMessageKeyVerifier;
import ch.qos.cal10n.verifier.MessageKeyVerifier;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class AllInOneStatusVerificationTest {
  @Test
  public void allError() {
    IMessageKeyVerifier mkv = new MessageKeyVerifier(Error.class);
    List<Cal10nError> errorList = mkv.verify(new Locale("en_ca"));
    errorList.addAll(mkv.verify(new Locale("code")));
    for (Cal10nError error : errorList) {
      System.out.println(error);
    }
    assertEquals(0, errorList.size());
  }

  @Test
  public void allWarn() {
    IMessageKeyVerifier mkv = new MessageKeyVerifier(Warn.class);
    List<Cal10nError> errorList = mkv.verify(new Locale("en_ca"));
    errorList.addAll(mkv.verify(new Locale("code")));
    for (Cal10nError error : errorList) {
      System.out.println(error);
    }
    assertEquals(0, errorList.size());
  }

  @Test
  public void allInfo() {
    IMessageKeyVerifier mkv = new MessageKeyVerifier(Info.class);
    List<Cal10nError> errorList = mkv.verify(new Locale("en_ca"));
    errorList.addAll(mkv.verify(new Locale("code")));
    for (Cal10nError error : errorList) {
      System.out.println(error);
    }
    assertEquals(0, errorList.size());
  }

  @Test
  public void allDebug() {
    IMessageKeyVerifier mkv = new MessageKeyVerifier(Debug.class);
    List<Cal10nError> errorList = mkv.verify(new Locale("en_ca"));
    errorList.addAll(mkv.verify(new Locale("code")));
    for (Cal10nError error : errorList) {
      System.out.println(error);
    }
    assertEquals(0, errorList.size());
  }

  @Test
  public void allTrace() {
    IMessageKeyVerifier mkv = new MessageKeyVerifier(Trace.class);
    List<Cal10nError> errorList = mkv.verify(new Locale("en_ca"));
    errorList.addAll(mkv.verify(new Locale("code")));
    for (Cal10nError error : errorList) {
      System.out.println(error);
    }
    assertEquals(0, errorList.size());
  }
}
