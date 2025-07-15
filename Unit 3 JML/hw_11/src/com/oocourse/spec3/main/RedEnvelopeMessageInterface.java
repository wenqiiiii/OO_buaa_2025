package com.oocourse.spec3.main;

public interface RedEnvelopeMessageInterface extends MessageInterface {
    //@ public instance model int money;

    //@ public invariant socialValue == money * 5;

    /*@ ensures type == 0;
      @ ensures tag == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures person2 == messagePerson2;
      @ ensures money == luckyMoney;
      @*/

    /*@ ensures type == 1;
      @ ensures person2 == null;
      @ ensures id == messageId;
      @ ensures person1 == messagePerson1;
      @ ensures tag == messageTag;
      @ ensures money == luckyMoney;
      @*/

    //@ ensures \result == money;
    public /*@ pure @*/ int getMoney();
}
