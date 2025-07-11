package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class TransportDomainProviderUtil extends DomainContextBase {

  @Provide
  Arbitrary<InterfaceDataUnit34Dto> idu34Dto() throws JsonProcessingException {
    Arbitrary<String> iriSourceArbitrary = Arbitraries.just("https://localhost:4000");
    Arbitrary<String> iriDestinationArbitrary = Arbitraries.just("https://localhost:4001");

    Arbitrary<String> codeSourceArbitrary = Arbitraries.just("codeSource");
    Arbitrary<String> codeDestinationArbitrary = Arbitraries.just("codeDestination");

    Arbitrary<UUID> trackingNumberArbitrary = Arbitraries.randomValue(
        random -> UUID.randomUUID());

    Arbitrary<Object> optionsArbitrary = Arbitraries.maps(
        Arbitraries.strings().alpha(),
        Arbitraries.strings().alpha()).asGeneric();

    Arbitrary<Context34Dto> contextArbitrary = Combinators.combine(
        codeSourceArbitrary,
        codeDestinationArbitrary,
        iriSourceArbitrary,
        trackingNumberArbitrary,
        iriDestinationArbitrary,
        optionsArbitrary)
        .as(Context34Dto::new);


    Arbitrary<String> messageArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit34Dto::new);
  }
}
