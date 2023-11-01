package org.example.runnables;

import org.example.dtos.ClientToServers;
import org.example.dtos.CalculatedData;
import org.example.dtos.OutServerToClient;
import org.example.dtos.WorkerToOutServer;
import org.example.utils.Utils;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

public class Client {
    public static void main(String[] args) throws Exception {
        String filepath = "src/main/java/resources/route6.gpx";

        File file = new File(filepath);
        ClientToServers clientToServers = new ClientToServers(UUID.randomUUID(), file, 5);

        Socket outSocket = new Socket("localhost", 1234, InetAddress.getLocalHost(), Utils.getRandomPort());
        Socket inSocket = new Socket("localhost", 1235, InetAddress.getLocalHost(), Utils.getRandomPort());

        ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(inSocket.getOutputStream());
        objectOutputStream1.writeObject(clientToServers);
        objectOutputStream1.flush();

        ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(outSocket.getOutputStream());
        objectOutputStream2.writeObject(clientToServers);
        objectOutputStream2.flush();

        while (inSocket.isConnected()) {
            System.out.println("Waiting...");
            ObjectInputStream objectInputStream = new ObjectInputStream(inSocket.getInputStream());
            OutServerToClient outData = (OutServerToClient) objectInputStream.readObject();
            CalculatedData results = outData.gpx();
            System.out.println(results);
        }
        inSocket.close();
        outSocket.close();
    }
}