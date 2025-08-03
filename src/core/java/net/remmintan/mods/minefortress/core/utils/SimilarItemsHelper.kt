package net.remmintan.mods.minefortress.core.utils

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper.getSimilarItems
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper.isIgnorable
import org.spongepowered.include.com.google.common.collect.Sets
import java.util.*

object SimilarItemsHelper {
    private val tags: List<TagKey<Item>> = Arrays.asList(
        ItemTags.WOODEN_BUTTONS,
        ItemTags.WOODEN_PRESSURE_PLATES,
        ItemTags.WOODEN_SLABS,
        ItemTags.WOODEN_STAIRS,
        ItemTags.WOODEN_TRAPDOORS,
        ItemTags.WOODEN_DOORS,
        ItemTags.WOODEN_FENCES,
        ItemTags.BOATS,
        ItemTags.SIGNS,
        ItemTags.FENCES,
        ItemTags.LEAVES,
        ItemTags.FLOWERS,
        ItemTags.LOGS,
        ItemTags.PLANKS,
        ItemTags.WOOL_CARPETS,
        ItemTags.WOOL
    )

    private val strippedLogs: List<Item> = Arrays.asList(
        Items.STRIPPED_ACACIA_LOG,
        Items.STRIPPED_BIRCH_LOG,
        Items.STRIPPED_DARK_OAK_LOG,
        Items.STRIPPED_JUNGLE_LOG,
        Items.STRIPPED_OAK_LOG,
        Items.STRIPPED_SPRUCE_LOG,
        Items.STRIPPED_WARPED_STEM,
        Items.STRIPPED_CRIMSON_STEM
    )

    private val strippedWood: List<Item> = Arrays.asList(
        Items.STRIPPED_ACACIA_WOOD,
        Items.STRIPPED_BIRCH_WOOD,
        Items.STRIPPED_DARK_OAK_WOOD,
        Items.STRIPPED_JUNGLE_WOOD,
        Items.STRIPPED_OAK_WOOD,
        Items.STRIPPED_SPRUCE_WOOD,
        Items.STRIPPED_WARPED_HYPHAE,
        Items.STRIPPED_CRIMSON_HYPHAE
    )

    private val similarDirt: List<Item> = Arrays.asList(
        Items.DIRT,
        Items.GRASS_BLOCK,
        Items.FARMLAND,
        Items.COARSE_DIRT,
        Items.PODZOL
    )

    private val similarFenceGate: List<Item> = Arrays.asList(
        Items.ACACIA_FENCE_GATE,
        Items.BIRCH_FENCE_GATE,
        Items.CRIMSON_FENCE_GATE,
        Items.OAK_FENCE_GATE,
        Items.JUNGLE_FENCE_GATE,
        Items.DARK_OAK_FENCE_GATE,
        Items.SPRUCE_FENCE_GATE,
        Items.WARPED_FENCE_GATE
    )

    private val similarGlass: List<Item> = Arrays.asList(
        Items.GLASS,
        Items.WHITE_STAINED_GLASS,
        Items.ORANGE_STAINED_GLASS,
        Items.MAGENTA_STAINED_GLASS,
        Items.LIGHT_BLUE_STAINED_GLASS,
        Items.YELLOW_STAINED_GLASS,
        Items.LIME_STAINED_GLASS,
        Items.PINK_STAINED_GLASS,
        Items.GRAY_STAINED_GLASS,
        Items.LIGHT_GRAY_STAINED_GLASS,
        Items.CYAN_STAINED_GLASS,
        Items.PURPLE_STAINED_GLASS,
        Items.BLUE_STAINED_GLASS,
        Items.BROWN_STAINED_GLASS,
        Items.GREEN_STAINED_GLASS,
        Items.RED_STAINED_GLASS,
        Items.BLACK_STAINED_GLASS
    )

    private val similarGlassPanes: List<Item> = Arrays.asList(
        Items.GLASS_PANE,
        Items.WHITE_STAINED_GLASS_PANE,
        Items.ORANGE_STAINED_GLASS_PANE,
        Items.MAGENTA_STAINED_GLASS_PANE,
        Items.LIGHT_BLUE_STAINED_GLASS_PANE,
        Items.YELLOW_STAINED_GLASS_PANE,
        Items.LIME_STAINED_GLASS_PANE,
        Items.PINK_STAINED_GLASS_PANE,
        Items.GRAY_STAINED_GLASS_PANE,
        Items.LIGHT_GRAY_STAINED_GLASS_PANE,
        Items.CYAN_STAINED_GLASS_PANE,
        Items.PURPLE_STAINED_GLASS_PANE,
        Items.BLUE_STAINED_GLASS_PANE,
        Items.BROWN_STAINED_GLASS_PANE,
        Items.GREEN_STAINED_GLASS_PANE,
        Items.RED_STAINED_GLASS_PANE,
        Items.BLACK_STAINED_GLASS_PANE
    )

