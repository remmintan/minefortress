package org.minefortress.fortress.resources.server;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

final class ServerStartingInventoryReader {

    private static final Identifier STARTING_INVENTORY_ID = new Identifier("minefortress", "starting_inventory.json");

    private final MinecraftServer server;

    ServerStartingInventoryReader(MinecraftServer server) {
        this.server = server;
    }

    List<InventorySlotInfo> readStartingSlots() {
        final var resourceManager = server.getResourceManager();
        try(
                final var resource = resourceManager.getResource(STARTING_INVENTORY_ID);
                final var is = resource.getInputStream();
                final var isr = new InputStreamReader(is);
                final var jsonReader = new JsonReader(isr)
        ){
            var inventorySlots = new ArrayList<InventorySlotInfo>();
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                final var slot = readSlot(jsonReader);
                inventorySlots.add(slot);
            }
            jsonReader.endArray();
            return inventorySlots;
        }catch (IOException e) {
            throw new RuntimeException("Failed to read starting inventory", e);
        }
    }

    private InventorySlotInfo readSlot(JsonReader jsonReader) throws IOException {
        var item = (Item) null;
        var amount = 0;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            final var name = jsonReader.nextName();
            switch (name) {
                case "item" -> item = Registry.ITEM.get(new Identifier(jsonReader.nextString()));
                case "amount" -> amount = jsonReader.nextInt();
                default -> jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return new InventorySlotInfo(item, amount);
    }

    record InventorySlotInfo(Item item, int amount) { }
}
