package ca.griis.speds.network.unit.service.host;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.serialization.SharedObjectMapper;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.InvalidPduIdException;
import ca.griis.speds.network.service.exception.InvalidSignatureException;
import ca.griis.speds.network.service.exception.MissingAuthenticationException;
import ca.griis.speds.network.service.host.ExchangeDataConfirm;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import ca.griis.speds.network.util.KeyVar;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExchangeDataConfirmTest {

  private final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();
  private ExchangeDataConfirm exchangeConfirm;
  private SentMessageIdSet messageIds;

  @BeforeEach
  void setUp() throws Exception {
    messageIds = new SentMessageIdSet();
    messageIds.clearMessageIds();
    this.exchangeConfirm = new ExchangeDataConfirm(new NetworkMarshaller(objectMapper),
        new SealManager(objectMapper), messageIds);
  }

  @Test
  public void confirmSuccess() throws Exception {
    messageIds.addMessageId("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f");

    Context56Dto cxt = new Context56Dto("https://host1.iri?code=host1", UUID.randomUUID(), false);
    String auth = KeyVar.host1CertRsa;
    String pk = KeyVar.host1PrikeyRsa;
    CertificatePrivateKeyPair cpp = CertificatePrivateKeyPair.importFromPem(auth, pk);

    HeaderDto header =
        new HeaderDto(HeaderDto.Msgtype.RES_REC, "0119c1d9-3f6a-4c5a-a19e-5e1ba272635f",
            "https://proxy.iri?code=host2", "https://host1.iri?code=host1",
            cpp.getAuthentification(), false, new SPEDSDto("3.0.0", "https://reference.iri/speds"));
    SealManager sm = new SealManager(objectMapper);
    String headerSeal = sm.createSeal(header, Seal.header, cpp.privateKey());
    StampDto stamp = new StampDto(headerSeal, "");
    ProtocolDataUnit5Dto pdu = new ProtocolDataUnit5Dto(header, stamp, "");
    InterfaceDataUnit56Dto idu56 =
        new InterfaceDataUnit56Dto(cxt, objectMapper.writeValueAsString(pdu));

    assertDoesNotThrow(
        () -> exchangeConfirm.dataConfirmProcess(objectMapper.writeValueAsString(idu56)));
    assertFalse(messageIds.containsMessageId("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f"));
  }

  @Test
  public void confirm_WrongMessageTypeException() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.ENV\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(DeserializationException.class, () -> exchangeConfirm.dataConfirmProcess(idu56));
  }

  @Test
  public void confirm_PduIdException() throws Exception {
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"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\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(InvalidPduIdException.class, () -> exchangeConfirm.dataConfirmProcess(idu56));
  }

  @Test
  public void confirm_MissingAuthExceptionNull() throws Exception {
    messageIds.addMessageId("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f");
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(MissingAuthenticationException.class,
        () -> exchangeConfirm.dataConfirmProcess(idu56));
  }

  @Test
  public void confirm_MissingAuthException() throws Exception {
    messageIds.addMessageId("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f");
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://host1.iri?code=host1\\\",\\\"destination_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"authentification\\\":\\\"\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"lI8OWDNs+JwGGz2KkTpxNcGZANruS+oVOJufWlnM7P0zBOX1lmfDi6z+sJn7mYnwhiF6yHvb7SLTO2CzRxbKMDFHCDFpgcM8xLwMKS/v9wILZ6jzb/NF/DU6bSXz4VMslqOwYjughknxkpkY9KaT50di1vG1c95js48op4ekISqVMMZK4DrVTl5tTIfJVZ9OZ6dx1vckpZb2m/iDhjgW0pFoOuCBVaSbLiI8s67LneEcYu6+rD4CSCxYDndPzlftm+2GQFTyoc2zWeHM6uPsULHUwsXbN3C4qRPW9pYUYf9300rBUqMcAAzRQ0Vol8j8/fCa7xVx3Yt44UvvBWk6Q2MmT9d+H1wOrnNYRNd40eJGewlgloXC30TrTJH2lbfAvN6oUcHsq9BPczFc7wp4WwsKFhBMbK+8++K3L2yeXEWK9fjpy77/9ckD5j5pGDVKGKwKl5e6qg8qEjTEI+O6jj7xVPqLrFK5/vhkTiI5wEtScsZQ1qMJpNNKwjqRONtiD2qNDh89ce0uGEFQDxO2nGtFHHETjYufGPdWLO4O7WbO7Q4IIszHJFFGgFBBE6AupS2wFXGBTbiamzEY5GqBDYjLc5AZV3j5xRnOC45hcfvHevo5U4pbO/AroUh2iKWpJgFYfUU1Jnl6GsQCUL04DuYYFjy3JrYX2I6/YzgOV14=\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(MissingAuthenticationException.class,
        () -> exchangeConfirm.dataConfirmProcess(idu56));
  }

  @Test
  public void confirm_SignatureException() throws Exception {
    messageIds.addMessageId("0119c1d9-3f6a-4c5a-a19e-5e1ba272635f");
    final String idu56 =
        "{\"context\":{\"destination_iri\":\"https://host1.iri?code=host1\",\"options\":false},\"message\":\"{\\\"header\\\":{\\\"msgtype\\\":\\\"RES.REC\\\",\\\"id\\\":\\\"0119c1d9-3f6a-4c5a-a19e-5e1ba272635f\\\",\\\"source_iri\\\":\\\"https://proxy.iri?code=host2\\\",\\\"destination_iri\\\":\\\"https://host1.iri\\\",\\\"authentification\\\":\\\""
            + KeyVar.host1CertRsa
            + "\\\",\\\"parameters\\\":false,\\\"SPEDS\\\":{\\\"version\\\":\\\"3.0.0\\\",\\\"reference\\\":\\\"https://reference.iri/speds\\\"}},\\\"stamp\\\":{\\\"header_seal\\\":\\\"\\\",\\\"content_seal\\\":\\\"\\\"},\\\"content\\\":\\\"\\\"}\"}";
    assertThrows(InvalidSignatureException.class, () -> exchangeConfirm.dataConfirmProcess(idu56));
  }
}
