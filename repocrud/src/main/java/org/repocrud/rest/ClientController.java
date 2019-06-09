package org.repocrud.rest;

import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.Client;
import org.repocrud.domain.Courier;
import org.repocrud.domain.GeoPosition;
import org.repocrud.domain.RequestRoute;
import org.repocrud.repository.ClientRepository;
import org.repocrud.repository.RequestRouteRepository;
import org.repocrud.rest.data.ClientRequest;
import org.repocrud.rest.data.ClientResponse;
import org.repocrud.rest.data.ClientStatusResponse;
import org.repocrud.rest.data.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Denis B. Kulikov<br/>
 * date: 08.06.2019:19:32<br/>
 */
@Slf4j
@RestController
public class ClientController {


    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RequestRouteRepository requestRouteRepository;


    /**
     * String clientId;
     * String clientName;
     * <p>
     * Position start;
     * Position end;
     */

    @ResponseBody
    @RequestMapping(value = "/request/{clientId}/{startLat}/{startLon}/{endLat}/{endLon}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE
    })
    @Transactional
    public ClientResponse request(@PathVariable String clientId,
                                  @PathVariable String startLat,
                                  @PathVariable String startLon,
                                  @PathVariable String endLat,
                                  @PathVariable String endLon) {
        log.info("Request {} ", clientId);


        Optional<Client> byId = Optional.ofNullable(clientRepository.findByClientId(clientId));

        final Client client = byId.orElseGet(() -> {log.info("Add new client {} ", clientId);
            Client newClient = new Client();
            newClient.setTitle("Auto");

            clientRepository.saveAndFlush(newClient);
            newClient.setClientId("Gen" + newClient.getId().toString());
            clientRepository.saveAndFlush(newClient);

            return newClient;
        });

        RequestRoute requestRoute = new RequestRoute();
        requestRoute.setClient(client);


        requestRoute.setStart(new GeoPosition(Double.parseDouble(startLat), Double.parseDouble(startLon), 0d));
        requestRoute.setEnd(new GeoPosition(Double.parseDouble(endLat), Double.parseDouble(endLon), 0d));

        requestRouteRepository.saveAndFlush(requestRoute);

        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setClientId(client.getClientId());
        clientResponse.setOrderId(Long.toString(requestRoute.getId()));

        return clientResponse;

    }


//    @ResponseBody
//    @RequestMapping(value = "/order/",
//            method = RequestMethod.POST,
//            produces = {MediaType.APPLICATION_JSON_VALUE})
//    @Transactional
//    public ClientResponse getOrder(@RequestBody ClientRequest clientRequest) {
//
//        Client client = clientRepository.findByClientId(clientRequest.getClientId());
//        if (client == null) {
//            client = new Client();
//            client.setTitle("Auto");
//            if (clientRequest.getClientId() == null) {
//                clientRepository.saveAndFlush(client);
//            }
//            client.setClientId("Auto " + client.getId());
//            clientRepository.saveAndFlush(client);
//        }
//
//        RequestRoute requestRoute = new RequestRoute();
//        requestRoute.setClient(client);
//
//        Position start = clientRequest.getStart();
//        Position end = clientRequest.getEnd();
//        requestRoute.setStart(new GeoPosition( start.getLatitude(), start.getLongitude(), start.getAltitude()));
//        requestRoute.setEnd(new GeoPosition(end.getLatitude(), end.getLongitude(), end.getAltitude()));
//
//        requestRouteRepository.saveAndFlush(requestRoute);
//
//        ClientResponse clientResponse = new ClientResponse();
//        clientResponse.setClientId(client.getClientId());
//        clientResponse.setOrderId(Long.toString(requestRoute.getId()));
//
//        return clientResponse;
//
//    }


    @Transactional
    @ResponseBody
    @RequestMapping(value = "/status/{orderId}", //
            method = RequestMethod.GET, //
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ClientStatusResponse getStatus(@PathVariable("orderId") String orderId) {


        Optional<RequestRoute> byId = requestRouteRepository.findById(Long.parseLong(orderId));

        if (byId.isPresent()) {
            RequestRoute requestRoute = byId.get();
            ClientStatusResponse clientStatusResponse = new ClientStatusResponse();
            clientStatusResponse.setClientId(requestRoute.getClient().getClientId());

            clientStatusResponse.setOrderId(orderId);
            clientStatusResponse.setStatus(requestRoute.getStatus());
            if (requestRoute.getCourier() != null) {


                Courier courier = requestRoute.getCourier();
                clientStatusResponse.setCurrent(new Position(courier.getLatitude(), courier.getLongitude(), 0d, courier.getTitle(), ""));
            }
            return clientStatusResponse;
        } else {
            throw new IllegalArgumentException("Not found");
        }
    }


}
