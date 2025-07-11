package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.js2p.gen.speds.application.api.dto.SPEDSDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class DomainProviderUtil extends DomainContextBase {
  private final ObjectMapper objectMapper;

  public DomainProviderUtil() {
    this.objectMapper = new ObjectMapper();
  }

  @Provide
  Arbitrary<InterfaceDataUnit01Dto> idu01Dto() throws JsonProcessingException {
    Arbitrary<String> projectNumberArbitrary = Arbitraries.just("someprojectnumber");
    Arbitrary<String> codeSourceArbitrary = Arbitraries.just("source");
    Arbitrary<String> codeDestinationArbitrary = Arbitraries.just("destination");

    Arbitrary<UUID> trackingNumberArbitrary = Arbitraries.randomValue(
        random -> UUID.randomUUID());

    Arbitrary<Object> optionsArbitrary = Arbitraries.maps(
        Arbitraries.strings().alpha(),
        Arbitraries.strings().alpha()).asGeneric();

    Arbitrary<ContextDto> contextArbitrary = Combinators.combine(
        projectNumberArbitrary, codeSourceArbitrary, codeDestinationArbitrary,
        trackingNumberArbitrary, optionsArbitrary)
        .as(ContextDto::new);

    Arbitrary<HeaderDto> headerAppArbitrary = Combinators.combine(
        Arbitraries.of(HeaderDto.Msgtype.class),
        Arbitraries.create(UUID::randomUUID),
        Arbitraries.just(false),
        createSPEDSDtoArbitrary())
        .as((msgtype, id, parameters, speds) -> new HeaderDto(msgtype, id, parameters, speds));

    Arbitrary<String> pduContentArbitrary = Arbitraries.strings()
        .alpha().ofMinLength(1).ofMaxLength(100);

    Arbitrary<ProtocolDataUnit1APPDto> appPduArbitrary =
        Combinators.combine(headerAppArbitrary, pduContentArbitrary)
            .as(ProtocolDataUnit1APPDto::new);

    Arbitrary<String> messageArbitrary = appPduArbitrary.map(pdu -> {
      try {
        return objectMapper.writeValueAsString(pdu);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit01Dto::new);
  }

  private Arbitrary<SPEDSDto> createSPEDSDtoArbitrary() {
    Arbitrary<String> spedsVersionArbitrary = Arbitraries.just("2.0.0");
    Arbitrary<String> spedsReferenceArbitrary = Arbitraries.just("a reference");

    Arbitrary<SPEDSDto> spedsDtoArbitrary = Combinators.combine(
        spedsVersionArbitrary, spedsReferenceArbitrary)
        .as(SPEDSDto::new);

    return spedsDtoArbitrary;
  }
}
