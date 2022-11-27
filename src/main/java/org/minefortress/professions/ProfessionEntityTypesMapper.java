package org.minefortress.professions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.registries.FortressEntities;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MethodsReturnNonnullByDefault
final class ProfessionEntityTypesMapper {
    private static final Type ENTITY_TYPES_MAP_TYPE = TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, String.class).getType()).getType();
    private static final Identifier RESOURCE_ID = new Identifier("minefortress", "professions/entitytypemap.json");
    private final Gson gson = new Gson();
    private final Map<String, EntityType<? extends BasePawnEntity>> entityTypeMap = new HashMap<>();

    void read(MinecraftServer server) {
        final var resourceManager = server.getResourceManager();
        final Map<String, List<String>> entityTypeNamesMap;
        try(
                final var resource = resourceManager.getResource(RESOURCE_ID);
                final var is = resource.getInputStream();
                final var isr = new InputStreamReader(is)
        ) {
            entityTypeNamesMap = gson.fromJson(isr, ENTITY_TYPES_MAP_TYPE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read professions", e);
        }

        entityTypeMap.clear();
        for (Map.Entry<String, List<String>> entry : entityTypeNamesMap.entrySet()) {
            final var typeLabel = entry.getKey();
            final EntityType<? extends BasePawnEntity> type = convert(typeLabel);
            for (String professionLabel : entry.getValue()) {
                entityTypeMap.put(professionLabel, type);
            }
        }

    }

    EntityType<? extends BasePawnEntity> getEntityTypeForProfession(String profession) {
        return entityTypeMap.getOrDefault(profession, FortressEntities.COLONIST_ENTITY_TYPE);
    }

    private EntityType<? extends BasePawnEntity> convert(String entityType) {
        if (entityType.equals("warrior")) {
            return FortressEntities.WARRIOR_PAWN_ENTITY_TYPE;
        }
        return FortressEntities.COLONIST_ENTITY_TYPE;
    }

}
