package org.repocrud.tools;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis B. Kulikov<br/>
 * date: 22.09.2018:0:16<br/>
 */
@Slf4j
public class JaxbTools {

    private static ConcurrentMap<Array, JAXBContext> contextConcurrentMap = new ConcurrentHashMap<>();

    private static ThreadLocal<ConcurrentMap<Array, Marshaller>> marshallerLocalMap = new ThreadLocal<>();


    public static String getBody(Object args, Array target) {

        try {
            String marshal = marshal(args, target);
            return marshal.length() > 1000 ? marshal.substring(0, 1000) : marshal;
        } catch (JAXBException e) {
            log.error("Invalid generate body " + e.getMessage() );
        }
        return "";
    }


    /**
     *
     *
     *
     * @param obj
     * @return
     * @throws JAXBException
     */
    public static String marshal(Object obj, Array type) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        Marshaller marshaller = getMarshaller(type);
        QName qName = new QName("history", "body");
        JAXBElement root = new JAXBElement(qName, obj.getClass(), obj);
        marshaller.marshal(root, stringWriter);

        return stringWriter.toString();

    }

    public static  Marshaller getMarshaller(Array type) {

        ConcurrentMap<Array, Marshaller> concurrentMap = marshallerLocalMap.get();
        if (concurrentMap == null) {
            concurrentMap = new ConcurrentHashMap<>();
            marshallerLocalMap.set(concurrentMap);
        }

        return concurrentMap.computeIfAbsent(type, requestType -> {
            System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
            JAXBContext jaxbContext = getJaxbContext(type);
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
                marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
                return marshaller;
            } catch (JAXBException e) {
                throw new IllegalStateException("Impossible init marshaller for " + type, e);
            }

        });


    }

    public static  JAXBContext getJaxbContext(Array type) {
        return contextConcurrentMap.computeIfAbsent(type, requestType -> {
            try {
                return JAXBContext.newInstance(type.getClasses());
            } catch (JAXBException e) {
                throw new IllegalStateException("Impossible init JAXBContext for " + type, e);
            }
        });
    }

    @Getter
    @RequiredArgsConstructor
    public static class Array {
        private final Class[] classes;

        public static Array of(Class... classes) {
            return new Array(classes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Array array = (Array) o;
            return Arrays.equals(classes, array.classes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(classes);
        }
    }
}
