package ca.griis.speds.presentation.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.handler.TransferResponseHandler;
import ca.griis.speds.session.api.SessionHost;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransferResponseHandlerTest {

  private TransferResponseHandler transferResponseHandler;
  private ConcurrentHashMap<PresentationTracking, TrackingInformation> serverTracking;

  @Mock
  private SessionHost mockSessionHost;

  private UUID trackingNumberUuid;
  private UUID sessionTrackingUuid;
  private SecretKey fakeKey;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    serverTracking = new ConcurrentHashMap<>();

    transferResponseHandler = new TransferResponseHandler(
        mockSessionHost, serverTracking);

    trackingNumberUuid = UUID.randomUUID();
    sessionTrackingUuid = UUID.randomUUID();
    fakeKey = KeyGenerator.getInstance("AES").generateKey();

    serverTracking.put(
        new PresentationTracking(trackingNumberUuid),
        new TrackingInformation(sessionTrackingUuid, fakeKey));
  }

  @Test
  void testHandleOptionsNotMap() {
    Context12Dto context = new Context12Dto(
        "550e8400-e29b-41d4-a716-446655440000",
        "source",
        "destination",
        "TRANSFER",
        ServicePrimitive.RESPONSE,
        null);

    InterfaceDataUnit12Dto inputIdu = new InterfaceDataUnit12Dto(context, "Response message");

    assertThrows(IllegalArgumentException.class, () -> {
      transferResponseHandler.handle(inputIdu);
    });
  }
}
