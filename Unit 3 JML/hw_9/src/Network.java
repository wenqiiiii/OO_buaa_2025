import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;

import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Network implements NetworkInterface {
    private final HashMap<Integer, PersonInterface> persons = new HashMap<>();
    private int tripleSum = 0;
    private final UnionFind uf = new UnionFind();

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
            ((Person) person1).link(person2, value);
            ((Person) person2).link(person1, value);
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
            if (person1.queryValue(person2) + value > 0) {
                ((Person) person1).valueIncrease(person2, value);
                ((Person) person2).valueIncrease(person1, value);
            } else {
                ((Person) person1).unlink(person2);
                ((Person) person2).unlink(person1);
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
                // 维护 tripleSum  end
                // 并查集重建
                HashSet<Integer> connectTo1 = new HashSet<>();  // 所有与 person1 连通的人
                dfs((Person) person1, connectTo1);
                if (!connectTo1.contains(id2)) {   // 边的删除影响了连通性，需要并查集重建
                    uf.rebuild(connectTo1, id1, id2);
                }
            }
        }
    }

    private void dfs(Person person, HashSet<Integer> visited) {
        int personId = person.getId();
        if (visited.contains(personId)) {
            return;
        }
        visited.add(personId);
        HashSet<PersonInterface> aquaintance = person.getAcquaintance();
        for (PersonInterface friend : aquaintance) {
            dfs((Person) friend, visited);
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
                person.delTag(tagId);
            }
        }
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

    public PersonInterface[] getPersons() { return null; }
}