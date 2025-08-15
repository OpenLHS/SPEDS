package ca.griis.speds.session.integration.concurency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Cases {

  public static void ct_13(SessionHost initiatrice1, SessionHost initiatrice2,
      Pidu triggerMsg1, Pidu triggerMsg2,
      MultipleTaskTrigger trigger, BlockingQueue<Map.Entry<String, SessionInformation>> outMsgQueue,
      Pidu responseMsg1, Pidu responseMsg2) {
    try {
      // Given
      ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
      String serialMessage1 = mapper.writeValueAsString(triggerMsg1);
      String serialMessage2 = mapper.writeValueAsString(triggerMsg2);

      // When
      trigger.execute(
          () -> initiatrice1.requestFuture(serialMessage1),
          () -> initiatrice2.requestFuture(serialMessage2));

      // Then
      Map.Entry<String, SessionInformation> firstOut = outMsgQueue.take();
      Map.Entry<String, SessionInformation> secondtOut = outMsgQueue.take();
      Map.Entry<String, SessionInformation> swap;
      // swap, si nécessaire, mais pas nécessairement swap
      if (firstOut.getKey().contains("sourceCode2")) {
        // Swap - mettre sourceCode1 en premier
        swap = firstOut;
        firstOut = secondtOut;
        secondtOut = swap;
      }

      // Validation du outMsg1
      Pidu outMsg1 =
          SharedObjectMapper.getInstance().getMapper().readValue(firstOut.getKey(), Pidu.class);
      assertEquals(triggerMsg1.getMessage(), outMsg1.getMessage());
      assertEquals(firstOut.getValue().trackingNumber, outMsg1.getContext().getTrackingNumber());

      // Validation du outMsg2
      Pidu outMsg2 =
          SharedObjectMapper.getInstance().getMapper().readValue(secondtOut.getKey(), Pidu.class);
      assertEquals(triggerMsg2.getMessage(), outMsg2.getMessage());
      assertEquals(secondtOut.getValue().trackingNumber, outMsg2.getContext().getTrackingNumber());

      // Validation du responseMsg1
      String responseMsgSerial1 = initiatrice1.confirm();
      Pidu actualResponseMsg1 = mapper.readValue(responseMsgSerial1, Pidu.class);
      assertEquals(triggerMsg1.getContext().getTrackingNumber(),
          actualResponseMsg1.getContext().getTrackingNumber());
      assertEquals(responseMsg1.getMessage(), actualResponseMsg1.getMessage());

      // Validation du responseMsg1
      String responseMsgSerial2 = initiatrice2.confirm();
      Pidu actualResponseMsg2 = mapper.readValue(responseMsgSerial2, Pidu.class);
      assertEquals(triggerMsg2.getContext().getTrackingNumber(),
          actualResponseMsg2.getContext().getTrackingNumber());
      assertEquals(responseMsg2.getMessage(), actualResponseMsg2.getMessage());
    } catch (Exception e) {
      fail("Exception : " + e.getCause() + e.getMessage() + Arrays.toString(e.getStackTrace()));
    }
  }

  public static void ct_14(SessionHost initiatrice,
      Pidu triggerMsg1, Pidu triggerMsg2,
      MultipleTaskTrigger trigger,
      BlockingQueue<Map.Entry<String, SessionInformation>> outMsgQueue1,
      BlockingQueue<Map.Entry<String, SessionInformation>> outMsgQueue2,
      Pidu responseMsg1, Pidu responseMsg2) {
    try {
      // Given
      ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
      String serialMessage1 = mapper.writeValueAsString(triggerMsg1);
      String serialMessage2 = mapper.writeValueAsString(triggerMsg2);

      // When
      trigger.execute(
          () -> initiatrice.requestFuture(serialMessage1),
          () -> initiatrice.requestFuture(serialMessage2));

      // Then

      // Validation du responseMsg1
      String responseMsgSerial1 = initiatrice.confirm();
      Pidu actualResponseMsg1 = mapper.readValue(responseMsgSerial1, Pidu.class);
      String responseMsgSerial2 = initiatrice.confirm();
      Pidu actualResponseMsg2 = mapper.readValue(responseMsgSerial2, Pidu.class);
      Pidu swap;

      // Reorder messages, je ne peux pas garantir l'ordre d'arrivée
      if (!actualResponseMsg1.getContext().getDestinationCode()
          .equals(triggerMsg1.getContext().getDestinationCode())) {
        swap = actualResponseMsg1;
        actualResponseMsg1 = actualResponseMsg2;
        actualResponseMsg2 = swap;
      }

      // Le outMsg1
      Map.Entry<String, SessionInformation> actualOuMsg1WithSesInfo = outMsgQueue1.take();
      Pidu outMsg1 = SharedObjectMapper.getInstance().getMapper()
          .readValue(actualOuMsg1WithSesInfo.getKey(), Pidu.class);
      assertEquals(triggerMsg1.getMessage(), outMsg1.getMessage());
      assertEquals(actualOuMsg1WithSesInfo.getValue().trackingNumber,
          outMsg1.getContext().getTrackingNumber());

      // Le outMsg2
      Map.Entry<String, SessionInformation> actualOuMsg2WithSesInfo = outMsgQueue2.take();
      Pidu outMsg2 = SharedObjectMapper.getInstance().getMapper()
          .readValue(actualOuMsg2WithSesInfo.getKey(), Pidu.class);
      assertEquals(triggerMsg2.getMessage(), outMsg2.getMessage());
      assertEquals(actualOuMsg2WithSesInfo.getValue().trackingNumber,
          outMsg2.getContext().getTrackingNumber());

      // Le message de réponse 1
      assertEquals(triggerMsg1.getContext().getTrackingNumber(),
          actualResponseMsg1.getContext().getTrackingNumber());
      assertEquals(responseMsg1.getMessage(), actualResponseMsg1.getMessage());

      // Le message de réponse 2
      assertEquals(triggerMsg2.getContext().getTrackingNumber(),
          actualResponseMsg2.getContext().getTrackingNumber());
      assertEquals(responseMsg2.getMessage(), actualResponseMsg2.getMessage());
    } catch (Exception e) {
      fail("Exception : " + e.getCause() + e.getMessage() + Arrays.toString(e.getStackTrace()));
    }
  }
}
