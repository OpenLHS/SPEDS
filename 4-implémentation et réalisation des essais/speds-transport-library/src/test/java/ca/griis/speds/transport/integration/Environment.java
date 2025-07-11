package ca.griis.speds.transport.integration;

import java.util.HashMap;
import java.util.Map;

public class Environment {
  Map<String, Object> testInputs;
  Map<String, Object> testOutputs;

  public Environment() {
    this.testInputs = new HashMap<>();
    this.testOutputs = new HashMap<>();
  }

  public void addInput(Map.Entry<String, Object> entry) {
    // todo changé en record, pour ça key.value
    this.testInputs.put(entry.getKey(), entry.getValue());
  }

  public void addOutput(Map.Entry<String, Object> entry) {
    testOutputs.put(entry.getKey(), entry.getValue());
  }

  public <T> T getInput(String key, Class<T> clazz) {
    Object value = this.testInputs.get(key);
    return clazz.cast(value);
  }

  public <T> T getOutput(String key, Class<T> clazz) {
    Object value = this.testOutputs.get(key);
    return clazz.cast(value);
  }
}
