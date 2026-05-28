package ca.griis.speds.integration.network;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.speds.integration.security.CryptographyServiceCreator;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpedsNetworkTest {

  private String parametersSrc;
  private String parametersDest;
  private CryptographyService cryptographyService;
  private ObjectMapper mapper;
  private NetworkHost initiatorHost;
  private NetworkHost peerHost;
  private NetEvent initiatorConsumer = new NetEvent();
  private NetEvent peerConsumer = new NetEvent();
  private ExecutorService executor;

  @BeforeEach
  public void setUp() throws Exception {
    executor = Executors.newFixedThreadPool(50);
    cryptographyService = CryptographyServiceCreator.createCryptographyService();
    mapper = new ObjectMapper();

    InitInParamsDto params = new InitInParamsDto(NetworkParamsCreator.createSourceParams());
    parametersSrc = mapper.writeValueAsString(params);

    InitInParamsDto params2 = new InitInParamsDto(NetworkParamsCreator.createDestinationParams());
    parametersDest = mapper.writeValueAsString(params2);

    NetworkFactory factory = new NetworkHostFactory(cryptographyService);
    initiatorHost = factory.initHost(parametersSrc, initiatorConsumer);
    peerHost = factory.initHost(parametersDest, peerConsumer);
  }

  @AfterEach
  public void afterEach() throws Exception {
    initiatorHost.close();
    peerHost.close();

    executor.shutdown();
  }

  @Test
  public void nominal() throws Exception {
    var context = new Context45Dto("https://localhost:8081", "https://localhost:8082", "transfer",
        Context45Dto.ServicePrimitive.REQUEST, false);
    var idu45 = new InterfaceDataUnit45Dto(context, "Some data to exchange");
    var idu = mapper.writeValueAsString(idu45);

    var submitResult = initiatorHost.submitIdu(idu);

    var confirmIsPresent = submitResult.get();
    assertTrue(confirmIsPresent.isPresent());

    var confirm = mapper.readValue(confirmIsPresent.get(), InterfaceDataUnit45Dto.class);
    assertTrue(confirm.getContext().getServicePrimitive() == Context45Dto.ServicePrimitive.CONFIRM);
    assertTrue(confirm.getMessage().equals("SUCCEED"));

    String peerReceivedIdu = peerConsumer.poll(10L);
    var indicationIdu = mapper.readValue(peerReceivedIdu, InterfaceDataUnit45Dto.class);
    assertTrue(indicationIdu.getContext()
        .getServicePrimitive() == Context45Dto.ServicePrimitive.INDICATION);
    assertTrue(indicationIdu.getMessage().equals("Some data to exchange"));

    final var params = mapper.convertValue(indicationIdu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});
    String id = params.get("TN");
    var options = Map.of("TN", id, "IRI", "https://localhost:8082");

    var responseContext =
        new Context45Dto("https://localhost:8081", "https://localhost:8082", "transfer",
            Context45Dto.ServicePrimitive.RESPONSE, options);
    var responseIdu = new InterfaceDataUnit45Dto(responseContext, "Thank you");
    var serializedResponseIdu = mapper.writeValueAsString(responseIdu);

    var responseResult = peerHost.submitIdu(serializedResponseIdu);
    assertTrue(responseResult.get().isEmpty());

    assertTrue(initiatorConsumer.isEmpty());
  }

  @Test
  public void stressAsync() throws Exception {
    int n = 50;
    AtomicInteger responses = new AtomicInteger(0);

    Runnable peerWorker = () -> {
      try {
        while (responses.get() < n) {

          String peerReceivedIdu = peerConsumer.poll(10L);
          assertNotNull(peerReceivedIdu);

          var indicationIdu =
              mapper.readValue(peerReceivedIdu, InterfaceDataUnit45Dto.class);

          final var params =
              mapper.convertValue(
                  indicationIdu.getContext().getOptions(),
                  new TypeReference<Map<String, String>>() {});

          String id = params.get("TN");
          var options = Map.of("TN", id, "IRI", "https://localhost:8082");

          var responseContext =
              new Context45Dto(
                  "https://localhost:8081",
                  "https://localhost:8082",
                  "transfer",
                  Context45Dto.ServicePrimitive.RESPONSE,
                  options);

          var responseIdu = new InterfaceDataUnit45Dto(responseContext, "Thank you");

          var serializedResponseIdu = mapper.writeValueAsString(responseIdu);

          var responseResult = peerHost.submitIdu(serializedResponseIdu);

          assertTrue(responseResult.get().isEmpty());

          responses.incrementAndGet();
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    for (int i = 0; i < 5; i++) {
      executor.submit(peerWorker);
    }

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < n; i++) {

      futures.add(
          CompletableFuture.runAsync(() -> {

            try {
              var context =
                  new Context45Dto(
                      "https://localhost:8081",
                      "https://localhost:8082",
                      "transfer",
                      Context45Dto.ServicePrimitive.REQUEST,
                      false);

              var idu45 =
                  new InterfaceDataUnit45Dto(context, "Some data to exchange with");

              var idu = mapper.writeValueAsString(idu45);

              var submitResult = initiatorHost.submitIdu(idu);

              var confirmIsPresent = submitResult.get();

              assertTrue(confirmIsPresent.isPresent());

              var confirm = mapper.readValue(confirmIsPresent.get(), InterfaceDataUnit45Dto.class);

              assertTrue(confirm.getContext()
                  .getServicePrimitive() == Context45Dto.ServicePrimitive.CONFIRM);

              assertTrue(confirm.getMessage().equals("SUCCEED"));

            } catch (Exception e) {
              throw new RuntimeException(e);
            }

          }, executor));
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    // attendre que toutes les réponses soient envoyées
    while (responses.get() < n) {
      Thread.sleep(100);
    }
  }
}
