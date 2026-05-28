package ca.griis.speds.toolkit.integration;

import static ca.griis.speds.toolkit.integration.Cases.ctCryptAlgo_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptAlgo_02;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptAsym_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptAsym_02;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptDh_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptHash_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptParam_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptParam_02;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptSign_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptSign_02;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptSymm_01;
import static ca.griis.speds.toolkit.integration.Cases.ctCryptSymm_02;
import static ca.griis.speds.toolkit.integration.Cases.ctProGe01_01;
import static ca.griis.speds.toolkit.integration.Cases.ctProGe02_01;
import static ca.griis.speds.toolkit.integration.Cases.ctProGe03_01;
import static ca.griis.speds.toolkit.integration.Cases.ctProGe04_01;
import static ca.griis.speds.toolkit.integration.Cases.ctProGe05_01;
import static ca.griis.speds.toolkit.integration.Cases.ctProSe01_01;
import static ca.griis.speds.toolkit.integration.Environment.envInvalidSpedsAlgo;
import static ca.griis.speds.toolkit.integration.Environment.envPreEffSym;
import static ca.griis.speds.toolkit.integration.Environment.envPreStrSym;
import static ca.griis.speds.toolkit.integration.Environment.envResEffHash;
import static ca.griis.speds.toolkit.integration.Environment.envResEffSign;
import static ca.griis.speds.toolkit.integration.Environment.envResStrHash;
import static ca.griis.speds.toolkit.integration.Environment.envResStrSign;
import static ca.griis.speds.toolkit.integration.Environment.envSesEffAsym;
import static ca.griis.speds.toolkit.integration.Environment.envSesEffDh;
import static ca.griis.speds.toolkit.integration.Environment.envSesEffHash;
import static ca.griis.speds.toolkit.integration.Environment.envSesEffSign;
import static ca.griis.speds.toolkit.integration.Environment.envSesEffSym;
import static ca.griis.speds.toolkit.integration.Environment.envSesStrAsym;
import static ca.griis.speds.toolkit.integration.Environment.envSesStrDh;
import static ca.griis.speds.toolkit.integration.Environment.envSesStrHash;
import static ca.griis.speds.toolkit.integration.Environment.envSesStrSign;
import static ca.griis.speds.toolkit.integration.Environment.envSesStrSym;
import static ca.griis.speds.toolkit.integration.Environment.envTraEffHash;
import static ca.griis.speds.toolkit.integration.Environment.envTraStrHash;
import static ca.griis.speds.toolkit.integration.GlobalData.data;
import static ca.griis.speds.toolkit.integration.GlobalData.efficientKeyPair;
import static ca.griis.speds.toolkit.integration.GlobalData.entity1Code;
import static ca.griis.speds.toolkit.integration.GlobalData.entity1Iri;
import static ca.griis.speds.toolkit.integration.GlobalData.entity1KeyPair;
import static ca.griis.speds.toolkit.integration.GlobalData.projectId;
import static ca.griis.speds.toolkit.integration.GlobalData.someEntityCertificate;
import static ca.griis.speds.toolkit.integration.GlobalData.someEntityIri;
import static ca.griis.speds.toolkit.integration.GlobalData.strongKeyPair;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.api.CryptographyFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.toolkit.project.ProjectService;
import ca.griis.speds.toolkit.security.api.CertificateService;
import org.junit.jupiter.api.Test;

public class Suites {
  @Test
  public void ge01() throws Exception {
    final ProjectService projectService = mock(ProjectService.class);
    final Boolean isLegitimate = true;

    when(projectService.getEntityIri(eq(projectId), eq(entity1Code))).thenReturn(entity1Iri);
    when(projectService.verifyEntityLegitimacy(eq(projectId), eq(entity1Code),
        eq(entity1KeyPair.getPublic()))).thenReturn(isLegitimate);
    when(projectService.getEntityPublicKey(eq(projectId), eq(entity1Code))).thenReturn(
        entity1KeyPair.getPublic());
    when(projectService.checkProjectActivity(eq(projectId))).thenReturn(isLegitimate);
    when(projectService.checkPlanActivity(eq(projectId))).thenReturn(isLegitimate);

    ctProGe01_01(projectService, entity1Iri, projectId, entity1Code);
    ctProGe02_01(projectService, isLegitimate, projectId, entity1Code, entity1KeyPair.getPublic());
    ctProGe03_01(projectService, entity1KeyPair.getPublic(), projectId, entity1Code);
    ctProGe04_01(projectService, projectId);
    ctProGe05_01(projectService, projectId);
  }

