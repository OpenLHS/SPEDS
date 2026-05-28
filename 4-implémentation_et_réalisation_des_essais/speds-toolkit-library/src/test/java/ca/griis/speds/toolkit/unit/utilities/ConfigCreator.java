package ca.griis.speds.toolkit.unit.utilities;

public class ConfigCreator {
  public static String createProfileConfig() {
    return """
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
  }

  public static String createAlgoConfig() {
    return """
          {
          "spedsProfile": [
            {
              "spedsLayer": "PRESENTATION",
              "algorithmCategory": "SYMM",
              "securityAlgorithm": "AES256-GCM"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityAlgorithm": "AES256-GCM"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "ASYM",
              "securityAlgorithm": "RSA4096-OAEP-MGF1-SHA512"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "HASH",
              "securityAlgorithm": "SHA512"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SIGN",
              "securityAlgorithm": "RSA4096-PSS-MGF1-SHA512"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "DH",
              "securityAlgorithm": "X25519"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "DH",
              "securityAlgorithm": "X25519"
            },
            {
              "spedsLayer": "TRANSPORT",
              "algorithmCategory": "HASH",
              "securityAlgorithm": "SHA512"
            },
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "SIGN",
              "securityAlgorithm": "RSA4096-PSS-MGF1-SHA512"
            },
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "HASH",
              "securityAlgorithm": "SHA512"
            }
          ]
        }
                    """;
  }
}
