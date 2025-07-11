package ca.griis.speds.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.speds.api.factory.SpedsFactory;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.ImmutablePresentationHost;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Field;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public final class ServerCases {
  private PresentationHost mockPresentationHost;
  private ApplicationHost host;

  public ServerCases(InitInParamsDto params)
      throws JsonProcessingException, IllegalArgumentException, IllegalAccessException,
      NoSuchFieldException, SecurityException {
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

  public void indicationSuccess(InterfaceDataUnit01Dto idu) throws Exception {
    final String iduString = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu);
    when(mockPresentationHost.indication()).thenReturn(iduString);
    InterfaceDataUnit01Dto resultIdu = host.indication();

    // Compares messages
    ProtocolDataUnit1APPDto resultPdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(resultIdu.getMessage(), ProtocolDataUnit1APPDto.class);
    ProtocolDataUnit1APPDto expectedPdu = SharedObjectMapper.getInstance().getMapper()
        .readValue(idu.getMessage(), ProtocolDataUnit1APPDto.class);
    assertEquals(resultPdu, expectedPdu);
  }

  public void responseSuccess(InterfaceDataUnit01Dto idu) throws Exception {
    // Behavior test
    host.response(idu);

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(mockPresentationHost, times(1)).response(captor.capture());

    String result = captor.getValue();
    assertNotNull(result);

    // IDU Comparaison
    InterfaceDataUnit12Dto resultIdu =
        SharedObjectMapper.getInstance().getMapper().readValue(result,
            InterfaceDataUnit12Dto.class);

    assertEquals(idu.getMessage(), resultIdu.getMessage());
  }
}
