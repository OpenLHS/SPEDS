
package ca.griis.js2p.gen.speds.application.api.dto;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * The service call from the initiating layer
 * 
 */
@Generated("jsonschema2pojo")
public enum Service {

    DELEGATE("delegate"),
    TRANSFER("transfer");
    private final String value;
    private final static Map<String, Service> CONSTANTS = new HashMap<String, Service>();

    static {
        for (Service c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    Service(String value) {
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
    public static Service fromValue(String value) {
        Service constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
