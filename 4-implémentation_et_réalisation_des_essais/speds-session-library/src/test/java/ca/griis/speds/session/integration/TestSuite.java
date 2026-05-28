package ca.griis.speds.session.integration;

import static ca.griis.speds.session.integration.Cases.ct_13;
import static ca.griis.speds.session.integration.Cases.ct_14;
import static ca.griis.speds.session.integration.Initiator.INITIATOR_1;
import static ca.griis.speds.session.integration.Initiator.INITIATOR_2;
import static ca.griis.speds.session.integration.Scenario.BAD_SDEK;
import static ca.griis.speds.session.integration.Scenario.BAD_STAMP;
import static ca.griis.speds.session.integration.Scenario.BAD_TOKEN;
import static ca.griis.speds.session.integration.Scenario.FAILED;
import static ca.griis.speds.session.integration.Scenario.ILLEGITIMATE;
import static ca.griis.speds.session.integration.Scenario.NO_SESSION;
import static ca.griis.speds.session.integration.Scenario.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
@ExtendWith(MockitoExtension.class)
public class TestSuite {
  private Environment environment;

  @BeforeEach
  void setup() throws Exception {
    environment = new Environment();
  }

  @Test
  void e_01() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    assertNotNull(cas01Out);
  }

  @Test
  void e_04() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    assertNotNull(cas02Out);
  }

  @Test
  void e_05() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, FAILED, cas01Out);
    assertNull(cas02Out);
  }

  @Test
  void e_06() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    //
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    assertNotNull(cas03Out);
  }


  @Test
  void e_07() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);

    String cas03Out = Cases.ct_03(environment, NO_SESSION, cas02Out, INITIATOR_1);
    assertNull(cas03Out);
  }

  @Test
  void e_08() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);

    String cas03Out = Cases.ct_03(environment, BAD_STAMP, cas02Out, INITIATOR_1);
    assertNull(cas03Out);
  }

  @Test
  void e_09() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);

    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    assertNotNull(cas04Out);
  }

  @Test
  void e_10() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);

    String cas04Out = Cases.ct_04(environment, NO_SESSION, cas03Out);
    assertNull(cas04Out);
  }

  @Test
  void e_11() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);

    String cas04Out = Cases.ct_04(environment, BAD_STAMP, cas03Out);
    assertNull(cas04Out);
  }

  @Test
  void e_13() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);

    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    assertNotNull(cas05Out);
  }

  @Test
  void e_14() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);

    String cas05Out = Cases.ct_05(environment, NO_SESSION, cas04Out, INITIATOR_1);
    assertNull(cas05Out);
  }

  @Test
  void e_15() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);

    String cas05Out = Cases.ct_05(environment, BAD_STAMP, cas04Out, INITIATOR_1);
    assertNull(cas05Out);
  }

  @Test
  void e_17() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);

    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    assertNotNull(cas06Out);
  }

  @Test
  void e_18() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);

    String cas06Out = Cases.ct_06(environment, NO_SESSION, cas05Out);
    assertNull(cas06Out);
  }

  @Test
  void e_19() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);

    String cas06Out = Cases.ct_06(environment, BAD_STAMP, cas05Out);
    assertNull(cas06Out);
  }

  @Test
  void e_20() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);

    when(environment.projectService.verifyEntityLegitimacy(
        environment.getPga(),
        environment.clientParameters.code(),
        environment.clientParameters
            .certificatePrivateKeysEntry()
            .getCertficate()
            .getPublicKey()))
                .thenReturn(false);

    String cas06Out = Cases.ct_06(environment, ILLEGITIMATE, cas05Out);
    assertNull(cas06Out);
  }


  @Test
  void e_21() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);

    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    assertNotNull(cas07Out);

  }

  @Test
  void e_22() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    assertNotNull(cas01Out);

    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    assertNotNull(cas02Out);

    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    assertNotNull(cas03Out);

    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    assertNotNull(cas04Out);

    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    assertNotNull(cas05Out);

    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    assertNotNull(cas06Out);

    String cas07Out = Cases.ct_07(environment, NO_SESSION, cas06Out, INITIATOR_1);
    assertNull(cas07Out);
  }

  @Test
  void e_23() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    assertNotNull(cas01Out);

    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    assertNotNull(cas02Out);

    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    assertNotNull(cas03Out);

    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    assertNotNull(cas04Out);

    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    assertNotNull(cas05Out);

    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    assertNotNull(cas06Out);

    String cas07Out = Cases.ct_07(environment, BAD_STAMP, cas06Out, INITIATOR_1);
    assertNull(cas07Out);
  }

  @Test
  void e_24() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    assertNotNull(cas01Out);

    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    assertNotNull(cas02Out);

    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    assertNotNull(cas03Out);

    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    assertNotNull(cas04Out);

    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    assertNotNull(cas05Out);

    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    assertNotNull(cas06Out);

    String cas07Out = Cases.ct_07(environment, BAD_SDEK, cas06Out, INITIATOR_1);
    assertNull(cas07Out);
  }

  @Test
  void e_25() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);

    assertNotNull(cas08Out);
  }

  @Test
  void e_26() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, NO_SESSION, cas07Out);
    assertNull(cas08Out);
  }

  @Test
  void e_27() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, BAD_STAMP, cas07Out);
    assertNull(cas08Out);
  }

  @Test
  void e_31() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    assertNotNull(cas10Out);
  }

  @Test
  void e_32() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, NO_SESSION, cas08Out);
    assertNull(cas10Out);
  }

  @Test
  void e_33() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, BAD_STAMP, cas08Out);
    assertNull(cas10Out);
  }

  @Test
  void e_34() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, SUCCESS, cas10Out);
    assertNotNull(cas11Out);
  }

  @Test
  void e_35() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, NO_SESSION, cas10Out);
    assertNull(cas11Out);
  }

  @Test
  void e_36() throws Exception {
    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, BAD_STAMP, cas10Out);
    assertNull(cas11Out);
  }

  @Test
  void e_37() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, SUCCESS, cas10Out);
    String cas12Out = Cases.ct_12(environment, SUCCESS, cas11Out);
    assertNull(cas12Out);
  }

  @Test
  void e_38() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, SUCCESS, cas10Out);
    String cas12Out = Cases.ct_12(environment, NO_SESSION, cas11Out);
    assertNull(cas12Out);
  }

  @Test
  void e_39() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, SUCCESS, cas10Out);
    String cas12Out = Cases.ct_12(environment, BAD_STAMP, cas11Out);
    assertNull(cas12Out);
  }

  @Test
  void e_40() throws Exception {

    String cas01Out = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out = Cases.ct_02(environment, SUCCESS, cas01Out);
    String cas03Out = Cases.ct_03(environment, SUCCESS, cas02Out, INITIATOR_1);
    String cas04Out = Cases.ct_04(environment, SUCCESS, cas03Out);
    String cas05Out = Cases.ct_05(environment, SUCCESS, cas04Out, INITIATOR_1);
    String cas06Out = Cases.ct_06(environment, SUCCESS, cas05Out);
    String cas07Out = Cases.ct_07(environment, SUCCESS, cas06Out, INITIATOR_1);
    String cas08Out = Cases.ct_08(environment, SUCCESS, cas07Out);
    String cas10Out = Cases.ct_10(environment, SUCCESS, cas08Out);
    String cas11Out = Cases.ct_11(environment, SUCCESS, cas10Out);
    String cas12Out = Cases.ct_12(environment, BAD_TOKEN, cas11Out);
    assertNull(cas12Out);
  }

  @Test
  void e_41() throws Exception {
    String cas01Out_1 = Cases.ct_01(environment, SUCCESS, INITIATOR_1);
    String cas02Out_1 = Cases.ct_02(environment, SUCCESS, cas01Out_1);
    String cas03Out_1 = Cases.ct_03(environment, SUCCESS, cas02Out_1, INITIATOR_1);
    String cas04Out_1 = Cases.ct_04(environment, SUCCESS, cas03Out_1);
    String cas05Out_1 = Cases.ct_05(environment, SUCCESS, cas04Out_1, INITIATOR_1);
    String cas06Out_1 = Cases.ct_06(environment, SUCCESS, cas05Out_1);
    Cases.ct_07(environment, SUCCESS, cas06Out_1, INITIATOR_1);

    String cas01Out_2 = Cases.ct_01(environment, SUCCESS, INITIATOR_2);
    String cas02Out_2 = Cases.ct_02(environment, SUCCESS, cas01Out_2);
    String cas03Out_2 = Cases.ct_03(environment, SUCCESS, cas02Out_2, INITIATOR_2);
    String cas04Out_2 = Cases.ct_04(environment, SUCCESS, cas03Out_2);
    String cas05Out_2 = Cases.ct_05(environment, SUCCESS, cas04Out_2, INITIATOR_2);
    String cas06Out_2 = Cases.ct_06(environment, SUCCESS, cas05Out_2);
    Cases.ct_07(environment, SUCCESS, cas06Out_2, INITIATOR_2);
    ct_13(environment);
  }

  @Test
  void e_42() throws Exception {
    ct_14(environment);
  }
}
