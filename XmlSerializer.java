package br.com.rponte.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;

public class XmlSerializer {
	
    private final String encoding;
	
    public XmlSerializer() {
        this("UTF-8");
    }
	
    public XmlSerializer(String encoding) {
        this.encoding = encoding;
    }

	/**
     * Serializes an Object to XML
     */
    public String toXml(Object o) {
        try {
            JAXBContext context = JAXBContext.newInstance(o.getClass());
            
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, this.encoding);
            
            StringWriter writer = new StringWriter();
            marshaller.marshal(o, writer);
            return writer.toString();
            
        } catch(Exception e) {
            throw new IllegalStateException("Error while serializing an Object to XML", e);
        }
    }
    
    /**
     * Deserializes a XML text to Object
     */
    public <T> T fromXml(String xml, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setAdapter(new NormalizedStringAdapter());
            
            Object o = unmarshaller.unmarshal(new StringReader(xml));
            return clazz.cast(o);
        } catch(Exception e) {
            throw new IllegalStateException("Error while deserializing a XML text to Object of type " + clazz, e);
        }
    }

    /**
     * Deserializes a XML file to Object
     */
    public <T> T fromXml(File xml, Class<T> clazz) {
    	try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setAdapter(new NormalizedStringAdapter());
            
            try (Reader reader = new InputStreamReader(new FileInputStream(xml), this.encoding);) {
            	Object o = unmarshaller.unmarshal(reader);
            	return clazz.cast(o);
            }
            
        } catch(Exception e) {
            throw new IllegalStateException("Error while deserializing a XML file to Object of type " + clazz, e);
        }
    }
    
}