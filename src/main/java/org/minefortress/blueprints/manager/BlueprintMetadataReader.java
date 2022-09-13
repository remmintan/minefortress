package org.minefortress.blueprints.manager;

import com.google.gson.stream.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.minefortress.MineFortressMod;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintMetadataReader {

    private static final Identifier PREDEFINED_BLUEPRINTS_ID = new Identifier(MineFortressMod.MOD_ID, "predefined_blueprints");
    private final Map<BlueprintGroup, List<BlueprintMetadata>> predefinedBlueprints = new HashMap<>();

    void read(MinecraftServer server) {
        final var resourceManager = server.getResourceManager();
        try (
                final var resource = resourceManager.getResource(PREDEFINED_BLUEPRINTS_ID);
                final var isr = new InputStreamReader(resource.getInputStream());
                final var jsonReader = new JsonReader(isr)
        ) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                final var blueprintGroup = BlueprintGroup.valueOf(jsonReader.nextName());
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    final BlueprintMetadata blueprintMetadata = readBlueprintMetadata(jsonReader);
                    predefinedBlueprints.computeIfAbsent(blueprintGroup, k -> new ArrayList<>()).add(blueprintMetadata);
                }
                jsonReader.endArray();
            }
            jsonReader.endObject();
        } catch (IOException exp) {
            throw new RuntimeException("Failed to read predefined blueprints", exp);
        }
    }

    private BlueprintMetadata readBlueprintMetadata(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String name = null;
        String file = null;
        String requirementId = null;
        int floorLevel = 0;
        while (jsonReader.hasNext()) {
            final var propertyName = jsonReader.nextName();
            switch (propertyName) {
                case "name" -> name = jsonReader.nextString();
                case "file" -> file = jsonReader.nextString();
                case "floorLevel" -> floorLevel = jsonReader.nextInt();
                case "requirementId" -> requirementId = jsonReader.nextString();
                default -> throw new RuntimeException("Unknown property " + propertyName);
            }
        }
        jsonReader.endObject();
        return new BlueprintMetadata(name, file, floorLevel, requirementId);
    }

}
