package ca.griis.speds.network.unit.api.sync;

import static ca.griis.speds.network.unit.signature.SignatureProvider.getCertificatePem;
import static ca.griis.speds.network.unit.signature.SignatureProvider.getKeyPem;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto.Msgtype;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.ImmutableNetworkHost;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import ca.griis.speds.network.util.KeyVar;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableNetworkHostTest {

  private final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();

  @Mock
  private DataLinkHost dataLinkHost;

  @Captor
  ArgumentCaptor<String> messageCaptor;

  private NetworkHost immutableNetworkClient;

  @BeforeEach
  public void setUp() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";

    immutableNetworkClient = new ImmutableNetworkHost(dataLinkHost, spedsVersion, spedsReference,
        CertificatePrivateKeyPair.importFromPem(getCertificatePem(), getKeyPem()),
        () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f", new NetworkMarshaller(objectMapper),
        new SealManager(objectMapper), new SentMessageIdSet());
  }

  @Test
  public void closeSuccess() throws Exception {
    immutableNetworkClient.close();
    verify(dataLinkHost).close();
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
    immutableNetworkClient.request(idu45);

    verify(dataLinkHost).request(messageCaptor.capture());

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
    InterfaceDataUnit56Dto received =
        objectMapper.readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    assertEquals(expectedIdu.getMessage(), received.getMessage());
    assertEquals(expectedIdu.getContext().getDestinationIri(),
        received.getContext().getDestinationIri());
    assertEquals(expectedIdu.getContext().getOptions(), received.getContext().getOptions());
  }

  @Test
  public void requestDeserializationException() {
    final String idu45 = """
        {
          "context": {
            "wrong_source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "tracking_number": "846c0b99-7b8f-44d9-b3b6-766058eed965",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    assertDoesNotThrow(() -> immutableNetworkClient.request(idu45));
  }

  @Test
  public void responseSuccess() throws Exception {
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
    immutableNetworkClient.response(idu45);

    verify(dataLinkHost).request(messageCaptor.capture());

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
    InterfaceDataUnit56Dto received =
        objectMapper.readValue(messageCaptor.getValue(), InterfaceDataUnit56Dto.class);

    assertEquals(expectedIdu.getMessage(), received.getMessage());
    assertEquals(expectedIdu.getContext().getDestinationIri(),
        received.getContext().getDestinationIri());
    assertEquals(expectedIdu.getContext().getOptions(), received.getContext().getOptions());
  }

  @Test
  public void responseDeserializationException() {
    final String idu45 = """
        {
          "context": {
            "wrong_source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "tracking_number": "846c0b99-7b8f-44d9-b3b6-766058eed965",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    assertDoesNotThrow(() -> immutableNetworkClient.response(idu45));
  }

  @Test
  public void confirmSuccess() throws Exception {
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    immutableNetworkClient.request(idu45);
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).confirm();
    assertDoesNotThrow(() -> immutableNetworkClient.confirm());
  }

  @Test
  public void confirmInvalidMessageType() throws Exception {
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    immutableNetworkClient.request(idu45);
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).confirm();
    assertDoesNotThrow(() -> immutableNetworkClient.confirm());
  }

  @Test
  public void confirmInvalidPduId() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).confirm();
    assertDoesNotThrow(() -> immutableNetworkClient.confirm());
  }

  @Test
  public void confirmMissingAuthentification() throws Exception {
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    immutableNetworkClient.request(idu45);
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":null,\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).confirm();
    assertDoesNotThrow(() -> immutableNetworkClient.confirm());
  }

  @Test
  public void confirmInvalidSignature() throws Exception {
    final String idu45 = """
        {
          "context": {
            "source_iri": "https://host1.iri?code=host1",
            "destination_iri": "https://proxy.iri?code=host2",
            "options": false
          },
          "message": "Protocol Data Unit (PDU) sérialisé de la couche Transport"
        }
        """;
    immutableNetworkClient.request(idu45);
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"kent+jgt7vaE55LUIWVVGjiAo9G2df0v/E04Ynx+wA8ChA5I1FhIVWuojewJyPeIKkhRIv8ykQ+7pmXVE68yRAsCGUy7UqX1GXi+iWM6noezHB8N6RjVfuyuYvCufIG49gpE7nhG8Ad3knl2+B4uXNtKVMrq0dOD6/neUq6OmyOsM1+VFIxqjDmNKsY5zsnyxtw7J6mW+qUnzQOsX2n9cPx07RQWcFWFnSbeHKyS16abUD5OaC3lbvde7X3bMoaCIza+SYEG2bvCYZm1iI8ZXizhvf/rTjw9ix1RLN+cp/bSzDEWDeiNHxhxP3SegAaQxZ83wHRd9I5RX1QSpRRL2RkJSgbghDRQezswEe6Yfrq23BlJsnzP3q3pRVKMxnQ2U7GmLEPbFqrwwAN9CnxXZhELPEnTmD5/p+4IxYnvC1wkBtBIWcjKeBND5Vz8pSQRFcA1TjrlczPAoqyLwEEl8qcxtDhggSdrlkpLhZyPpRIeAo1a9TTNAiPOpfOki4m0k8vIgjvvXha/G2bPClr5A98nrPy+kYoPQN8qnHgFZ1KyNNmJIHbgWO43iEY/B579PgXzmEAOWCkjSmXDXM8/aq3sVJPgtHxItO26bDhJK7Yh2PhqM2TkfdFPmXZEShE0ak3WjSIWOZNvztWrZY9de+BrCXu0J0TWz1up3wwsJCI=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).confirm();
    assertDoesNotThrow(() -> immutableNetworkClient.confirm());
  }

  @Test
  public void indicationSuccess() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(auth, pk);
    NetworkHost immutableNetworkServer = new ImmutableNetworkHost(dataLinkHost, spedsVersion,
        spedsReference, cpkp, () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper),
        new SentMessageIdSet());

    // final String idu56 =
    Context56Dto cxt = new Context56Dto("https://host1.iri",
        UUID.fromString("75553e0e-cfcc-434a-9189-04df7ddb1544"), false);
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto header =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://host1.iri?code=host1", "https://proxy.iri?code=host2",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(header, Seal.header, cpp.privateKey());
    String contentSeal = sm.createSeal("Protocol Data Unit (PDU) sérialisé de la couche Transport",
        Seal.content, cpp.privateKey());
    StampDto stamp = new StampDto(headerSeal, contentSeal);
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(header, stamp,
        "Protocol Data Unit (PDU) sérialisé de la couche Transport");

    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));
    doReturn(objectMapper.writeValueAsString(idu56)).when(dataLinkHost).indication();

    final String expectedIdu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\""
            + KeyVar.host1CertRsa
            + "\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"Phnk+7wl2SFU7jl6Ry7HJvh7vK4xEcIf0rxdNvfM7mOsfBYNBMltUtmm/R4Ux8FxYaDdCyMATQBr37RvyUso7P9h1Cn16eIFIRUNToPiUUx3w/KQDOZibdGCzauiDLouOfHuTl1HurCrItAJNErJLTYmkaSZyHwLEk1TOKNS6sByHJZBs1Ge3rgQfJfzaRv6qtiHXfNmNSsHO853PaZtlqN/6kgoLfHqoRR/H62PhtmNEMRQCWDY9y3SH11M2en+tGTrQi4+oTsLkr2rhcZifrAWNkjmUn+d3uy2TaAPAdGEmBDpYTNihMEIchmUbzSgPL8DPYXQYoVqFIrnftFMl3T13j2HOCsNKMHf8qb8vEdXq05O3aANkgcv22UEwRTJTAByjoPKWjpIDokdobpe/8hvQR0TK6MEUEV2rWCirs3HRX/dEzWjE59SXYcBAi/CUlXJcOB1u0qg67XwHN9bEMCuDoEf20L96Qsix6RRJkF2sux+fxqZrfsko6VWDsqqGcUqRFbyMbonGWhADLzWjZqBW+FZ3sLR1nBC00WO9WeuOuBmjGJOXNwqPfeZ/UHvMb7VX70hzeh6RMqtr7hqi/hfR425orOnrmTBASx3qNhRo2TXaSHvAZEK7utNOhD+1/nhPYaHrO6UykIrxfocZEeqVyJOdMbiXjCc+1Z+sWk=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    final String expectedIdu45 =
        "{\"context\":{\"source_iri\":\"https://host1.iri?code=host1\",\"destination_iri\":\"https://proxy.iri?code=host2\",\"tracking_number\":\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\",\"options\":false},\"message\":\"Protocol Data Unit (PDU) sérialisé de la couche Transport\"}";

    final String actualIdu45 = immutableNetworkServer.indication();

    verify(dataLinkHost).response(messageCaptor.capture());
    assertEquals(expectedIdu56, messageCaptor.getValue());
    assertEquals(expectedIdu45, actualIdu45);
  }

  @Test
  public void indicationInvalidMessageType() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(
        "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");
    NetworkHost immutableNetworkServer = new ImmutableNetworkHost(dataLinkHost, spedsVersion,
        spedsReference, cpkp, () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper),
        new SentMessageIdSet());
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).indication();
    assertDoesNotThrow(() -> immutableNetworkServer.indication());
  }

  @Test
  public void indicationMissingAuthentification() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(
        "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");
    NetworkHost immutableNetworkServer = new ImmutableNetworkHost(dataLinkHost, spedsVersion,
        spedsReference, cpkp, () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper),
        new SentMessageIdSet());
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":null,\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).indication();
    assertDoesNotThrow(() -> immutableNetworkServer.indication());
  }

  @Test
  public void indicationInvalidHeaderSignature() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(
        "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");
    NetworkHost immutableNetworkServer = new ImmutableNetworkHost(dataLinkHost, spedsVersion,
        spedsReference, cpkp, () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper),
        new SentMessageIdSet());
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"kent+jgt7vaE55LUIWVVGjiAo9G2df0v/E04Ynx+wA8ChA5I1FhIVWuojewJyPeIKkhRIv8ykQ+7pmXVE68yRAsCGUy7UqX1GXi+iWM6noezHB8N6RjVfuyuYvCufIG49gpE7nhG8Ad3knl2+B4uXNtKVMrq0dOD6/neUq6OmyOsM1+VFIxqjDmNKsY5zsnyxtw7J6mW+qUnzQOsX2n9cPx07RQWcFWFnSbeHKyS16abUD5OaC3lbvde7X3bMoaCIza+SYEG2bvCYZm1iI8ZXizhvf/rTjw9ix1RLN+cp/bSzDEWDeiNHxhxP3SegAaQxZ83wHRd9I5RX1QSpRRL2RkJSgbghDRQezswEe6Yfrq23BlJsnzP3q3pRVKMxnQ2U7GmLEPbFqrwwAN9CnxXZhELPEnTmD5/p+4IxYnvC1wkBtBIWcjKeBND5Vz8pSQRFcA1TjrlczPAoqyLwEEl8qcxtDhggSdrlkpLhZyPpRIeAo1a9TTNAiPOpfOki4m0k8vIgjvvXha/G2bPClr5A98nrPy+kYoPQN8qnHgFZ1KyNNmJIHbgWO43iEY/B579PgXzmEAOWCkjSmXDXM8/aq3sVJPgtHxItO26bDhJK7Yh2PhqM2TkfdFPmXZEShE0ak3WjSIWOZNvztWrZY9de+BrCXu0J0TWz1up3wwsJCI=\\\",\\\"content_seal\\\":\\\"PBeEqelCgDRbT0+/uct1ZdlGI5Vpv5Hkr0PA3t55bvj8ha82cm1vW2KwW+f6aMKRB7orPEU+QC9o28gWidFmTS7OnPiclmbBc20f3c37FZ0TNlm8P/71m5lsxW73KWkdS+8+nzv0/56BiJrVnX44Vdpsdas+UFXWc9d/nm322qQUTqOFqzKaylYbilnA4+p1yzxULbv2/Zf2SqCh7+KQP7gyq7jCRtCTT8mgXdpxrRDnTlZMrsUeNUvqnaLLpvTHNlA6CFQ9I9nJkBctH0iLUwpwMVZOlXYg2RQ1hrQ9YI6aZ6StHN5KYiHPww26DLqZz1AraK070JBncD6VFGuhKrG22BIl0gcmMz2t75gE4U24VVJxzcvFZ5AEyHNdYk6QHLYOZ7MAKqbWgxfhFSN7q0oK/Is1R0aYFgMsEWWb2RE7n/sTay6gWZ4MhnSVTvpuFsR9v0WIw9M94yT14xlFZL/5rT6Y+BL9Kv7LhOLFO9uWkdGKMJMlvvBL2TBbjPK4hG3b+WBhRJzzvdrry6a1Ki73317IwYSEd8lX54SEKgQ4AXEiwvAweJRUXrJQn/+u8FsNAgrfKnqctNNsIbtxPag2Flz99rnblg75n81VLxFrKK94DLlcOK/cb6Pud3zwsM+6bbkej4l6n55qRCV4Xi5Cpd8nu1A57rPLSjjMSGY=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).indication();
    assertDoesNotThrow(() -> immutableNetworkServer.indication());
  }

  @Test
  public void indicationInvalidContentSignature() throws Exception {
    final String spedsVersion = "3.0.0";
    final String spedsReference = "https://reference.iri/speds";
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
    CertificatePrivateKeyPair cpkp = CertificatePrivateKeyPair.importFromPem(
        "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");
    NetworkHost immutableNetworkServer = new ImmutableNetworkHost(dataLinkHost, spedsVersion,
        spedsReference, cpkp, () -> "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
        new NetworkMarshaller(objectMapper), new SealManager(objectMapper),
        new SentMessageIdSet());
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host2.iri\",\"tracking_number\":\"75553e0e-cfcc-434a-9189-04df7ddb1544\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://host2.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"i2W2VBeU1BQ/cl/JEz1qZKShtDItNZHdxre9+VJv8qr41/xhYZMlW2agYZNw2HLJXZUf+RXa+1P3Y35iNEvXTTY2abUDrxuUksQjcPisxTmTTvHlqEsXHA/uSnk9aRjs2A7eAUDWTTVyOdW441Er9Rtzk9Raiud6C9WsHZycvyJ/GedINqTUXk/Q9d5TCkpq8PftgOtnWxItSPGGWArj+61zOn6PrSmaiBndDm4klm3jy7M2bUyzM5fCWjOhjzgvNnp+M3vMGrbJavc2pPndvJf4KuRUUI1WyFLr2wNfv9bb37rYbIJ6A0/MMMgLUyYGHwLqrBGIBPeQ/L2QTFL7z6pQMwQqJvEkgpISwq5JotgROQTGtZUi8Ml57ytZ2I6JSnagp+/v+2gHtFsofwIPfmpe0x6zGjs86P08XTX80+Z9U3aoRUwFVp+PVfgpdrHyAXT7CnYwoYmxpe3gRc/aeAmDSdtqF7mip7sR3YGIpiW/w4kOWQFE73Rpdlj4uNMJKsbZ4FmHxUL+cmqDTyemUAMgtQLQIlO9GSUZ+gBjXzbgWhYw3dIlsHEapRrEL6nTFRUkhf/d/582iwjqInpj5Fiaw9XH9mTicDZtgos/f8RLni6NBPwATRmWFt9bdPjQV4BqwBgwU+q/8Gh0KrhNVrxeHsu2S1TKiuVoO7t3y2U=\\\",\\\"content_seal\\\":\\\"kent+jgt7vaE55LUIWVVGjiAo9G2df0v/E04Ynx+wA8ChA5I1FhIVWuojewJyPeIKkhRIv8ykQ+7pmXVE68yRAsCGUy7UqX1GXi+iWM6noezHB8N6RjVfuyuYvCufIG49gpE7nhG8Ad3knl2+B4uXNtKVMrq0dOD6/neUq6OmyOsM1+VFIxqjDmNKsY5zsnyxtw7J6mW+qUnzQOsX2n9cPx07RQWcFWFnSbeHKyS16abUD5OaC3lbvde7X3bMoaCIza+SYEG2bvCYZm1iI8ZXizhvf/rTjw9ix1RLN+cp/bSzDEWDeiNHxhxP3SegAaQxZ83wHRd9I5RX1QSpRRL2RkJSgbghDRQezswEe6Yfrq23BlJsnzP3q3pRVKMxnQ2U7GmLEPbFqrwwAN9CnxXZhELPEnTmD5/p+4IxYnvC1wkBtBIWcjKeBND5Vz8pSQRFcA1TjrlczPAoqyLwEEl8qcxtDhggSdrlkpLhZyPpRIeAo1a9TTNAiPOpfOki4m0k8vIgjvvXha/G2bPClr5A98nrPy+kYoPQN8qnHgFZ1KyNNmJIHbgWO43iEY/B579PgXzmEAOWCkjSmXDXM8/aq3sVJPgtHxItO26bDhJK7Yh2PhqM2TkfdFPmXZEShE0ak3WjSIWOZNvztWrZY9de+BrCXu0J0TWz1up3wwsJCI=\\\"},\\\"content\\\":\\\"Protocol Data Unit (PDU) sérialisé de la couche Transport\\\"}\"}";
    doReturn(idu56).when(dataLinkHost).indication();
    assertDoesNotThrow(() -> immutableNetworkServer.indication());
  }
}
