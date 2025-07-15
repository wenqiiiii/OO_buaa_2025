import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;

public class Tag implements TagInterface {
    private final int id;
    private final HashMap<Integer, PersonInterface> persons = new HashMap<>();
    private int ageSum = 0;
    private int agePowSum = 0;

    public Tag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TagInterface)) {
            return false;
        }
        return ((TagInterface) obj).getId() == id;
    }

    public void addPerson(PersonInterface person) {
        int age = person.getAge();
        ageSum += age;
        agePowSum += age * age;
        persons.put(person.getId(), person);
    }

    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        } else {
            return ageSum / persons.size();
        }
    }

    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        } else {
            int mean = getAgeMean();
            return (agePowSum - 2 * ageSum * mean + persons.size() * mean * mean)
                    / persons.size();
        }
    }

    public void delPerson(PersonInterface person) {
        int age = person.getAge();
        ageSum -= age;
        agePowSum -= age * age;
        persons.remove(person.getId());
    }

    public int getSize() {
        return persons.size();
    }
}