package ca.griis.speds.session.integration.concurency;

import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.ImmutableSessionServer;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import java.lang.reflect.Field;
import java.util.Map;

public class Util {

  public static Map<SessionId, SessionInformation> getSesInfo(SessionHost sessionHost,
      String field) {
    Field partnerField = null;
    try {
      partnerField = sessionHost.getClass().getDeclaredField(field);
      partnerField.setAccessible(true);
      return ((ImmutableSessionServer) partnerField.get(sessionHost)).getServerInfo();
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }
}
