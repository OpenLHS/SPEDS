package ca.griis.speds.application.testdata;

import ca.griis.js2p.gen.speds.application.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit12Dto;
import java.util.UUID;

public class ExpectedInterfaceDataUnit {
  public static InterfaceDataUnit12Dto getTacheEnvoiMessage12() {
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "executor",
        "connector_1", null, Boolean.FALSE);

    String message = """
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

    InterfaceDataUnit12Dto tacheMsg = new InterfaceDataUnit12Dto(context, message);

    return tacheMsg;
  }

  public static InterfaceDataUnit01Dto getTacheEnvoiMessage01() {

    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "executor",
        "connector_1", Boolean.FALSE);

    String message =
        "{\"header\":{\"msgtype\":\"TACHE.ENVOI\",\"id\":\"c51fd8ef-eba6-4239-9453-f75ca95a90b2\",\"parameters\":false,\"SPEDS\":{\"version\":\"2.0.0\",\"reference\":\"a reference\"}},\"content\":\"\"}";

    InterfaceDataUnit01Dto tacheMsg = new InterfaceDataUnit01Dto(context, message);

    return tacheMsg;
  }

  public static InterfaceDataUnit01Dto getTacheReceptionMessage() {
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", Boolean.FALSE);

    String message =
        "{\"header\":{\"msgtype\":\"TACHE.RECEPTION\",\"id\":\"c51fd8ef-eba6-4239-9453-f75ca95a90b2\",\"parameters\":false,\"SPEDS\":{\"version\":\"2.0.0\",\"reference\":\"a reference\"}},\"content\":\"\"}";
    InterfaceDataUnit01Dto tacheMsg = new InterfaceDataUnit01Dto(context, message);

    return tacheMsg;
  }

  public static InterfaceDataUnit12Dto getTacheReceptionMessage12() {
    ContextDto context = new ContextDto("736bfe3a-3e9d-4d94-ada5-b69d051bcea3", "connector_1",
        "executor", UUID.fromString("26ac0b75-430c-4cbb-8bb6-516a69444f0e"), Boolean.FALSE);

    String message = """
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
    InterfaceDataUnit12Dto tacheMsg = new InterfaceDataUnit12Dto(context, message);

    return tacheMsg;
  }
}
