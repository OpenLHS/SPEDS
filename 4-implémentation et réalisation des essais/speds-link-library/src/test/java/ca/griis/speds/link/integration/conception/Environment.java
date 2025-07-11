package ca.griis.speds.link.integration.conception;


import static ca.griis.speds.link.integration.conception.KeyHelper.makeKeyWithCert;

import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.dto.InitInParamsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class Environment {
  public DataLinkHost originHost;
  public DataLinkHost targetHost;

  public int originPort;
  public String originAddress;
  public int targetPort;
  public String targetAddress;

  public ObjectMapper objMap;

  public String instantiateParams(int port) {
    try {
      Map.Entry<String, String> keyCert = makeKeyWithCert();
      String serialKey = keyCert.getKey();
      String serialCert = keyCert.getValue();

      HashMap<String, Object> options = new HashMap<>();
      options.put("speds.dl.protocol", "https");
      options.put("speds.dl.https.server.host", "localhost");
      options.put("speds.dl.https.server.port", port);
      options.put("speds.dl.https.server.cert", serialCert);
      options.put("speds.dl.https.server.private.key", serialKey);
      options.put("speds.dl.https.client.cert.trustmanager.mode", "strict");

      InitInParamsDto initParams = new InitInParamsDto(options);

      return objMap.writeValueAsString(initParams);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
