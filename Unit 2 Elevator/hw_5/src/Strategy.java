import com.oocourse.elevator1.PersonRequest;

import java.util.ArrayList;
import java.util.Iterator;

public class Strategy {
    private final WaitingList waiting;
    private final ArrayList<PersonRequest> onboard = new ArrayList<>();
    private final ArrayList<PersonRequest> pending = new ArrayList<>();
    public static final int MAX_CAPACITY = 6;
    private String direction = "UP";

    public Strategy(WaitingList waitinglist) {
        this.waiting = waitinglist;
    }

    public String getAction(int floor) {
        if (!onboard.isEmpty()) {
            return direction;
        } else {
            if (waiting.isEmpty() && waiting.isEnd()) {
                return "END";
            } else {
                Boolean hasSameDirPR = waiting.hasSameDir(floor, direction);
                if (hasSameDirPR == null) {
                    return "END";
                } else if (hasSameDirPR) {
                    return direction;
                } else {
                    return "REVERSE";
                }
            }
        }
    }

    public Boolean shouldOpen(int floor) {      //判断是否需要开门，处理好乘客并准本好pending
        Boolean should = false;
        int size = onboard.size();
        for (PersonRequest request : onboard) {     //看看有谁下
            if (floorInt(request.getToFloor()) == floor) {
                if (size > 0) {
                    size--;
                    should = true;
                }
                if (size == 0) {
                    break;
                }
            }
        }
        pending.clear();        //看看谁能上，并准备好pending
        while (size < MAX_CAPACITY) {
            PersonRequest pickup = waiting.findPickup(floor, direction);
            if (pickup != null) {
                should = true;
                onboard.add(pickup);
                pending.add(pickup);
                size++;
            } else {
                break;
            }
        }
        return should;
    }

    public PersonRequest Unload(int floor) {
        Iterator<PersonRequest> iter = onboard.iterator();
        while (iter.hasNext()) {
            PersonRequest now = iter.next();
            if (floorInt(now.getToFloor()) == floor) {
                onboard.remove(now);
                return now;
            }
        }
        return null;
    }

    public PersonRequest load() {
        if (!pending.isEmpty()) {
            return pending.remove(0);
        }
        return null;
    }

    public String reverse() {
        if (direction.equals("UP")) {
            direction = "DOWN";
        } else {
            direction = "UP";
        }
        return direction;
    }

    private int floorInt(String f) {
        StringBuilder sb = new StringBuilder();
        if (f.charAt(0) == 'B') {
            sb.append('-');
        }
        sb.append(f.charAt(1));
        return Integer.parseInt(sb.toString());
    }
}
