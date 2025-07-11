#!/bin/sh
rm ../src/test/resources/communication-test-keystore.p12
rm ../src/test/resources/integration-source-test-keystore.p12
rm ../src/test/resources/integration-destination-test-keystore.p12
rm ../src/test/resources/integration-source-proxy-test-keystore.p12
rm ../src/test/resources/integration-destination-proxy-test-keystore.p12
rm ../src/test/resources/integration-ano-test-keystore.p12
rm ../src/test/resources/communication-test-keystore.crt
rm ../src/test/resources/integration-source-test-keystore.crt
rm ../src/test/resources/integration-destination-test-keystore.crt
rm ../src/test/resources/integration-source-proxy-test-keystore.crt
rm ../src/test/resources/integration-destination-proxy-test-keystore.crt
rm ../src/test/resources/integration-ano-test-keystore.crt
rm ../src/test/resources/truststore.p12
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/communication-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=ca.griis.changeit, O=GRIIS, C=CA"
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/integration-source-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=source.ca, O=GRIIS, C=CA"
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/integration-destination-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=*.destination.ca, O=GRIIS, C=CA"
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/integration-source-proxy-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=source.proxy.ca, O=GRIIS, C=CA"
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/integration-destination-proxy-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=destination.proxy.ca, O=GRIIS, C=CA"
keytool -genkeypair -alias changeit -keyalg EC -keysize 256 -sigalg SHA256withECDSA -storetype PKCS12 -keystore ../src/test/resources/integration-ano-test-keystore.p12 -validity 3650 -storepass changeit -dname "CN=ano.ca, O=GRIIS, C=CA"

keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/communication-test-keystore.crt -keystore ../src/test/resources/communication-test-keystore.p12 -storepass changeit -storetype PKCS12

keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/communication-test-keystore.crt -keystore ../src/test/resources/communication-test-keystore.p12 -storepass changeit -storetype PKCS12
keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/integration-source-test-keystore.crt -keystore ../src/test/resources/integration-source-test-keystore.p12 -storepass changeit -storetype PKCS12
keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/integration-destination-test-keystore.crt -keystore ../src/test/resources/integration-destination-test-keystore.p12 -storepass changeit -storetype PKCS12
keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/integration-source-proxy-test-keystore.crt -keystore ../src/test/resources/integration-source-proxy-test-keystore.p12 -storepass changeit -storetype PKCS12
keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/integration-destination-proxy-test-keystore.crt -keystore ../src/test/resources/integration-destination-proxy-test-keystore.p12 -storepass changeit -storetype PKCS12
keytool -exportcert -noprompt -rfc -alias changeit -file ../src/test/resources/integration-ano-test-keystore.crt -keystore ../src/test/resources/integration-ano-test-keystore.p12 -storepass changeit -storetype PKCS12

keytool -importkeystore -noprompt -srckeystore ../src/test/resources/cacerts -destkeystore ../src/test/resources/truststore.p12 -deststoretype PKCS12 -srcstorepass changeit -deststorepass changeit
keytool -importcert -trustcacerts -noprompt -alias communication-test-keystore -file ../src/test/resources/communication-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
keytool -importcert -trustcacerts -noprompt -alias integration-source-test-keystore -file ../src/test/resources/integration-source-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
keytool -importcert -trustcacerts -noprompt -alias integration-destination-test-keystore -file ../src/test/resources/integration-destination-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
keytool -importcert -trustcacerts -noprompt -alias integration-source-proxy-test-keystore -file ../src/test/resources/integration-source-proxy-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
keytool -importcert -trustcacerts -noprompt -alias integration-destination-proxy-test-keystore -file ../src/test/resources/integration-destination-proxy-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
keytool -importcert -trustcacerts -noprompt -alias integration-ano-test-keystore -file ../src/test/resources/integration-ano-test-keystore.crt -keypass changeit -keystore ../src/test/resources/truststore.p12 -storepass changeit -storetype PKCS12
