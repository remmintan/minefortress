package org.minefortress.blueprints.manager;

import com.google.gson.stream.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import org.minefortress.MineFortressMod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlueprintMetadataReader {

    private static final Identifier PREDEFINED_BLUEPRINTS_ID = new Identifier(MineFortressMod.MOD_ID, "predefined_blueprints.json");
    private List<BlueprintMetadata> predefinedBlueprints;
    private final MinecraftServer server;

    public BlueprintMetadataReader(MinecraftServer server) {
        this.server = server;
    }

    void read() {
        final var bluerpints = new ArrayList<BlueprintMetadata>();
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
                    final BlueprintMetadata blueprintMetadata = readBlueprintMetadata(jsonReader, blueprintGroup);
                    bluerpints.add(blueprintMetadata);
                }
                jsonReader.endArray();
            }
            jsonReader.endObject();
        } catch (IOException exp) {
            throw new RuntimeException("Failed to read predefined blueprints", exp);
        }

        predefinedBlueprints = Collections.unmodifiableList(bluerpints);
    }

    List<BlueprintMetadata> getPredefinedBlueprints() {
        return predefinedBlueprints;
    }

    private BlueprintMetadata readBlueprintMetadata(JsonReader jsonReader, BlueprintGroup group) throws IOException {
        jsonReader.beginObject();
        String name = null;
        String file = null;
        int floorLevel = 0;
        int capacity = 2; // Default capacity if not specified
        while (jsonReader.hasNext()) {
            final var propertyName = jsonReader.nextName();
            switch (propertyName) {
                case "name" -> name = jsonReader.nextString();
                case "file" -> file = jsonReader.nextString();
                case "floorLevel" -> floorLevel = jsonReader.nextInt();
                case "capacity" -> capacity = jsonReader.nextInt();
                default -> throw new RuntimeException("Unknown property " + propertyName);
            }
        }
        jsonReader.endObject();
        return new BlueprintMetadata(name, file, floorLevel, capacity, group);
    }

}
