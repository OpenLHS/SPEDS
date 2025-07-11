package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.link.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.UUID;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class LinkDomainProviderUtil extends DomainContextBase {

  @Provide
  Arbitrary<InterfaceDataUnit56Dto> idu56Dto() throws JsonProcessingException {

    Arbitrary<String> iriDestinationArbitrary = Arbitraries.just("localhost:4051");

    Arbitrary<UUID> trackingNumberArbitrary = Arbitraries.just(
        UUID.fromString("00000000-0000-0000-0000-000000000001"));

    Arbitrary<Object> optionsArbitrary = Arbitraries.maps(
        Arbitraries.strings().alpha(),
        Arbitraries.strings().alpha()).asGeneric();

    Arbitrary<Context56Dto> contextArbitrary = Combinators.combine(iriDestinationArbitrary,
        trackingNumberArbitrary,
        optionsArbitrary)
        .as(Context56Dto::new);

    Arbitrary<String> messageArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit56Dto::new);
  }

}
