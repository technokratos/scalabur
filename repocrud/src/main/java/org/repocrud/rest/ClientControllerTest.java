package org.repocrud.rest;

import org.repocrud.rest.data.ClientRequest;
import org.repocrud.rest.data.ClientResponse;
import org.repocrud.rest.data.Position;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:20:51<br/>
 */
public class ClientControllerTest {


    public static void main(String[] args) {

        RestTemplate restTemplate = new RestTemplate();

        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setClientId("21341");
        clientRequest.setClientName("awet");
        clientRequest.setStart(new Position(0d, 0d,0d, null, null));
        clientRequest.setEnd(new Position(0d, 0d,0d,null, null));
        // Send request with GET method and default Headers.
        ResponseEntity<ClientResponse> clientResponse = restTemplate.postForEntity("https://localhost:8443/order/", clientRequest, ClientResponse.class);

        System.out.println(clientRequest);
    }
}