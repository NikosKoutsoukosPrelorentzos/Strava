package org.example.dtos;

import java.io.Serializable;
import java.util.UUID;

public record InServerToWorker(UUID uuid, byte[] gpx, Integer size) implements Serializable {
}