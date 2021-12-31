package smallville7123.infinity_wire.mod.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import smallville7123.infinity_wire.mod.items.ItemInfinityWire;

public class ModItems {

    private static List<Item> items;

    public static Item EMERALD_APPLE;

    private static void init() {
        items = new ArrayList<Item>();

        EMERALD_APPLE = new ItemInfinityWire();
    }

    public static void register(Item item) {
        items.add(item);
    }

    public static Item[] getItems() {
        if (items == null)
            init();
        return items.toArray(new Item[items.size()]);
    }
}