package ca.griis.speds.network.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Suites {

  Environment environment;

  @BeforeEach
  public void setup() throws JsonProcessingException {

    // Initiation de l'environnement
    environment = new Environment();
  }

  @Test
  public void e01() throws JsonProcessingException {
    Cases.ct_pro_03_01(environment);
    Cases.ct_pro_04_01(environment);
    Cases.ct_pro_05_01(environment);
  }

  @Test
  public void e02() throws JsonProcessingException {
    Cases.ct_pro_04_01(environment);
    Cases.ct_pro_04_02(environment);
    Cases.ct_pro_04_03(environment);
    Cases.ct_pro_04_04(environment);
    Cases.ct_pro_04_05(environment);
    Cases.ct_pro_04_01(environment);
  }

  @Test
  public void e03() throws JsonProcessingException {
    Cases.ct_pro_05_01(environment);
    Cases.ct_pro_05_02(environment);
    Cases.ct_pro_05_03(environment);
    Cases.ct_pro_05_04(environment);
    Cases.ct_pro_05_05(environment);
    Cases.ct_pro_05_01(environment);

    verify(environment.dataLinkServer, times(2)).response(anyString());
  }
}
