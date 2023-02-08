package org.minefortress.professions.hire;

import net.minecraft.item.Item;
import org.minefortress.fortress.resources.ItemInfo;

import java.io.Serializable;

public record HireCost(int itemId, int amount) implements Serializable {

    ItemInfo toItemInfo() {
        return new ItemInfo(Item.byRawId(itemId), amount);
    }

    static HireCost fromItemInfo(ItemInfo info) {
        return new HireCost(Item.getRawId(info.item()), info.amount());
    }

}
