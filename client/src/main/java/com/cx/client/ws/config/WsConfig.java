package com.cx.client.ws.config;


import com.cx.client.ws.client.CountryClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.wss4j.common.WSS4JConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ResourceUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class WsConfig {

    public static final String KEYSTORE_LOCATION = "classpath:client-keystore.jks";
    public static final Resource KEYSTORE_LOCATION_RESOURCE = new ClassPathResource("client-keystore.jks");
    public static final String KEYSTORE_PASSWORD = "changeit";
    public static final String TRUSTSTORE_LOCATION = "classpath:client-truststore.jks";
    public static final String TRUSTSTORE_PASSWORD = "changeit";
    public static final String KEY_ALIAS = "client-alias";

    // @Bean
    Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.cx.client.ws.dto.countries");
        return marshaller;
    }

  /*  @Bean
    KeyStoreFactoryBean keyStore() {
        KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();
        factoryBean.setLocation(TRUSTSTORE_LOCATION);
        factoryBean.setPassword(TRUSTSTORE_PASSWORD);
        return factoryBean;
    }

    @Bean
    TrustManagersFactoryBean trustManagers(KeyStoreFactoryBean keyStore) throws NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        TrustManagersFactoryBean factoryBean = new TrustManagersFactoryBean();
        factoryBean.setKeyStore(keyStore.createKeyStore());
        return factoryBean;
    }

    @Bean
    HttpsUrlConnectionMessageSender messageSender(
            KeyStoreFactoryBean keyStore,
            TrustManagersFactoryBean trustManagers
    ) throws Exception {
        HttpsUrlConnectionMessageSender sender = new HttpsUrlConnectionMessageSender();

        KeyManagersFactoryBean keyManagersFactoryBean = new KeyManagersFactoryBean();
        keyManagersFactoryBean.setKeyStore(keyStore.createKeyStore());
        keyManagersFactoryBean.setPassword(TRUSTSTORE_PASSWORD);
        keyManagersFactoryBean.afterPropertiesSet();

        sender.setKeyManagers(keyManagersFactoryBean.getObject());

        sender.setTrustManagers(trustManagers.getObject());
        return sender;
    }*/

    // -- client signature

    @Bean
    CryptoFactoryBean cryptoFactoryBean() throws IOException {
        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
        cryptoFactoryBean.setKeyStoreLocation(KEYSTORE_LOCATION_RESOURCE);
        cryptoFactoryBean.setKeyStorePassword(KEYSTORE_PASSWORD);
        return cryptoFactoryBean;
    }

    @Bean
    Wss4jSecurityInterceptor securityInterceptor(CryptoFactoryBean cryptoFactoryBean) throws Exception {
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

        securityInterceptor.setSecurementActions("Signature");
        securityInterceptor.setSecurementUsername(KEY_ALIAS);
        securityInterceptor.setSecurementPassword(KEYSTORE_PASSWORD);

        securityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
        securityInterceptor.setSecurementSignatureAlgorithm(WSS4JConstants.RSA_SHA1);
        securityInterceptor.setSecurementSignatureDigestAlgorithm(WSS4JConstants.SHA1);

        securityInterceptor.setSecurementSignatureCrypto(cryptoFactoryBean.getObject());

        return securityInterceptor;
    }


    public WebServiceTemplate webServiceTemplate(Wss4jSecurityInterceptor securityInterceptor) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMessageSender(messageSender());
        webServiceTemplate.setInterceptors(new ClientInterceptor[]{securityInterceptor});
        webServiceTemplate.setMarshaller(marshaller());
        webServiceTemplate.setUnmarshaller(marshaller());

        return webServiceTemplate;
    }

    public WebServiceMessageSender messageSender() {
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender(httpClient());
        return messageSender;
    }

    public HttpClient httpClient() {
        return HttpClients.custom()
                .setSSLContext(getSslContext())
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .build();
    }


    @Bean
    CountryClient countryClient(
            // Jaxb2Marshaller marshaller,
            // HttpsUrlConnectionMessageSender messageSender,
            Wss4jSecurityInterceptor securityInterceptor
    ) {
        CountryClient countryClient = new CountryClient();

/*        countryClient.setInterceptors(new ClientInterceptor[]{securityInterceptor});
        countryClient.setMessageSender(messageSender);

        countryClient.setMarshaller(marshaller);
        countryClient.setUnmarshaller(marshaller);*/

        countryClient.setWebServiceTemplate(webServiceTemplate(securityInterceptor));
        return countryClient;
    }


    private SSLContext getSslContext() {
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
                    .loadTrustMaterial(ResourceUtils.getFile(WsConfig.TRUSTSTORE_LOCATION), WsConfig.TRUSTSTORE_PASSWORD.toCharArray(), acceptingTrustStrategy)
                    .build();
            return sslContext;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


}