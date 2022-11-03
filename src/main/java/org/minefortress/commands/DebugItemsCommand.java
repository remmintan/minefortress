package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import org.minefortress.fortress.FortressServerManager;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DebugItemsCommand extends MineFortressCommand {

    private static final List<Item> ITEMS_TO_ADD = Arrays.asList(
//            Items.OAK_PLANKS,
//            Items.POTATO,
//            Items.SALMON,
//            Items.COOKED_SALMON,
//            Items.COOKED_PORKCHOP,
//            Items.STONE_SHOVEL,
//            Items.STONE_PICKAXE,
//            Items.STONE_AXE,
//            Items.STONE_HOE,
//            Items.COBBLESTONE,
//            Items.STONE,
//            Items.STONE_BRICKS,
//            Items.STONE_SLAB,
//            Items.STONE_STAIRS,
//            Items.STONE_PRESSURE_PLATE,
//            Items.STONE_BUTTON,
//            Items.OAK_WOOD,
//            Items.OAK_LOG,
//            Items.OAK_SAPLING,
//            Items.BONE,
//            Items.BONE_BLOCK,
//            Items.RED_WOOL,
//            Items.WHITE_WOOL,
//            Items.ORANGE_WOOL,
//            Items.MAGENTA_WOOL,
//            Items.LIGHT_BLUE_WOOL,
//            Items.OAK_BOAT
            Items.COBBLESTONE,
            Items.STONE_BRICKS,
            Items.TORCH,
            Items.OAK_STAIRS,
            Items.OAK_SLAB,
            Items.OAK_PLANKS,
            Items.OAK_LOG,
            Items.STRIPPED_OAK_LOG,
            Items.OAK_WOOD,
            Items.STRIPPED_OAK_WOOD,
            Items.DIRT,
            Items.OAK_DOOR,
            Items.COBBLESTONE_STAIRS,
            Items.COBBLESTONE_SLAB,
            Items.OAK_FENCE,
            Items.OAK_FENCE_GATE,
            Items.CRAFTING_TABLE,
            Items.COOKED_BEEF,
            Items.LADDER,
            Items.OAK_PRESSURE_PLATE,
            Items.GREEN_CARPET,
            Items.CHEST,
            Items.WHEAT_SEEDS,
            Items.BARREL,
            Items.OAK_TRAPDOOR,
            Items.WHITE_WOOL,
            Items.YELLOW_WOOL,
            Items.YELLOW_CARPET,
            Items.STONE_SWORD,
            Items.IRON_SWORD,
            Items.IRON_HELMET,
            Items.IRON_CHESTPLATE,
            Items.IRON_LEGGINGS,
            Items.IRON_BOOTS

    );

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("fortress")
                .then(literal("items")
                    .then(argument("num", IntegerArgumentType.integer())
                        .executes(
                            context -> {
                                int num = IntegerArgumentType.getInteger(context, "num");
                                final FortressServerManager serverManager = getFortressServerManager(context);
                                final var resourceManager = serverManager.getServerResourceManager();

                                final var random = context.getSource().getWorld().random;
                                for (int i = 0; i < num; i++) {
                                    final var item = ITEMS_TO_ADD.get(random.nextInt(ITEMS_TO_ADD.size()));
                                    resourceManager.increaseItemAmount(item, random.nextInt(250));
                                }
                                return 1;
                            }
                        )
                    )
                )
        );

        dispatcher.register(
            literal("fortress")
                .then(literal("items")
                        .then(literal("clear")
                                .executes(
                                        context -> {
                                            final FortressServerManager serverManager = getFortressServerManager(context);
                                            final var resourceManager = serverManager.getServerResourceManager();

                                            resourceManager
                                                    .getAllItems()
                                                    .stream()
                                                    .map(ItemStack::getItem)
                                                    .forEach(it -> resourceManager.setItemAmount(it, 0));
                                            return 1;
                                        }
                                )
                        )
                )
        );
    }
}