    private val IGNORED_ITEMS: Set<Item>

    init {
        val items = Sets.newHashSet(
            Items.AIR,
            Items.STRUCTURE_VOID,
            Items.BARRIER,
            Items.GRASS_BLOCK,
            Items.DIRT_PATH,
            Items.WHITE_BED,
            Items.ORANGE_BED,
            Items.MAGENTA_BED,
            Items.LIGHT_BLUE_BED,
            Items.YELLOW_BED,
            Items.LIME_BED,
            Items.PINK_BED,
            Items.GRAY_BED,
            Items.LIGHT_GRAY_BED,
            Items.CYAN_BED,
            Items.PURPLE_BED,
            Items.BLUE_BED,
            Items.BROWN_BED,
            Items.GREEN_BED,
            Items.RED_BED,
            Items.BLACK_BED,
            Items.GRASS,
            Items.TALL_GRASS,
            Items.ACACIA_LEAVES,
            Items.BIRCH_LEAVES,
            Items.DARK_OAK_LEAVES,
            Items.JUNGLE_LEAVES,
            Items.OAK_LEAVES,
            Items.SPRUCE_LEAVES,
            Items.STRIPPED_ACACIA_LOG,
            Items.STRIPPED_BIRCH_LOG,
            Items.STRIPPED_DARK_OAK_LOG,
            Items.STRIPPED_JUNGLE_LOG,
            Items.STRIPPED_OAK_LOG,
            Items.STRIPPED_SPRUCE_LOG,
            Items.STRIPPED_WARPED_STEM,
            Items.STRIPPED_CRIMSON_STEM
        )
        items.addAll(getItems(ItemTags.FLOWERS))
        items.addAll(getItems(ItemTags.BANNERS))
        items.addAll(getItems(ItemTags.BEDS))
        items.addAll(getItems(ItemTags.DOORS))
        IGNORED_ITEMS = items
    }

    @JvmStatic
    fun isIgnorable(it: Item): Boolean {
        return IGNORED_ITEMS.contains(it)
    }

    @JvmStatic
    fun convertItemIconInTheGUI(item: Item): Item {
        if (Items.FARMLAND == item) {
            return Items.DIRT
        }
        return item
    }

    @JvmStatic
    fun getSimilarItems(item: Item): List<Item> {
        for (items in Arrays.asList(
            strippedLogs,
            strippedWood,
            similarDirt,
            similarFenceGate,
            similarGlass,
            similarGlassPanes
        )) {
            if (items.contains(item)) return items.stream().filter { i: Item -> i !== item }.toList()
        }

        return getItemTag(item)
            .map { tag: TagKey<Item> ->
                getItems(tag)
                    .stream()
                    .filter { it: Item -> it !== item }
                    .filter { it: Item -> !strippedLogs.contains(it) }
                    .filter { it: Item -> !strippedWood.contains(it) }
                    .filter { it: Item -> !similarDirt.contains(it) }
                    .filter { it: Item -> !similarFenceGate.contains(it) }
                    .filter { it: Item -> !similarGlass.contains(it) }
                    .toList()
            }
            .orElse(emptyList())
    }

    private fun getItemTag(item: Item): Optional<TagKey<Item>> {
        val defaultStack = item.defaultStack
        return defaultStack.streamTags().filter { o: TagKey<Item> -> tags.contains(o) }.findFirst()
    }

    private fun getItems(tag: TagKey<Item>): List<Item> {
        val items = ArrayList<Item>()
        for (it in Registries.ITEM.iterateEntries(tag)) {
            items.add(it.value())
        }
        return Collections.unmodifiableList(items)
    }
}

@Suppress("UnstableApiUsage")
fun Storage<ItemVariant>.extractItemsConsideringSimilar(
    itemVariant: ItemVariant,
    amountToExtract: Long,
    tr: Transaction
): Long {
    require(Transaction.isOpen())
    val extractedAmount = this.extract(itemVariant, amountToExtract, tr)
    require(extractedAmount <= amountToExtract)
    if (extractedAmount == amountToExtract)
        return amountToExtract

    val item = itemVariant.item
    if (isIgnorable(item))
        return amountToExtract

    var restAmount = amountToExtract - extractedAmount
    for (similarItem in getSimilarItems(item)) {
        val extracted = this.extract(ItemVariant.of(similarItem), restAmount, tr)
        require(extracted <= restAmount)
        if (extracted == restAmount)
            return amountToExtract

        restAmount -= extracted
    }

    return extractedAmount
}
