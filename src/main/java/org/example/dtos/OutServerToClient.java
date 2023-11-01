package org.example.dtos;

import java.io.Serializable;
import java.util.UUID;

public record OutServerToClient(UUID uuid, CalculatedData gpx) implements Serializable {
}
