package org.minefortress.professions;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionFullInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ProfessionsReader {

    private static final Logger LOG = LoggerFactory.getLogger(ProfessionsReader.class);

    private final Identifier professionsResourceId = new Identifier("minefortress", "professions/list.json");
    private final Identifier professionTreeId = new Identifier("minefortress", "professions/tree.json");

    private final MinecraftServer server;

    ProfessionsReader(MinecraftServer server) {
        this.server = server;
    }

    List<ProfessionFullInfo> readProfessions() {
        final var resourceManager = server.getResourceManager();
        final var professions = new ArrayList<ProfessionFullInfo>();
        final var resource = resourceManager.getResource(professionsResourceId).orElseThrow();
        try(
                final var is = resource.getInputStream();
                final var isr = new InputStreamReader(is);
                final var jsonReader = new JsonReader(isr)
        ) {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                final var profession = readProfession(jsonReader);
               professions.add(profession);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read professions", e);
        }

        return professions;
    }

    String readTreeJson() {
        final var resourceManager = server.getResourceManager();
        final var resource = resourceManager.getResource(professionTreeId).orElseThrow();
        try(

                final var is = resource.getInputStream();
                final var isr = new InputStreamReader(is);
                final var br = new BufferedReader(isr)
        ) {
            return br.lines().reduce("", (a, b) -> a + b);
        }catch (IOException e) {
            throw new RuntimeException("Failed to read professions tree string", e);
        }
    }

    private ProfessionFullInfo readProfession(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String key = null;

        String title = null;
        Item icon = null;
        String description = null;
        String unlockMessage = null;
        String unlockMoreMessage = null;
        boolean cantRemove = false;
        ProfessionFullInfo.Requirements requirements = null;

        while (jsonReader.hasNext()) {
            final var propertyName = jsonReader.nextName();
            switch (propertyName) {
                case "key" -> key = jsonReader.nextString();
                case "title" -> title = jsonReader.nextString();
                case "icon" -> icon = decodeIcon(jsonReader);
                case "description" -> description = jsonReader.nextString();
                case "unlockMessage" -> unlockMessage = jsonReader.nextString();
                case "unlockMoreMessage" -> unlockMoreMessage = jsonReader.nextString();
                case "hireMenu" -> cantRemove = jsonReader.nextBoolean();
                case "requirements" -> requirements = readRequirements(jsonReader);
            }
        }
        jsonReader.endObject();

        return new ProfessionFullInfo(key, title, icon, description,  unlockMessage, unlockMoreMessage, cantRemove, requirements);
    }

    private Item decodeIcon(JsonReader reader) throws IOException {
        final var iconName = reader.nextString();
        return Registries.ITEM.get(new Identifier(iconName));
    }

    private ProfessionFullInfo.Requirements readRequirements(JsonReader reader) throws IOException {
        ProfessionFullInfo.BuildingRequirement buildingRequirement = null;
        List<ProfessionFullInfo.ItemRequirement> items = null;

        reader.beginObject();
        while (reader.hasNext()) {
            final var propertyName = reader.nextName();
            switch (propertyName) {
                case "building" -> buildingRequirement = readBuildingRequirement(reader);
                case "items" -> items = readItemRequirements(reader);
            }
        }
        reader.endObject();
        return new ProfessionFullInfo.Requirements(buildingRequirement, items);
    }

    private ProfessionFullInfo.BuildingRequirement readBuildingRequirement(JsonReader reader) throws IOException {
        String typeString = null;
        int level = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            final var propertuName = reader.nextName();
            switch (propertuName) {
                case "type" -> typeString = reader.nextString();
                case "level" -> level = reader.nextInt();
            }
        }
        reader.endObject();

        if (typeString == null) return null;
        ProfessionType type;
        try {
            type = ProfessionType.valueOf(typeString);
        } catch (IllegalArgumentException exp) {
            LOG.warn("Can't profession requirement. Unknown  blueprint requirement type: {}", typeString);
            return null;
        }

        return new ProfessionFullInfo.BuildingRequirement(type, level);
    }

    private List<ProfessionFullInfo.ItemRequirement> readItemRequirements(JsonReader reader) throws IOException {
        final var itemRequirements = new ArrayList<ProfessionFullInfo.ItemRequirement>();
        reader.beginArray();
        while (reader.hasNext()) {
            itemRequirements.add(readItemRequirement(reader));
        }
        reader.endArray();
        return Collections.unmodifiableList(itemRequirements);
    }

    private ProfessionFullInfo.ItemRequirement readItemRequirement(JsonReader reader) throws IOException {
        String itemName = null;
        int count = 1;

        reader.beginObject();
        while (reader.hasNext()) {
            final var propertyName = reader.nextName();
            switch (propertyName) {
                case "id" -> itemName = reader.nextString();
                case "count" -> count = reader.nextInt();
            }
        }
        reader.endObject();
        if(itemName == null) {
            throw new IllegalStateException("Item requirement must have an item");
        }
        return new ProfessionFullInfo.ItemRequirement(Registries.ITEM.get(new Identifier(itemName)), count);
    }

}
