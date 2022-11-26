package org.minefortress.professions;

import com.google.gson.Gson;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class ProfessionEntityTypesMapper {

    private final Gson gson = new Gson();

    private final Identifier resourceId = new Identifier("minefortress", "professions/entitytypemap.json");
    private final MinecraftServer server;

    private final Map<String, EntityType> entityTypeMap = new HashMap<>();

    ProfessionEntityTypesMapper(MinecraftServer server) {
        this.server = server;
    }

    void read() {
        final var resourceManager = server.getResourceManager();
        try(
                final var resource = resourceManager.getResource(resourceId);
                final var is = resource.getInputStream();
                gson.fromJson()
        ) {

        } catch (IOException e) {
            throw new RuntimeException("Failed to read professions", e);
        }
    }

}
