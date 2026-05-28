package ca.griis.speds.integration.application;

import ca.griis.js2p.gen.speds.application.api.dto.Service;
import ca.griis.js2p.gen.speds.application.api.dto.ServicePrimitive;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.internal.domain.ApplicationInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AppHostEvent implements ApplicationHostEvent {
  private final LinkedBlockingQueue<ApplicationInterface> idus = new LinkedBlockingQueue<>();
  private final ObjectMapper mapper;

  public AppHostEvent(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public ApplicationInterface poll(Long times) throws InterruptedException {
    return idus.poll(times, TimeUnit.SECONDS);
  }

  @Override
  public void notifyException(Exception exception) {}

  @Override
  public CompletableFuture<ApplicationInterface> notify(ApplicationInterface applicationInterface) {
    idus.add(applicationInterface);

    final Map<String, Object> responseMap = Map.of("response", "content");
    String responseContent;
    try {
      responseContent = mapper.writeValueAsString(responseMap);
    } catch (JsonProcessingException e) {
      responseContent = "";
    }

    var response = new ApplicationInterface(
        Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        applicationInterface.sourceCode(),
        applicationInterface.destinationCode(),
        applicationInterface.projectId(),
        applicationInterface.msgId(),
        applicationInterface.msgType(),
        responseContent);

    return CompletableFuture.completedFuture(response);
  }

  public Boolean isEmpty() {
    return idus.isEmpty();
  }
}
