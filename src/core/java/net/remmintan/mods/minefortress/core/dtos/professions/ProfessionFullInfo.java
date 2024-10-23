package net.remmintan.mods.minefortress.core.dtos.professions;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

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
        boolean hireMenu,
        Requirements requirements
) {


    public static ProfessionFullInfo read(PacketByteBuf packet) {
        final var key = packet.readString();
        final var title = packet.readString();
        final var icon = packet.readItemStack().getItem();
        final var description = packet.readString();
        final var unlockMessage = packet.readString();
        final var unlockMoreMessage = packet.readString();
        final var cantRemove = packet.readBoolean();
        final var buildingRequirementLevel = packet.readInt();
        final var blueprintRequirementType = buildingRequirementLevel >= 0 ? packet.readEnumConstant(ProfessionType.class) : null;

        var items = packet
                .readList(PacketByteBuf::readItemStack)
                .stream()
                .map(ItemRequirement::fromStack)
                .toList();

        final var requirements = new Requirements(new BuildingRequirement(blueprintRequirementType, buildingRequirementLevel), items);
        return new ProfessionFullInfo(key, title, icon, description, unlockMessage, unlockMoreMessage, cantRemove, requirements);
    }

    public void write(PacketByteBuf packet) {
        packet.writeString(key);
        packet.writeString(title);
        packet.writeItemStack(new ItemStack(icon));
        packet.writeString(description);
        packet.writeString(unlockMessage);
        packet.writeString(unlockMoreMessage);
        packet.writeBoolean(hireMenu);
        if(requirements != null) {
            final var buildingRequirement = requirements.building();
            packet.writeInt(buildingRequirement.level);
            packet.writeEnumConstant(buildingRequirement.type);
            packet.writeVarInt(requirements.items().size());
            for (var itemReq : requirements.items()) {
                packet.writeItemStack(itemReq.toStack());
            }
        } else {
            packet.writeInt(-1);
            packet.writeVarInt(0);
        }
    }

    public record BlockRequirement(Block block, boolean inBlueprint) {
        @Override
        public Block block() {
            return Optional.ofNullable(block).orElse(Blocks.AIR);
        }
    }

    public record ItemRequirement(Item item, int count) {

        private ItemStack toStack() {
            return new ItemStack(item, count);
        }

        private static ItemRequirement fromStack(ItemStack stack) {
            return new ItemRequirement(stack.getItem(), stack.getCount());
        }

    }

    public record Requirements(BuildingRequirement building, List<ItemRequirement> items) {
        @Override
        public List<ItemRequirement> items() {
            return Optional.ofNullable(items).orElse(Collections.emptyList());
        }
    }

    public record BuildingRequirement(ProfessionType type, int level) {
    }

}
