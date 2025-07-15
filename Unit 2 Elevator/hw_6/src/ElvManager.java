import com.oocourse.elevator2.ScheRequest;

import java.util.HashMap;

public class ElvManager {
    private static ElvManager manager = null;
    private static final Object managerLock = new Object();
    private static final int eleNum = 6;
    private final HashMap<Integer, Elevator> elevators = new HashMap<>();        //key： elevatorId
    private final HashMap<Integer, WaitingList> waitinglists = new HashMap<>();

    private ElvManager() {
    }

    public static ElvManager Manager() {
        synchronized (managerLock) {
            if (manager == null) {
                manager = new ElvManager();
            }
        }
        return manager;
    }

    public void assemble() {
        for (int i = 1; i <= eleNum; i++) {
            WaitingList queue = new WaitingList();
            Elevator elevator = new Elevator(i, queue);
            elevators.put(i, elevator);
            waitinglists.put(i, queue);
            elevator.start();
        }
    }

    public synchronized HashMap<Integer, WaitingList> getlists() {
        return this.waitinglists;
    }

    public Boolean CheckInSche(int elvId) {
        if (elevators.get(elvId).isInSche()) {
            return true;
        }
        return false;
    }

    public void scheElevator(ScheRequest sche) {
        elevators.get(sche.getElevatorId()).informsche(sche);
    }

}
