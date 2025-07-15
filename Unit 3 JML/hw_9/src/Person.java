import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Person implements PersonInterface {
    private final int id;
    private String name;
    private int age;
    private final HashMap<Integer, PersonInterface> acquaintance = new HashMap<>();
    private final HashMap<Integer, Integer> value = new HashMap<>();    // <personId, value>
    private final HashMap<Integer, TagInterface> tags = new HashMap<>();

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public boolean containsTag(int tagId) {
        return tags.containsKey(tagId);
    }

    public TagInterface getTag(int id) {
        return tags.get(id);
    }

    public void addTag(TagInterface tag) {
        int tagId = tag.getId();
        tags.put(tagId, tag);
    }

    public void delTag(int tagId) {
        tags.remove(tagId);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PersonInterface)) {
            return false;
        }
        return ((PersonInterface) obj).getId() == id;
    }

    public boolean isLinked(PersonInterface person) {
        int personId = person.getId();
        return acquaintance.containsKey(personId) || personId == id;
    }

    public int queryValue(PersonInterface person) {
        if (acquaintance.containsValue(person)) {
            return value.get(person.getId());
        } else {
            return 0;
        }
    }

    public void link(PersonInterface person, int v) {
        int personId = person.getId();
        acquaintance.put(personId, person);
        value.put(personId, v);
    }

    public void unlink(PersonInterface person) {    // O(n)
        int personId = person.getId();
        acquaintance.remove(personId);
        value.remove(personId);
        Iterator<TagInterface> iterator = tags.values().iterator();
        while (iterator.hasNext()) {
            TagInterface next = iterator.next();
            if (next.hasPerson(person)) {
                next.delPerson(person);
            }
        }
    }

    public void valueIncrease(PersonInterface person, int increment) {  // my method
        int personId = person.getId();
        int oldValue = value.remove(personId);
        value.put(personId, oldValue + increment);
    }

    public int acquaintanceSize() {  // my method
        return acquaintance.size();
    }

    public HashSet<PersonInterface> getAcquaintance() {    // my method
        return new HashSet<>(acquaintance.values());
    }

    public int bestAcquaintance() {     // O(n)
        int bestId = 0;
        Boolean first = true;
        Iterator<Integer> iterator = acquaintance.keySet().iterator();
        while (iterator.hasNext()) {
            int next = iterator.next();
            if (first) {
                bestId = next;
                first = false;
            } else {
                int maxV = value.get(bestId);
                if (value.get(next) > maxV) {
                    bestId = next;
                } else if (next < bestId && value.get(next) == maxV) {
                    bestId = next;
                }
            }
        }
        return bestId;
    }

    public boolean strictEquals(PersonInterface person) { return true; }
}
