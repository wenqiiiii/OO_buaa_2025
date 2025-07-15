import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.TimableOutput;

public class Elevator extends Thread {
    private final int id;
    private final FloorState curFloor = new FloorState();
    private DoorState doorState = DoorState.CLOSED;
    private final WaitingList waitinglist;
    private final Strategy strategy;
    private String dir = "UP";

    enum DoorState {
        OPEN,
        CLOSED
    }

    public Elevator(int id, WaitingList waitinglist) {
        this.id = id;
        this.waitinglist = waitinglist;
        this.strategy = new Strategy(waitinglist);
    }

    @Override
    public void run() {
        Boolean isEnd = false;
        while (!isEnd) {
            if (strategy.shouldOpen(curFloor.peek())) {
                openDoor();
                handlePassengers();
                closeDoor();
            }
            switch (strategy.getAction(curFloor.peek())) {
                case "UP":
                    up();
                    break;
                case "DOWN":
                    down();
                    break;
                case "END":
                    isEnd = true;
                    break;
                case "REVERSE":
                    dir = strategy.reverse();
                    break;
                default:
            }
        }
    }

    private void handlePassengers() {
        // 处理下电梯
        int floor = curFloor.peek();
        while (true) {
            PersonRequest unload = strategy.Unload(floor);
            if (unload == null) {
                break;
            }
            String out = String.format("OUT-%d-%s-%d", unload.getPersonId(), curFloor.print(), id);
            TimableOutput.println(out);
        }
        // 处理上电梯
        while (true) {
            PersonRequest load = strategy.load();
            if (load == null) {
                break;
            }
            String out = String.format("IN-%d-%s-%d", load.getPersonId(), curFloor.print(), id);
            TimableOutput.println(out);
        }
    }

    private void up() {
        try {
            curFloor.up();
            Thread.sleep(400);
            String formatted = String.format("ARRIVE-%s-%d", curFloor.print(), id);
            TimableOutput.println(formatted);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void down() {
        try {
            curFloor.down();
            Thread.sleep(400); // 移动耗时
            String formatted = String.format("ARRIVE-%s-%d", curFloor.print(), id);
            TimableOutput.println(formatted);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void openDoor() {
        if (doorState == DoorState.CLOSED) {
            String formatted = String.format("OPEN-%s-%d", curFloor.print(), id);
            TimableOutput.println(formatted);
            doorState = DoorState.OPEN;
        }
    }

    private void closeDoor() {
        if (doorState == DoorState.OPEN) {
            try {
                Thread.sleep(400);      //保持开门状态，保证开关门之间不小于0.4s
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String formatted = String.format("CLOSE-%s-%d", curFloor.print(), id);
            TimableOutput.println(formatted);
            doorState = DoorState.CLOSED;
        }
    }

}
