package ca.griis.speds.transport.integration;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import ca.griis.js2p.gen.speds.transport.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import ca.griis.speds.transport.exception.ParameterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class Suite {

  /// Convention
  ///
  /// Les identifiants marqués par Z sont des ajouts de l'implémentation.
  /// Il serait judicieux de rapporter la plupart dans la spécification des essais,
  /// car ils m'apparaissent comme nécessaire d'être spécifié dans l'optique d'être
  /// aussi prescriptif que possible dans ce document.
  ///

  private Environment environment;
  private String spedsVersion = RandomStringUtils.randomAlphanumeric(100);
  private String spedsReference = RandomStringUtils.randomAlphanumeric(100);
  private Speds45Dto speds45Dto = new Speds45Dto(spedsVersion, spedsReference);
  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private NetworkHost networkHost;

  @BeforeEach
  public void setup() throws JsonProcessingException, ParameterException {
    openMocks(this);
    // Instanciation d'un environnement frais
    environment = new Environment();

    // Préparation de l'hôte à tester
    Map<String, Object> options =
        Map.of("speds.tra.version", spedsVersion, "speds.tra.reference", spedsReference);
    SyncTransportFactory transportFactory = new SyncTransportFactory() {
      @Override
      public NetworkHost initNetworkHost(String parameters) {
        return networkHost;
      }
    };

    InitInParamsDto initInParamsDto = new InitInParamsDto(options);
    TransportHost host = transportFactory.init(objectMapper.writeValueAsString(initInParamsDto));
    environment.addInput(Map.entry("Z001_host", host));
  }

  @AfterEach
  public void cleanUp() {
    TransportHost givenHost = environment.getInput("Z001_host", TransportHost.class);
    givenHost.close();
  }

  @Test
  public void e_01() throws JsonProcessingException {
    init_ct_pro_04_01();
    String messageId = Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_02_msg =
        TestInputs.make_ct_pro_05_01_e1(speds45Dto, messageId, objectMapper);
    String pro_05_01_e1 = objectMapper.writeValueAsString(e_02_msg);
    environment.addInput(Map.entry("CT_PRO-05_01_E1", pro_05_01_e1));
    when(networkHost.indication()).thenReturn(pro_05_01_e1);

    Cases.ct_pro_05_01(environment);
  }

  @Test
  public void e_02() throws JsonProcessingException {
    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_01_msg = TestInputs.make_ct_pro_11_01_e1(speds45Dto, objectMapper);
    String ct_pro_11_01_e1 = objectMapper.writeValueAsString(e_01_msg);
    when(networkHost.indication()).thenReturn(ct_pro_11_01_e1);
    environment.addInput(Map.entry("CT_PRO-11_01_E1", ct_pro_11_01_e1));

    // Préparation de la capture de la première sortie
    ArgumentCaptor<String> requestCaptor = ArgumentCaptor.forClass(String.class);
    doNothing().when(networkHost).request(requestCaptor.capture());
    Supplier<String> supplier = requestCaptor::getValue;
    environment.addInput(Map.entry("CT_PRO-11_01_Z002_supplier", supplier));

    // Ajout de l'objet de mise-en-correspondance dans l'environnement
    environment.addInput(Map.entry("CT_PRO-11_01_Z003_mapper", objectMapper));

    Cases.ct_pro_11_01(environment);
  }

  @Test
  public void e_05() {
    // Préparation de la première entrée
    String ct_pro_04_02_e1 = TestInputs.make_ct_pro_04_02_e1();
    environment.addInput(Map.entry("CT_PRO-04_01_E1", ct_pro_04_02_e1));

    Cases.ct_pro_04_02(environment);
  }

  @Test
  public void e_06() throws JsonProcessingException {
    init_ct_pro_04_01();
    String messageId = Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_01_msg =
        TestInputs.make_ct_pro_05_03_e1(speds45Dto, messageId, objectMapper);
    String ct_pro_05_03_e1 = objectMapper.writeValueAsString(e_01_msg);
    environment.addInput(Map.entry("CT_PRO-05_03_E1", ct_pro_05_03_e1));
    when(networkHost.indication()).thenReturn(ct_pro_05_03_e1);

    Cases.ct_pro_05_03(environment);
  }

  @Test
  public void e_07() throws JsonProcessingException {
    init_ct_pro_04_01();
    String messageId = Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_01_msg =
        TestInputs.make_ct_pro_05_04_e1(speds45Dto, messageId, objectMapper);
    String ct_pro_05_04_e1 = objectMapper.writeValueAsString(e_01_msg);
    environment.addInput(Map.entry("CT_PRO-05_04_E1", ct_pro_05_04_e1));
    when(networkHost.indication()).thenReturn(ct_pro_05_04_e1);

    Cases.ct_pro_05_04(environment);
  }

  @Test
  public void e_08() throws JsonProcessingException {
    init_ct_pro_04_01();
    Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_08_msg = TestInputs.make_ct_pro_05_05_e1(speds45Dto, objectMapper);
    String ct_pro_05_05_e1 = objectMapper.writeValueAsString(e_08_msg);
    environment.addInput(Map.entry("CT_PRO-05_05_E1", ct_pro_05_05_e1));
    when(networkHost.indication()).thenReturn(ct_pro_05_05_e1);

    // @todo 2025-06-20 [SSC] - Attention, ici le test n'aurait pas du passer avant que charles
    // impl. le messageid
    // Si on met un mauvais content seal ou header seal, la validation du messageId arrive avant
    // donc on devrait
    // avoir l'erreur de seal!
    Cases.ct_pro_05_05(environment);
  }

  @Test
  public void e_09() throws JsonProcessingException {
    init_ct_pro_04_01();
    Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    String c005_e1 = TestInputs.make_ct_pro_05_06_e1();
    environment.addInput(Map.entry("CT_PRO-05_06_E1", c005_e1));
    when(networkHost.indication()).thenReturn(c005_e1);
    Cases.ct_pro_05_06(environment);
  }

  @Test
  public void e_10() throws JsonProcessingException {
    // Préparation de la première entrée
    String ct_pro_11_03_e1 = TestInputs.make_ct_pro_11_03_e1(speds45Dto, objectMapper);
    environment.addInput(Map.entry("CT_PRO-11_03_E1", ct_pro_11_03_e1));
    when(networkHost.indication()).thenReturn(ct_pro_11_03_e1);

    Cases.ct_pro_11_03(environment);
  }

  @Test
  public void e_11() throws JsonProcessingException {
    // Préparation de la première entrée
    String ct_pro_11_04_e1 = TestInputs.make_ct_pro_11_04_e1(speds45Dto, objectMapper);
    environment.addInput(Map.entry("CT_PRO-11_04_E1", ct_pro_11_04_e1));
    when(networkHost.indication()).thenReturn(ct_pro_11_04_e1);

    Cases.ct_pro_11_04(environment);
  }

  @Test
  public void e_12() {
    // Préparation de la première entrée
    String ct_pro_11_05_e1 = TestInputs.make_ct_pro_11_05_e1(speds45Dto, objectMapper);
    environment.addInput(Map.entry("CT_PRO-11_05_E1", ct_pro_11_05_e1));
    when(networkHost.indication()).thenReturn(ct_pro_11_05_e1);

    Cases.ct_pro_11_05(environment);
  }

  private void init_ct_pro_04_01() throws JsonProcessingException {
    // Préparation de la première entrée
    InterfaceDataUnit34Dto e_01_msg = TestInputs.make_ct_pro_04_01_e1();
    String ct_pro_04_01_e1 = objectMapper.writeValueAsString(e_01_msg);
    environment.addInput(Map.entry("CT_PRO-04_01_E1", ct_pro_04_01_e1));

    // Préparation du fournisseur de réponse
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doNothing().when(networkHost).request(captor.capture());
    Supplier<String> supplier = captor::getValue;

    environment.addInput(Map.entry("CT_PRO-04_01_Z002_supplier", supplier));

    // Ajout de speds pour faciliter l'évaluation
    environment.addInput(Map.entry("CT_PRO-04_01_Z003_speds", speds45Dto));

    // Ajout de l'objet de mise-en-correspondance dans l'environnement
    environment.addInput(Map.entry("CT_PRO-04_01_Z003_mapper", objectMapper));
  }
}
