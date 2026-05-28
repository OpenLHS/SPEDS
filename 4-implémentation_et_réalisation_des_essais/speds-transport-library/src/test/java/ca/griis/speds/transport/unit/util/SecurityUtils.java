package ca.griis.speds.transport.unit.util;

import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SecurityUtils {
  public static CryptographyService createCryptographyService() throws JsonProcessingException {
    final var init = """
                    {
          "spedsProfile": [
            {
              "spedsLayer": "TRANSPORT",
              "algorithmCategory": "HASH",
              "securityProfile": "STRONG"
            }
          ]
        }
                    """;
    var service = new DefaultCryptographyFactory().initCipherSuite(init);
    return service;
  }
}
