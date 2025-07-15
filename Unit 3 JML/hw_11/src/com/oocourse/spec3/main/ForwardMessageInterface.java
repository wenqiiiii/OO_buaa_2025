package com.oocourse.spec3.main;

public interface ForwardMessageInterface extends MessageInterface {
    //@ public instance model non_null int articleId;

    //@ public invariant socialValue == abs(articleId) % 200;
    //此处abs()表示取绝对值

    /*@ ensures type == 0;
      @ ensures tag == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures person2 == messagePerson2;
      @ ensures articleId == article;
      @*/

    /*@ ensures type == 1;
      @ ensures person2 == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures tag == messageTag;
      @ ensures articleId == article;
      @*/

    //@ ensures \result == articleId;
    public /*@ pure @*/ int getArticleId();
}
