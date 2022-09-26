package org.minefortress.professions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record ProfessionFullInfo(
        String key,
        String title,
        Item icon,
        String description,
        String unlockMessage,
        String unlockMoreMessage,
        Requirements requirements
) {

    public void write(PacketByteBuf packet) {
        packet.writeString(key);
        packet.writeString(title);
        packet.writeItemStack(new ItemStack(icon));
        packet.writeString(description);
        packet.writeString(unlockMessage);
        packet.writeString(unlockMoreMessage);
        packet.writeString(requirements.building());
        final var blockRequirement = requirements.block();
        packet.writeIdentifier(Registry.BLOCK.getId(blockRequirement.block()));
        packet.writeBoolean(blockRequirement.inBlueprint());
        packet.writeInt(requirements.items().size());
        for (var itemReq : requirements.items()) {
            packet.writeItemStack(itemReq.toStack());
        }
    }

    public static ProfessionFullInfo read(PacketByteBuf packet) {
        final var key = packet.readString();
        final var title = packet.readString();
        final var icon = packet.readItemStack().getItem();
        final var description = packet.readString();
        final var unlockMessage = packet.readString();
        final var unlockMoreMessage = packet.readString();
        final var building = packet.readString();
        final var block = Registry.BLOCK.get(packet.readIdentifier());
        final var inBlueprint = packet.readBoolean();
        final var items = packet.readList(PacketByteBuf::readItemStack).stream().map(ItemRequirement::fromStack).toList();
        final var requirements = new Requirements(building, new BlockRequirement(block, inBlueprint), items);
        return new ProfessionFullInfo(key, title, icon, description, unlockMessage, unlockMoreMessage, requirements);
    }

    record BlockRequirement(Block block, boolean inBlueprint) {
        @Override
        public Block block() {
            return Optional.ofNullable(block).orElse(Blocks.AIR);
        }
    }

    record ItemRequirement(Item item, int count) {

        private ItemStack toStack() {
            return new ItemStack(item, count);
        }

        private static ItemRequirement fromStack(ItemStack stack) {
            return new ItemRequirement(stack.getItem(), stack.getCount());
        }

    }

    record Requirements(String building, BlockRequirement block, List<ItemRequirement> items) {
        @Override
        public String building() {
            return Optional.ofNullable(building).orElse("_");
        }

        @Override
        public List<ItemRequirement> items() {
            return Optional.ofNullable(items).orElse(Collections.emptyList());
        }
    }

}
