package com.labresa.patterns;

import com.labresa.model.Equipment;
import com.labresa.model.LabRoom;
import com.labresa.model.Resource;


public class ResourceFactory {

    private ResourceFactory() { }

    public static Resource create(String type, int id, String name, double cost, String detail) {
        switch (type.toUpperCase()) {
            case "LAB_ROOM": {
                int capacity = detail == null ? 20 : Integer.parseInt(detail);
                return new LabRoom(id, name, cost,  200, capacity);
            }
            case "EQUIPMENT": {
                String category = detail == null ? "GENERAL" : detail;
                int threshold = defaultThresholdFor(category);
                return new Equipment(id, name, cost, threshold, category);
            }
            default:
                throw new IllegalArgumentException("Unknown resource type: " + type);
        }
    }

    private static int defaultThresholdFor(String category) {

        switch (category.toUpperCase()) {
            case "MICROSCOPE": return 50;
            case "3D_PRINTER": return 30;
            case "TESTING_KIT": return 15;
            default: return 40;
        }
    }
}

