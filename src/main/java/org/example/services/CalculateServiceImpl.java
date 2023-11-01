package org.example.services;

import org.example.dtos.CalculatedData;
import org.example.entities.Waypoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class CalculateServiceImpl implements CalculateService {
    @Override
    public CalculatedData gpxIntermediate(ArrayList<Waypoint> objectsM) {
        double finalElevation = objectsM.stream()
                .mapToDouble(Waypoint::elevation)
                .max()
                .orElse(Double.NaN)
                - objectsM.stream()
                .mapToDouble(Waypoint::elevation)
                .min()
                .orElse(Double.NaN);
        long finalTime = objectsM.stream()
                .map(Waypoint::time)
                .filter(Objects::nonNull)
                .map(Date::getTime)
                .max(Long::compareTo)
                .orElse(Long.MIN_VALUE)
                - objectsM.stream()
                .map(Waypoint::time)
                .filter(Objects::nonNull)
                .map(Date::getTime)
                .min(Long::compareTo)
                .orElse(Long.MAX_VALUE);

        double distance = objectsM.size() * 5;
        double speed;
        if (finalTime != 0) {
            speed = ((distance / (((double) finalTime / 1000))));
        } else {
            speed = 0.0;
        }
        System.out.println("------------------");
        System.out.println("The final elevation is : " + finalElevation);
        System.out.println("The final time is : " + finalTime);
        System.out.println("The final distance is  : " + distance);
        System.out.println("The final speed is : " + speed);
        System.out.println("-------------------");

        return new CalculatedData(distance, speed, finalElevation, finalTime);
    }

    @Override
    public ArrayList<Waypoint> readGPX(byte[] arrayGpx) throws Exception {
        ArrayList<Waypoint> waypoints = new ArrayList<>();

        //Convert the byte array into an input stream;
        InputStream inputStream = new ByteArrayInputStream(arrayGpx);

        //Parse the GPX input stream into a Document object
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);

        // Extract waypoint elements from the GPX file
        NodeList wptNodeList = doc.getElementsByTagName("wpt");
        for (int i = 0; i < wptNodeList.getLength(); i++) {
            Element wptElement = (Element) wptNodeList.item(i);
            double latitude = Double.parseDouble(wptElement.getAttribute("lat"));
            double longitude = Double.parseDouble(wptElement.getAttribute("lon"));
            double elevation = 0.0; // Default value if <ele> element is not present
            String timeStr = null; // Default value if <time> element is not present

            // Check if <ele> element is present
            Element eleElement = (Element) wptElement.getElementsByTagName("ele").item(0);
            if (eleElement != null) {
                elevation = Double.parseDouble(eleElement.getTextContent());
            }

            // Check if <time> element is present
            Element timeElement = (Element) wptElement.getElementsByTagName("time").item(0);
            if (timeElement != null) {
                timeStr = timeElement.getTextContent();
            }

            // Parse time string if present
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
            Date time = null;
            if (timeStr != null) {
                time = sdf.parse(timeStr);
            }

            // Create org.example.entities.Waypoint object and add to the list
            Waypoint waypoint = new Waypoint(latitude, longitude, elevation, time);
            waypoints.add(waypoint);
        }
        return waypoints;
    }
}