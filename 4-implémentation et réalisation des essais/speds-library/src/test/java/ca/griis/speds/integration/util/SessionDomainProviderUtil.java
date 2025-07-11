package ca.griis.speds.integration.util;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.SecretKey;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class SessionDomainProviderUtil extends DomainContextBase {

  @Provide
  Arbitrary<InterfaceDataUnit23Dto> idu23Dto() throws JsonProcessingException {
    Arbitrary<String> projectNumberArbitrary = Arbitraries.just("someprojectnumber");
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    byte[] keyBytes = secretKey.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    Arbitrary<String> sdekArbitrary = Arbitraries.just(serialKey);

    Arbitrary<String> codeSourceArbitrary = Arbitraries.just("source");
    Arbitrary<String> codeDestinationArbitrary = Arbitraries.just("destination");

    Arbitrary<UUID> trackingNumberArbitrary = Arbitraries.randomValue(
        random -> UUID.randomUUID());

    Arbitrary<Object> optionsArbitrary = Arbitraries.maps(
        Arbitraries.strings().alpha(),
        Arbitraries.strings().alpha()).asGeneric();

    Arbitrary<Context23Dto> contextArbitrary = Combinators.combine(
        projectNumberArbitrary,
        codeSourceArbitrary,
        codeDestinationArbitrary,
        sdekArbitrary,
        trackingNumberArbitrary,
        optionsArbitrary)
        .as(Context23Dto::new);


    Arbitrary<String> messageArbitrary = Arbitraries.strings().ofMinLength(1).alpha();

    return Combinators.combine(contextArbitrary, messageArbitrary)
        .as(InterfaceDataUnit23Dto::new);
  }
}
