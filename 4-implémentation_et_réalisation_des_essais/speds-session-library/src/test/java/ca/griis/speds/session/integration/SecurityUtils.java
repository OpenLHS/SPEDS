package ca.griis.speds.session.integration;

import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SecurityUtils {
  public static CryptographyService createCryptographyService() throws JsonProcessingException {
    final var init = """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "ASYM",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "HASH",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SIGN",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "DH",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "TRANSPORT",
              "algorithmCategory": "HASH",
              "securityProfile": "STRONG"
            },
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
