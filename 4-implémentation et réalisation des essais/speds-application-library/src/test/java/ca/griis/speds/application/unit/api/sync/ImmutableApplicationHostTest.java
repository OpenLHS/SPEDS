package ca.griis.speds.application.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.DeserializationException;
import ca.griis.speds.application.api.exception.InvalidPduIdException;
import ca.griis.speds.application.api.exception.SerializationException;
import ca.griis.speds.application.api.sync.ImmutableApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.testdata.ExpectedInterfaceDataUnit;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImmutableApplicationHostTest {

  private ObjectMapper objectMapper = new ObjectMapper();
  private ObjectMapper objectMapperSpy;
  private PresentationHost presentationHost;
  private ApplicationHost appHost;

  @BeforeEach
  public void setUp() throws Exception {
    PgaService pgaServiceMock = Mockito.mock(PgaService.class);

    objectMapperSpy = Mockito.spy(objectMapper);
    SyncApplicationFactory factory = Mockito.spy(new SyncApplicationFactory(pgaServiceMock));
    presentationHost = Mockito.mock(PresentationHost.class);
    doReturn(presentationHost).when(factory).initPresentationHost(anyString());

    String params = """
        {
          "options": {
            "speds.app.version":"2.0.0",
            "speds.app.reference": "a reference"
          }
        }
        """;
    appHost = factory.init(params);
  }

  @BeforeEach
  public void cleanUp() throws Exception {
    appHost.close();
  }

  @Test
  public void testRequest_noerror() throws Exception {
    String pdu = """
        {
          "header": {
            "msgtype": "TACHE.ENVOI",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "executor",
        "connector_1", Boolean.FALSE);
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(context, pdu);

    doNothing().when(presentationHost).request(anyString());
    ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

    appHost.request(idu01);
    verify(presentationHost).request(argCaptor.capture());

    String value = argCaptor.getValue();
    InterfaceDataUnit12Dto dto = objectMapper.readValue(value, InterfaceDataUnit12Dto.class);
    assertEquals(ExpectedInterfaceDataUnit.getTacheEnvoiMessage12(), dto);
    assertEquals(pdu, dto.getMessage());
  }

  @Test
  public void testRequest_badpdu() throws Exception {
    String pdu = """
        {
          "msgtype": "TACHE.ENVOI",
          "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
          "parameters": false,
          "SPEDS": {
            "version": "2.0.0",
            "reference": "a reference"
          }
        }
        """;
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "executor",
        "connector_1", Boolean.FALSE);
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(context, pdu);

    assertThrows(DeserializationException.class, () -> {
      appHost.request(idu01);
    });
  }

  @Test
  public void testConfirm_noError() throws Exception {
    // Send request first to commit message id to memory since accessing private field by reflection
    // was giving errors.
    String pduIn = """
        {
          "header": {
            "msgtype": "TACHE.ENVOI",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    ContextDto contextIn = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "executor",
        "connector_1", Boolean.FALSE);
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(contextIn, pduIn);

    doNothing().when(presentationHost).request(anyString());
    appHost.request(idu01);

    // then proceed with test of confirm
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", Boolean.FALSE);
    String pdu = """
        {
          "header": {
            "msgtype": "TACHE.RECEPTION",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "ref"
            }
          },
          "content": ""
        }
        """;
    InterfaceDataUnit12Dto msg = new InterfaceDataUnit12Dto(context, pdu);
    String msgStr = objectMapper.writeValueAsString(msg);
    when(presentationHost.confirm()).thenReturn(msgStr);

    InterfaceDataUnit01Dto result = appHost.confirm();

    assertEquals(ExpectedInterfaceDataUnit.getTacheReceptionMessage(), result);
    // verify
    verify(presentationHost, times(1)).confirm();
  }

  @Test
  public void testConfirm_exceptionInvalidPduId() throws Exception {
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", Boolean.FALSE);
    String pdu = """
        {
          "header": {
            "msgtype": "TACHE.RECEPTION",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    InterfaceDataUnit12Dto msg = new InterfaceDataUnit12Dto(context, pdu);
    String msgStr = objectMapper.writeValueAsString(msg);
    when(presentationHost.confirm()).thenReturn(msgStr);

    assertThrows(InvalidPduIdException.class, () -> {
      appHost.confirm();
    });
  }

  @Test
  public void testConfirm_exceptionDeserializationPdu() throws Exception {
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", Boolean.FALSE);
    String pdu = """
        {
          "msgtype": "TACHE.RECEPTION",
          "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
          "parameters": false,
          "SPEDS": {
            "version": "2.0.0",
            "reference": "ref"
          }
        }
        """;
    InterfaceDataUnit12Dto msg = new InterfaceDataUnit12Dto(context, pdu);
    String msgStr = objectMapper.writeValueAsString(msg);
    when(presentationHost.confirm()).thenReturn(msgStr);

    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      appHost.confirm();
    });

    assertEquals(exception.getMessage(), "Pdu is not as expected.");
  }

  @Test
  public void testConfirm_exceptionDeserializationIdu() throws Exception {
    String idu = """
        {
          "context": {
            "PGA": "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
            "destination_code": "executor",
            "options": false,
            "tracking_number": "c51fd8ef-eba6-4239-9453-f75ca95a90b2"
          },
          "message": "{
            "msgtype": "TACHE.RECEPTION",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "ref"
            }
          }"
        }
        """;
    when(presentationHost.confirm()).thenReturn(idu);

    DeserializationException exception = assertThrows(DeserializationException.class, () -> {
      appHost.confirm();
    });

    assertEquals(exception.getMessage(), "Received IDU is invalid.");
  }

  @Test
  public void testIndication_noerror() throws DeserializationException {
    String idu12 =
        """
            {
              "context":{"PGA":"736bfe3a-3e9d-4d94-ada5-b69d051bcea3","source_code":"executor","destination_code":"connector_1","options":false,"tracking_number": "26ac0b75-430c-4cbb-8bb6-516a69444f0e"},
              "message":"{ \\"header\\": { \\"msgtype\\": \\"TACHE.ENVOI\\", \\"id\\": \\"c51fd8ef-eba6-4239-9453-f75ca95a90b2\\", \\"parameters\\": false, \\"SPEDS\\": { \\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\" }}, \\"content\\": \\"\\"}"
            }
              """;

    doReturn(idu12).when(presentationHost).indication();


    InterfaceDataUnit01Dto dto = appHost.indication();

    assertEquals(ExpectedInterfaceDataUnit.getTacheEnvoiMessage01(), dto);
  }

  @Test
  public void testIndication_deserializationError() throws DeserializationException {
    String idu12_noDestCode = """
            {
              "context": {
                "PGA": "736bfe3a-3e9d-4d94-ada5-b69d051bcea3",
                "source_code": "executor",
                "options": false,
                "tracking_number": "26ac0b75-430c-4cbb-8bb6-516a69444f0e"
              },
              "message": "{
                "header": {
                  "msgtype": "TACHE.ENVOI",
                  "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
                  "parameters": false,
                  "SPEDS": {
                    "version": "2.0.0",
                    "reference": "a reference"
                  }
                },
                "content": "xyz"
              }"
            }
        """;

    doReturn(idu12_noDestCode).when(presentationHost).indication();

    assertThrows(DeserializationException.class, () -> {
      appHost.indication();
    });
  }

  @Test
  public void testResponse_noerror() throws JsonProcessingException {
    // send indication first
    String idu12 =
        """
            {
              "context":{"PGA":"736bfe3a-3e9d-4d94-ada5-b69d051bcea3","source_code":"executor","destination_code":"connector_1","options":false,"tracking_number": "26ac0b75-430c-4cbb-8bb6-516a69444f0e"},
              "message":"{ \\"header\\": { \\"msgtype\\": \\"TACHE.ENVOI\\", \\"id\\": \\"c51fd8ef-eba6-4239-9453-f75ca95a90b2\\", \\"parameters\\": false, \\"SPEDS\\": { \\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\" }}, \\"content\\": \\"\\"}"
            }
              """;

    doReturn(idu12).when(presentationHost).indication();
    appHost.indication();

    // response
    String pduIn = """
        {
          "header": {
            "msgtype": "TACHE.RECEPTION",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    ContextDto contextIn = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", Boolean.FALSE);
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(contextIn, pduIn);

    doNothing().when(presentationHost).response(anyString());
    ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

    appHost.response(idu01);

    verify(presentationHost).response(argCaptor.capture());

    String value = argCaptor.getValue();
    InterfaceDataUnit12Dto dto12 = objectMapper.readValue(value, InterfaceDataUnit12Dto.class);
    assertEquals(ExpectedInterfaceDataUnit.getTacheReceptionMessage12(), dto12);
    assertEquals(pduIn, dto12.getMessage());
  }

  @Test
  public void testResponse_exceptionSerialization() throws JsonProcessingException,
      NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    // Get the package-private constructor
    Constructor<ImmutableApplicationHost> constructor =
        ImmutableApplicationHost.class.getDeclaredConstructor(PresentationHost.class,
            ObjectMapper.class, String.class, String.class);
    // Make the constructor accessible (public)
    constructor.setAccessible(true);
    // Instantiate the object using the constructor
    ImmutableApplicationHost myClassInstance =
        constructor.newInstance(presentationHost, objectMapperSpy, "2.0.0", "a reference");

    // send indication first
    String idu12 =
        """
            {
              "context":{"PGA":"736bfe3a-3e9d-4d94-ada5-b69d051bcea3","source_code":"executor","destination_code":"connector_1","options":false,"tracking_number": "26ac0b75-430c-4cbb-8bb6-516a69444f0e"},
              "message":"{ \\"header\\": { \\"msgtype\\": \\"TACHE.ENVOI\\", \\"id\\": \\"c51fd8ef-eba6-4239-9453-f75ca95a90b2\\", \\"parameters\\": false, \\"SPEDS\\": { \\"version\\": \\"2.0.0\\", \\"reference\\": \\"a reference\\" }}, \\"content\\": \\"\\"}"
            }
              """;

    doReturn(idu12).when(presentationHost).indication();
    myClassInstance.indication();

    // Continue with Response
    doThrow(JsonProcessingException.class).when(objectMapperSpy).writeValueAsString(any());

    String pdu = """
        {
          "header": {
            "msgtype": "TACHE.ENVOI",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(new ContextDto(), pdu);

    assertThrows(SerializationException.class, () -> {
      myClassInstance.response(idu01);
    });
  }

  @Test
  public void testResponse_exceptionInvalidPduId() {
    String pdu = """
        {
          "header": {
            "msgtype": "TACHE.ENVOI",
            "id": "c51fd8ef-eba6-4239-9453-f75ca95a90b2",
            "parameters": false,
            "SPEDS": {
              "version": "2.0.0",
              "reference": "a reference"
            }
          },
          "content": ""
        }
        """;
    InterfaceDataUnit01Dto idu01 = new InterfaceDataUnit01Dto(new ContextDto(), pdu);

    assertThrows(InvalidPduIdException.class, () -> {
      appHost.response(idu01);
    });
  }
}
