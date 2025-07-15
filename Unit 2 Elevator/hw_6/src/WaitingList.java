import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.util.ArrayList;
import java.util.Iterator;

public class WaitingList {
    private final ArrayList<PersonRequest> personRequests = new ArrayList<>();
    private boolean isEnd = false;

    public synchronized void offer(Request request) {
        if (request instanceof PersonRequest) {
            personRequests.add((PersonRequest) request);
        }
        notifyAll();
    }

    public synchronized Boolean hasSameDir(int floor, String dir) {
        while (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (isEmpty() && isEnd()) {
            return null;
        }
        Iterator<PersonRequest> iter = personRequests.iterator();
        while (iter.hasNext()) {
            PersonRequest pr = iter.next();
            if (floorInt(pr.getFromFloor()) > floor && dir.equals("UP")) {
                return true;
            } else if (floorInt(pr.getFromFloor()) < floor && dir.equals("DOWN")) {
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<PersonRequest> canceled() {
        ArrayList<PersonRequest> canceled = new ArrayList<>();
        for (PersonRequest pr : personRequests) {
            canceled.add(pr);
        }
        return canceled;
    }

    public synchronized PersonRequest findPickup(int floor, String direction) {
        PersonRequest pickup = null;
        Iterator<PersonRequest> iter = personRequests.iterator();
        while (iter.hasNext()) {
            PersonRequest now = iter.next();
            if (canPickup(floor, direction, now)) {
                pickup = now;
                personRequests.remove(pickup);
                break;
            }
        }
        return pickup;
    }

    private Boolean canPickup(int floor, String direction, PersonRequest one) {
        //策略是携带from相同且同向
        int from = floorInt(one.getFromFloor());
        if (from == floor) {
            int to = floorInt(one.getToFloor());
            if ((to > from) && direction.equals("UP")) {
                return true;
            } else if ((to < from) && direction.equals("DOWN")) {
                return true;
            }
        }
        return false;
    }

    private int floorInt(String f) {
        StringBuilder sb = new StringBuilder();
        if (f.charAt(0) == 'B') {
            sb.append('-');
        }
        sb.append(f.charAt(1));
        return Integer.parseInt(sb.toString());
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return personRequests.isEmpty();
    }
}
