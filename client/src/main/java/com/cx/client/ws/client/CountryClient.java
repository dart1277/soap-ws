package com.cx.client.ws.client;


import com.cx.client.ws.dto.countries.GetCountryRequest;
import com.cx.client.ws.dto.countries.GetCountryResponse;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.transform.TransformerException;
import java.io.IOException;

public class CountryClient extends WebServiceGatewaySupport {

    public GetCountryResponse getCountry(String country) {
        GetCountryRequest request = new GetCountryRequest();
        request.setName(country);

        return (GetCountryResponse) getWebServiceTemplate()
                .marshalSendAndReceive("https://localhost:8011/ws/countries", request, new WebServiceMessageCallback() {
                    @Override
                    public void doWithMessage(WebServiceMessage webServiceMessage) throws IOException, TransformerException {

                    }
                });
    }

}