package com.labresa.ui;

import com.labresa.model.Resource;

/** One line item in the booking cart: a resource plus how many units are wanted. */
public class CartItem {
    private final Resource resource;
    private final int quantity;

    public CartItem(Resource resource, int quantity) {
        this.resource = resource;
        this.quantity = quantity;
    }

    public Resource getResource() { return resource; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return resource.getName() + "  \u00D7  " + quantity +
                (resource.getCategory() == Resource.Category.SPECIAL ? "   [SPECIAL - needs approval]" : "   [Common]");
    }
}
