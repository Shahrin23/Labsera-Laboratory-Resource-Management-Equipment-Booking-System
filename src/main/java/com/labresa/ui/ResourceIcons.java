package com.labresa.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Produces small, colorful icon badges per resource category. Uses original
 * flat-icon-style graphics (unicode glyph on a colored rounded badge) rather
 * than bundled or hotlinked photographs, so the UI has zero licensing risk
 * and works fully offline - the same approach used by apps like Notion/Slack
 * for entity icons.
 */
public final class ResourceIcons {

    private ResourceIcons() { }

    public static StackPane badge(String category) {
        String glyph;
        String colorClass;

        String key = category == null ? "" : category.toUpperCase();
        if (key.contains("MICROSCOPE")) {
            glyph = "\uD83D\uDD2C"; // microscope
            colorClass = "icon-blue";
        } else if (key.contains("PRINTER")) {
            glyph = "\uD83D\uDDA8"; // printer
            colorClass = "icon-purple";
        } else if (key.contains("TESTING") || key.contains("KIT") || key.contains("CHEM")) {
            glyph = "\uD83E\uDDEA"; // test tube
            colorClass = "icon-teal";
        } else if (key.contains("ROOM") || key.contains("LAB_ROOM")) {
            glyph = "\uD83D\uDEAA"; // door
            colorClass = "icon-orange";
        } else {
            glyph = "\uD83D\uDD27"; // wrench (generic equipment)
            colorClass = "icon-gray";
        }

        Label label = new Label(glyph);
        label.setStyle("-fx-font-size: 16px;");

        StackPane pane = new StackPane(label);
        pane.setAlignment(Pos.CENTER);
        pane.getStyleClass().addAll("icon-badge", colorClass);
        pane.setPrefSize(30, 30);
        pane.setMinSize(30, 30);
        pane.setMaxSize(30, 30);
        return pane;
    }
}
