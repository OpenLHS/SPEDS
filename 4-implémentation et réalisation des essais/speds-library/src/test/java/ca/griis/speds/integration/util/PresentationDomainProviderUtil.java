package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class PresentationDomainProviderUtil extends DomainContextBase {

  @Provide
  Arbitrary<InterfaceDataUnit12Dto> idu12Dto() throws JsonProcessingException {
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


    Arbitrary<String> messageArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit12Dto::new);
  }
}
