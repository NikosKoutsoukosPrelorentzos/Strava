package org.example.services;

import org.example.dtos.ClientToServers;

import java.util.List;

public interface ChunksService {
    List<byte[]> createChunks(ClientToServers clientToServers) throws Exception;
}

