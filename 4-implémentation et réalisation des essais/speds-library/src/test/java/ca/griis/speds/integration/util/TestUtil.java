package ca.griis.speds.integration.util;

import ca.griis.js2p.gen.speds.application.api.dto.*;
import ca.griis.js2p.gen.speds.link.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ContextDto__1;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class TestUtil {

  private final ObjectMapper objectMapper;

  public TestUtil(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public InterfaceDataUnit01Dto buildIdu01() throws JsonProcessingException {
    // Message construction
    List<InArgDto> inArgs = Arrays.asList(
        new InArgDto("ABCDEFGHIJKLMNOPQ", "ABCDEFGHIJKLMNOPQ"),
        new InArgDto("ABCDEFGHIJKLMNOP", "ABCDEFGHIJKLMNOPQRSTUVWXYZABC"));
    TacheEnvoiDto taskEnvDto =
        new TacheEnvoiDto(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            inArgs,
            new ArrayList<ParamDto>(),
            TacheEnvoiDto.Command.START);
    String taskEnvContent =
        objectMapper.writeValueAsString(taskEnvDto);
    ProtocolDataUnit1APPDto pdu = new ProtocolDataUnit1APPDto(
        new HeaderDto(
            HeaderDto.Msgtype.TACHE_ENVOI,
            UUID.randomUUID(),
            false,
            new SPEDSDto("2.0.0", "a reference")),
        taskEnvContent);
    final String message = objectMapper.writeValueAsString(pdu);

    // IDU construction
    final ca.griis.js2p.gen.speds.application.api.dto.ContextDto context =
        new ca.griis.js2p.gen.speds.application.api.dto.ContextDto(
            UUID.randomUUID().toString(),
            "executor",
            "connector_1",
            null,
            Boolean.FALSE);

    return new InterfaceDataUnit01Dto(context, message);
  }


  public void freePorts(ServerSocket originSocket, ServerSocket targetSocket,
      ServerSocket proxySocket)
      throws IOException {
    // Ensure no time out after we close it.
    // md - Je suis pas sur comment réserver un socket plus propre que ça.
    // Je réserve le port, ce faisant je le bloque.
    // Je met reuseAdress parce que quand tu close, il peut y avoir un timeout
    // Je met close dans l'espoir que personne pogne le port d'ici à ce que je l'utilise
    // Pense pas que c'est trop grave, pas trop le temps.
    originSocket.setReuseAddress(true);
    originSocket.close();
    targetSocket.setReuseAddress(true);
    targetSocket.close();

    if (proxySocket != null) {

      proxySocket.setReuseAddress(true);
      proxySocket.close();
    }
  }

  public InterfaceDataUnit56Dto transformIdu12ToIdu56(
      InterfaceDataUnit12Dto idu12Dto, int localPort) {
    return new InterfaceDataUnit56Dto(
        new Context56Dto(
            "https://localhost:%1$s".formatted(localPort), UUID.randomUUID(), false),
        idu12Dto.getMessage());
  }

  public InterfaceDataUnit12Dto transformIdu56ToIdu12(
      InterfaceDataUnit56Dto idu56Dto, String pga, String sourceCode, String destinationCode) {
    return new InterfaceDataUnit12Dto(
        new ca.griis.js2p.gen.speds.application.api.dto.ContextDto(pga, sourceCode, destinationCode,
            UUID.randomUUID(), false),
        idu56Dto.getMessage());
  }

  public InterfaceDataUnit56Dto transformIdu23ToIdu56(
      InterfaceDataUnit23Dto idu23Dto, int localPort) {
    return new InterfaceDataUnit56Dto(
        new Context56Dto(
            "https://localhost:%1$s".formatted(localPort), UUID.randomUUID(), false),
        idu23Dto.getMessage());
  }

  public InterfaceDataUnit23Dto transformIdu56ToIdu23(
      InterfaceDataUnit56Dto idu56Dto, String pga, String sourceCode, String destinationCode,
      String sdek, UUID trackingNumber) {
    return new InterfaceDataUnit23Dto(
        new ContextDto__1(pga, sourceCode, destinationCode, sdek, trackingNumber, false),
        idu56Dto.getMessage());
  }

  public String formatMemorySize(long bytes) {
    return bytes / (1024 * 1024) + " MB";
  }

  public boolean hasReachedPlateau(List<Long> memoryDifferences) {
    // Vérifier si les dernières mesures de mémoire sont relativement stables
    // Par exemple, vérifier si les 3 dernières valeurs ne diffèrent pas de plus de X%

    if (memoryDifferences.size() < 4) {
      return false; // Pas assez de données
    }

    // Calculer la variation moyenne des 3 dernières différences
    long lastValues = memoryDifferences.get(memoryDifferences.size() - 1);
    long previousValues = memoryDifferences.get(memoryDifferences.size() - 4);

    // Tolérance de 10% pour considérer qu'un plateau est atteint
    double variation = Math.abs((double) (lastValues - previousValues) / previousValues);
    return variation < 0.10; // 10% de tolérance
  }
}
