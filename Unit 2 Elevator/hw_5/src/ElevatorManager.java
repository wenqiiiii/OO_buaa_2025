import java.util.HashMap;

public class ElevatorManager {
    private static final int eleNum = 6;
    private final HashMap<Integer, Elevator> elvsMap = new HashMap<>();
    private final HashMap<Integer, WaitingList> waitinglistMap = new HashMap<>();
    private static ElevatorManager manager = null;

    private ElevatorManager() {
        assemble(eleNum);
    }

    public static ElevatorManager getManager() {
        if (manager == null) {
            manager = new ElevatorManager();
        }
        return manager;
    }

    private void assemble(Integer num) {
        for (int i = 1; i <= num; i++) {
            WaitingList queue = new WaitingList();
            Elevator elevator = new Elevator(i, queue);
            elvsMap.put(i, elevator);
            waitinglistMap.put(i, queue);
            elevator.start();
        }
    }

    public synchronized HashMap<Integer, WaitingList> getWaitinglistMap() {
        return this.waitinglistMap;
    }

}
