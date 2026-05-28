package ca.griis.speds.link.integration.conception;


import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.api.dto.InitInParamsDto;
import ca.griis.speds.link.api.factory.ImmutableDataLinkFactory;
import ca.griis.speds.utils.X509CertificateCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public final class Environment implements HostEvent {
  private final ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();

  private final Host originHost;
  private final Host targetHost;
  private final Integer originPort;
  private final String originAddress;
  private final Integer targetPort;
  private final String targetAddress;
  private final ObjectMapper objMap;
  private final LinkedBlockingQueue<String> idus = new LinkedBlockingQueue<>();

  public Environment(
      Integer originPort,
      String originAddress,
      Integer targetPort,
      String targetAddress,
      ObjectMapper objMap) throws Exception {
    this.originPort = originPort;
    this.originAddress = originAddress;
    this.targetPort = targetPort;
    this.targetAddress = targetAddress;
    this.objMap = objMap;

    KeyPair rootKeyPair = X509CertificateCreator.generateKeyPair();
    X509Certificate rootCert = X509CertificateCreator.createCertificate(
        "CN=Root CA",
        "CN=Root CA",
        rootKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);
    final String rootCertBase64 = Base64.getEncoder().encodeToString(rootCert.getEncoded());

    this.originHost =
        factory.init(instantiateParams(originPort, rootCertBase64, rootKeyPair), this);
    this.targetHost =
        factory.init(instantiateParams(targetPort, rootCertBase64, rootKeyPair), this);
  }

  public String instantiateParams(Integer port, String rootCert, KeyPair rootKeyPair) {
    try {
      final KeyPair keys = X509CertificateCreator.generateKeyPair();
      final PrivateKey privateKey = keys.getPrivate();
      X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
          "CN=Intermediate CA",
          "CN=Root CA",
          keys.getPublic(),
          rootKeyPair.getPublic(),
          rootKeyPair.getPrivate(),
          false,
          false,
          "localhost");

      final String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
      final String certBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());

      Map<String, Object> options = new HashMap<>();
      options.put("speds.dl.protocol", "https");
      options.put("speds.dl.https.server.host", "localhost");
      options.put("speds.dl.https.server.port", port);
      options.put("speds.dl.https.cert", certBase64);
      options.put("speds.dl.https.private.key", privateKeyBase64);
      options.put("speds.dl.https.mode", "mTLS");
      options.put("speds.dl.https.root.certs", List.of(rootCert));

      InitInParamsDto initParams = new InitInParamsDto(options);

      return objMap.writeValueAsString(initParams);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public Host getOriginHost() {
    return originHost;
  }

  public Host getTargetHost() {
    return targetHost;
  }

  public Integer getOriginPort() {
    return originPort;
  }

  public String getOriginAddress() {
    return originAddress;
  }

  public Integer getTargetPort() {
    return targetPort;
  }

  public String getTargetAddress() {
    return targetAddress;
  }

  public ObjectMapper getObjMap() {
    return objMap;
  }

  public LinkedBlockingQueue<String> getIdus() {
    return idus;
  }

  @Override
  public void notifyIdu(String idu) {
    idus.add(idu);
  }

  @Override
  public void notifyException(Exception exception) {}
}
