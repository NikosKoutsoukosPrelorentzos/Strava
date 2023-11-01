package org.example.dtos;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

public record ClientToServers(UUID uuid, File file, Integer size) implements Serializable {
}