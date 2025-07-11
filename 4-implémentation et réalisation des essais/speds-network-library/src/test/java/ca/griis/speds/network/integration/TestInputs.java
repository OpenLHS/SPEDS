package ca.griis.speds.network.integration;

import ca.griis.js2p.gen.speds.network.api.dto.*;
import ca.griis.speds.network.util.KeyVar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;

public class TestInputs {
  ///
  /// Attention
  ///
  /// La définition des messages est volontairement explicite et répétitive.
  ///

  static TestCryptoUtils sealManager = new TestCryptoUtils();

  public static InterfaceDataUnit45Dto make_ct_pro_03_01_e1() {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";
    UUID trackingNumber = UUID.randomUUID();

    int messageLength = ThreadLocalRandom.current().nextInt(100);
    String message = RandomStringUtils.randomAlphanumeric(messageLength);

    return new InterfaceDataUnit45Dto(
        new Context45Dto(sourceIri, destinationIri, trackingNumber, false), message);
  }


  public static InterfaceDataUnit45Dto make_ct_pro_04_e1() {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";
    UUID trackingNumber = UUID.randomUUID();

    int messageLength = ThreadLocalRandom.current().nextInt(100);
    String message = RandomStringUtils.randomAlphanumeric(messageLength);

    return new InterfaceDataUnit45Dto(
        new Context45Dto(sourceIri, destinationIri, trackingNumber, false), message);
  }

