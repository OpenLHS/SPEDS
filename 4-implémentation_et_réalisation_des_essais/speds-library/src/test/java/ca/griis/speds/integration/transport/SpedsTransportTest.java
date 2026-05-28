package ca.griis.speds.integration.transport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.speds.integration.security.CryptographyServiceCreator;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SpedsTransportTest {
  private static final String sourceCode = "source";
  private static final String destinationCode = "destination";

  private String parametersSrc;
  private String parametersDest;
  private TransportFactory factory;
  private CryptographyService cryptographyService;
  private ObjectMapper mapper;
  private TransportHost initiatorHost;
  private TransportHost peerHost;
  private TraEvent initiatorHostConsumer = new TraEvent();
  private TraEvent peerHostConsumer = new TraEvent();
  private ExecutorService executor;

  @BeforeEach
  public void setUp() throws Exception {
    executor = Executors.newFixedThreadPool(50);
    cryptographyService = CryptographyServiceCreator.createCryptographyService();
    mapper = new ObjectMapper();

    InitInParamsDto params = new InitInParamsDto(TransportParamsCreator.createSourceParams());
    parametersSrc = mapper.writeValueAsString(params);

    InitInParamsDto params2 = new InitInParamsDto(TransportParamsCreator.createDestinationParams());
    parametersDest = mapper.writeValueAsString(params2);

    factory = new TransportHostFactory(cryptographyService);
    initiatorHost = factory.initHost(parametersSrc, initiatorHostConsumer);
    peerHost = factory.initHost(parametersDest, peerHostConsumer);
  }

  @AfterEach
  public void afterEach() throws Exception {
    initiatorHost.close();
    peerHost.close();

    executor.shutdown();
  }

  @Test
  public void nominal() throws Exception {
    var context = new Context34Dto(sourceCode, destinationCode, "https://localhost:8081",
        Context34Dto.Service.TRANSFER, ServicePrimitive.REQUEST, "https://localhost:8082",
        false);
    var idu45 = new InterfaceDataUnit34Dto(context, "Some data to exchange");
    var idu = mapper.writeValueAsString(idu45);

    var submitResult = initiatorHost.submitIdu(idu);

    String peerReceivedIdu = peerHostConsumer.poll(10L);

    var indicationIdu = mapper.readValue(peerReceivedIdu, InterfaceDataUnit34Dto.class);
    assertTrue(indicationIdu.getContext().getServicePrimitive() == ServicePrimitive.INDICATION);
    assertTrue(indicationIdu.getMessage().equals("Some data to exchange"));

    final var params = SharedObjectMapper.getInstance().getMapper().convertValue(
        indicationIdu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});
    String id = params.get("TN");
    var options = Map.of("TN", id);

    var responseContex = new Context34Dto(sourceCode, destinationCode, "https://localhost:8081",
        Context34Dto.Service.TRANSFER, ServicePrimitive.RESPONSE, "https://localhost:8082",
        options);
    var responseIdu = new InterfaceDataUnit34Dto(responseContex, "Thank you");
    var serializedResponseIdu = mapper.writeValueAsString(responseIdu);

    var responseResult = peerHost.submitIdu(serializedResponseIdu);

    assertTrue(responseResult.get().isEmpty());
    assertTrue(submitResult.get().isPresent());

    var confirmIsPresent = submitResult.get();
    var confirm = mapper.readValue(confirmIsPresent.get(), InterfaceDataUnit34Dto.class);
    assertTrue(confirm.getContext().getServicePrimitive() == ServicePrimitive.CONFIRM);
    assertTrue(confirm.getMessage().equals("SUCCEED"));

    assertTrue(initiatorHostConsumer.isEmpty());
  }
}
