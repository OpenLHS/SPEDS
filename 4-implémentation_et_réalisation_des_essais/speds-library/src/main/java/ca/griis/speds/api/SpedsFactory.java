/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpedsFactory.
 * @brief @~english Contains description of SpedsFactory class.
 */

package ca.griis.speds.api;

import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.internal.ApplicationHostFactory;
import ca.griis.speds.application.internal.verification.InterfaceChecker;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpedsFactory {
  private final ObjectMapper mapper;
  private final ApplicationFactory factory;

  public SpedsFactory(ProjectService projectService, CryptographyService cryptoService) {
    this.factory = new ApplicationHostFactory(cryptoService, projectService);
    this.mapper = SharedObjectMapper.getInstance().getMapper();
  }

  public ApplicationHost init(InitInParamsDto parameters, ApplicationHostEvent applicationHostEvent,
      InterfaceChecker checker) throws JsonProcessingException {
    String params = mapper.writeValueAsString(parameters);
    ApplicationHost host = init(params, applicationHostEvent, checker);
    return host;
  }

  public ApplicationHost init(String parameters, ApplicationHostEvent applicationHostEvent,
      InterfaceChecker checker) {
    ApplicationHost host = factory.init(parameters, applicationHostEvent, checker);

    return host;
  }
}
