package org.example.entities;

import java.util.Date;

public record Waypoint(double longitude, double altitude, double elevation, Date time) {
}