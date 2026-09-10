package com.labresa.patterns;

import com.labresa.model.Equipment;
import com.labresa.model.LabRoom;
import com.labresa.model.Resource;

public class ResourceFactory {

    private ResourceFactory() { }

    /**
     * @param type EQUIPMENT or LAB_ROOM
     * @param detail for EQUIPMENT: the equipment kind (e.g. MICROSCOPE); for LAB_ROOM: the capacity as a string
     */
    public static Resource create(String type, int id, String name, Resource.Category category,
                                   int totalQuantity, String detail) {
        switch (type.toUpperCase()) {
            case "LAB_ROOM": {
                int capacity = detail == null ? 20 : Integer.parseInt(detail);
                return new LabRoom(id, name, category, totalQuantity, 200, capacity);
            }
            case "EQUIPMENT": {
                String kind = detail == null ? "GENERAL" : detail;
                int threshold = defaultThresholdFor(kind);
                return new Equipment(id, name, category, totalQuantity, threshold, kind);
            }
            default:
                throw new IllegalArgumentException("Unknown resource type: " + type);
        }
    }

    private static int defaultThresholdFor(String equipmentKind) {
        switch (equipmentKind.toUpperCase()) {
            case "MICROSCOPE": return 50;
            case "3D_PRINTER": return 30;
            case "TESTING_KIT": return 15;
            default: return 40;
        }
    }
}
