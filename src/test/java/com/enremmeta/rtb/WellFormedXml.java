package com.enremmeta.rtb;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class WellFormedXml {
    public static Document getXmlDoc(String xmlString) throws Exception {
        InputSource xmlSource = new InputSource(new ByteArrayInputStream(xmlString.getBytes()));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlSource);

        return doc;
    }

    public static boolean validate(String xmlString) {
        try {
            getXmlDoc(xmlString);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        String testString = "<h1>Admin</h1>";

        String xmlString = String.format("<xmlRoot>%1$s</xmlRoot>", testString);
        
        if (validate(xmlString)) {
            System.out.println("String is well-formed xml");
        }
    }
}
