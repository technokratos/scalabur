package org.scalobur;
 
import org.springframework.web.client.RestTemplate;
 
public class SimplestGetExample {


    /**
     *
     * APP ID
     * 5e1we9cQyN3Fw9DgKj0N
     * APP CODE
     * ErXbohenf21dJzCSdDioMw
     *
     * app_id
     * A 20 bytes Base64 URL-safe encoded string used for the authentication of the client application.
     *
     * You must include an app_id with every request. To get an app_id assigned to you, please see Acquiring Credentials.
     *
     * app_code
     * A 20 bytes Base64 URL-safe encoded string used for the authentication of the client application.
     *
     * Y
     */
//    ../routing/7.2/getroute.{format}?routeId=<ROUTEID>&<parameter>=<value>...
    static final String URL_EMPLOYEES = "https://route.api.here.com/routing/7.2/getroute.json?app_id=5e1we9cQyN3Fw9DgKj0N&app_code=ErXbohenf21dJzCSdDioMw";
 
    static final String URL_EMPLOYEES_XML = "http://localhost:8080/employees.xml";
    static final String URL_EMPLOYEES_JSON = "http://localhost:8080/employees.json";
 
    public static void main(String[] args) {
 
        RestTemplate restTemplate = new RestTemplate();
 
        // Send request with GET method and default Headers.
        String result = restTemplate.getForObject(URL_EMPLOYEES, String.class);
 
        System.out.println(result);
    }
 
}