package org.minefortress.blueprints.manager;

import com.google.gson.stream.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import org.minefortress.MineFortressMod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BlueprintMetadataReader {

    private static final Identifier PREDEFINED_BLUEPRINTS_ID = new Identifier(MineFortressMod.MOD_ID, "predefined_blueprints.json");
    private final Map<BlueprintGroup, List<IBlueprintMetadata>> predefinedBlueprints = new HashMap<>();
    private final MinecraftServer server;

    public BlueprintMetadataReader(MinecraftServer server) {
        this.server = server;
    }

    void read() {
        predefinedBlueprints.clear();
        final var resourceManager = server.getResourceManager();
        final var resource = resourceManager.getResource(PREDEFINED_BLUEPRINTS_ID).orElseThrow();
        try (
                final var isr = new InputStreamReader(resource.getInputStream());
                final var jsonReader = new JsonReader(isr)
        ) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                final var blueprintGroup = BlueprintGroup.valueOf(jsonReader.nextName());
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    final IBlueprintMetadata blueprintMetadata = readBlueprintMetadata(jsonReader);
                    predefinedBlueprints.computeIfAbsent(blueprintGroup, k -> new ArrayList<>()).add(blueprintMetadata);
                }
                jsonReader.endArray();
            }
            jsonReader.endObject();
        } catch (IOException exp) {
            throw new RuntimeException("Failed to read predefined blueprints", exp);
        }
    }

    Map<BlueprintGroup, List<IBlueprintMetadata>> getPredefinedBlueprints() {
        return Map.copyOf(predefinedBlueprints);
    }

    private IBlueprintMetadata readBlueprintMetadata(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String name = null;
        String file = null;
        int floorLevel = 0;
        while (jsonReader.hasNext()) {
            final var propertyName = jsonReader.nextName();
            switch (propertyName) {
                case "name" -> name = jsonReader.nextString();
                case "file" -> file = jsonReader.nextString();
                case "floorLevel" -> floorLevel = jsonReader.nextInt();
                default -> throw new RuntimeException("Unknown property " + propertyName);
            }
        }
        jsonReader.endObject();
        return new BlueprintMetadata(name, file, floorLevel, 10);
    }

    public Optional<BlueprintGroup> convertIdToGroup(String blueprintId) {
        for (Map.Entry<BlueprintGroup, List<IBlueprintMetadata>> entry : predefinedBlueprints.entrySet()) {
            if (entry.getValue().stream().anyMatch(it -> it.getId().equals(blueprintId)))
                return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }

}
