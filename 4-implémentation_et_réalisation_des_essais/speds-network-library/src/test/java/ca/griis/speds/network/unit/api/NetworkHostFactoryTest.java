package ca.griis.speds.network.unit.api;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.OptionsDto;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.api.factory.ImmutableDataLinkFactory;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.network.api.NetworkHostFactory;
import ca.griis.speds.network.api.exception.ParameterException;
import ca.griis.speds.network.internal.event.NetworkEventHandler;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.network.util.X509CertificateCreator;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkHostFactoryTest {
  private static KeyPair key;
  private static X509Certificate cert;

  private String parameters;

  @Mock
  private Host dataLinkHost;

  private NetworkFactory networkFactory;

  private CryptographyService cryptographyService;

  @Mock
  private ImmutableDataLinkFactory dataLinkFactory;

  @Captor
  private ArgumentCaptor<String> messageCaptor;

  @BeforeAll
  public static void setupAll() throws Exception {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }

    key = X509CertificateCreator.generateRsaKeyPair(4096);
    cert = X509CertificateCreator.createCertificate(
        "CN=host1.ca",
        "CN=host1.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "host1.ca",
        "SHA256withRSA");
  }

  @BeforeEach
  public void setUp() throws Exception {
    OptionsDto options = new OptionsDto(
        "7",
        "https://reference.iri/speds",
        Base64.getEncoder().encodeToString(cert.getEncoded()),
        Base64.getEncoder().encodeToString(key.getPrivate().getEncoded()),
        1);
    InitInParamsDto params = new InitInParamsDto(options);
    parameters = SharedObjectMapper.getInstance().getMapper().writeValueAsString(params);
    cryptographyService = SecurityUtils.createCryptographyService();
  }

  @Test
  public void noArgConstructor() throws Exception {
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        return dataLinkHost;
      }
    };

    assertNotNull(networkFactory);
  }

  @Test
  public void initHostSuccess() throws Exception {
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        assertNotNull(hostEventConsumer);
        assertTrue(hostEventConsumer instanceof NetworkEventHandler);

        return dataLinkHost;
      }
    };

    final var eventsConsumer = mock(NetworkHostEvent.class);
    final var host = networkFactory.initHost(parameters, eventsConsumer);
    assertNotNull(host);

    host.close();
  }

  @Test
  public void initHostBadJsonParameters() throws Exception {
    final IdentifierGenerator gen = () -> UUID.randomUUID();
    networkFactory = new NetworkHostFactory(gen, cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        return dataLinkHost;
      }
    };

    var eventsConsumer = mock(NetworkHostEvent.class);
    assertThrows(ParameterException.class,
        () -> networkFactory.initHost("wrongJson", eventsConsumer));
  }

  @Test
  public void submitRequestIdu() throws Exception {
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        return dataLinkHost;
      }
    };

    final var eventsConsumer = mock(NetworkHostEvent.class);
    final var host = networkFactory.initHost(parameters, eventsConsumer);

    final var confirmLinkContext =
        new Context56Dto("https://host1.ca:4044", "transfer", Context56Dto.ServicePrimitive.REQUEST,
            false);
    final var confirmIdu56 = new InterfaceDataUnit56Dto(confirmLinkContext, "SUCCEED");
    final var confirmLinkIdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu56);
    final var future = CompletableFuture.completedFuture(Optional.of(confirmLinkIdu));
    when(dataLinkHost.submitIdu(anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "DATA");
    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);

    host.submitIdu(idu);
    host.submitIdu(idu);

    verify(dataLinkHost, times(2)).submitIdu(messageCaptor.capture());

    var messages = messageCaptor.getAllValues();
    assertNotEquals(messages.get(0), messages.get(1));

    host.close();
  }

  @Test
  public void submitResponseIduWitoutIndication() throws Exception {
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        return dataLinkHost;
      }
    };

    final var eventsConsumer = mock(NetworkHostEvent.class);
    final var host = networkFactory.initHost(parameters, eventsConsumer);
    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.RESPONSE, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "data");
    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);

    var result = host.submitIdu(idu).get();
    verify(dataLinkHost).submitIdu(messageCaptor.capture());

    assertTrue(result.isEmpty());

    host.close();
  }

  @Test
  public void requestIndicationIdu() throws Exception {
    final NetworkEventHandler[] eventHandler = new NetworkEventHandler[1];
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        eventHandler[0] = (NetworkEventHandler) hostEventConsumer;
        return dataLinkHost;
      }
    };

    final var eventsConsumer = mock(NetworkHostEvent.class);
    final var host = networkFactory.initHost(parameters, eventsConsumer);

    final var confirmLinkContext =
        new Context56Dto("https://host1.ca:4043", "transfer",
            Context56Dto.ServicePrimitive.REQUEST, false);
    var confirmIdu56 = new InterfaceDataUnit56Dto(confirmLinkContext, "SUCCEED");
    var confirmLinkIdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu56);

    final var future =
        CompletableFuture.completedFuture(Optional.of(confirmLinkIdu));
    when(dataLinkHost.submitIdu(anyString())).thenReturn(future);

    var context = new Context45Dto("https://host1.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "DATA");
    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);

    var submitResult = host.submitIdu(idu);
    verify(dataLinkHost).submitIdu(messageCaptor.capture());

    assertTrue(submitResult.get().isPresent());

    var submitIdu = SharedObjectMapper.getInstance().getMapper().readValue(messageCaptor.getValue(),
        InterfaceDataUnit56Dto.class);

    var c = new Context56Dto(submitIdu.getContext().getDestinationIri(), "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    var indication = new InterfaceDataUnit56Dto(c, submitIdu.getMessage());
    var indicationIdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(indication);

    // Signal
    eventHandler[0].notifyIdu(indicationIdu);

    verify(eventsConsumer, times(1)).notifyIdu(any());

    host.close();
  }

  @Test
  public void requestIndicationIduInvalidIri() throws Exception {
    final NetworkEventHandler[] eventHandler = new NetworkEventHandler[1];
    networkFactory = new NetworkHostFactory(cryptographyService) {
      @Override
      public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
        eventHandler[0] = (NetworkEventHandler) hostEventConsumer;
        return dataLinkHost;
      }
    };

    final var eventsConsumer = mock(NetworkHostEvent.class);
    final var host = networkFactory.initHost(parameters, eventsConsumer);

    final var confirmLinkContext =
        new Context56Dto("https://host2.ca:4043", "transfer",
            Context56Dto.ServicePrimitive.REQUEST, false);
    var confirmIdu56 = new InterfaceDataUnit56Dto(confirmLinkContext, "SUCCEED");
    var confirmLinkIdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu56);

    final var future =
        CompletableFuture.completedFuture(Optional.of(confirmLinkIdu));
    when(dataLinkHost.submitIdu(anyString())).thenReturn(future);

    var context = new Context45Dto("https://host2.ca:4043", "https://host2.ca:4044", "transfer",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "DATA");
    var idu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);

    var submitResult = host.submitIdu(idu);
    verify(dataLinkHost, times(1)).submitIdu(messageCaptor.capture());

    assertTrue(submitResult.get().isPresent());

    var submitIdu = SharedObjectMapper.getInstance().getMapper().readValue(messageCaptor.getValue(),
        InterfaceDataUnit56Dto.class);

    var c = new Context56Dto(submitIdu.getContext().getDestinationIri(), "transfer",
        Context56Dto.ServicePrimitive.INDICATION, false);
    var indication = new InterfaceDataUnit56Dto(c, submitIdu.getMessage());
    var indicationIdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(indication);

    // Signal
    eventHandler[0].notifyIdu(indicationIdu);

    verify(eventsConsumer, times(0)).notifyIdu(any());
  }

  @Test
  void initDataLinkHostCalled() throws NoSuchFieldException, IllegalAccessException {
    // Given
    String parameters = "someParameters";

    networkFactory = new NetworkHostFactory(cryptographyService);

    // Override exit
    when(dataLinkFactory.init(any(), any())).thenReturn(dataLinkHost);
    Field factoryField = networkFactory.getClass().getDeclaredField("factory");
    factoryField.setAccessible(true);
    factoryField.set(networkFactory, dataLinkFactory);

    // When
    var eventsConsumer = mock(HostEvent.class);
    Host actual = networkFactory.initDataLinkHost(parameters, eventsConsumer);

    // Then
    assertNotNull(actual);
  }
}
