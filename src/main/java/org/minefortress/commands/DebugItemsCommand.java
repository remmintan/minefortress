package org.minefortress.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.lang3.stream.IntStreams;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("UnstableApiUsage")
public class DebugItemsCommand extends MineFortressCommand {

    private static final List<Item> ITEMS_TO_ADD = Arrays.asList(
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
                                final var serverManager = getServerManagersProvider(context);
                                final var resourceHelper = serverManager.getResourceHelper();

                                final var random = context.getSource().getWorld().random;

                                final var items = IntStreams
                                        .range(num)
                                        .mapToObj(i -> ITEMS_TO_ADD.get(random.nextInt(ITEMS_TO_ADD.size())))
                                        .map(it -> it.getDefaultStack())
                                        .toList();
                                if (resourceHelper.putItemsToSuitableContainer(items))
                                    return 1;
                                return 0;
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
                                            final var serverManager = getServerManagersProvider(context);
                                            final var resourceManager = serverManager.getResourceManager();

                                            final var storage = resourceManager.getStorage();
                                            try (var tr = Transaction.openOuter()) {
                                                for (var nonEmptyView : storage.nonEmptyViews()) {
                                                    storage.extract(nonEmptyView.getResource(), nonEmptyView.getAmount(), tr);
                                                }
                                                tr.commit();
                                            }
                                            return 1;
                                        }
                                )
                        )
                )
        );
    }
}
