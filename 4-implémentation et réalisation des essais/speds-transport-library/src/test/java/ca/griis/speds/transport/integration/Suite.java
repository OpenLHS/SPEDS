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
import java.util.concurrent.ExecutionException;
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

  @Mock
  private NetworkHost networkHost2;

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
    String initInParams = objectMapper.writeValueAsString(initInParamsDto);
    TransportHost host = transportFactory.init(initInParams);
    environment.addInput(Map.entry("Z001_host", host));

    environment.addInput(Map.entry("Z006_Net_host", networkHost));

    SyncTransportFactory transportFactory2 = new SyncTransportFactory() {
      @Override
      public NetworkHost initNetworkHost(String parameters) {
        return networkHost2;
      }
    };

    TransportHost host2 = transportFactory2.init(initInParams);
    environment.addInput(Map.entry("Z002_host", host2));

    environment.addInput(Map.entry("Z007_Net_host", networkHost2));
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

    Cases.ct_pro_05_01(messageId, environment);
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
  public void e_06() throws JsonProcessingException {
    init_ct_pro_04_01();
    String messageId = Cases.ct_pro_04_01(environment);

    // Préparation de la première entrée
    InterfaceDataUnit45Dto e_01_msg =
        TestInputs.make_ct_pro_05_03_e1(speds45Dto, messageId, objectMapper);
    String ct_pro_05_03_e1 = objectMapper.writeValueAsString(e_01_msg);
    environment.addInput(Map.entry("CT_PRO-05_03_E1", ct_pro_05_03_e1));
    when(networkHost.indication()).thenReturn(ct_pro_05_03_e1).thenReturn(null);

    Cases.ct_pro_05_03(messageId, environment);
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
    when(networkHost.indication()).thenReturn(ct_pro_05_04_e1).thenReturn(null);

    Cases.ct_pro_05_04(messageId, environment);
  }

  @Test
  public void e_10() throws JsonProcessingException {
    // Préparation de la première entrée
    String ct_pro_11_03_e1 = TestInputs.make_ct_pro_11_03_e1(speds45Dto, objectMapper);
    environment.addInput(Map.entry("CT_PRO-11_03_E1", ct_pro_11_03_e1));
    when(networkHost.indication()).thenReturn(ct_pro_11_03_e1).thenReturn(null);

    Cases.ct_pro_11_03(environment);
  }

  @Test
  public void e_11() throws JsonProcessingException {
    // Préparation de la première entrée
    String ct_pro_11_04_e1 = TestInputs.make_ct_pro_11_04_e1(speds45Dto, objectMapper);
    environment.addInput(Map.entry("CT_PRO-11_04_E1", ct_pro_11_04_e1));
    when(networkHost.indication()).thenReturn(ct_pro_11_04_e1).thenReturn(null);

    Cases.ct_pro_11_04(environment);
  }

  @Test
  public void e_15() throws JsonProcessingException, ExecutionException, InterruptedException {
    init_ct_gen_01();
    Cases.ct_gen_01(environment);
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

  private void init_ct_gen_01() throws JsonProcessingException {
    // Préparation des prémières entrées
    InterfaceDataUnit34Dto e_01_msg = TestInputs.make_ct_gen_01_e1();
    String ct_gen_01_e1 = objectMapper.writeValueAsString(e_01_msg);
    environment.addInput(Map.entry("CT_GEN_01_E1", ct_gen_01_e1));

    InterfaceDataUnit34Dto e_02_msg = TestInputs.make_ct_gen_01_e2();
    String ct_gen_01_e2 = objectMapper.writeValueAsString(e_02_msg);
    environment.addInput(Map.entry("CT_GEN_01_E2", ct_gen_01_e2));

    // Préparation des fournisseurs de réponse
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doNothing().when(networkHost).request(captor.capture());
    Supplier<String> supplier = captor::getValue;

    environment.addInput(Map.entry("CT_GEN_01_Z003_supplier", supplier));

    ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
    doNothing().when(networkHost2).request(captor2.capture());
    Supplier<String> supplier2 = captor2::getValue;

    environment.addInput(Map.entry("CT_GEN_01_Z004_supplier", supplier2));

    // Ajout de l'objet de mise-en-correspondance dans l'environnement
    environment.addInput(Map.entry("CT_GEN_01_Z005_mapper", objectMapper));
  }
}
