package com.naixue.nxdp.attachment.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class XmlUtils {

    public static <T> String toXml(T object) {
        return toXml(object, false);
    }

    public static <T> String toXml(T object, boolean removeXmlHead) {
        try {
            Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, removeXmlHead);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(object, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toBean(String xml, Class<T> clazz) {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
            return (T) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
}
