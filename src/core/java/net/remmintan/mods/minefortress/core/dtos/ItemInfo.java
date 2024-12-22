package net.remmintan.mods.minefortress.core.dtos;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

public record ItemInfo(Item item, int amount) {

    public static ItemInfo fromNbt(NbtCompound nbt) {
        final var item = Item.byRawId(nbt.getInt("item"));
        final var amount = nbt.getInt("amount");
        return new ItemInfo(item, amount);
    }

    public NbtCompound toNbt() {
        final var nbt = new NbtCompound();
        nbt.putInt("item", Item.getRawId(item));
        nbt.putInt("amount", amount);
        return nbt;
    }

}
