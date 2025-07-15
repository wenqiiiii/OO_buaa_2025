import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

import java.util.HashMap;
import java.util.HashSet;

public class OfficialAccount implements OfficialAccountInterface {
    private int ownerId;
    private final int id;
    private String name;
    private HashMap<Integer, PersonInterface> followers = new HashMap<>();
    private HashMap<Integer, Integer> contributions = new HashMap<>();  // <personId, contribution>
    private HashSet<Integer> articles = new HashSet<>();
    private int bestId;   // bestContributorId

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        this.bestId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void addFollower(PersonInterface person) {
        int personId = person.getId();
        followers.put(personId, person);
        contributions.put(personId, 0);
        // 维护 bestId
        if (personId < bestId && contributions.get(bestId) == 0) {
            bestId = personId;
        }
    }

    public boolean containsFollower(PersonInterface person) {
        return followers.containsKey(person.getId());
    }

    public void addArticle(PersonInterface person, int id) {
        articles.add(id);
        int personId = person.getId();
        int oldCtb = 0;
        if (contributions.containsKey(personId)) {
            oldCtb = contributions.get(personId);
        }
        followers.put(personId, person);
        int newCtb = oldCtb + 1;
        contributions.put(personId, newCtb);
        // 维护 bestId
        int maxCtb = contributions.get(bestId);
        if (newCtb > maxCtb) {
            bestId = personId;
        } else if (newCtb == maxCtb && personId < bestId) {
            bestId = personId;
        }
    }

    public void informFollowersAdd(int articleId) {  // my method 添加新文章时要通知所有的读者
        for (PersonInterface person : followers.values()) {
            ((Person) person).receiveArticle(articleId);
        }
    }

    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    public void removeArticle(int id) {
        articles.remove(id);
    }

    public void informFollowersRemove(int articleId) {  // my method 删除文章时要通知所有的读者
        for (PersonInterface person : followers.values()) {
            ((Person) person).removeArticle(articleId);
        }
    }

    public void reduceContribution(int personId) {  // my method 删除文章时作者的贡献值要减1
        int oldCtb = contributions.get(personId);
        int newCtb = oldCtb - 1;
        contributions.put(personId, newCtb);
        // 维护 bestId
        if (personId == bestId) {
            updateBestId();
        }
    }

    public int getBestContributor() {
        return bestId;
    }

    private void updateBestId() {
        bestId = ownerId;
        int maxCtb = contributions.get(bestId);
        for (Integer personId : contributions.keySet()) {
            if (contributions.get(personId) > maxCtb) {
                bestId = personId;
                maxCtb = contributions.get(personId);
            } else if (contributions.get(personId) == maxCtb && personId < bestId) {
                bestId = personId;
            }
        }
    }
}
