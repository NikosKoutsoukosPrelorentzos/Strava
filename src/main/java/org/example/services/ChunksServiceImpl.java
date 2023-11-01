package org.example.services;

import org.example.dtos.ClientToServers;
import org.example.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChunksServiceImpl implements ChunksService {
    private static final String element = "wpt";

    @Override
    public List<byte[]> createChunks(ClientToServers clientToServers) throws Exception {
        byte[] xmlByteArray = Utils.gpxArray(clientToServers.file());
        int n = clientToServers.size();

        // Parse the XML data into a DOM document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlByteArray));

        // Get a list of all elements with the given tag name
        NodeList elementList = document.getElementsByTagName(element);

        // Calculate the chunk size based on the number of elements and desired number of chunks
        int numElements = elementList.getLength();
        int chunkSize = numElements / n;
        int remainder = numElements % n;

        // Create an ArrayList to hold the chunk byte arrays
        List<byte[]> chunkList = new ArrayList<>();

        // Loop through the elements and split them into chunks
        int start = 0;
        for (int i = 0; i < n; i++) {
            int end = start + chunkSize + (i < remainder ? 1 : 0);
            Element chunkRootElement = document.createElement("root");
            for (int j = start; j < end; j++) {
                Element element = (Element) elementList.item(j);
                Element importedElement = (Element) chunkRootElement.getOwnerDocument().importNode(element, true);
                chunkRootElement.appendChild(importedElement);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(chunkRootElement), new StreamResult(outputStream));
            byte[] chunkByteArray = outputStream.toByteArray();
            chunkList.add(chunkByteArray);
            start = end;
        }
        return chunkList;
    }
}