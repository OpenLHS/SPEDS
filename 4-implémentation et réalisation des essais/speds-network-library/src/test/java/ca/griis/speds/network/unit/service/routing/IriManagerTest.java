package ca.griis.speds.network.unit.service.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.routing.IriManager;
import org.junit.jupiter.api.Test;

public class IriManagerTest {

  @Test
  public void noArgConstructorTest() throws Exception {
    final IriManager iriManager = new IriManager();
    assertNotNull(iriManager);
  }

  @Test
  public void retrieveCodeTest() throws Exception {
    final String scheme = "https";
    final String host = "some.iri";
    final String path = "path";
    final String expected = "entityCode";
    final String query = "param1=value1&param2=value2&code=" + expected;
    final String fragment = "fragment";
    final String serializedIri = scheme + "://" + host + "/" + path + "?" + query + "#" + fragment;
    final String actual = IriManager.retrieveCode(serializedIri);
    assertEquals(expected, actual);
  }

  @Test
  public void retrieveCodeBadIri() throws Exception {
    final String badIri = "#bad#IRI";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(badIri));
  }

  @Test
  public void retrieveCodeNoQuery() throws Exception {
    final String iriNoQuery = "https://some.iri";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(iriNoQuery));
  }

  @Test
  public void retrieveCodeEmptyValueCode1() throws Exception {
    final String emptyCodeIri = "https://some.iri/path?code=#fragment";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(emptyCodeIri));
  }

  @Test
  public void retrieveCodeEmptyValueCode2() throws Exception {
    final String emptyCodeIri = "https://some.iri/path?code#fragment";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(emptyCodeIri));
  }

  @Test
  public void retrieveCodeBadCode() throws Exception {
    final String badCodeIri = "https://some.iri/?=entityCode";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(badCodeIri));
  }

  @Test
  public void retrieveCodeNoCode() throws Exception {
    final String noCodeIri = "https://some.iri/?";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(noCodeIri));
  }

  @Test
  public void retrieveCodeSeveralCode() throws Exception {
    final String severalCodeIri = "https://some.iri/path?code=entityCode1&code=entityCode2";
    assertThrows(DeserializationException.class, () -> IriManager.retrieveCode(severalCodeIri));
  }

  @Test
  public void replaceCodeWith() throws Exception {
    final String scheme = "https";
    final String host = "some.iri";
    final String path = "path";

    final String toReplace = "entityCode";
    final String replacement = "anotherEntityCode";
    final String baseQuery = "param1=value1&param2=value2&code=";
    final String initialQuery = baseQuery + toReplace;
    final String updatedQuery = baseQuery + replacement;

    final String fragment = "fragment";
    final String given = scheme + "://" + host + "/" + path + "?" + initialQuery + "#" + fragment;
    final String expected =
        scheme + "://" + host + "/" + path + "?" + updatedQuery + "#" + fragment;

    final String actual = IriManager.replaceCodeWith(given, replacement);
    assertEquals(expected, actual);
  }

  @Test
  public void replaceCodeWithBadIri() throws Exception {
    final String badIri = "#bad#IRI";
    final String replacement = "anotherEntityCode";
    assertThrows(DeserializationException.class,
        () -> IriManager.replaceCodeWith(badIri, replacement));
  }
}
