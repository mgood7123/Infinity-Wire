package smallville7123.infinity_wire.mod.items;

import net.minecraft.item.Item;
import smallville7123.infinity_wire.mod.init.ModItems;

/**
 * <em><b>Copyright (c) 2018 Ocelot5836.</b></em>
 *
 * <br>
 * </br>
 *
 * A basic item that has the capability to register itself.
 *
 * @author Ocelot5836
 */
public class ModItem extends Item {

    /**
     * The default constructor.
     */
    public ModItem() {
    }

    /**
     * Sets the names of the item to the name specified and registers the item.
     *
     * @param name
     *            The names of the item
     */
    public ModItem(String name) {
        this(name, name);
    }

    /**
     * Sets the names of the item to the names specified and registers the item.
     *
     * @param registryName
     *            The registryName of the item
     * @param unlocalizedName
     *            The unlocalizedName of the item
     */
    public ModItem(String registryName, String unlocalizedName) {
        this.setRegistryName(registryName);
        this.setTranslationKey(unlocalizedName);
        ModItems.register(this);
    }
}