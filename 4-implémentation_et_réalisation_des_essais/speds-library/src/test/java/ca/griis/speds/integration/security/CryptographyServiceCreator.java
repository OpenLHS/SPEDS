package ca.griis.speds.integration.security;

import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class CryptographyServiceCreator {
  public static CryptographyService createCryptographyService() throws JsonProcessingException {
    final var init = """
        {
          "spedsProfile": [
             {
              "spedsLayer": "PRESENTATION",
              "algorithmCategory": "SYMM",
              "securityProfile": "STRONG"
            },
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
