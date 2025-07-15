import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.EmojiMessageInterface;

import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons = new HashMap<>();
    private final HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private final HashMap<Integer, Integer> articles = new HashMap<>(); //<articleId, contributorId>
    private final HashMap<Integer, MessageInterface> messages = new HashMap<>();
    private final HashMap<Integer, Integer> emojis = new HashMap<>(); //<emojiId, emojiHeat>

    private int tripleSum = 0;
    private final UnionFind uf = new UnionFind();   // for qci 连通性
    private int coupleSum = 0;
    private final HashSet<TagInterface> tags = new HashSet<>();

    public Network() {
    }

    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    public PersonInterface getPerson(int id) {
        return persons.get(id);
    }

    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        int personId = person.getId();
        if (containsPerson(personId)) {
            throw new EqualPersonIdException(personId);
        }
        persons.put(personId, person);
        // 维护并查集
        uf.add(personId);
    }

    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        } else {
            PersonInterface person1 = persons.get(id1);
            PersonInterface person2 = persons.get(id2);
            // 维护 coupleSum : 记录之前的 bestId
            final int oldBest1 = ((Person) person1).bestAcquaintance();
            final int oldBest2 = ((Person) person2).bestAcquaintance();
            ((Person) person1).link(person2, value);
            ((Person) person2).link(person1, value);
            // 维护 tag valueSum
            for (TagInterface tag : tags) {
                if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                    ((Tag) tag).addValueSum(value);
                }
            }
            // 维护 tripleSum
            Iterator<PersonInterface> iterator = persons.values().iterator();
            while (iterator.hasNext()) {
                PersonInterface next = iterator.next();
                if (next.equals(person1) || next.equals(person2)) {
                    continue;
                }
                if (next.isLinked(person1) && next.isLinked(person2)) {
                    tripleSum++;
                }
            }
            // 维护并查集
            uf.union(id1, id2);
            // 维护 coupleSum
            updateCoupleSum(id1, id2, oldBest1, oldBest2);
        }
    }

    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            PersonInterface person1 = getPerson(id1);
            PersonInterface person2 = getPerson(id2);
            // 维护 coupleSum : 记录之前的 bestId
            int oldBest1 = ((Person) person1).bestAcquaintance();
            int oldBest2 = ((Person) person2).bestAcquaintance();

            int oldValue = person1.queryValue(person2);
            if (oldValue + value > 0) {
                ((Person) person1).valueIncrease(person2, value);
                ((Person) person2).valueIncrease(person1, value);
                // 维护 tag valueSum
                for (TagInterface tag : tags) {
                    if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                        ((Tag) tag).addValueSum(value);
                    }
                }
            } else {
                ((Person) person1).unlink(person2);
                ((Person) person2).unlink(person1);
                // 维护 tag valueSum
                for (TagInterface tag : tags) {
                    if (tag.hasPerson(person1) && tag.hasPerson(person2)) {
                        ((Tag) tag).addValueSum(-oldValue);
                    }
                }
                // 维护 tripleSum  begin
                Iterator<PersonInterface> iterator = persons.values().iterator();
                while (iterator.hasNext()) {
                    PersonInterface next = iterator.next();
                    if (next.equals(person1) || next.equals(person2)) {
                        continue;
                    }
                    if (next.isLinked(person1) && next.isLinked(person2)) {
                        tripleSum--;
                    }
                }
                // 并查集重建 for qci
                HashSet<Integer> connectTo1 = new HashSet<>();  // 所有与 person1 连通的人
                dfs((Person) person1, connectTo1);
                if (!connectTo1.contains(id2)) {   // 边的删除影响了连通性，需要并查集重建
                    uf.rebuild(connectTo1, id1, id2);
                }
            }
            // 维护 coupleSum
            updateCoupleSum(id1, id2, oldBest1, oldBest2);
        }
    }

    private void dfs(Person person, HashSet<Integer> visited) {
        int personId = person.getId();
        if (visited.contains(personId)) {
            return;
        }
        visited.add(personId);
        for (PersonInterface friend : person.getAcquaintance()) {
            dfs((Person) friend, visited);
        }
    }

    private void updateCoupleSum(int id1, int id2, int oldBest1, int oldBest2) {
        Person person1 = (Person) persons.get(id1);
        Person person2 = (Person) persons.get(id2);
        int newBest1 = person1.bestAcquaintance();
        int newBest2 = person2.bestAcquaintance();
        if (newBest1 != oldBest1 || newBest2 != oldBest2) {
            if (newBest1 == id2 && newBest2 == id1) {
                coupleSum++;
            }
            if (oldBest1 == id2 && oldBest2 == id1) {
                coupleSum--;
            }
            if (newBest1 != oldBest1) {
                if (oldBest1 != id1 && newBest1 == id2
                    && ((Person) persons.get(oldBest1)).bestAcquaintance() == id1) {
                    coupleSum--;
                }
                if (newBest1 != id1 && newBest1 != id2
                    && ((Person) persons.get(newBest1)).bestAcquaintance() == id1) {
                    coupleSum++;
                }
            }
            if (newBest2 != oldBest2) {
                if (oldBest2 != id2 && newBest2 == id1
                    && ((Person) persons.get(oldBest2)).bestAcquaintance() == id2) {
                    coupleSum--;
                }
                if (newBest2 != id2 && newBest2 != id1
                    && ((Person) persons.get(newBest2)).bestAcquaintance() == id2) {
                    coupleSum++;
                }
            }
        }
    }

    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            return getPerson(id1).queryValue(getPerson(id2));
        }
    }

    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else {
            return uf.isConnected(id1, id2);
        }
    }

    public int queryTripleSum() {
        return tripleSum;
    }

    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else {
            PersonInterface person = getPerson(personId);
            if (person.containsTag(tag.getId())) {
                throw new EqualTagIdException(tag.getId());
            } else {
                tags.add(tag);  // 为了维护tagvalueSum

                person.addTag(tag);
            }
        }
    }

    public void addPersonToTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        } else {
            PersonInterface person1 = getPerson(personId1);
            PersonInterface person2 = getPerson(personId2);
            if (!person2.isLinked(person1)) {
                throw new RelationNotFoundException(personId2, personId1);
            } else if (!person2.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            } else {
                TagInterface tag = person2.getTag(tagId);
                if (tag.hasPerson(person1)) {
                    throw new EqualPersonIdException(personId1);
                } else {
                    if (tag.getSize() <= 999) {
                        tag.addPerson(person1);
                    }
                }
            }
        }
    }

    public int queryTagValueSum(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else {
            PersonInterface person = getPerson(personId);
            if (!person.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            } else {
                return person.getTag(tagId).getValueSum();
            }
        }
    }

    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else {
            PersonInterface person = getPerson(personId);
            if (!person.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            } else {
                return person.getTag(tagId).getAgeVar();
            }
        }
    }

    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else {
            PersonInterface person1 = getPerson(personId1);
            PersonInterface person2 = getPerson(personId2);
            if (!person2.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            } else {
                TagInterface tag = person2.getTag(tagId);
                if (!tag.hasPerson(person1)) {
                    throw new PersonIdNotFoundException(personId1);
                } else {
                    tag.delPerson(person1);
                }
            }
        }
    }

    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else {
            PersonInterface person = getPerson(personId);
            if (!person.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            } else {
                // 为了维护tagvalueSum
                TagInterface tag = person.getTag(tagId);
                tags.remove(tag);

                person.delTag(tagId);
            }
        }
    }

    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    public void addMessage(MessageInterface message)
        throws EqualMessageIdException, EmojiIdNotFoundException,
        EqualPersonIdException, ArticleIdNotFoundException {
        int messageId = message.getId();
        if (containsMessage(messageId)) {
            throw new EqualMessageIdException(messageId);
        } else {
            PersonInterface person1 = message.getPerson1();
            int type = message.getType();
            if (message instanceof EmojiMessageInterface) {
                int emojiId = ((EmojiMessageInterface) message).getEmojiId();
                if (!containsEmojiId(emojiId)) {
                    throw new EmojiIdNotFoundException(emojiId);
                } else {
                    if (type == 0 && person1.equals(message.getPerson2())) {
                        throw new EqualPersonIdException(person1.getId());
                    }
                    messages.put(messageId, message);
                }
            } else if (message instanceof ForwardMessageInterface) {
                int articleId = ((ForwardMessageInterface) message).getArticleId();
                if (!containsArticle(articleId)) {
                    throw new ArticleIdNotFoundException(articleId);
                } else {
                    if (!(person1.getReceivedArticles().contains(articleId))) {
                        throw new ArticleIdNotFoundException(articleId);
                    } else {
                        if (type == 0 && person1.equals(message.getPerson2())) {
                            throw new EqualPersonIdException(person1.getId());
                        }
                        messages.put(messageId, message);
                    }
                }
            } else {
                if (type == 0 && person1.equals(message.getPerson2())) {
                    throw new EqualPersonIdException(person1.getId());
                }
                messages.put(messageId, message);
            }
        }
    }

    public MessageInterface getMessage(int id) {
        return messages.get(id);
    }

    public void sendMessage(int id) throws RelationNotFoundException, MessageIdNotFoundException,
        TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        } else {
            MessageInterface message = messages.get(id);
            int messageId = message.getId();
            int messageSV = message.getSocialValue();
            int type = message.getType();
            if (type == 0) {
                PersonInterface person1 = message.getPerson1();
                PersonInterface person2 = message.getPerson2();
                if (!(person1.isLinked(person2))) {
                    throw new RelationNotFoundException(person1.getId(), person2.getId());
                } else {
                    if (person1 != person2) {
                        messages.remove(messageId);
                        person1.addSocialValue(messageSV);
                        person2.addSocialValue(messageSV);
                        if (message instanceof RedEnvelopeMessageInterface) {
                            int messageMoney = ((RedEnvelopeMessageInterface) message).getMoney();
                            person1.addMoney(-messageMoney);
                            person2.addMoney(messageMoney);
                        } else if (message instanceof ForwardMessageInterface) {
                            int articleId = ((ForwardMessageInterface) message).getArticleId();
                            ((Person) person2).receiveArticle(articleId);
                        } else if (message instanceof EmojiMessageInterface) {
                            int emojiId = ((EmojiMessageInterface) message).getEmojiId();
                            if (emojis.containsKey(emojiId)) {
                                emojis.compute(emojiId, (k, oldHeat) -> oldHeat + 1);
                            } else {
                                emojis.put(emojiId, 1);
                            }
                        }
                        ((Person) person2).receiveMessage(message);
                    }
                }
            } else if (type == 1) {
                PersonInterface person1 = message.getPerson1();
                TagInterface tag = message.getTag();
                if (!person1.containsTag(tag.getId())) {
                    throw new TagIdNotFoundException(tag.getId());
                } else {
                    messages.remove(messageId);
                    person1.addSocialValue(messageSV);
                    if (message instanceof RedEnvelopeMessageInterface) {
                        if (tag.getSize() > 0) {
                            int personNum = tag.getSize();
                            int totalMoney = ((RedEnvelopeMessageInterface) message).getMoney();
                            int perMoney = totalMoney / personNum;
                            person1.addMoney(-perMoney * personNum);  // 发送者扣除总金额
                            for (PersonInterface person : ((Tag) tag).getPersons()) { // 每个人获得平均金额
                                person.addMoney(perMoney);
                            }
                        }
                    } else if (message instanceof ForwardMessageInterface) {
                        if (tag.getSize() > 0) {
                            int articleId = ((ForwardMessageInterface) message).getArticleId();
                            for (PersonInterface person : ((Tag) tag).getPersons()) {
                                ((Person) person).receiveArticle(articleId);
                            }
                        }
                    } else if (message instanceof EmojiMessageInterface) {
                        int emojiId = ((EmojiMessageInterface) message).getEmojiId();
                        if (emojis.containsKey(emojiId)) {
                            emojis.compute(emojiId, (k, oldHeat) -> oldHeat + 1);
                        } else {
                            emojis.put(emojiId, 1);
                        }
                    }
                    for (PersonInterface person : ((Tag) tag).getPersons()) {
                        person.addSocialValue(messageSV);
                        ((Person) person).receiveMessage(message);
                    }
                }
            }
        }
    }

    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).getSocialValue();
        }
    }

    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).getReceivedMessages();
        }
    }

    public boolean containsEmojiId(int id) {
        return emojis.containsKey(id);
    }

    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        } else {
            emojis.put(id, 0);
        }
    }

    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).getMoney();
        }
    }

    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        } else {
            return emojis.get(id);
        }
    }

    public int deleteColdEmoji(int limit) {
        emojis.entrySet().removeIf(entry -> entry.getValue() < limit);
        messages.entrySet().removeIf(entry -> entry.getValue() instanceof EmojiMessage &&
            !containsEmojiId(((EmojiMessage) entry.getValue()).getEmojiId()));
        return emojis.size();
    }

    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            PersonInterface person = getPerson(id);
            if (((Person) person).acquaintanceSize() == 0) {
                throw new AcquaintanceNotFoundException(id);
            } else {
                return ((Person) person).bestAcquaintance();
            }
        }
    }

    public int queryCoupleSum() {
        return coupleSum;
    }

    public int queryShortestPath(int id1, int id2)
        throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else {
            if (id1 == id2) {
                return 0;
            }   // bfs 寻找最短路径
            PersonInterface start = persons.get(id1);
            PersonInterface end = persons.get(id2);
            Queue<PersonInterface> queue = new LinkedList<>();
            HashMap<PersonInterface, Integer> distances = new HashMap<>();
            queue.add(start);
            distances.put(start, 0);
            while (!queue.isEmpty()) {
                PersonInterface current = queue.poll();
                int currentDistance = distances.get(current);
                for (PersonInterface friend : ((Person) current).getAcquaintance()) {
                    if (!distances.containsKey(friend)) {
                        int newDistance = currentDistance + 1;
                        distances.put(friend, newDistance);
                        queue.add(friend);
                        if (friend.equals(end)) {
                            return newDistance;
                        }
                    }
                }
            }
            throw new PathNotFoundException(id1, id2);
        }
    }

    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    public void createOfficialAccount(int personId, int accountId, String name)
        throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        } else {
            OfficialAccount account = new OfficialAccount(personId, accountId, name);
            account.addFollower(persons.get(personId));
            accounts.put(accountId, account);
        }
    }

    public void deleteOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        } else {
            accounts.remove(accountId);
        }
    }

    public boolean containsArticle(int id) {
        return articles.containsKey(id);
    }

    public void contributeArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        } else {
            OfficialAccount account = accounts.get(accountId);
            PersonInterface person = getPerson(personId);
            if (!account.containsFollower(person)) {
                throw new ContributePermissionDeniedException(personId, articleId);
            } else {
                articles.put(articleId, personId);
                account.addArticle(person, articleId);
                account.informFollowersAdd(articleId);
            }
        }
    }

    public void deleteArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else {
            OfficialAccount account = accounts.get(accountId);
            if (!account.containsArticle(articleId)) {
                throw new ArticleIdNotFoundException(articleId);
            } else if (account.getOwnerId() != personId) {
                throw new DeleteArticlePermissionDeniedException(personId, articleId);
            } else {
                account.removeArticle(articleId);
                account.informFollowersRemove(articleId);
                account.reduceContribution(articles.get(articleId));
            }
        }
    }

    public void followOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else {
            PersonInterface person = getPerson(personId);
            OfficialAccount account = accounts.get(accountId);
            if (account.containsFollower(person)) {
                throw new EqualPersonIdException(personId);
            } else {
                account.addFollower(person);
            }
        }
    }

    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        } else {
            return accounts.get(id).getBestContributor();
        }
    }

    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).queryReceivedArticles();
        }
    }

    public PersonInterface[] getPersons() {
        return null;
    }
}