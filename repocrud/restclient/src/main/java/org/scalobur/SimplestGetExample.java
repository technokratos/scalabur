package org.scalobur;
 
import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteResponseType;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;

public class SimplestGetExample {


//    ../routing/7.2/getroute.{format}?routeId=<ROUTEID>&<parameter>=<value>...
    static final String URL_EMPLOYEES = "https://route.api.here.com/routing/7.2/calculateroute.xml?" +
            "app_id=5e1we9cQyN3Fw9DgKj0N" +
            "&app_code=ErXbohenf21dJzCSdDioMw" +

            "&waypoint0=geo!52.5,13.4" +
            "&waypoint1=geo!52.5,13.45" +
            "&mode=fastest;car;traffic:disabled";
 
    static final String URL_EMPLOYEES_XML = "http://localhost:8080/employees.xml";
    static final String URL_EMPLOYEES_JSON = "http://localhost:8080/employees.json";
 
    public static void main(String[] args) throws JAXBException {


        CalculateRouteResponseType responseType = new CalculateRouteResponseType();
        responseType.setLanguage("any");
        CalculateRoute calculateRoute = new CalculateRoute();
        String xml = marshalToXmlString(CalculateRoute.class, CalculateRoute.class.getSimpleName(), "", calculateRoute);



        RestTemplate restTemplate = new RestTemplate();
 
        // Send request with GET method and default Headers.
        String result = restTemplate.getForObject(URL_EMPLOYEES, String.class);


        System.out.println(result);
        CalculateRoute res = unmarshalXmlString(CalculateRoute .class, result);
        System.out.println(result);
    }

    static <T> T unmarshalXmlString(Class<T> cl, String xml) throws JAXBException {
        return unmarshalXml(cl, new StringReader(xml));
    }

    static <T> T unmarshalXml(Class<T> cl, Reader reader) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(cl);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(reader);
        return (T) JAXBIntrospector.getValue(obj);
    }

    static <T> String marshalToXmlString(Class<T> cl, String name, String namespace, T value) throws JAXBException {
        JAXBContext jcav = JAXBContext.newInstance(cl);
        Marshaller marsh = jcav.createMarshaller();
        marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marsh.setProperty(Marshaller.JAXB_FRAGMENT, true);
        QName qName = new QName(namespace, name);
        StringWriter strWri = new StringWriter();
        JAXBElement<?> root;
        root = new JAXBElement<T>(qName, cl, value);
        marsh.marshal(root, strWri);
        return strWri.toString();
    }

}