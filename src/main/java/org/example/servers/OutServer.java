package org.example.servers;

import org.example.dtos.CalculatedData;
import org.example.dtos.ClientToServers;
import org.example.dtos.OutServerToClient;
import org.example.dtos.WorkerToOutServer;
import org.example.repositories.UsersStatistics;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class OutServer {
    private final ServerSocket serverSocket;
    private final Map<UUID, Socket> clients;
    private final Map<UUID, Integer> clientNumberOfChunks;
    private final Map<UUID, ArrayList<WorkerToOutServer>> clientReturnData;
    private final ArrayList<Integer> workersPorts;
    private final UsersStatistics usersStatistics;

    public OutServer(ServerSocket serverSocket, ArrayList<Integer> workersPorts) {
        this.serverSocket = serverSocket;
        this.workersPorts = workersPorts;
        this.clients = new HashMap<>();
        this.clientNumberOfChunks = new HashMap<>();
        this.clientReturnData = new HashMap<>();
        this.usersStatistics = new UsersStatistics();
    }

    public void startServer() {
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    if (workersPorts.contains(socket.getPort())) {
                        workerHandler(socket);
                    } else {
                        clientHandler(socket);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void clientHandler(Socket client) {
        new Thread(() -> {
            try {
                System.out.println("Client with port: " + client.getPort() + " connected to OutServer");
                ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                ClientToServers clientToServers = (ClientToServers) objectInputStream.readObject();
                System.out.println("Incoming data came to OutServer from client: ");
                System.out.println(clientToServers.uuid() + "Client Handler");
                initiateHashMaps(clientToServers.uuid(), client);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void initiateHashMaps(UUID uuid, Socket client) {
        clients.put(uuid, client);
        clientReturnData.put(uuid, new ArrayList<>());
        clientNumberOfChunks.put(uuid, 0);
        usersStatistics.getAverageDistance().put(uuid, 0.0);
        usersStatistics.getAverageTime().put(uuid, 0.0);
        usersStatistics.getAverageSpeed().put(uuid, 0.0);
    }

    private void workerHandler(Socket worker) {
        new Thread(() -> {
            try {
                System.out.println("Worker with port: " + worker.getPort() + " connected to OutServer");
                while (worker.isConnected()) {
                    ObjectInputStream objectInputStream = new ObjectInputStream(worker.getInputStream());
                    WorkerToOutServer workerToOutServer = (WorkerToOutServer) objectInputStream.readObject();
                    handleData(workerToOutServer);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private synchronized void handleData(WorkerToOutServer workerToOutServer) {
        if (clientNumberOfChunks.get(workerToOutServer.uuid()) <= workerToOutServer.size()) {
            System.out.println("Got Inside");
            updateChunksSize(workerToOutServer);
            updateData(workerToOutServer);
            if (Objects.equals(clientNumberOfChunks.get(workerToOutServer.uuid()), workerToOutServer.size())) {
                ArrayList<WorkerToOutServer> dataToCalculate = clientReturnData.get(workerToOutServer.uuid());
                sendData(workerToOutServer.uuid(), calculateFinalData(dataToCalculate));
                clearEverything(workerToOutServer);
            }
        }
    }

    private void updateChunksSize(WorkerToOutServer workerToOutServer) {
        int oldSize = clientNumberOfChunks.get(workerToOutServer.uuid());
        clientNumberOfChunks.replace(workerToOutServer.uuid(), oldSize, oldSize + 1);
    }

    private void updateData(WorkerToOutServer workerToOutServer) {
        ArrayList<WorkerToOutServer> oldData = clientReturnData.get(workerToOutServer.uuid());
        if (oldData.isEmpty()) {
            oldData.add(workerToOutServer);
            clientReturnData.replace(workerToOutServer.uuid(), new ArrayList<>(), oldData);
        } else {
            ArrayList<WorkerToOutServer> newData = clientReturnData.get(workerToOutServer.uuid());
            newData.add(workerToOutServer);
            clientReturnData.replace(workerToOutServer.uuid(), oldData, newData);
        }
    }

    private void sendData(UUID client, CalculatedData dataToSend) {
        try {
            OutServerToClient outServerToClient = new OutServerToClient(client, dataToSend);
            Socket clientToSend = clients.get(client);

            System.out.println("Got to inside");
            System.out.println(client);
            System.out.println(outServerToClient);
            System.out.println("Out server send data");

            usersStatistics.updateUserStatistics(client, dataToSend);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientToSend.getOutputStream());
            objectOutputStream.writeObject(outServerToClient);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CalculatedData calculateFinalData(ArrayList<WorkerToOutServer> returnData) {
        ArrayList<CalculatedData> results = new ArrayList<>();
        for (WorkerToOutServer workerToOutServer : returnData) {
            results.add(workerToOutServer.gpx());
        }
        long finalTime = results.stream().mapToLong(CalculatedData::time).sum();
        double finalElevation = results.stream().mapToDouble(CalculatedData::elevation).sum();
        Double finalSpeed = results.stream().mapToDouble(CalculatedData::speed).average().orElse(Double.NaN);
        double finalDistance = results.stream().mapToDouble(CalculatedData::distance).sum();

        return new CalculatedData(finalDistance, finalSpeed, finalElevation, finalTime);
    }

    private void clearEverything(WorkerToOutServer data) {
        ArrayList<WorkerToOutServer> oldData = clientReturnData.get(data.uuid());
        ArrayList<WorkerToOutServer> newData = new ArrayList<>();
        clientReturnData.replace(data.uuid(), oldData, newData);
        clientNumberOfChunks.replace(data.uuid(), data.size(), 0);
    }
}