package com.oocourse.spec3.main;

public interface EmojiMessageInterface extends MessageInterface {
    //@ public instance model int emojiId;

    //@ public invariant socialValue == emojiId;

    /*@ ensures type == 0;
      @ ensures tag == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures person2 == messagePerson2;
      @ ensures emojiId == emojiNumber;
      @*/

    /*@ ensures type == 1;
      @ ensures person2 == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures tag == messageTag;
      @ ensures emojiId == emojiNumber;
      @*/

    //@ ensures \result == emojiId;
    public /*@ pure @*/ int getEmojiId();
}
