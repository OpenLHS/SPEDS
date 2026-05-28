
package ca.griis.js2p.gen.speds.presentation.api.dto;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * The service primitive used to communicate among the layers.
 * 
 */
@Generated("jsonschema2pojo")
public enum ServicePrimitive {

    REQUEST("request"),
    CONFIRM("confirm"),
    RESPONSE("response"),
    INDICATION("indication");
    private final String value;
    private final static Map<String, ServicePrimitive> CONSTANTS = new HashMap<String, ServicePrimitive>();

    static {
        for (ServicePrimitive c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    ServicePrimitive(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static ServicePrimitive fromValue(String value) {
        ServicePrimitive constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
