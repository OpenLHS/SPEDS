package ca.griis.speds.link.integration.conception;

import static ca.griis.speds.link.integration.conception.Cases.c010_response_missingIri_fail;
import static ca.griis.speds.link.integration.conception.Cases.c011_response_missingSdu_fail;
import static ca.griis.speds.link.integration.conception.Cases.c012_confirmation_success;
import static ca.griis.speds.link.integration.conception.Cases.c05_request_invalidIri_fail;
import static ca.griis.speds.link.integration.conception.Cases.c06_request_missingIri_fail;
import static ca.griis.speds.link.integration.conception.Cases.c07_request_missingSdu_fail;
import static ca.griis.speds.link.integration.conception.Cases.c08_request_success;
import static ca.griis.speds.link.integration.conception.Cases.c09_response_invalidIri_fail;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

public class TestSuite {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  @Test
  public void e02_test() throws IOException, InterruptedException {
    try (
        ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {
      // todo
      freePorts(originSocket, targetSocket);

      // Configure l'environnement de tests
      Environment environment = new Environment();
      environment.objMap = this.objectMapper;

      ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();

      environment.originPort = originSocket.getLocalPort();
      environment.originAddress = "0.0.0.0";
      String originParams = environment.instantiateParams(environment.originPort);
      environment.originHost = factory.init(originParams);

      environment.targetPort = targetSocket.getLocalPort();
      environment.targetAddress = "localhost";
      String targetParams = environment.instantiateParams(environment.targetPort);
      environment.targetHost = factory.init(targetParams);

      // Exécution des cas de tests dans l'ordre.
      c05_request_invalidIri_fail(environment);
      c06_request_missingIri_fail(environment);
      c07_request_missingSdu_fail(environment);
      c08_request_success(environment);
    }
  }

  @Test
  public void e03_test() throws IOException, InterruptedException {
    try (
        ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {
      // todo
      freePorts(originSocket, targetSocket);

      // Configure l'environnement de tests
      Environment environment = new Environment();
      environment.objMap = this.objectMapper;

      // init so we get something else than nullPointer
      ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();

      environment.originPort = originSocket.getLocalPort();
      environment.originAddress = "localhost";
      String originParams = environment.instantiateParams(environment.originPort);
      environment.originHost = factory.init(originParams);

      environment.targetPort = targetSocket.getLocalPort();
      environment.targetAddress = "localhost";
      String targetParams = environment.instantiateParams(environment.targetPort);
      environment.targetHost = factory.init(targetParams);

      // MD - prerequisite for environement is target host previously received a request
      // c08 satisfies this condition.
      InterfaceDataUnit56Dto incomingServerMessage = c08_request_success(environment);

      // actual test
      c012_confirmation_success(environment, incomingServerMessage);
    }
  }

  @Test
  public void e04_test() throws IOException, InterruptedException {
    try (
        ServerSocket originSocket = new ServerSocket(0);
        ServerSocket targetSocket = new ServerSocket(0)) {

      // todo
      freePorts(originSocket, targetSocket);

      // Configure l'environnement de tests
      Environment environment = new Environment();
      environment.objMap = this.objectMapper;

      // init so we get something else than nullPointer
      ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();

      environment.originPort = originSocket.getLocalPort();
      environment.originAddress = "localhost";
      String originParams = environment.instantiateParams(environment.originPort);
      environment.originHost = factory.init(originParams);

      environment.targetPort = targetSocket.getLocalPort();
      environment.targetAddress = "localhost";
      String targetParams = environment.instantiateParams(environment.targetPort);
      environment.targetHost = factory.init(targetParams);

      // MD - prerequisite for environement is target host previously received a request
      // c08 satisfies this condition.
      InterfaceDataUnit56Dto incomingServerMessage = c08_request_success(environment);

      // actual test
      c09_response_invalidIri_fail(environment, incomingServerMessage);
      c010_response_missingIri_fail(environment, incomingServerMessage);
      c011_response_missingSdu_fail(environment, incomingServerMessage);
      c012_confirmation_success(environment, incomingServerMessage);
    }
  }

  // todo
  private static void freePorts(ServerSocket originSocket, ServerSocket targetSocket)
      throws IOException {
    // Ensure no time out after we close it.
    // md - Je suis pas sur comment réserver un socket plus propre que ça.
    // Je réserve le port, ce faisant je le bloque.
    // Je met reuseAdress parce que quand tu close, il peut y avoir un timeout
    // Je met close dans l'espoir que personne pogne le port d'ici à ce que je l'utilise
    // Pense pas que c'est trop grave, pas trop le temps.
    originSocket.setReuseAddress(true);
    originSocket.close();
    targetSocket.setReuseAddress(true);
    targetSocket.close();
  }
}
