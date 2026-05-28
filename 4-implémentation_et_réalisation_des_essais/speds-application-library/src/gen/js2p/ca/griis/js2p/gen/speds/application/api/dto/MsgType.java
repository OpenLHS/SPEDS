
package ca.griis.js2p.gen.speds.application.api.dto;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * The type of a SPEDS message. Mandatory in all layers.
 * 
 */
@Generated("jsonschema2pojo")
public enum MsgType {

    PLAN("PLAN"),
    ADMINISTRATION("ADMINISTRATION");
    private final String value;
    private final static Map<String, MsgType> CONSTANTS = new HashMap<String, MsgType>();

    static {
        for (MsgType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    MsgType(String value) {
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
    public static MsgType fromValue(String value) {
        MsgType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
