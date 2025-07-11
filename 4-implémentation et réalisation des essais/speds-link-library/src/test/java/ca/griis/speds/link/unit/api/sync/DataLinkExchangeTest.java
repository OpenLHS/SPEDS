package ca.griis.speds.link.unit.api.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.speds.link.api.sync.ImmutableDataLinkFactory;
import ca.griis.speds.link.api.sync.ImmutableDataLinkHost;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataLinkExchangeTest {

  private ImmutableDataLinkHost client;
  private ImmutableDataLinkHost server;

  @BeforeEach
  void setUp() {
    ImmutableDataLinkFactory factory = new ImmutableDataLinkFactory();

    String parametersJson1 =
        """
            {
                "options": {
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.host": "0.0.0.0",
                  "speds.dl.https.server.port": 8080
                 }
            }
            """;

    client = (ImmutableDataLinkHost) factory.init(parametersJson1);

    String parametersJson2 =
        """
            {
                "options": {
                  "speds.dl.protocol": "https",
                  "speds.dl.https.server.private.key": "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpzZXKDX2Q1bgvLJR3Y3dVmnNw7dnHC6VKAic25rpbSrlvs/eYgjJ2gr6Dby0zXOKGlZf4ieONu7Nfl1nL68Lg9WXpY+pHOyMarF/sBjhj29VzXPunIw8pycBPdRSwUr5cRdUEj0MD+GlIKf7GS+HiSo3aI6zh5AI6mZCrHR4bDIkCACTIUvFaHWsYoa8llfV7kAcMQJxjxnhvohi3zqZykDrYdQ72vBCveMdOYpfiXmYssOz+f/TJZEqSshZl82dsdK5Xiy5W5U3i6527itaLQv4g76RwA6FnBFfZHluHh/e+Qo3C0IXOUyseS0lA6xWFdDKVRNoqixVcKqgeHVKBAgMBAAECggEABo9/yPgFd0Bl2d8mK1WBL+8EPjaj+LNKYxYhnFbrMbzGdmDkx5RimiT9P5T4XgKQNzTCoHxfYBBo/Hmbo0VyjABSb2NU+Wr5AiS30T9nE8Cghuqh4UUsmefTUZ6YVCNJeviXVt8PVUqjP4HT3kB8Gglyx2m9pvEJMjXQ55n75rD6Rd4eqZCorGC/JYU/gurmmnnpyW3Nmii4cvDGvFwr8jC4CZN0jwrkwWj9X+Fv4dNNDLH4LxTvj03NBRU0ThyWLas/EE/NzA1mv713VMPyZwx7UvMxxto91GWv/nbh4sTNcBfKO+ZK+UrNhGWRkFG0kwjo0wzs4vYXN7RSjTpPuQKBgQDYI0VtMRN4WRreeRNntiPTBIA4w8Z4b4aCoeNY2WIVkNACSZgBNo9rn7SqM0yw0beaWNqSBIHSX5Ch5QMBW6QRRt6ZK6Tq3uNjui7J2uv+d/j+838R+2OoSqChYLDy/vdxUzCpVpkaisonIarKfcoqqc7SLRjsu845HQPt6erG3QKBgQDJHqq626GOzDuWp+hG1Qq3JdW3ZkXY8LqtQcbzhKKAz3w8u+zh++E+QscJfrr5sRoVzdmkr0lcMGeo1O3gtVWqnzWT4vzzLZi/l0+pKKCgbeiBzEI+mNFIQnZ1O/iKJQfs+fblNl3ub1Mlxm17sKFMFWWi0I3hnXgkv1BlWBJ19QKBgQC98TFAJlLP/q6IOKr/B6gv99KfEB3JFWmGP7LGEDQMc7j5aad12Xbsw+tHb9HDymmp8NAUZnWYZXd7bwDXHqvuqvNQdHR4G+yFZcdciVG/zbs6gs53BQ+tg/fqGkknIz5djxhCmOHv22yQOxwW27jhCV3CgvNWiC1RL9iWKm2y2QKBgGVtRNbliq10TBznYtnN+RByUTyjpFgK12onAQmwey+Q8+vBLm6tU2PN04jzU6I28ZvLa5aFG+8VLkHT2H95k9FvZ1rEn6KX/S+qRG9f4Nnnc9l5xHLDKNBTTGBFNUud70hQq3XfHDHyDLHBR1eYtU+kftREbzk36+5EWWwypWS9AoGAYdu5nDPqRAbHgCMknPzpZHoaPYdRBA4Z8jfbfKkSRscQEmkjRtyUumvju4lsdGzUdbIXmE1Or+cKF409Gu1hPPdA+H1LKx56ozwyUyGfM3XYPIcmtLu9GnR3d6tjoC2AsXqR2TX9gkdAIQjdlC5wsBz6HzOVZbLNtfJb3m/p4lA=",
                  "speds.dl.https.server.cert": "MIIDhTCCAm2gAwIBAgIUBAFWukNh1P1hIceIcRb9NvR//HUwDQYJKoZIhvcNAQELBQAwUjELMAkGA1UEBhMCQ0ExDzANBgNVBAgMBlF1ZWJlYzETMBEGA1UEBwwKU2hlcmJyb29rZTEOMAwGA1UECgwFR1JJSVMxDTALBgNVBAMMBFRlc3QwHhcNMjUwMjIwMTYwOTIzWhcNMjYwMjIwMTYwOTIzWjBSMQswCQYDVQQGEwJDQTEPMA0GA1UECAwGUXVlYmVjMRMwEQYDVQQHDApTaGVyYnJvb2tlMQ4wDAYDVQQKDAVHUklJUzENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKnNlcoNfZDVuC8slHdjd1Wac3Dt2ccLpUoCJzbmultKuW+z95iCMnaCvoNvLTNc4oaVl/iJ4427s1+XWcvrwuD1Zelj6kc7IxqsX+wGOGPb1XNc+6cjDynJwE91FLBSvlxF1QSPQwP4aUgp/sZL4eJKjdojrOHkAjqZkKsdHhsMiQIAJMhS8VodaxihryWV9XuQBwxAnGPGeG+iGLfOpnKQOth1Dva8EK94x05il+JeZiyw7P5/9MlkSpKyFmXzZ2x0rleLLlblTeLrnbuK1otC/iDvpHADoWcEV9keW4eH975CjcLQhc5TKx5LSUDrFYV0MpVE2iqLFVwqqB4dUoECAwEAAaNTMFEwHQYDVR0OBBYEFD8KOlGdKZr17kb0Zv5CdWOz5cQdMB8GA1UdIwQYMBaAFD8KOlGdKZr17kb0Zv5CdWOz5cQdMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH5iKkUnKGOMODV4I9lYaXFVLl1MA23ceuhIpvmV/Hqjoeig+/FrbuAsq8iRsiibGtBGuCcrjuqGaXEkAX2B+nA6iY1/Z3T0u+7aKHxvbxlaHbUVXc1P45Ps7Ef46zSMc+uRQWcysLdKubh14eh/lubI8uqq8TSYLgMziOW5OBwV5Im6J0a2XrETMfbvbZse7U+lvp1FWwTmjGbNZMMR3uGziPyEVAIZ6+S644VQWhA+DS492MWLOZx9mwxhG8faVtXKNL+SJrgmzCdJUmRT2ypTYqrZnCoy1QI5DX02WdQ2hS1pyg69/HoIWD03Wv/XRdOon11gpVWspzprLDy5KoU=",
                  "speds.dl.https.server.host": "0.0.0.0",
                  "speds.dl.https.server.port": 8081
                  }
            }
            """;

    server = (ImmutableDataLinkHost) factory.init(parametersJson2);
  }

  @AfterEach
  public void teardown() {
    client.close();
    server.close();
  }

  @Test
  public void exchange() throws JsonProcessingException {
    String idu1Json =
        """
            {
                "context": {
                   "destination_iri": "https://localhost:8081",
                   "tracking_number" : "6d10e181-c637-4cba-ad26-1f81b52ce935",
                   "options" : false
                 },
                "message": "%s"
            }
            """.formatted(bigMessage());
    client.request(idu1Json);

    String receivedIdu1Json = server.indication();

    InterfaceDataUnit56Dto expectedIdu1 = SharedObjectMapper.getInstance().getMapper()
        .readValue(idu1Json, InterfaceDataUnit56Dto.class);
    InterfaceDataUnit56Dto receivedIdu1 = SharedObjectMapper.getInstance().getMapper()
        .readValue(receivedIdu1Json, InterfaceDataUnit56Dto.class);

    assertEquals(expectedIdu1.getMessage(), receivedIdu1.getMessage());


    String idu2Json =
        """
            {
                "context": {
                   "destination_iri": "https://localhost:8080",
                   "tracking_number" : "%s",
                   "options" : false
                 },
                "message": "%s"
            }
            """.formatted(receivedIdu1.getContext().getTrackingNumber(), bigMessage());
    server.response(idu2Json);

    String receivedIdu2Json = client.confirm();

    InterfaceDataUnit56Dto expectedIdu2 = SharedObjectMapper.getInstance().getMapper()
        .readValue(idu2Json, InterfaceDataUnit56Dto.class);
    InterfaceDataUnit56Dto receivedIdu2 = SharedObjectMapper.getInstance().getMapper()
        .readValue(receivedIdu2Json, InterfaceDataUnit56Dto.class);

    assertEquals(expectedIdu2.getMessage(), receivedIdu2.getMessage());
  }

  private String bigMessage() {
    return """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer auctor ligula et vestibulum scelerisque. Cras massa tellus, tempor nec congue non, pellentesque nec elit. Nunc lacus dolor, aliquet eu diam porta, rhoncus sodales quam. Maecenas ut neque dignissim, blandit lacus a, accumsan enim. Ut massa purus, faucibus ut ex quis, vulputate pretium velit. Curabitur commodo sed lorem non blandit. Nullam et euismod lacus.
        Suspendisse rhoncus id arcu pretium laoreet. Proin maximus ante eu semper pellentesque. Maecenas non consectetur ex, non faucibus massa. Cras nec dolor quam. Sed in consectetur mi. Cras justo lacus, sagittis a elementum quis, tempor at purus. Pellentesque turpis nisl, dictum a velit sit amet, vehicula vestibulum ante.
        Ut euismod velit eu lectus fringilla, eu bibendum nibh suscipit. Praesent eu massa laoreet, ultrices orci vel, malesuada odio. Proin nec molestie orci, ut maximus ligula. Proin semper non lorem quis feugiat. In dignissim elementum neque, sit amet accumsan ante dignissim et. Suspendisse ultricies mollis velit eu malesuada. Maecenas eget feugiat ex, ac venenatis enim. Mauris suscipit felis in commodo consequat. Nullam porta massa neque. Fusce finibus augue sed tincidunt ornare. Sed nunc arcu, malesuada quis faucibus vel, faucibus non metus. Sed vitae fringilla leo.
        Suspendisse potenti. Fusce dignissim ante sit amet iaculis dictum. Vivamus eu tristique justo. Integer imperdiet iaculis elementum. Nulla facilisi. Sed sagittis blandit nisl, sed ultrices ligula dapibus a. Integer consectetur eu sem ut vehicula. Curabitur euismod at lectus eu egestas. Pellentesque nec tellus arcu. Nulla congue ex sit amet porttitor tempor. Suspendisse tincidunt tellus nec magna vestibulum rhoncus.
        Vivamus maximus purus enim, sit amet semper turpis rhoncus ac. Sed eget magna pharetra, faucibus justo vitae, mattis dui. Vivamus et lorem mi. Vivamus nulla felis, pretium ut bibendum at, faucibus vel nulla. Nullam sed nisi ut tellus sollicitudin accumsan. Nunc massa augue, lobortis ut risus at, iaculis tempus ipsum. In elementum massa ligula. Ut quam neque, vulputate eu malesuada ut, pretium at tellus. Phasellus dapibus aliquet augue eu scelerisque. Vestibulum at orci sed enim eleifend egestas vitae at risus. Maecenas vel magna finibus, fringilla mi ut, pellentesque sapien. Nam tincidunt arcu at metus mattis, ut maximus leo ultricies. Fusce cursus orci id purus feugiat faucibus. Cras dapibus in erat ac dignissim.
        Suspendisse commodo convallis velit non consectetur. Integer est nibh, laoreet sed ullamcorper id, semper et magna. Aenean auctor rutrum rutrum. Mauris dictum mollis consectetur. Praesent nec tellus sed metus dignissim lobortis eu sit amet tellus. Ut lacinia dui vitae auctor iaculis. In quis eros a arcu sodales efficitur eget ac diam. Cras ut posuere odio. Aenean vel mattis neque, eu rhoncus odio.
        Cras et dui est. Mauris consectetur in turpis a mollis. Integer mattis fringilla gravida. Nulla in tempus purus. Maecenas pretium nibh nec eros auctor dignissim. Quisque elementum congue tristique. Phasellus luctus urna ac enim lobortis, euismod auctor elit aliquam. Phasellus sit amet velit neque. Praesent placerat sit amet sem ac iaculis. Maecenas facilisis, velit a iaculis efficitur, massa erat tristique elit, sed rutrum libero leo et nulla.
        Mauris sit amet lectus dapibus, maximus elit nec, cursus odio. Aenean volutpat erat a luctus facilisis. Nulla a dolor non diam euismod rutrum. Etiam in faucibus nibh. Nullam ac sem ut sapien vestibulum vulputate vitae non augue. Quisque sit amet rhoncus mi. Ut cursus lectus quis ex consectetur, eget viverra ante interdum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aliquam justo ipsum, convallis vitae dui id, tempor consectetur eros. Maecenas dapibus quam sed urna finibus, et semper dui mollis. Duis gravida nibh ut libero ultrices interdum. Etiam malesuada nisl ac volutpat scelerisque. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Vestibulum varius, orci a vulputate laoreet, diam augue viverra sem, et tempor enim velit at metus. Aenean rhoncus sollicitudin nulla sed tristique. Quisque sit amet mauris eget sapien eleifend maximus in sed ante.
        Maecenas condimentum blandit mi, non gravida sapien pellentesque eu. Proin molestie iaculis convallis. Vestibulum dui odio, gravida ac dui at, volutpat varius urna. Maecenas nec lorem neque. Nullam sit amet dolor et odio efficitur euismod quis in felis. Curabitur ut mauris eget quam auctor dapibus. Proin lacinia aliquam cursus. Nullam eu ultrices nulla, in luctus nulla. Integer eget ante mollis, dictum sapien lobortis, sodales mi. Donec ullamcorper mauris non blandit viverra. Duis sed aliquet nulla, non semper nibh. Sed in pharetra est, et commodo nisi. Donec ac nibh eget urna vestibulum finibus. Duis auctor scelerisque magna. Integer molestie quis justo vel blandit. Aliquam erat volutpat.
        Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Cras pulvinar, enim eget auctor convallis, est est viverra tellus, at pretium arcu tellus nec elit. In et dapibus odio. Vestibulum pretium arcu non est posuere, at interdum massa maximus. Cras eleifend ipsum mauris, eu sollicitudin ante porta ut. Vivamus ultricies, mi at euismod rutrum, neque nibh sagittis erat, sit amet blandit orci est eget elit. Vivamus vestibulum nulla eget semper ultricies. Curabitur et viverra nibh. Nam tempus magna sit amet fringilla luctus. Curabitur eget dapibus massa, eu fringilla ante. Quisque vel tincidunt nisi. Nulla cursus consequat diam, vitae venenatis velit scelerisque id.
        Duis orci sem, pellentesque vel rhoncus et, mollis ut ex. Nullam ut odio et lectus gravida condimentum. Aliquam cursus in nulla id cursus. Sed placerat ligula a dui luctus pulvinar. Sed vel tincidunt mi. Mauris id enim lorem. Etiam maximus arcu at nulla bibendum, pellentesque rhoncus arcu convallis. In tincidunt lacus et leo scelerisque, vitae faucibus augue iaculis. Nunc rutrum, purus eu suscipit tincidunt, mi dolor malesuada tortor, in dictum odio augue et neque. Fusce lacinia nibh nec lorem bibendum, non eleifend orci condimentum. Cras diam leo, fringilla sit amet feugiat nec, tempor eu justo.
        Vivamus interdum tortor vitae turpis ultrices vulputate. Curabitur ut erat lacus. Aliquam aliquam ex nulla, sit amet aliquet orci ultrices molestie. Aliquam pharetra tincidunt eros, eget commodo ipsum. Donec scelerisque odio neque, et pellentesque diam efficitur vel. Aenean sapien eros, lobortis sed sapien quis, vestibulum fringilla nulla. Donec a massa non diam porttitor volutpat non sed mauris. Nulla sagittis feugiat elementum. Mauris eu diam in massa interdum luctus. Nam iaculis metus quis dolor consequat, eu interdum odio auctor.
        Mauris augue velit, malesuada sit amet risus vel, luctus imperdiet leo. Nam eu molestie urna, in consequat leo. Mauris quis arcu vel metus tristique volutpat ac eu odio. Mauris at velit dapibus diam elementum efficitur. Integer arcu neque, consequat et quam ac, posuere molestie quam. Nunc placerat placerat eros, quis molestie justo commodo eu. Nunc quis cursus justo. Quisque lorem est, maximus eu risus a, porta cursus nibh. Sed eu tincidunt felis, ut dapibus diam. Suspendisse hendrerit dui eu pharetra feugiat. Suspendisse finibus ultrices magna vestibulum tincidunt. Etiam dictum eu lacus vel tincidunt. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam elementum mauris eget nibh pretium tristique. Ut ut finibus purus, sit amet aliquet lacus.
        Duis consequat velit ipsum, sed hendrerit libero cursus ac. Nunc convallis elit et pharetra porttitor. Phasellus in varius nisl, eget ultrices metus. Vivamus laoreet euismod nulla ac facilisis. In a eleifend dolor, ut euismod neque. Cras ac tristique metus. Phasellus vel mollis nibh. Vivamus a semper est. Nullam et fringilla orci. Nullam odio risus, aliquam sit amet est a, condimentum mollis nulla.
        Nullam ultricies malesuada dui in ullamcorper. Aliquam accumsan justo a erat pellentesque malesuada. Nunc id viverra ligula. Duis sit amet semper diam, vel sollicitudin nisi. Donec laoreet eleifend nisi id hendrerit. Aenean sit amet nibh bibendum, euismod tellus non, elementum nulla. Duis tempor urna magna, in condimentum elit efficitur nec. Vivamus at purus sit amet mauris porttitor auctor. Morbi sollicitudin nisl ut convallis malesuada. Duis gravida elit sem, non gravida est rutrum nec. Aliquam erat volutpat. Donec tempor, ligula eu elementum pretium, mi sem laoreet mi, vel finibus ipsum ipsum eget lectus. Etiam blandit velit ut nisl facilisis vestibulum. Vivamus eget mi dignissim purus eleifend volutpat condimentum eget risus. Praesent aliquam pellentesque augue, vel tincidunt metus blandit eu. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
                    """
        .stripIndent().replaceAll("[\n\r]", "");
  }
}
