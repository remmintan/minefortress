package org.minefortress.professions;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.utils.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Profession {

    private static final int MAX_WIDTH = 150;

    private final String title;
    private final ItemStack icon;
    private int amount = 0;
    private final AdvancementFrame type = AdvancementFrame.TASK;
    private final List<Text> description;
    private final List<Text> unlockMoreMessage;
    private final List<Text> unlockMessage;
    private final String buildingRequirement;
    private final Block blockRequirement;
    private final List<ItemInfo> itemsRequirement;
    private final boolean cantRemove;
    private final boolean blueprint;

    private Profession parent;
    private final List<Profession> children = new ArrayList<>();

    public Profession(ProfessionFullInfo fullInfo) {
        this.title = fullInfo.title();
        this.icon = new ItemStack(fullInfo.icon());
        this.cantRemove = fullInfo.cantRemove();

        final var requirements = fullInfo.requirements();

        if(requirements != null) {
            buildingRequirement = requirements.building();

            final var blockRequirement = requirements.block();
            if(blockRequirement != null && !Blocks.AIR.equals(blockRequirement.block())) {
                this.blockRequirement = blockRequirement.block();
                blueprint = blockRequirement.inBlueprint();
            } else {
                this.blockRequirement = null;
                blueprint = false;
            }

            this.itemsRequirement = requirements
                    .items()
                    .stream()
                    .map(it -> new ItemInfo(it.item(), it.count()))
                    .toList();
        } else {
            buildingRequirement = "";
            blockRequirement = null;
            blueprint = false;
            itemsRequirement = Collections.emptyList();
        }

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.description = GuiUtils.splitTextInWordsForLength(fullInfo.description(), MAX_WIDTH);
            this.unlockMessage = GuiUtils.splitTextInWordsForLength(fullInfo.unlockMessage(), MAX_WIDTH);
            this.unlockMoreMessage = GuiUtils.splitTextInWordsForLength(fullInfo.unlockMoreMessage(), MAX_WIDTH);
        } else {
            this.description = null;
            this.unlockMessage = null;
            this.unlockMoreMessage = null;
        }
    }

    public boolean isCantRemove() {
        return cantRemove;
    }

    public String getTitle() {
        return title;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public AdvancementFrame getType() {
        return type;
    }

    public void setParent(Profession profession) {
        if(this.parent != null)
            throw new IllegalStateException("Profession already has a parent");
        this.parent = profession;
    }

    public void addChild(Profession profession) {
        this.children.add(profession);
    }

    public List<Profession> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<Text> getDescription() {
        return description;
    }

    public List<Text> getUnlockMessage() {
        return unlockMessage;
    }

    public List<Text> getUnlockMoreMessage() {
        return unlockMoreMessage;
    }

    public Profession getParent() {
        return parent;
    }

    public List<ItemInfo> getItemsRequirement() {
        return itemsRequirement;
    }

    @Nullable
    public String getBuildingRequirement() {
        return buildingRequirement;
    }

    @Nullable
    public BlockRequirement getBlockRequirement() {
        return new BlockRequirement(blockRequirement, blueprint);
    }

    public NbtCompound toNbt() {
        final NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("amount", amount);
        return nbtCompound;
    }

    public void readNbt(NbtCompound nbtCompound) {
        if(nbtCompound.contains("amount"))
            amount = nbtCompound.getInt("amount");
        else
            amount = 0;
    }

    public static record BlockRequirement(Block block, boolean blueprint) {}
}
