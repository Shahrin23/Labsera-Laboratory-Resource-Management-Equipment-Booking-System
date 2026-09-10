package com.labresa.model;

public class Equipment extends Resource {

    /** e.g. MICROSCOPE, 3D_PRINTER, TESTING_KIT - used for iconography and grouping, distinct from Category (COMMON/SPECIAL). */
    private final String equipmentKind;

    public Equipment(int id, String name, Category category, int totalQuantity, int maintenanceThreshold, String equipmentKind) {
        super(id, name, category, totalQuantity, maintenanceThreshold);
        this.equipmentKind = equipmentKind;
    }

    public String getEquipmentKind() {
        return equipmentKind;
    }

    @Override
    public String getType() {
        return "EQUIPMENT";
    }
}
