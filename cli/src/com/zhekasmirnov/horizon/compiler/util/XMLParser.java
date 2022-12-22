package com.zhekasmirnov.horizon.compiler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLParser {
    @SuppressWarnings("unused")
    private static final String TAG = "XMLParser";

    public String getXmlFromFile(String str) {
        File file = new File(str);
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    sb.append(readLine);
                    sb.append("\n");
                } else {
                    bufferedReader.close();
                    return sb.toString();
                }
            }
        } catch (IOException e) {
            System.err.println("XMLParser getRepoXmlFromFile() IO error " + e);
            return null;
        }
    }

    public Document getDomElement(String str) {
        try {
            DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(str));
            return newDocumentBuilder.parse(inputSource);
        } catch (IOException e) {
            System.err.println("XMLParser Error: " + e.getMessage());
            return null;
        } catch (ParserConfigurationException e2) {
            PrintStream printStream2 = System.err;
            printStream2.println("XMLParser Error: " + e2.getMessage());
            return null;
        } catch (SAXException e3) {
            System.err.println("XMLParser Error: " + e3.getMessage());
            return null;
        }
    }

    public final String getElementValue(Node node) {
        if (node == null || !node.hasChildNodes()) {
            return "";
        }
        Node firstChild = node.getFirstChild();
        while (true) {
            Node firstChild2 = firstChild;
            if (firstChild2 != null) {
                if (firstChild2.getNodeType() != 3) {
                    firstChild = firstChild2.getNextSibling();
                } else {
                    return firstChild2.getNodeValue();
                }
            } else {
                return "";
            }
        }
    }

    public String getValue(Element element, String str) {
        return getElementValue(element.getElementsByTagName(str).item(0));
    }
}
