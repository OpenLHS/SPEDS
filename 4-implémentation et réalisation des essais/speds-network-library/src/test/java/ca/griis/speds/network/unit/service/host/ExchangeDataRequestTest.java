package ca.griis.speds.network.unit.service.host;

import static ca.griis.speds.network.unit.signature.SignatureProvider.getCertificatePem;
import static ca.griis.speds.network.unit.signature.SignatureProvider.getKeyPem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto.Msgtype;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.host.ExchangeDataRequest;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.SealManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExchangeDataRequestTest {

  private final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
  private ExchangeDataRequest exchangeRequest;

  @BeforeEach
  public void setUp() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    exchangeRequest = new ExchangeDataRequest(() -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SentMessageIdSet(), spedsVersion,
        spedsReference,
        CertificatePrivateKeyPair.importFromPem(getCertificatePem(), getKeyPem()),
        new SealManager(objectMapper));
  }

  @Test
  public void requestSuccess() throws Exception {
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "tracking_number": "846c0b99-7b8f-44d9-b3b6-766058eed965",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    String receivedStr = exchangeRequest.dataRequestProcess(idu45);
    InterfaceDataUnit56Dto received =
        objectMapper.readValue(receivedStr, InterfaceDataUnit56Dto.class);

    ProtocolDataUnit5Dto expectedPdu = new ProtocolDataUnit5Dto(new HeaderDto(Msgtype.RES_ENV,
        "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f", "https://host1.iri?code=host1",
        "https://proxy.iri?code=host2",
        "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",
        false, new SPEDSDto("3.0.0", "https://reference.iri/speds")),
        new StampDto(
            "TXyb6HOAgu9yZA8iVSCYvIIrlYv7HF/Zr2iDlWLCwHgQtmr6TX6c34Av6jmYk5s5XWB7OoEancHa0VlulmajBj8Gxd8Pytv2R4mstffLcm9nwT3cOyOyCoy/Uqeo3JgNR/bN0GbEx4k0WrnQZwzEBTlQML5EpX+Iu4xji1afC29et3+tGzdWUswQZj4gHl5woEOeohe0DybAZA4/gN2DgoZuFeS4p4ZQezMKColPh0JsK47DU52UHKNDBCsWX7DRbb8BUT+Y6Iv4Pg4GuCFGAsFZE+g6FCxvMiMLG3kv5G0/2NaRaAaXQ4lnYo/a+4kLGAZcduJ9q7Tfc+icqo0SCV2XKtjO657A/M0fY9m3tLumXzn0hB9G8t8AYHPmc3HaPr7cwzcYfda6ql4IOgOwZw9E3YlRvYsv4m1evs3GxjD84CFnv1P1op+hdfhH2LRs0OfA31BYsr2bDtOTe00E1uxer2Xe3aFAcNGFGB9FVaTg0pUvrorh0frF+xY2PuUl7EkoTLzuHSH3EtpmA5vgfUfZDTCpDBdO6yjlzkLH1msRMnEEHdkeZmae3fd03//rR+u57JUTTawgi3RM/DdtJm1BDGnUp6cCaM4RgOfZbtCMbev6OkVeGAWaUFMjcIfZaZjpPXtcaxqBTTlqzDvNb2Wcl2GhBGPGFvMcCDQdFWc=",
            "adrHo86PeEIDEMIMY1nD7FvlYAzV1GpS/Y7CYlRaajfSqXpRqI03P3XCDEq3c3MC6MiV8fpVipuFAF3aXVOdz4gWL1fdJ2ZAoZ7eJaaQDnJgIMFUIttSkN5DbZrd63MUbqhVUn5O6EuOmJM4tzdsQOZMmWMu8/t9mslV5iLpBdurdj7P756GjFQvBFLt7k3csAnwxSO6VX66jlCh/NkikpmLEYX6yAY5KXAkhNlShtibLbKC9DemWXRVLl99CBDYGQw+zGicFVWFnPY1qAzaaV5JLyp5ymjhF2LiwhSWq1ty8UgCsR4vYauIa8WyQYHVXxOJ1i3+zZl5ZWSkCHBTAgfCpFcoUK+oLi9wSDa9bCSSfpLtLJNEVwK/moOFNbyZNZ0+Ng/b9Y20de1RJnXjNKXW/aYMwB85G1SHej4kjaqgENSbmNFUy8EyiW/HsKjpJ8l+0SeLdd1rZWY7YpbYa0gg3Q6IPGMXDE2B6vPOZ9GYtBBi4C83okRi/KMx20QmFUtN2Zu1AF3AnHpSaUQBO7Jl9oBBEGbw9EPMse56WRSOEJw6ukPA6Ug6g1qiSOsoB5kOWzk432U4doYR6t41Z33DQjiUUyLluagvIQHSXPPkmMy/S6t71TqHpWY3n9PE3hykkuXT+biKQWqvZPnefxzIfPyTmrtfok38Tg0kF70="),
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");
    InterfaceDataUnit56Dto expectedIdu = new InterfaceDataUnit56Dto(
        new Context56Dto("https://proxy.iri?code=host2", UUID.randomUUID(), false),
        objectMapper.writeValueAsString(expectedPdu));

    assertEquals(expectedIdu.getMessage(), received.getMessage());
    assertEquals(expectedIdu.getContext().getDestinationIri(),
        received.getContext().getDestinationIri());
    assertEquals(expectedIdu.getContext().getOptions(), received.getContext().getOptions());
  }

  @Test
  public void requestSerializationException() {
    final String idu45 = """
        {
          "context": {
            "WRONG": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "tracking_number": "846c0b99-7b8f-44d9-b3b6-766058eed965",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;

    assertThrows(DeserializationException.class, () -> exchangeRequest.dataRequestProcess(idu45));
  }
}
