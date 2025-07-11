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

public class ApplicationDomainProviderUtil extends DomainContextBase {

  private ObjectMapper mapper = new ObjectMapper();

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
        projectNumberArbitrary,
        codeSourceArbitrary,
        codeDestinationArbitrary,
        trackingNumberArbitrary,
        optionsArbitrary)
        .as(ContextDto::new);


    Arbitrary<HeaderDto> headerArbitrary = Combinators.combine(
        Arbitraries.of(HeaderDto.Msgtype.class),
        Arbitraries.create(UUID::randomUUID),
        Arbitraries.just(false),
        createSPEDSDtoArbitrary())
        .as((msgtype, id, parameters, speds) -> new HeaderDto(msgtype, id, parameters, speds));

    Arbitrary<String> contentArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    Arbitrary<ProtocolDataUnit1APPDto> messageArbitrary = Combinators.combine(
        headerArbitrary,
        contentArbitrary)
        .as(ProtocolDataUnit1APPDto::new);

    ProtocolDataUnit1APPDto mess = messageArbitrary.sample();
    Arbitrary<String> messageString = Arbitraries.just(mapper.writeValueAsString(mess));

    Arbitrary<InterfaceDataUnit01Dto> dtoArbitrary = Combinators.combine(
        contextArbitrary,
        messageString)
        .as(InterfaceDataUnit01Dto::new);

    return dtoArbitrary;
  }

  private Arbitrary<SPEDSDto> createSPEDSDtoArbitrary() {
    Arbitrary<String> spedsVersionArbitrary = Arbitraries.just("3.0.0");
    Arbitrary<String> spedsReferenceArbitrary = Arbitraries.just("https://reference.iri/speds");

    Arbitrary<SPEDSDto> spedsDtoArbitrary = Combinators.combine(
        spedsVersionArbitrary, spedsReferenceArbitrary)
        .as(SPEDSDto::new);

    return spedsDtoArbitrary;
  }
}
