import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private final int id;
    private int socialValue;
    private int type;
    private PersonInterface person1;
    private PersonInterface person2 = null;
    private TagInterface tag = null;

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, PersonInterface messagePerson2) {
        id = messageId;
        socialValue = messageSocialValue;
        type = 0;
        person1 = messagePerson1;
        person2 = messagePerson2;
    }

    public Message(int messageId, int messageSocialValue,
        PersonInterface messagePerson1, TagInterface messageTag) {
        id = messageId;
        socialValue = messageSocialValue;
        type = 1;
        person1 = messagePerson1;
        tag = messageTag;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSocialValue() {
        return socialValue;
    }

    public PersonInterface getPerson1() {
        return person1;
    }

    public PersonInterface getPerson2() {
        return person2;
    }

    public TagInterface getTag() {
        return tag;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MessageInterface)) {
            return false;
        }
        return ((MessageInterface) obj).getId() == id;
    }

}