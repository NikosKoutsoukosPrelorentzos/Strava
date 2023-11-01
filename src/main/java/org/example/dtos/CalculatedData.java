package org.example.dtos;

import java.io.Serializable;

public record CalculatedData(double distance, Double speed, double elevation, long time) implements Serializable {
}