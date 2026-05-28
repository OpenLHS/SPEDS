package ca.griis.speds.integration.presentation;

import static ca.griis.logger.GriisLoggerFactory.getLogger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.integration.security.X509CertificateCreator;
import ca.griis.speds.toolkit.project.ProjectService;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.iri.IRI;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PresentationParamsCreator {
  private static final GriisLogger logger = getLogger(PresentationParamsCreator.class);

  private static KeyPair sourceKey;
  private static X509Certificate sourceCert;
  private static KeyPair destKey;
  private static X509Certificate destCert;
  private static String rootCertBase64;

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    try {
      KeyPair rootKeyPair = X509CertificateCreator.generateRsaKeyPair(4096);
      X509Certificate rootCert = X509CertificateCreator.createCertificate(
          "CN=Root CA",
          "CN=Root CA",
          rootKeyPair.getPublic(),
          rootKeyPair.getPublic(),
          rootKeyPair.getPrivate(),
          true,
          false,
          "",
          "SHA256withRSA");
      rootCertBase64 = Base64.getEncoder().encodeToString(rootCert.getEncoded());

      sourceKey = X509CertificateCreator.generateRsaKeyPair(4096);
      sourceCert = X509CertificateCreator.createCertificate(
          "CN=localhost",
          "CN=Root CA",
          sourceKey.getPublic(),
          rootKeyPair.getPublic(),
          rootKeyPair.getPrivate(),
          false,
          false,
          "localhost",
          "SHA256withRSA");

      destKey = X509CertificateCreator.generateRsaKeyPair(4096);
      destCert = X509CertificateCreator.createCertificate(
          "CN=localhost",
          "CN=Root CA",
          destKey.getPublic(),
          rootKeyPair.getPublic(),
          rootKeyPair.getPrivate(),
          false,
          false,
          "localhost",
          "SHA256withRSA");
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }

  public static Map<String, Object> createSourceParams() throws CertificateEncodingException {
    Map<String, Object> options = new HashMap<>();
    options.put("speds.pre.version", "7");
    options.put("speds.pre.reference", "https://reference.iri/speds");
    options.put("speds.ses.cert", Base64.getEncoder().encodeToString(sourceCert.getEncoded()));
    options.put("speds.ses.private.key",
        Base64.getEncoder().encodeToString(sourceKey.getPrivate().getEncoded()));
    options.put("speds.ses.version", "7");
    options.put("speds.ses.reference", "https://reference.iri/speds");

    options.put("speds.tra.version", "7");
    options.put("speds.tra.reference", "https://reference.iri/speds");
    options.put("speds.tra.response.window.minutes", 10);
    options.put("speds.tra.confirm.window.minutes", 10);

    options.put("speds.net.version", "7");
    options.put("speds.net.reference", "https://reference.iri/speds");
    options.put("speds.net.cert", Base64.getEncoder().encodeToString(sourceCert.getEncoded()));
    options.put("speds.net.private.key",
        Base64.getEncoder().encodeToString(sourceKey.getPrivate().getEncoded()));
    options.put("speds.net.response.window.minutes", 10);

    options.put("speds.dl.protocol", "https");
    options.put("speds.dl.https.server.host", "0.0.0.0");
    options.put("speds.dl.https.server.port", 8081);
    options.put("speds.dl.https.cert",
        Base64.getEncoder().encodeToString(sourceCert.getEncoded()));
    options.put("speds.dl.https.private.key",
        Base64.getEncoder().encodeToString(sourceKey.getPrivate().getEncoded()));
    options.put("speds.dl.https.mode", "mTLS");
    options.put("speds.dl.https.root.certs", List.of(rootCertBase64));
    return options;
  }

  public static Map<String, Object> createDestinationParams() throws CertificateEncodingException {
    Map<String, Object> options = new HashMap<>();
    options.put("speds.pre.version", "7");
    options.put("speds.pre.reference", "https://reference.iri/speds");
    options.put("speds.ses.cert", Base64.getEncoder().encodeToString(destCert.getEncoded()));
    options.put("speds.ses.private.key",
        Base64.getEncoder().encodeToString(destKey.getPrivate().getEncoded()));
    options.put("speds.ses.version", "7");
    options.put("speds.ses.reference", "https://reference.iri/speds");

    options.put("speds.tra.version", "7");
    options.put("speds.tra.reference", "https://reference.iri/speds");
    options.put("speds.tra.response.window.minutes", 10);
    options.put("speds.tra.confirm.window.minutes", 10);

    options.put("speds.net.cert", Base64.getEncoder().encodeToString(destCert.getEncoded()));
    options.put("speds.net.private.key",
        Base64.getEncoder().encodeToString(destKey.getPrivate().getEncoded()));
    options.put("speds.net.response.window.minutes", 10);

    options.put("speds.dl.protocol", "https");
    options.put("speds.dl.https.server.host", "0.0.0.0");
    options.put("speds.dl.https.server.port", 8082);
    options.put("speds.dl.https.cert",
        Base64.getEncoder().encodeToString(sourceCert.getEncoded()));
    options.put("speds.dl.https.private.key",
        Base64.getEncoder().encodeToString(sourceKey.getPrivate().getEncoded()));
    options.put("speds.dl.https.mode", "mTLS");
    options.put("speds.dl.https.root.certs", List.of(rootCertBase64));
    return options;
  }

  public static ProjectService createProjectService(String pgaId, String sourceCode,
      String destinationCode, IRI sourceIri, IRI destinationIri) {
    ProjectService projectService = mock(ProjectService.class);

    when(projectService.getEntityIri(pgaId, sourceCode)).thenReturn(sourceIri);
    when(projectService.getEntityIri(pgaId, destinationCode)).thenReturn(destinationIri);
    when(projectService.getEntityPublicKey(pgaId, destinationCode))
        .thenReturn(destCert.getPublicKey());
    when(projectService.verifyEntityLegitimacy(pgaId, sourceCode, sourceKey.getPublic()))
        .thenReturn(true);

    return projectService;
  }
}
