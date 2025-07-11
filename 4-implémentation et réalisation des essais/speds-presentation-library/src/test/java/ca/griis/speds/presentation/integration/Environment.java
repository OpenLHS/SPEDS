package ca.griis.speds.presentation.integration;

import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.mockito.Mock;

public class Environment {
  public PresentationHost presentationHost;

  public String pga;
  public String source;
  public String destination;

  public String spedsVersion;
  public String spedsReference;

  public HeaderDto.Msgtype msgtype;
  public UUID msgId;
  public UUID trackinNumber;

  public ObjectMapper objectMapper;

  public SessionHost sessionHost;

  public String idu12ToSend;
  public String idu23Sent;

  public InterfaceDataUnit12Dto receivedIdu12;

  @Mock
  PgaService pgaServiceMock;

  public Environment(ObjectMapper objectMapper, SessionHost sessionHost) {

    this.pga = UUID.randomUUID().toString();
    this.source = UUID.randomUUID().toString();
    this.destination = UUID.randomUUID().toString();

    this.spedsVersion = "6.0.0";
    this.spedsReference = "referenceTest";

    this.objectMapper = objectMapper;
    this.sessionHost = sessionHost;

    String valid_params = """
        {
          "options" : {
            "speds.pre.version": "%1$s",
            "speds.pre.reference": "%2$s"
          }
        }
        """.formatted(this.spedsVersion, this.spedsReference);

    MutablePresentationFactory presentationFactory =
        new MutablePresentationFactory(this.pgaServiceMock) {
          @Override
          public SessionHost initSessionHost(String parameters) {
            return sessionHost;
          }
        };

    this.presentationHost = presentationFactory.init(valid_params);
  }
}
