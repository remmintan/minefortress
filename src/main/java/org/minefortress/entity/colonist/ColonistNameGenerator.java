package org.minefortress.entity.colonist;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

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

    private static final Queue<String> mandatoryNames = new ArrayDeque<>();

    static {
        mandatoryNames.add("Jèff");
    }

    public static String generateRandomName() {
        if(!mandatoryNames.isEmpty()) {
            return mandatoryNames.remove();
        } else {
            return randomNames.get((int) (Math.random() * randomNames.size()));
        }
    }

}