  @Test
  public void se01() throws Exception {
    final CertificateService certificateService = mock(CertificateService.class);
    final Boolean isValid = true;

    when(certificateService.checkCertificateValidity(eq(someEntityCertificate),
        eq(someEntityIri))).thenReturn(isValid);

    ctProSe01_01(certificateService, isValid, someEntityCertificate, someEntityIri);
  }

  @Test
  public void cePre01() throws Exception {
    String expected = "AES/GCM/NoPadding";
    final String envPreEffSym = envPreEffSym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envPreEffSym);

    ctCryptSymm_01(cryptographyService, SpedsLayer.PRESENTATION, data);
    ctCryptSymm_02(cryptographyService, SpedsLayer.PRESENTATION, data);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.PRESENTATION, AlgorithmCategory.SYMM, expected);
  }

  @Test
  public void cePre02() throws Exception {
    String expected = "AES/GCM/NoPadding";
    final String envPreEffSym = envPreStrSym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envPreEffSym);

    ctCryptSymm_01(cryptographyService, SpedsLayer.PRESENTATION, data);
    ctCryptSymm_02(cryptographyService, SpedsLayer.PRESENTATION, data);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.PRESENTATION, AlgorithmCategory.SYMM, expected);
  }

  @Test
  public void ceSes01() throws Exception {
    String expected = "AES/GCM/NoPadding";
    final String envPreEffSym = envSesEffSym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envPreEffSym);

    ctCryptSymm_01(cryptographyService, SpedsLayer.SESSION, data);
    ctCryptSymm_02(cryptographyService, SpedsLayer.SESSION, data);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.SYMM, expected);
  }

  @Test
  public void ceSes02() throws Exception {
    String expected = "AES/GCM/NoPadding";
    final String envPreEffSym = envSesStrSym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envPreEffSym);

    ctCryptSymm_01(cryptographyService, SpedsLayer.SESSION, data);
    ctCryptSymm_02(cryptographyService, SpedsLayer.SESSION, data);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.SYMM, expected);
  }

  @Test
  public void ceSes03() throws Exception {
    String expected = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    String securityProfile = "EFFICIENT";
    final String envSesEffAsym = envSesEffAsym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesEffAsym);

    ctCryptAsym_01(cryptographyService, SpedsLayer.SESSION, efficientKeyPair, data);
    ctCryptAsym_02(cryptographyService, SpedsLayer.SESSION, efficientKeyPair, data,
        securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.ASYM, expected);
  }

  @Test
  public void ceSes04() throws Exception {
    String expected = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    String securityProfile = "STRONG";
    final String envSesStrAsym = envSesStrAsym();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesStrAsym);

    ctCryptAsym_01(cryptographyService, SpedsLayer.SESSION, strongKeyPair, data);
    ctCryptAsym_02(cryptographyService, SpedsLayer.SESSION, strongKeyPair, data,
        securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.ASYM, expected);
  }

  @Test
  public void ceSes05() throws Exception {
    String expected = "SHA-256";
    final String envSesEffHash = envSesEffHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesEffHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.SESSION, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.HASH, expected);
  }

  @Test
  public void ceSes06() throws Exception {
    String expected = "SHA-512";
    final String envSesStrHash = envSesStrHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesStrHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.SESSION, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.HASH, expected);
  }

  @Test
  public void ceSes07() throws Exception {
    String expected = "RSASSA-PSS";
    String securityProfile = "EFFICIENT";
    final String envSesEffSign = envSesEffSign();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesEffSign);

    ctCryptSign_01(cryptographyService, SpedsLayer.SESSION, efficientKeyPair, data,
        securityProfile);
    ctCryptSign_02(cryptographyService, SpedsLayer.SESSION, efficientKeyPair, data,
        securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.SIGN, expected);
  }

  @Test
  public void ceSes08() throws Exception {
    String expected = "RSASSA-PSS";
    String securityProfile = "STRONG";
    final String envSesStrSign = envSesStrSign();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesStrSign);

    ctCryptSign_01(cryptographyService, SpedsLayer.SESSION, strongKeyPair, data, securityProfile);
    ctCryptSign_02(cryptographyService, SpedsLayer.SESSION, strongKeyPair, data, securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.SIGN, expected);
  }

  @Test
  public void ceSes09() throws Exception {
    String expected = "X25519";
    final String envSesEffDh = envSesEffDh();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesEffDh);

    ctCryptDh_01(cryptographyService, SpedsLayer.SESSION);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.DH, expected);
  }

  @Test
  public void ceSes10() throws Exception {
    String expected = "X25519";
    final String envSesStrDh = envSesStrDh();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envSesStrDh);

    ctCryptDh_01(cryptographyService, SpedsLayer.SESSION);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.SESSION, AlgorithmCategory.DH, expected);
  }

  @Test
  public void ceTra01() throws Exception {
    int paramExpected = 0;
    String expected = "SHA-256";
    String algo = "SHA-256";
    final String envTraEffHash = envTraEffHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envTraEffHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.TRANSPORT, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.TRANSPORT, AlgorithmCategory.HASH, expected);
    ctCryptParam_01(cryptographyService, SpedsLayer.TRANSPORT, algo, paramExpected);
  }

  @Test
  public void ceTra02() throws Exception {
    String expected = "SHA-512";
    final String envTraStrHash = envTraStrHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envTraStrHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.TRANSPORT, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.TRANSPORT, AlgorithmCategory.HASH, expected);
  }

  @Test
  public void ceRes01() throws Exception {
    String expected = "SHA-256";
    final String envResEffHash = envResEffHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envResEffHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.NETWORK, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.NETWORK, AlgorithmCategory.HASH, expected);
  }

  @Test
  public void ceRes02() throws Exception {
    String expected = "SHA-512";
    final String envResStrHash = envResStrHash();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envResStrHash);

    ctCryptHash_01(cryptographyService, SpedsLayer.NETWORK, data, expected);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.NETWORK, AlgorithmCategory.HASH, expected);
  }

  @Test
  public void ceRes03() throws Exception {
    String expected = "RSASSA-PSS";
    String securityProfile = "EFFICIENT";
    final String envResEffSign = envResEffSign();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envResEffSign);

    ctCryptSign_01(cryptographyService, SpedsLayer.NETWORK, efficientKeyPair, data,
        securityProfile);
    ctCryptSign_02(cryptographyService, SpedsLayer.NETWORK, efficientKeyPair, data,
        securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.NETWORK, AlgorithmCategory.SIGN, expected);
  }

  @Test
  public void ceRes04() throws Exception {
    String expected = "RSASSA-PSS";
    String securityProfile = "STRONG";
    final String envResStrSign = envResStrSign();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envResStrSign);

    ctCryptSign_01(cryptographyService, SpedsLayer.NETWORK, efficientKeyPair, data,
        securityProfile);
    ctCryptSign_02(cryptographyService, SpedsLayer.NETWORK, efficientKeyPair, data,
        securityProfile);
    ctCryptAlgo_01(cryptographyService, SpedsLayer.NETWORK, AlgorithmCategory.SIGN, expected);
  }

  @Test
  public void ceAlgo01() throws Exception {
    final String envInvalidSpedsAlgo = envInvalidSpedsAlgo();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envInvalidSpedsAlgo);

    ctCryptAlgo_02(cryptographyService, SpedsLayer.NETWORK, AlgorithmCategory.SIGN);
  }

  @Test
  public void ceAlgo02() throws Exception {
    int expected = 3;
    String algo = "DRBG";
    String wrongAlgo = "wrongAlgo";
    final String envResStrSign = envResStrSign();
    final CryptographyFactory cryptographyFactory = new DefaultCryptographyFactory();
    final CryptographyService cryptographyService =
        cryptographyFactory.initCipherSuite(envResStrSign);

    ctCryptParam_01(cryptographyService, SpedsLayer.NETWORK, algo, expected);
    ctCryptParam_02(cryptographyService, SpedsLayer.NETWORK, wrongAlgo);
  }
}
