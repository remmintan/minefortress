package org.minefortress.entity.colonist;

import net.minecraft.nbt.NbtCompound;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

public class ColonistNameGenerator {

    private static final List<String> randomNames = Arrays.asList(
            "James",
            "Robert",
            "John",
            "Michael",
            "William",
            "David",
            "Richard",
            "Joseph",
            "Thomas",
            "Charles",
            "Christopher",
            "Daniel",
            "Matthew",
            "Anthony",
            "Mark",
            "Donald",
            "Steven",
            "Paul",
            "Andrew",
            "Joshua",
            "Kenneth",
            "Kevin",
            "Brian",
            "George",
            "Edward",
            "Ronald",
            "Timothy",
            "Jason",
            "Jeffrey",
            "Ryan",
            "Jacob",
            "Gary",
            "Nicholas",
            "Eric",
            "Jonathan",
            "Stephen",
            "Larry",
            "Justin",
            "Scott",
            "Brandon",
            "Benjamin",
            "Samuel",
            "Gregory",
            "Frank",
            "Alexander",
            "Raymond",
            "Patrick",
            "Jack",
            "Dennis",
            "Jerry",
            "Tyler",
            "Aaron",
            "Jose",
            "Adam",
            "Henry",
            "Nathan",
            "Douglas",
            "Zachary",
            "Peter",
            "Kyle",
            "Walter",
            "Ethan",
            "Jeremy",
            "Harold",
            "Keith",
            "Christian",
            "Roger",
            "Noah",
            "Gerald",
            "Carl",
            "Terry",
            "Sean",
            "Austin",
            "Arthur",
            "Lawrence",
            "Jesse",
            "Dylan",
            "Bryan",
            "Joe"
    );

    private final List<String> mandatoryNames;

    public ColonistNameGenerator() {
        mandatoryNames = new ArrayList<>();
        mandatoryNames.addAll(Arrays.asList("Jèff", "Travis", "Fed", "Noah",  "Ray", "Moench", "Hunter", "Christian", "Dean", "Lyam", "Takelale", "Varneke", "Brandon", "Aki"));
    }

    public ColonistNameGenerator(NbtCompound nbtCompound) {
        if(nbtCompound.contains("mandatoryNames")) {
            final String mandatoryNamesString = nbtCompound.getString("mandatoryNames");
            if(Strings.isNotBlank(mandatoryNamesString)) {
                final String[] mandatoryNames = mandatoryNamesString.split(",");
                this.mandatoryNames = new ArrayList<>(Arrays.asList(mandatoryNames));
                return;
            }
        }
        mandatoryNames = new ArrayList<>();
    }

    public String generateRandomName() {
        if(!mandatoryNames.isEmpty()) {
            final var name = mandatoryNames.get((int) (Math.random() * mandatoryNames.size()));
            mandatoryNames.remove(name);
            return name;
        } else {
            return randomNames.get((int) (Math.random() * randomNames.size()));
        }
    }

    public void write(NbtCompound compound) {
        if(!mandatoryNames.isEmpty()) {
            compound.putString("mandatoryNames", String.join(",", mandatoryNames));
        }
    }

}
