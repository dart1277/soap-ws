package com.cx.client.ws.controller.feign;

import com.cx.client.ws.config.WsConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration // Don't use @Configuration if per client feign config is required, @Configuration is a app global setting !!
public class SslFeignConfig {

    private SslFeignClient sslFeignClient;

    @Bean
    public SslFeignClient sslFeignClient() {
        return this.sslFeignClient;
    }

    //@Bean
    public Decoder feignDecoder() {
        HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());
        return new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(jacksonConverter)));
    }

    //@Bean
    public Encoder feignEncoder() {
        HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());
        return new SpringEncoder(() -> new HttpMessageConverters(jacksonConverter));
    }

    @PostConstruct
    public void init() {
        this.sslFeignClient = Feign.builder().client(feignClient())
                .encoder(feignEncoder())
                .contract(new SpringMvcContract())
                .logLevel(Logger.Level.FULL) // can add this as a bean in annotation based FeignClient config
                .errorDecoder(new FeignErrorDecoder()) // can add this as a bean in annotation based FeignClient config
                .decoder(feignDecoder())
                //.requestInterceptor(new BasicAuthRequestInterceptor("user", "user"))
                .target(SslFeignClient.class, "https://localhost:8011");
    }

/*
        ;*/

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME)); // DateTimeFormatter.ofPattern();
        objectMapper.registerModule(module);

        return objectMapper;
    }

    //@Bean
    public Client feignClient() {
        Client trustSSLSockets = new Client.Default(getSSLSocketFactory(), new NoopHostnameVerifier()); // HttpsURLConnection.getDefaultHostNameVerifier()
        return trustSSLSockets;
    }

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return false;
                }
            };
            SSLContext sslContext = SSLContextBuilder
                    .create()
                    // .loadKeyMaterial(ResourceUtils.getFile("classpath:keystore.p12"), allPassword.toCharArray(), allPassword.toCharArray())
                    .loadKeyMaterial(ResourceUtils.getFile(WsConfig.KEYSTORE_LOCATION), WsConfig.KEYSTORE_PASSWORD.toCharArray(), WsConfig.KEYSTORE_PASSWORD.toCharArray())
                    .loadTrustMaterial(ResourceUtils.getFile(WsConfig.TRUSTSTORE_LOCATION),
                            WsConfig.TRUSTSTORE_PASSWORD
                                    .toCharArray(),
                            acceptingTrustStrategy)
                    .build();
            return sslContext.getSocketFactory();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}

class FeignErrorDecoder implements ErrorDecoder {
    // LoggerFactory.getLogger(this.getClass());

    @Override
    public Exception decode(String methodKey, Response response) {


        switch (response.status()) {
            case 400:
                //  logger.error("Status code " + response.status() + ", methodKey = " + methodKey);
            case 404: {
                // logger.error("Error took place when using Feign client to send HTTP Request. Status code " + response.status() + ", methodKey = " + methodKey);
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), "<You can add error message description here>");
            }
            default:
                return new Exception(response.reason());
        }
    }

}

/*
* Java Keytool Commands for Creating and Importing

These commands allow you to generate a new Java Keytool keystore file, create a CSR, and import certificates. Any root or intermediate certificates will need to be imported before importing the primary certificate for your domain.

    Import *.pfx / *.p12 keystore (use firefox cert import / backup to fix line endings on linx, *.p12 and *.jks keystore passwords must match !!)

    keytool -v -importkeystore -srckeystore a.p12 -srcstoretype PKCS12 -destkeystore b.jks -deststoretype JKS

    Generate a Java keystore and key pair

    keytool -genkey -alias mydomain -keyalg RSA -keystore keystore.jks  -keysize 2048

    Generate a certificate signing request (CSR) for an existing Java keystore

    keytool -certreq -alias mydomain -keystore keystore.jks -file mydomain.csr

    Import a root or intermediate CA certificate to an existing Java keystore

    keytool -import -trustcacerts -alias root -file Thawte.crt -keystore keystore.jks

    Import a signed primary certificate to an existing Java keystore

    keytool -import -trustcacerts -alias mydomain -file mydomain.crt -keystore keystore.jks # can import *.crt certificate this way

    Generate a keystore and self-signed certificate (see How to Create a Self Signed Certificate using Java Keytoolfor more info)

    keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 360 -keysize 2048

Java Keytool Commands for Checking

If you need to check the information within a certificate, or Java keystore, use these commands.

    Check a stand-alone certificate

    keytool -printcert -v -file mydomain.crt

    Check which certificates are in a Java keystore

    keytool -list -v -keystore keystore.jks

    Check a particular keystore entry using an alias

    keytool -list -v -keystore keystore.jks -alias mydomain

Other Java Keytool Commands

    Delete a certificate from a Java Keytool keystore

    keytool -delete -alias mydomain -keystore keystore.jks

    Change a Java keystore password

    keytool -storepasswd -new new_storepass -keystore keystore.jks

    Export a certificate from a keystore

    keytool -export -alias mydomain -file mydomain.crt -keystore keystore.jks

    List Trusted CA Certs

    keytool -list -v -keystore $JAVA_HOME/jre/lib/security/cacerts

    Import New CA into Trusted Certs
    * keytool -import -trustcacerts -file /path/to/ca/ca.pem -alias CA_ALIAS -keystore $JAVA_HOME/jre/lib/security/cacerts 
    
    
* */
