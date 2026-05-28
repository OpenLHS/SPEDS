package ca.griis.speds.link.integration.conception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.link.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Cases {
  public void ct_001(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException {
    assertNotNull(environment.getOriginHost(), "precondition originHost should not be null");
    assertNotNull(environment.getTargetHost(), "precondition targetHost should not be null");

    String destinationIri = "garbage iri";
    var iduDto = new InterfaceDataUnit56Dto(
        new ContextDto(destinationIri, ContextDto.Service.TRANSFER,
            ContextDto.ServicePrimitive.REQUEST, false),
        "This is the message");

    var idu = environment.getObjMap().writeValueAsString(iduDto);
    var confirm = environment.getOriginHost().submitIdu(idu).get().get();

    var result = environment.getObjMap().readValue(confirm, InterfaceDataUnit56Dto.class);
    assertEquals(result.getContext().getServicePrimitive(), ContextDto.ServicePrimitive.CONFIRM);
    assertEquals(result.getContext().getService(), ContextDto.Service.TRANSFER);
    assertTrue(result.getMessage().contains("FAILED:"));
  }

  public void ct_002(Environment environment)
      throws JsonProcessingException, InterruptedException, ExecutionException {
    assertNotNull(environment.getOriginHost(), "precondition originHost should not be null");
    assertNotNull(environment.getTargetHost(), "precondition targetHost should not be null");

    var destinationIri = "https://localhost:" + environment.getTargetPort();
    var iduDto = new InterfaceDataUnit56Dto(
        new ContextDto(destinationIri, ContextDto.Service.TRANSFER,
            ContextDto.ServicePrimitive.REQUEST, false),
        "This is the message");

    var idu = environment.getObjMap().writeValueAsString(iduDto);
    var confirm = environment.getOriginHost().submitIdu(idu).get().get();

    var result = environment.getObjMap().readValue(confirm, InterfaceDataUnit56Dto.class);
    assertEquals(result.getContext().getServicePrimitive(), ContextDto.ServicePrimitive.CONFIRM);
    assertEquals(result.getContext().getService(), ContextDto.Service.TRANSFER);
    assertEquals(result.getMessage(), "SUCCEED");
  }

  public void ct_003(Environment environment) throws JsonProcessingException, InterruptedException {
    assertNotNull(environment.getOriginHost(), "precondition originHost should not be null");
    assertNotNull(environment.getTargetHost(), "precondition targetHost should not be null");

    final String content = "This is the message";

    var destinationIri = "https://localhost:" + environment.getTargetPort();
    var iduDto = new InterfaceDataUnit56Dto(
        new ContextDto(destinationIri, ContextDto.Service.TRANSFER,
            ContextDto.ServicePrimitive.REQUEST, false),
        "This is the message");

    var idu = environment.getObjMap().writeValueAsString(iduDto);
    environment.getOriginHost().submitIdu(idu);

    var indication = environment.getIdus().poll(2, TimeUnit.SECONDS);
    var result = environment.getObjMap().readValue(indication, InterfaceDataUnit56Dto.class);

    assertEquals(result.getContext().getServicePrimitive(), ContextDto.ServicePrimitive.INDICATION);
    assertEquals(result.getContext().getService(), ContextDto.Service.TRANSFER);
    assertEquals(result.getMessage(), content);
  }
}
