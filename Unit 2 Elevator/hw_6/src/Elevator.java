import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;

public class Elevator extends Thread {
    private final int id;
    private final FloorState cur = new FloorState();
    private DoorState doorState = DoorState.CLOSED;
    private final Strategy strategy;
    private String dir = "UP";
    private ScheRequest scheRequest = null;
    private final Object scheRequestlock = new Object();
    private Boolean inSche = false;
    private final Object inSchelock = new Object();
    private ArrayList<PersonRequest> canceled = null;

    enum DoorState {
        OPEN,
        CLOSED
    }

    public Elevator(int id, WaitingList waitinglist) {
        this.id = id;
        this.strategy = new Strategy(waitinglist);
    }

    @Override
    public void run() {
        Boolean isEnd = false;
        while (!isEnd || scheRequest != null) {
            if (strategy.shouldOpen(cur.peek())) {
                openDoor();
                handlePassengers();
                try {
                    Thread.sleep(400);      //保持开门状态，保证开关门之间不小于0.4s
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                closeDoor();
            }
            if (scheRequest != null) {   //查看SCHE请求
                long speed = (long) (scheRequest.getSpeed() * 1000);
                int target = floorInt(scheRequest.getToFloor());
                solveSche(speed, target);
                synchronized (scheRequestlock) {
                    scheRequest = null;    //处理完要置空
                    scheRequestlock.notifyAll();    // 告诉 informSche 可以接受下一个SCHE请求了
                }
                continue;
            }
            switch (strategy.getAction(cur.peek())) {
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
        int floor = cur.peek();
        while (true) {
            PersonRequest unload = strategy.Unload(floor);
            if (unload == null) {
                break;
            }
            String out = String.format("OUT-S-%d-%s-%d", unload.getPersonId(), cur.print(), id);
            TimableOutput.println(out);
        }

        // 处理上电梯
        while (true) {
            PersonRequest load = strategy.load();
            if (load == null) {
                break;
            }
            String out = String.format("IN-%d-%s-%d", load.getPersonId(), cur.print(), id);
            TimableOutput.println(out);
        }
    }

    private void up() {
        try {
            cur.up();
            Thread.sleep(400);
            String formatted = String.format("ARRIVE-%s-%d", cur.print(), id);
            TimableOutput.println(formatted);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void down() {
        try {
            cur.down();
            Thread.sleep(400); // 移动耗时
            String formatted = String.format("ARRIVE-%s-%d", cur.print(), id);
            TimableOutput.println(formatted);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void openDoor() {
        if (doorState == DoorState.CLOSED) {
            String formatted = String.format("OPEN-%s-%d", cur.print(), id);
            TimableOutput.println(formatted);
            doorState = DoorState.OPEN;
        }
    }

    private void closeDoor() {
        if (doorState == DoorState.OPEN) {
            String formatted = String.format("CLOSE-%s-%d", cur.print(), id);
            TimableOutput.println(formatted);
            doorState = DoorState.CLOSED;
        }
    }

    public void informsche(ScheRequest sche) {
        synchronized (scheRequestlock) {
            if (scheRequest != null) {
                try {
                    scheRequestlock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }     //上一个SCHE请求未完成，需要等待
            scheRequest = sche;
            scheRequestlock.notifyAll();
        }
    }

    private void solveSche(long speed, int target) {
        synchronized (inSchelock) {
            inSche = true;
            inSchelock.notifyAll();
        }
        String scheBegin = String.format("SCHE-BEGIN-%d", id);
        TimableOutput.println(scheBegin);
        canceled = strategy.cancelReceive();               //取消此前所有的receive
        while (cur.peek() != target) {      //移动到检修楼层
            try {
                if (cur.peek() > target) {
                    cur.down();
                } else {
                    cur.up();
                }
                Thread.sleep(speed);      // 指定速度移动耗时
                String formatted = String.format("ARRIVE-%s-%d", cur.print(), id);
                TimableOutput.println(formatted);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        openDoor();     //无论是否有人，出于检修目的必须开门且维持 1s
        unloadForSche();
        try {
            Thread.sleep(1000);      //保持开门状态，检修耗时 1s
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        closeDoor();
        String scheEnd = String.format("SCHE-END-%d", id);
        TimableOutput.println(scheEnd);
        restoreReceive();
        inSche = false;
    }

    private void restoreReceive() {
        for (PersonRequest cancel : canceled) {
            String receive = String.format("RECEIVE-%d-%d", cancel.getPersonId(), id);
            TimableOutput.println(receive);
        }
        canceled.clear();
    }

    private void unloadForSche() {
        while (true) {      //赶人
            PersonRequest unload = strategy.UnloadForSche();
            if (unload == null) {
                break;
            }
            if (cur.peek() == floorInt(unload.getToFloor())) {
                String out = String.format("OUT-S-%d-%s-%d", unload.getPersonId(), cur.print(), id);
                TimableOutput.println(out);
            } else {
                String tmp = String.format("OUT-F-%d-%s-%d", unload.getPersonId(), cur.print(), id);
                TimableOutput.println(tmp);
                PersonRequest one = new PersonRequest(cur.print(), unload.getToFloor(),
                        unload.getPersonId(), unload.getPriority());
                strategy.offer(one);    //重新写一个PR
                canceled.add(one);
            }
        }
    }

    private int floorInt(String f) {
        StringBuilder sb = new StringBuilder();
        if (f.charAt(0) == 'B') {
            sb.append('-');
        }
        sb.append(f.charAt(1));
        return Integer.parseInt(sb.toString());
    }

    public Boolean isInSche() {
        synchronized (inSchelock) {
            inSchelock.notifyAll();
            return inSche;
        }
    }

}
