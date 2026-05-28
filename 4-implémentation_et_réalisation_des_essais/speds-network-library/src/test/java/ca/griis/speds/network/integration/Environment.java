package ca.griis.speds.network.integration;

import static org.mockito.Mockito.mock;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.network.api.dto.OptionsDto;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostFactory;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.network.util.X509CertificateCreator;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

public class Environment {
  private static final UUID messageId = UUID.randomUUID();
  private static final String version = "version";
  private static final String reference = "reference";
  private static final Host linkHost = mock(Host.class);

  private NetworkHost client;
  private NetworkHost server;

  private HostEvent hostEventConsumer;
  private CryptographyService cryptographyService;
  private NetEvent clientNetEvent = new NetEvent();
  private NetEvent serverNetEvent = new NetEvent();

  public UUID getMessageId() {
    return messageId;
  }

  public String getVersion() {
    return version;
  }

  public String getReference() {
    return reference;
  }

  public NetworkHost getClient() {
    return client;
  }

  public NetworkHost getServer() {
    return server;
  }

  public Host getLinkHost() {
    return linkHost;
  }

  public HostEvent getHostEventConsumer() {
    return hostEventConsumer;
  }

  public void setHostEventConsumer(HostEvent hostEventConsumer) {
    this.hostEventConsumer = hostEventConsumer;
  }

  public String getClientResult() {
    return clientNetEvent.getResult();
  }

  public void setClientResult(String clientResult) {
    clientNetEvent.setResult(clientResult);
  }

  public String getServerResult() {
    return serverNetEvent.getResult();
  }

  public void setServerResult(String serverResult) {
    serverNetEvent.setResult(serverResult);
  }

  public CryptographyService getCryptographyService() {
    return cryptographyService;
  }

  public Environment en1() throws Exception {
    cryptographyService = SecurityUtils.createCryptographyService();
    final var factory = getNetworkFactory();

    KeyPair key = X509CertificateCreator.generateRsaKeyPair(4096);
    X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=host1.ca",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");


    OptionsDto options = new OptionsDto(
        "7",
        "https://reference.iri/speds",
        Base64.getEncoder().encodeToString(cert.getEncoded()),
        Base64.getEncoder().encodeToString(key.getPrivate().getEncoded()), 2);
    InitInParamsDto params = new InitInParamsDto(options);
    final String parametersJson =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(params);

    this.client = factory.initHost(parametersJson, clientNetEvent);
    this.server = factory.initHost(parametersJson, serverNetEvent);

    return this;
  }

  private NetworkHostFactory getNetworkFactory() {

    return new NetworkHostFactory(() -> messageId, cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        setHostEventConsumer(hostEventConsumer);
        return linkHost;
      }
    };
  }
}
