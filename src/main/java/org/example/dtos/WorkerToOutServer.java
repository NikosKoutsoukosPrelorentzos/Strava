package org.example.dtos;

import java.io.Serializable;
import java.util.UUID;

public record WorkerToOutServer(UUID uuid, CalculatedData gpx, int size) implements Serializable {
}