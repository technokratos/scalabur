package org.scalobur;

import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteRequestMetaInfoType;
import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteRequestType;
import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteResponseType;
import com.navteq.lbsp.routing_calculateroute._4.CalculateRouteType;
import com.navteq.lbsp.routing_common._4.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class SimplestGetExample {


//    ../routing/7.2/getroute.{format}?routeId=<ROUTEID>&<parameter>=<value>...
    static final String URL_EMPLOYEES = "https://route.api.here.com/routing/7.2/calculateroute.xml?" +
            "app_id=5e1we9cQyN3Fw9DgKj0N" +
            "&app_code=ErXbohenf21dJzCSdDioMw" +
            "&waypoint0=geo!52.5,13.4" +
            "&waypoint1=geo!52.5,13.45" +
            "&mode=fastest;car;traffic:disabled" +
        "&routeAttributes=routeId";
 
    static final String URL_EMPLOYEES_XML = "http://localhost:8080/employees.xml";
    static final String URL_EMPLOYEES_JSON = "http://localhost:8080/employees.json";
 
    public static void main(String[] args) throws JAXBException {


        CalculateRouteResponseType responseType = new CalculateRouteResponseType();
        RouteType e = new RouteType();
        e.setRouteId("12123");
        responseType.getRoute().add(e);




        RestTemplate restTemplate = new RestTemplate();
 
        // Send request with GET method and default Headers.
        String result = restTemplate.getForObject(URL_EMPLOYEES, String.class);


        String withoutRtcr = result.replace("<rtcr:CalculateRoute xmlns:rtcr=\"http://www.navteq.com/lbsp/Routing-CalculateRoute/4\">", "").replace("</rtcr:CalculateRoute>", "");
        System.out.println(result);
        CalculateRoute res = unmarshalCalculateXml(new StringReader(result));

        System.out.println(result);
    }

    static <T> T unmarshalXmlString(Class<T> cl, String xml) throws JAXBException {
        return unmarshalCalculateXml( new StringReader(xml));
    }

    static <T> T unmarshalCalculateXml(Reader reader) throws JAXBException {
        JAXBContext jaxbContext = getCalculateJaxb();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(reader);
        return (T) JAXBIntrospector.getValue(obj);
    }

    private static <T> JAXBContext getCalculateJaxb() throws JAXBException {
        return JAXBContext.newInstance(
                //todo
//                Response.class,
                CalculateRoute.class,
                CalculateRouteRequestMetaInfoType.class,
                CalculateRouteRequestType.class,
                CalculateRouteResponseType.class,
                CalculateRouteType.class,
                ConditionalTruckRestrictionType.class,
                ConsumptionModelOptionsType.class,
                CorridorAreaType.class,
        CorridorRepresentationOptionsType.class,
                CorridorToleranceLevelType.class,
                CountryChangeDetailsType.class,
                DirectionType.class,
                DynamicSpeedInfoType.class,
                EngineType.class,
                ExternalResourceType.class,
                FerryType.class,
                GeneralizationType.class,
                GeoWaypointParameterType.class,
                HazardousGoodTypeType.class,
                IncidentType.class,
                IncidentTypeType.class,
                InstructionFormatType.class,
                IntersectionIconType.class,
                IntersectionLegRoleType.class,
                IntersectionLegType.class,
                IntersectionTypeType.class,
                LicensePlateSegmentDataTypeType.class,
                LicensePlateSegmentType.class,
                LicensePlateType.class,
                LineStyleType.class,
                LinkPositionType.class,
                ManeuverAttributeType.class,
                ManeuverGroupType.class,
                ManeuverType.class,
                NavigationFlagType.class,
                NavigationModeType.class,
                NavigationWaypointParameterType.class,
                ObjectFactory.class,
                PlaceEquipmentType.class,
                PrivateTransportActionType.class,
                PrivateTransportLinkType.class,
                PrivateTransportManeuverType.class,
                PublicTransportActionType.class,
                PublicTransportFlagType.class,
                PublicTransportLineAttributeType.class,
                PublicTransportLineType.class,
                PublicTransportLinkFlagType.class,
                PublicTransportLinkType.class,
                PublicTransportManeuverType.class,
                PublicTransportProfileType.class,
                PublicTransportRouteSummaryType.class,
                PublicTransportStopType.class,
                PublicTransportTicketsType.class,
                PublicTransportTicketType.class,
                PublicTransportTypeType.class,
                PublicTransportWaypointParameterType.class,
                ResourceTypeType.class,
                RoadShieldType.class,
                RouteAttributeType.class,
                RouteFeatureType.class,
                RouteLaneType.class,
                RouteLegAttributeType.class,
                RouteLegType.class,
                RouteLinkAttributeType.class,
                RouteLinkFlagType.class,
                RouteLinkType.class,
                RouteNoteCodeType.class,
                RouteNoteType.class,
                RouteNoteTypeType.class,
                RouteQualifierType.class,
                RouteRepresentationModeType.class,
                RouteRepresentationOptionsType.class,
                RouteRequestMetaInfoType.class,
                RouteResponseMetaInfoType.class,
                RouteShapeReferenceType.class,
                RouteSummaryByCountryType.class,
                RouteSummaryEntryType.class,
                RouteSummaryType.class,
                RouteType.class,
                RoutingModeType.class,
                RoutingTypeType.class,
                RoutingZoneType.class,
                RoutingZoneTypeType.class,
                ShapeQualityType.class,
                SourceAttributionType.class,
                SourceSupplierNoteType.class,
                SourceSupplierNoteTypeType.class,
                SourceSupplierType.class,
                SpeedProfileType.class,
                StreetPositionType.class,
                TimeDependentRestrictionType.class,
                TimeDependentRestrictionTypeType.class,
                TrafficDirectionType.class,
                TrafficModeType.class,
                TransportModeType.class,
                TravelProgressType.class,
                TruckProfileType.class,
                TruckRestrictionConditionType.class,
                TruckRestrictionPenaltyType.class,
                TruckRestrictionsType.class,
                TruckTypeType.class,
                TurnType.class,
                VehicleRestrictionType.class,
                VehicleType.class,
                WaypointParameterEntityTypeType.class,
                WaypointParameterSourceType.class,
                WaypointParameterType.class,
                WaypointParameterTypeType.class,
                WaypointType.class,
                WeightedRouteFeatureType.class);
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

    public static <T> T unmarshal(String xml, Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance();
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(xml);
        return (T) jaxbUnmarshaller.unmarshal(reader);
    }

}