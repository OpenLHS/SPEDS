package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class NetworkDomainProviderUtil extends DomainContextBase {

  @Provide
  Arbitrary<InterfaceDataUnit45Dto> idu45Dto() throws JsonProcessingException {
    Arbitrary<String> iriSourceArbitrary = Arbitraries.just("https://localhost:4000");

    Arbitrary<String> iriDestinationArbitrary = Arbitraries.just("https://localhost:4001");

    Arbitrary<UUID> trackingNumberArbitrary = Arbitraries.randomValue(
        random -> UUID.randomUUID());

    Arbitrary<Object> optionsArbitrary = Arbitraries.maps(
        Arbitraries.strings().alpha(),
        Arbitraries.strings().alpha()).asGeneric();

    Arbitrary<Context45Dto> contextArbitrary = Combinators.combine(
        iriSourceArbitrary,
        iriDestinationArbitrary,
        trackingNumberArbitrary, optionsArbitrary)
        .as(Context45Dto::new);


    Arbitrary<String> messageArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit45Dto::new);
  }
}
