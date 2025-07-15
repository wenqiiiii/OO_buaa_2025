import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class Person implements PersonInterface {
    private final int id;
    private String name;
    private int age;
    private final HashMap<Integer, PersonInterface> acquaintance = new HashMap<>();
    private final HashMap<Integer, Integer> value = new HashMap<>();    // <personId, value>
    private final HashMap<Integer, TagInterface> tags = new HashMap<>();
    private final ArrayList<Integer> receivedArticles = new ArrayList<>();
    private int money = 0;
    private int socialValue = 0;
    private final ArrayList<MessageInterface> messages = new ArrayList<>();
    private int bestId; // bestAcquaintanceId

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.bestId = id;
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
        // 维护 bestId
        if (bestId != id) {    // 验证 bestId 是否有效
            int maxV = value.get(bestId);
            if (v > maxV) {
                bestId = personId;
            } else if (v == maxV && personId < bestId) {
                bestId = personId;
            }
        } else {
            bestId = personId;
        }
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
        // 维护 bestId
        if (personId == bestId) {
            updateBestId();
        }
    }

    public void valueIncrease(PersonInterface person, int increment) {  // my method
        if (increment == 0) {
            return;
        }
        int personId = person.getId();
        int oldValue = value.remove(personId);
        value.put(personId, oldValue + increment);
        // 维护 bestId
        if (increment > 0) {
            int maxV = value.get(bestId);
            if (oldValue + increment > maxV) {
                bestId = personId;
            } else if (oldValue + increment == maxV && personId < bestId) {
                bestId = personId;
            }
        } else {    // increment < 0
            if (bestId == personId) {
                updateBestId();
            }
        }
    }

    public int acquaintanceSize() {  // my method
        return acquaintance.size();
    }

    public Collection<PersonInterface> getAcquaintance() {    // my method
        return acquaintance.values();
    }

    public int bestAcquaintance() {
        return bestId;  // bestId 在 acquaintance 不为空时,即 bestId!=id 时有效
    }

    public List<Integer> getReceivedArticles() {
        return new ArrayList<>(receivedArticles);
    }

    public List<Integer> queryReceivedArticles() {
        ArrayList<Integer> query = new ArrayList<>();
        for (int i = 0; i < receivedArticles.size(); i++) {
            if (i == 5) {
                break;
            }
            query.add(i, receivedArticles.get(i));
        }
        return query;
    }

    public void addSocialValue(int num) {
        socialValue += num;
    }

    public int getSocialValue() {
        return socialValue;
    }

    public ArrayList<MessageInterface> getMessages() {
        return new ArrayList<>(messages);
    }

    public ArrayList<MessageInterface> getReceivedMessages() {
        ArrayList<MessageInterface> receivedMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (i == 5) {
                break;
            }
            receivedMessages.add(i, messages.get(i));
        }
        return receivedMessages;
    }

    public void addMoney(int num) {
        money += num;
    }

    public int getMoney() {
        return money;
    }

    public void receiveArticle(int articleId) {   // my method
        receivedArticles.add(0, articleId);
    }

    public void removeArticle(Integer articleId) {   // my method
        while (receivedArticles.remove(articleId)){
        }
    }

    public void receiveMessage(MessageInterface message) {  // my method
        messages.add(0, message);
    }

    private void updateBestId() {    // 遍历acquaintance更新bestId    my method
        bestId = id;
        Iterator<Integer> iterator = acquaintance.keySet().iterator();
        while (iterator.hasNext()) {
            int next = iterator.next();
            if (bestId == id) {
                bestId = next;
            } else {
                int maxV = value.get(bestId);
                if (value.get(next) > maxV) {
                    bestId = next;
                } else if (next < bestId && value.get(next) == maxV) {
                    bestId = next;
                }
            }
        }
    }

    public boolean strictEquals(PersonInterface person) {
        return true;
    }
}
