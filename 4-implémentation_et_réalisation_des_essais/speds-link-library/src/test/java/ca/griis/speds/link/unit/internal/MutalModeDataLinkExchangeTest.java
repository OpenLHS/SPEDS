package ca.griis.speds.link.unit.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.api.factory.ImmutableDataLinkFactory;
import ca.griis.speds.link.internal.ImmutableDataLinkHost;
import ca.griis.speds.link.internal.serializer.SharedObjectMapper;
import ca.griis.speds.utils.X509CertificateCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MutalModeDataLinkExchangeTest implements HostEvent {
  private Host client;
  private Host server;
  private LinkedBlockingQueue<String> idus = new LinkedBlockingQueue<>();

  @BeforeEach
  void setUp() throws Exception {
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

    final ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();
    final String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
    final String certBase64 = Base64.getEncoder().encodeToString(certificate.getEncoded());
    final String rootCertBase64 = Base64.getEncoder().encodeToString(rootCert.getEncoded());

    final String parametersJson1 = """
        {
          "options": {
            "speds.dl.protocol": "https",
            "speds.dl.https.private.key": "%s",
            "speds.dl.https.cert": "%s",
            "speds.dl.https.root.certs": ["%s"],
            "speds.dl.https.server.host": "0.0.0.0",
            "speds.dl.https.server.port": 8080,
            "speds.dl.https.mode":  "mTLS",
            "speds.dl.https.max.content.length.bytes": 50000
          }
        }
        """.formatted(
        privateKeyBase64,
        certBase64,
        rootCertBase64);

    client = (ImmutableDataLinkHost) factory.init(parametersJson1, this);

    final String parametersJson2 = """
        {
          "options": {
            "speds.dl.protocol": "https",
            "speds.dl.https.private.key": "%s",
            "speds.dl.https.cert": "%s",
            "speds.dl.https.root.certs": ["%s"],
            "speds.dl.https.server.host": "0.0.0.0",
            "speds.dl.https.server.port": 8081,
            "speds.dl.https.mode": "mTLS",
            "speds.dl.https.max.content.length.bytes": 50000
          }
        }
        """.formatted(
        privateKeyBase64,
        certBase64,
        rootCertBase64);

    server = (ImmutableDataLinkHost) factory.init(parametersJson2, this);
  }

  @AfterEach
  public void teardown() {
    client.close();
    server.close();
  }

  @Test
  public void exchange() throws JsonProcessingException, InterruptedException {
    String idu1Json =
        """
            {
            "context": {
            "destination_iri" : "https://localhost:8081",
            "service" : "transfer",
            "service_primitive" : "request",
            "options" : false
            },
            "message": "%s"
            }
            """.formatted(bigMessage());
    client.submitIdu(idu1Json);

    String receivedIdu1Json = idus.poll(2, TimeUnit.SECONDS);

    InterfaceDataUnit56Dto expectedIdu1 = SharedObjectMapper.getInstance().getMapper()
        .readValue(idu1Json, InterfaceDataUnit56Dto.class);
    InterfaceDataUnit56Dto receivedIdu1 = SharedObjectMapper.getInstance().getMapper()
        .readValue(receivedIdu1Json, InterfaceDataUnit56Dto.class);

    assertEquals(expectedIdu1.getMessage(), receivedIdu1.getMessage());
  }

  @Override
  public void notifyIdu(String idu) {
    idus.add(idu);
  }

  private String bigMessage() {
    return """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer auctor ligula et vestibulum scelerisque. Cras massa tellus, tempor nec congue non, pellentesque nec elit. Nunc lacus dolor, aliquet eu diam porta, rhoncus sodales quam. Maecenas ut neque dignissim, blandit lacus a, accumsan enim. Ut massa purus, faucibus ut ex quis, vulputate pretium velit. Curabitur commodo sed lorem non blandit. Nullam et euismod lacus.
        Suspendisse rhoncus id arcu pretium laoreet. Proin maximus ante eu semper pellentesque. Maecenas non consectetur ex, non faucibus massa. Cras nec dolor quam. Sed in consectetur mi. Cras justo lacus, sagittis a elementum quis, tempor at purus. Pellentesque turpis nisl, dictum a velit sit amet, vehicula vestibulum ante.
        Ut euismod velit eu lectus fringilla, eu bibendum nibh suscipit. Praesent eu massa laoreet, ultrices orci vel, malesuada odio. Proin nec molestie orci, ut maximus ligula. Proin semper non lorem quis feugiat. In dignissim elementum neque, sit amet accumsan ante dignissim et. Suspendisse ultricies mollis velit eu malesuada. Maecenas eget feugiat ex, ac venenatis enim. Mauris suscipit felis in commodo consequat. Nullam porta massa neque. Fusce finibus augue sed tincidunt ornare. Sed nunc arcu, malesuada quis faucibus vel, faucibus non metus. Sed vitae fringilla leo.
        Suspendisse potenti. Fusce dignissim ante sit amet iaculis dictum. Vivamus eu tristique justo. Integer imperdiet iaculis elementum. Nulla facilisi. Sed sagittis blandit nisl, sed ultrices ligula dapibus a. Integer consectetur eu sem ut vehicula. Curabitur euismod at lectus eu egestas. Pellentesque nec tellus arcu. Nulla congue ex sit amet porttitor tempor. Suspendisse tincidunt tellus nec magna vestibulum rhoncus.
        Vivamus maximus purus enim, sit amet semper turpis rhoncus ac. Sed eget magna pharetra, faucibus justo vitae, mattis dui. Vivamus et lorem mi. Vivamus nulla felis, pretium ut bibendum at, faucibus vel nulla. Nullam sed nisi ut tellus sollicitudin accumsan. Nunc massa augue, lobortis ut risus at, iaculis tempus ipsum. In elementum massa ligula. Ut quam neque, vulputate eu malesuada ut, pretium at tellus. Phasellus dapibus aliquet augue eu scelerisque. Vestibulum at orci sed enim eleifend egestas vitae at risus. Maecenas vel magna finibus, fringilla mi ut, pellentesque sapien. Nam tincidunt arcu at metus mattis, ut maximus leo ultricies. Fusce cursus orci id purus feugiat faucibus. Cras dapibus in erat ac dignissim.
        Suspendisse commodo convallis velit non consectetur. Integer est nibh, laoreet sed ullamcorper id, semper et magna. Aenean auctor rutrum rutrum. Mauris dictum mollis consectetur. Praesent nec tellus sed metus dignissim lobortis eu sit amet tellus. Ut lacinia dui vitae auctor iaculis. In quis eros a arcu sodales efficitur eget ac diam. Cras ut posuere odio. Aenean vel mattis neque, eu rhoncus odio.
        Cras et dui est. Mauris consectetur in turpis a mollis. Integer mattis fringilla gravida. Nulla in tempus purus. Maecenas pretium nibh nec eros auctor dignissim. Quisque elementum congue tristique. Phasellus luctus urna ac enim lobortis, euismod auctor elit aliquam. Phasellus sit amet velit neque. Praesent placerat sit amet sem ac iaculis. Maecenas facilisis, velit a iaculis efficitur, massa erat tristique elit, sed rutrum libero leo et nulla.
        Mauris sit amet lectus dapibus, maximus elit nec, cursus odio. Aenean volutpat erat a luctus facilisis. Nulla a dolor non diam euismod rutrum. Etiam in faucibus nibh. Nullam ac sem ut sapien vestibulum vulputate vitae non augue. Quisque sit amet rhoncus mi. Ut cursus lectus quis ex consectetur, eget viverra ante interdum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aliquam justo ipsum, convallis vitae dui id, tempor consectetur eros. Maecenas dapibus quam sed urna finibus, et semper dui mollis. Duis gravida nibh ut libero ultrices interdum. Etiam malesuada nisl ac volutpat scelerisque. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Vestibulum varius, orci a vulputate laoreet, diam augue viverra sem, et tempor enim velit at metus. Aenean rhoncus sollicitudin nulla sed tristique. Quisque sit amet mauris eget sapien eleifend maximus in sed ante.
        Maecenas condimentum blandit mi, non gravida sapien pellentesque eu. Proin molestie iaculis convallis. Vestibulum dui odio, gravida ac dui at, volutpat varius urna. Maecenas nec lorem neque. Nullam sit amet dolor et odio efficitur euismod quis in felis. Curabitur ut mauris eget quam auctor dapibus. Proin lacinia aliquam cursus. Nullam eu ultrices nulla, in luctus nulla. Integer eget ante mollis, dictum sapien lobortis, sodales mi. Donec ullamcorper mauris non blandit viverra. Duis sed aliquet nulla, non semper nibh. Sed in pharetra est, et commodo nisi. Donec ac nibh eget urna vestibulum finibus. Duis auctor scelerisque magna. Integer molestie quis justo vel blandit. Aliquam erat volutpat.
        Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Cras pulvinar, enim eget auctor convallis, est est viverra tellus, at pretium arcu tellus nec elit. In et dapibus odio. Vestibulum pretium arcu non est posuere, at interdum massa maximus. Cras eleifend ipsum mauris, eu sollicitudin ante porta ut. Vivamus ultricies, mi at euismod rutrum, neque nibh sagittis erat, sit amet blandit orci est eget elit. Vivamus vestibulum nulla eget semper ultricies. Curabitur et viverra nibh. Nam tempus magna sit amet fringilla luctus. Curabitur eget dapibus massa, eu fringilla ante. Quisque vel tincidunt nisi. Nulla cursus consequat diam, vitae venenatis velit scelerisque id.
        Duis orci sem, pellentesque vel rhoncus et, mollis ut ex. Nullam ut odio et lectus gravida condimentum. Aliquam cursus in nulla id cursus. Sed placerat ligula a dui luctus pulvinar. Sed vel tincidunt mi. Mauris id enim lorem. Etiam maximus arcu at nulla bibendum, pellentesque rhoncus arcu convallis. In tincidunt lacus et leo scelerisque, vitae faucibus augue iaculis. Nunc rutrum, purus eu suscipit tincidunt, mi dolor malesuada tortor, in dictum odio augue et neque. Fusce lacinia nibh nec lorem bibendum, non eleifend orci condimentum. Cras diam leo, fringilla sit amet feugiat nec, tempor eu justo.
        Vivamus interdum tortor vitae turpis ultrices vulputate. Curabitur ut erat lacus. Aliquam aliquam ex nulla, sit amet aliquet orci ultrices molestie. Aliquam pharetra tincidunt eros, eget commodo ipsum. Donec scelerisque odio neque, et pellentesque diam efficitur vel. Aenean sapien eros, lobortis sed sapien quis, vestibulum fringilla nulla. Donec a massa non diam porttitor volutpat non sed mauris. Nulla sagittis feugiat elementum. Mauris eu diam in massa interdum luctus. Nam iaculis metus quis dolor consequat, eu interdum odio auctor.
        Mauris augue velit, malesuada sit amet risus vel, luctus imperdiet leo. Nam eu molestie urna, in consequat leo. Mauris quis arcu vel metus tristique volutpat ac eu odio. Mauris at velit dapibus diam elementum efficitur. Integer arcu neque, consequat et quam ac, posuere molestie quam. Nunc placerat placerat eros, quis molestie justo commodo eu. Nunc quis cursus justo. Quisque lorem est, maximus eu risus a, porta cursus nibh. Sed eu tincidunt felis, ut dapibus diam. Suspendisse hendrerit dui eu pharetra feugiat. Suspendisse finibus ultrices magna vestibulum tincidunt. Etiam dictum eu lacus vel tincidunt. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam elementum mauris eget nibh pretium tristique. Ut ut finibus purus, sit amet aliquet lacus.
        Duis consequat velit ipsum, sed hendrerit libero cursus ac. Nunc convallis elit et pharetra porttitor. Phasellus in varius nisl, eget ultrices metus. Vivamus laoreet euismod nulla ac facilisis. In a eleifend dolor, ut euismod neque. Cras ac tristique metus. Phasellus vel mollis nibh. Vivamus a semper est. Nullam et fringilla orci. Nullam odio risus, aliquam sit amet est a, condimentum mollis nulla.
        Nullam ultricies malesuada dui in ullamcorper. Aliquam accumsan justo a erat pellentesque malesuada. Nunc id viverra ligula. Duis sit amet semper diam, vel sollicitudin nisi. Donec laoreet eleifend nisi id hendrerit. Aenean sit amet nibh bibendum, euismod tellus non, elementum nulla. Duis tempor urna magna, in condimentum elit efficitur nec. Vivamus at purus sit amet mauris porttitor auctor. Morbi sollicitudin nisl ut convallis malesuada. Duis gravida elit sem, non gravida est rutrum nec. Aliquam erat volutpat. Donec tempor, ligula eu elementum pretium, mi sem laoreet mi, vel finibus ipsum ipsum eget lectus. Etiam blandit velit ut nisl facilisis vestibulum. Vivamus eget mi dignissim purus eleifend volutpat condimentum eget risus. Praesent aliquam pellentesque augue, vel tincidunt metus blandit eu. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
                    """
        .stripIndent().replaceAll("[\n\r]", "");
  }

  @Override
  public void notifyException(Exception exception) {}
}
