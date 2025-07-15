import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.HashMap;

public class Dispatcher extends Thread {
    private final HashMap<Integer, WaitingList> queMap;
    private final RequestQueue requestQueue;
    private int turn = 1;

    public Dispatcher(RequestQueue queue, HashMap<Integer, WaitingList> lists) {
        requestQueue = queue;
        queMap = lists;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEmpty() && requestQueue.isEnd()) {
                for (WaitingList queue : queMap.values()) {
                    queue.setEnd();
                }
                break;
            }
            PersonRequest pr = requestQueue.poll();
            if (pr == null) {
                continue;
            }
            dispatch(pr);
        }
    }

    private void dispatch(PersonRequest pr) {
        while (ElvManager.Manager().CheckInSche(turn)) {    //你有没有想过如果都在sche，你会轮询
            if (turn == 6) {
                turn = 1;
            } else {
                turn++;
            }
        }
        String receive = String.format("RECEIVE-%d-%d", pr.getPersonId(), turn); //RECEIVE-乘客ID-电梯ID
        TimableOutput.println(receive);
        queMap.get(turn).offer(pr);
        if (turn == 6) {
            turn = 1;
        } else {
            turn++;
        }
    }
}

