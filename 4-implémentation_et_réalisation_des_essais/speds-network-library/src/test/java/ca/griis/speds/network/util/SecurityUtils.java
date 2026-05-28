package ca.griis.speds.network.util;

import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SecurityUtils {
  public static CryptographyService createCryptographyService() throws JsonProcessingException {
    final var init = """
                    {
          "spedsProfile": [
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "SIGN",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "NETWORK",
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
