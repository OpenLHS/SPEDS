package ca.griis.speds.transport.unit.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.Poller;
import ca.griis.speds.transport.service.client.ExchangeDataConfirmation;
import ca.griis.speds.transport.service.client.ExchangeDataRequest;
import ca.griis.speds.transport.service.server.ExchangeDataReply;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


public class PollerTest {
  @Mock
  private NetworkHost mockNetworkHost;

  private Poller poller;

  private Set<String> ids = ConcurrentHashMap.newKeySet();

  @BeforeEach
  public void setUp() {
    mockNetworkHost = Mockito.mock(NetworkHost.class);

    ExchangeDataReply indication = new ExchangeDataReply();
    ExchangeDataConfirmation confirm = new ExchangeDataConfirmation(ids);
    poller = new Poller(mockNetworkHost, indication, confirm, 100, 1, 200);
  }

  @AfterEach
  public void cleanUp() {
    poller.close();
  }

  @Test
  public void testPoolIdu_addIdu() throws Exception {
    ExchangeDataRequest request = new ExchangeDataRequest(() -> UUID.randomUUID().toString(), ids);

    InterfaceDataUnit34Dto idu = new InterfaceDataUnit34Dto(
        new Context34Dto("x", "y", "z", UUID.randomUUID(), "u", false), "houdini");
    String push = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu);
    String result = request.request(push, "x.x.x", "reference");

    when(mockNetworkHost.indication()).thenReturn(result);

    var r = poller.poll();
    assertNotNull(r);
  }
}
