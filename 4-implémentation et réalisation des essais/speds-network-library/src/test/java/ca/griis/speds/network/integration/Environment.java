package ca.griis.speds.network.integration;

import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.ImmutableNetworkHost;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.SealManager;
import ca.griis.speds.network.util.KeyVar;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class Environment {
  ObjectMapper objectMapper;

  NetworkHost networkClient;

  NetworkHost networkServer;

  @Mock
  DataLinkHost dataLinkClient;

  @Mock
  DataLinkHost dataLinkServer;

  SentMessageIdSet clientMessageIdSet;

  SentMessageIdSet serverMessageIdSet;

  String spedsVersion;

  String spedsReference;

  public Environment() {
    MockitoAnnotations.openMocks(this);

    objectMapper = new ObjectMapper();

    spedsVersion = "v6.2.0";
    spedsReference =
        "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library";

    String clientCertificate = KeyVar.griisCertRsa;
    String clientPrivateKey = KeyVar.griisPrikeyRsa;

    clientMessageIdSet = new SentMessageIdSet();

    networkClient = new ImmutableNetworkHost(dataLinkClient, spedsVersion, spedsReference,
        CertificatePrivateKeyPair.importFromPem(clientCertificate, clientPrivateKey),
        () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper), clientMessageIdSet);


    String serverCertificate = KeyVar.griisCertRsa;
    String serverPrivateKey = KeyVar.griisPrikeyRsa;

    serverMessageIdSet = new SentMessageIdSet();

    networkServer = new ImmutableNetworkHost(dataLinkServer, spedsVersion, spedsReference,
        CertificatePrivateKeyPair.importFromPem(serverCertificate, serverPrivateKey),
        () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper), serverMessageIdSet);

  }
}
