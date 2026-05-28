package ca.griis.speds.toolkit.integration;

public class Environment {

  public static String envPreEffSym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "PRESENTATION",
              "algorithmCategory": "SYMM",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envPreStrSym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "PRESENTATION",
              "algorithmCategory": "SYMM",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envSesEffSym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envSesStrSym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envSesEffAsym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "ASYM",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envSesStrAsym() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "ASYM",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envSesEffHash() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "HASH",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envSesStrHash() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "HASH",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envSesEffSign() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SIGN",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envSesStrSign() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SIGN",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envSesEffDh() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "DH",
              "securityProfile": "EFFICIENT"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envSesStrDh() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "DH",
              "securityProfile": "STRONG"
            },
            {
              "spedsLayer": "SESSION",
              "algorithmCategory": "SYMM",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envTraEffHash() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "TRANSPORT",
              "algorithmCategory": "HASH",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envTraStrHash() {
    return """
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
  }

  public static String envResEffHash() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "HASH",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envResStrHash() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "HASH",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envResEffSign() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "SIGN",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }

  public static String envResStrSign() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "NETWORK",
              "algorithmCategory": "SIGN",
              "securityProfile": "STRONG"
            }
          ]
        }
        """;
  }

  public static String envInvalidSpedsAlgo() {
    return """
        {
          "spedsProfile": [
            {
              "spedsLayer": "APPLICATION",
              "algorithmCategory": "DH",
              "securityProfile": "EFFICIENT"
            }
          ]
        }
        """;
  }
}
