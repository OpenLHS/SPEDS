package ca.griis.speds.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.speds.api.factory.SpedsFactory;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.session.api.PgaService;
import java.lang.reflect.Field;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public final class ClientCases {
  private PresentationHost mockPresentationHost;
  private ApplicationHost host;

  public ClientCases(String params) throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {
    PgaService pgaServiceMock = Mockito.mock(PgaService.class);
    mockPresentationHost = Mockito.mock(ImmutablePresentationHost.class);

    ApplicationFactory appFactory = new SyncApplicationFactory(pgaServiceMock) {
      @Override
      public PresentationHost initPresentationHost(String parameters) {
        return mockPresentationHost;
      }
    };

    SpedsFactory factory = new SpedsFactory(pgaServiceMock);
    Field presFactory = factory.getClass().getDeclaredField("factory");
    presFactory.setAccessible(true);
    presFactory.set(factory, appFactory);

    host = factory.init(params);
    assertNotNull(host);
  }

  public void close() {
    host.close();
  }

  public void requestSuccess(InterfaceDataUnit01Dto idu) throws Exception {
    // Behavior test
    host.request(idu);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockPresentationHost).request(captor.capture());

    String result = captor.getValue();
    assertNotNull(result);

    // IDU Comparaison
    InterfaceDataUnit12Dto resultIdu =
        SharedObjectMapper.getInstance().getMapper().readValue(result,
            InterfaceDataUnit12Dto.class);
    assertEquals(idu.getMessage(), resultIdu.getMessage());
  }

  public void confirmSuccess(InterfaceDataUnit01Dto idu) throws Exception {
    final String indicationIdu = SharedObjectMapper.getInstance().getMapper()
        .writeValueAsString(new InterfaceDataUnit12Dto(idu.getContext(), idu.getMessage()));
    when(mockPresentationHost.confirm()).thenReturn(indicationIdu);

    InterfaceDataUnit01Dto confirmResult = host.confirm();
    InterfaceDataUnit01Dto confirmIdu =
        new InterfaceDataUnit01Dto(idu.getContext(), idu.getMessage());

    assertEquals(confirmResult.getMessage(), confirmIdu.getMessage());
  }
}