  public static InterfaceDataUnit56Dto make_ct_pro_04_01_e2(String idOfPduSent, ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_REC, idOfPduSent, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, ""), "");

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_04_02_e2(String idOfPduSent, ObjectMapper obm)
      throws JsonProcessingException {

    String messageId;

    do {
      messageId = UUID.randomUUID().toString();
    } while (messageId.equals(idOfPduSent));

    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_REC, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, ""), "");

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_04_03_e2(String idOfPduSent, ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, idOfPduSent, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, ""), "");

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }



  public static InterfaceDataUnit56Dto make_ct_pro_04_04_e2(String idOfPduSent, ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    int fakeAuthLength = ThreadLocalRandom.current().nextInt(20);
    String authentification = RandomStringUtils.randomAlphanumeric(fakeAuthLength);

    String privateKey =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQD5n9KIHuAD9xGeihm4a+FPNMOosiO+XgHpLmlQv536ZON7fR1BQbBGLv9tdXZjKlijRFYU0EmtOpYWh3pIyEwMxUqmFc9fi4QJUOII/1uO8N4p1f7tozRFhmihdy+D3SUT+I9jIG3B+tIXrhnbxgQnjwKPhZxd18O2B/y5bub8vlp2NV+jVw8k6e1/CzkMbdEQcmSy+NMk5NCb3udBiz/yRh6rtCpHmNqCe/mFCUsMYAPNjQ9gahfO0chnHc34iN02dS8xhuloJJ3e55n8OX8JfsbI0ytZCn7g5I5T0KSj9lUImWwXsqb912sAntjStiMBupMr9JMRzpZmn9YQbN45AgMBAAECggEABGlOI6iusHM/z6Dve8Ja93beaaSdnBd2NNmPSTrcVFikaoIXWm1flkXuA/sE7hNeq01MW3l41oOys6WORCtXU2HIhBnePHkvNVOawv+cQxuU7tardtChCQTV5DCX2Yk2xrJoT59QQT1+U/vNf4RG04TOo0cAWz+HuQz4FYt/r9dhEM4Crvpt2tEV4xKXH9zPX6xahlvgJqH4GwTO9MUYq/000aFItrarXydi89PycgOYsgh793qfsxznIe+F0VAoMrJVgJDPySslriTiLLWze+G7bbGk7IdeYvpQLkMzmo3rCoHVKaM8GoZOicjLN4ACIacAc4jMaYxKYYgECiLA/wKBgQD/KCSfi0YeFzgypPi82i1f9jJ9v828jbjvI5wSueOiNjcaiwvQn3Nus4+V85z2FyzJapKzNihbFZgCTk6CCL8D1dp9BdXASIOGJxq4vZNslHkQSnAKrYmPfQykp0iONNvdHjGF144FmeSxKsBIrxHpp2b2D1ApqwrbQ7nQu00quwKBgQD6cv+7o43BSqP6/ewgzp1MtV5mCYPoK6VvtgYMzclDiCQ0YxgL2gQsOZdgPe3ymqN4NknS1ZQuneeag6XVKhyNVeB2qODKYDlsBE10QHs4rbU5EgZzCtqop5WjxaVB7A8soazTeAPTlSYtYcAiEg5UnZqB6oWNPmFPh5rHOVGNmwKBgAYvvfuUT+Xo4DbyIwnJLHJ4MK812FIrCYHa1izqDufe8qUVG/s58n2Lov6awoKA+1gC2n234XCb7GyZ43NprN/17m87lfwshevZxN8X0Zw/WVQYyfCoVn6BJK5VVAAGrhIFamP5XvspyQ62n6TjgcathWSP6HyqJgA8+yNhdzSDAoGAJcxjYktd4pVESGY4U/866E/bLmUzPkAQGNsM8LPCM2oBpNJr16YQySfr+799AQdrsoHnBGXtlleIk95YkeJoKsCSPvZf7ss0/2Nq2Se+MSCuYC2jQYua0qdwQyZaOafJ5n+EGOpdEXrKJ0FAgSPee9DJd+AQRe/rOdiKeJTjBQ8CgYEAvdtq/wYjkKueYfiGECBifWnfgmcCwfiS4OiYoEiqMc7HoOMhnU8UMZCHklN4yh9N4rbrfc3BmwBX0kOqnaBUrnW+xJZx9RfS959Yuti+HjZx+YVvxogFOqcX0tvIYt13/c0iY/VOoyGrWmipHy/k3mw0RM8x7X4qsbb176+KxlA=";

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, idOfPduSent, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, ""), "");

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }



  public static InterfaceDataUnit56Dto make_ct_pro_04_05_e2(String idOfPduSent, ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, idOfPduSent, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    int fakeHeaderLength = ThreadLocalRandom.current().nextInt(10);
    String headerSeal = RandomStringUtils.randomAlphanumeric(fakeHeaderLength);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, ""), "");

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }

  public static InterfaceDataUnit56Dto make_ct_pro_05_01_e1(ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    String messageId = UUID.randomUUID().toString();

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);
    String contentSeal = sealManager.createSeal(content, "content", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, contentSeal), content);

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_05_02_e1(ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    String messageId = UUID.randomUUID().toString();

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_REC, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);
    String contentSeal = sealManager.createSeal(content, "content", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, contentSeal), content);

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_05_03_e1(ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    int fakeAuthLength = ThreadLocalRandom.current().nextInt(20);
    String authentification = RandomStringUtils.randomAlphanumeric(fakeAuthLength);

    String privateKey =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC1dFd7kV2Q5VE1UsblfDvb/wtbs/pNw0mDdClQHTSNmSGjHoEbfknWSMD/iEcYvWJbPGdwO0Y4/MWqKdCpp0j0s4eYDYjiGJ+VPYwGY8x5tGmaaHw2tnyiHsfeO2aL8LvXnIWpgaqa1qrgaD6y4g7gyLocVw192Ds7ZBZjIXri+4wdpkmSqwNZURaooNapgHwi7b55mROsRdb7E0+D499y9fXFOcNPg6b3ZVXzc9Vwq5Ks09cUKRmat0TEUFe33pnoLo6IflcswAJSCxtA0LvV72oMUNfrOq+IrxjVTmNisierZZ4yXk99JWsUGFnIQfiLRC8GYbavCylmCM55Wzw5AgMBAAECggEAA06DwTZrDTBr1FWi8HZDav6dv0XE3agdtCRCR0aSkuZqMZxLmWvDcbQkT3CboKywcndj1SynWqa0lwv2Ov+khmndImnth11DTzqhzVE1RGrp8vMZ5fwmW+TFGw/CQYuZO+3xlTi0Zxta95++s/Wg2sgEDqJbFkkblZ+Wm6dCo4jbfXnAluLwL91E4oha56Ke5vso2BWSMhJgwj+nA13ox9U1NkxGkaSo46vuuNld9dM5AuEnkKMMFrvlAn3jx5xbrsToVot2BKcTNswOIp9h0qW83tXo6whKuaEDby5g5I5xqgP/Y9WTHkqg8uM7+2O+lIwKZ7yJZHvCOPqYO99wJwKBgQDwdnXSSit2Va6td5eMPxoeMlTa0fScZxJ2WvjAhBFQCbtRfwQsDrqvuDLaDDdmg+TvBHTz4PwWyKvc3kfqA7fBij+8X5K2loXm3DxcB1A30OgoI08/Wxrx1oFyffDfiNKT2hXhSfweuqGcAWLKK4180/Bi7uMYjZiCk2ocRQW9swKBgQDBLdB8ojhI3w3VVN4GyjAmQDyrfEEjxW5yN8xDoRMG5ym9vOt5pacXXxaIrrw/xO6AOZRmysi131EEki3A1EDj6iRtvxLniMRkfucXIoqPHeFQPAzb/SmWvm5T1qp5Ck8UIxhDNqwb6SGFI8Z1WVQjgyZSc8ORgEQX3RkWjKGgYwKBgGED2itao0hd71irPGhn+Rifw955ANCtQSShNWUuo3GAS6Pos/02gBlvHo0ciqmAozfPu4O9jNeAZN56i9cdYuDh7y8H/EBQYHuw9WVHyhSK4292N33fOvLBUX6o0yrMn/Do9xTq7Z5UO+meIYZhpfbr5ztzGAqK7jIYWaJIMrXDAoGAWu/5PCKVQmCWP32zAxk871S/+q4EcBVpt6TiqFd0AITjVmlPpDVB4jOmBPPUtGQ9LeltHbKKJ2uX1QIHPcXtQh0fxvXyrUHpybEfkfl1poeXYFDUcN7NLCQ7BYaBO3eJ4F3g9wsOnBjz6Zv8uZMyk4ESki/iuWTbKSywube8AicCgYEA1B55MncNa4xone0Hy75PFsxTaeDJ7MHcsnPdEEf+AZKHeJBedufoHoH1LA5Z21xn13YBTYAaIbHAoxBU4tLX7D4DlOJC4D11JFq6c5HXGXT/40djJLuRjS2/PnAA86g0L7WpfI/Dtzeaok9YLQr4VrjP19Tirmiyuy0uDhFwaZc=";

    String messageId = UUID.randomUUID().toString();

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);
    String contentSeal = sealManager.createSeal(content, "content", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, contentSeal), content);

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_05_04_e1(ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    String messageId = UUID.randomUUID().toString();

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    int fakeSealLength = ThreadLocalRandom.current().nextInt(50);
    String headerSeal = RandomStringUtils.randomAlphanumeric(fakeSealLength);

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);

    String contentSeal = sealManager.createSeal(content, "content", privateKey);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, contentSeal), content);

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }


  public static InterfaceDataUnit56Dto make_ct_pro_05_05_e1(ObjectMapper obm)
      throws JsonProcessingException {
    String sourceIri = "https://www.griis.ca/15";
    String destinationIri = "https://www.griis.ca/51";

    String authentification = KeyVar.griisCertRsa;
    String privateKey = KeyVar.griisPrikeyRsa;

    String messageId = UUID.randomUUID().toString();

    HeaderDto header = new HeaderDto(HeaderDto.Msgtype.RES_ENV, messageId, sourceIri,
        destinationIri, authentification, false, new SPEDSDto("v6.2.0",
            "https://depot.griis.usherbrooke.ca/documentations/product-asciidoc-documentation/-/tree/main/speds-line/speds-library"));

    String headerSeal = sealManager.createSeal(header, "header", privateKey);

    int contentLength = ThreadLocalRandom.current().nextInt(100);
    String content = RandomStringUtils.randomAlphanumeric(contentLength);

    int fakeSealLength = ThreadLocalRandom.current().nextInt(50);
    String contentSeal = RandomStringUtils.randomAlphanumeric(fakeSealLength);

    ProtocolDataUnit5Dto pdu5Dto =
        new ProtocolDataUnit5Dto(header, new StampDto(headerSeal, contentSeal), content);

    String message = obm.writeValueAsString(pdu5Dto);

    Context56Dto context = new Context56Dto(destinationIri, UUID.randomUUID(), false);

    return new InterfaceDataUnit56Dto(context, message);
  }
}
