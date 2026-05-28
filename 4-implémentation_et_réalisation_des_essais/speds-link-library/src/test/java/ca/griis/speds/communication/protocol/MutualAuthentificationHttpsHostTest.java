package ca.griis.speds.communication.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.utils.X509CertificateCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MutualAuthentificationHttpsHostTest implements ProtocolHostEvent {
  private ProtocolHttpHost server;
  private ProtocolHttpHost client;
  private ObjectMapper objectMapper = new ObjectMapper();
  private LinkedBlockingQueue<ProtocolIdu> idus = new LinkedBlockingQueue<>();

  @BeforeEach
  public void init() throws Exception {
    KeyPair rootKeyPair = X509CertificateCreator.generateKeyPair();
    X509Certificate rootCert = X509CertificateCreator.createCertificate(
        "CN=Root CA",
        "CN=Root CA",
        rootKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

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

    final SslContext sslContextClient =
        SslContextBuilder.forClient().keyManager(privateKey,
            certificate).trustManager(List.of(rootCert))
            .build();

    final SslContextBuilder sslContextBuilderServer =
        SslContextBuilder.forServer(privateKey, certificate).trustManager(List.of(rootCert))
            .clientAuth(ClientAuth.REQUIRE);
    final SslContext sslCtxServer = sslContextBuilderServer.build();
    server =
        new ProtocolHttpHost(objectMapper, "localhost", 8080, sslContextClient, sslCtxServer,
            1048576, this);
    client =
        new ProtocolHttpHost(objectMapper, "localhost", 8081, sslContextClient, sslCtxServer,
            1048576, this);
  }

  @AfterEach
  public void teardown() {
    client.close();
    server.close();
  }

  @Test
  public void exchange() throws InterruptedException {
    for (int i = 0; i < 1; ++i) {
      final ProtocolIdu request = new ProtocolIdu("https://localhost:8080", "Allo");
      client.send(request);

      final ProtocolIdu indication = idus.poll(2, TimeUnit.SECONDS);
      assertEquals(indication.destinationUri(), "https://localhost:8080");
      assertEquals(indication.sdu(), request.sdu());
    }
  }

  @Override
  public void notifyIdu(ProtocolIdu event) {
    idus.add(event);
  }

  @Override
  public void notifyException(Exception exception) {}
}
