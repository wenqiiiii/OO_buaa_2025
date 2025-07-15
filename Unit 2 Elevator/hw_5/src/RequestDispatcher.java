import com.oocourse.elevator1.PersonRequest;
import com.oocourse.elevator1.Request;

import java.util.HashMap;

public class RequestDispatcher extends Thread {
    private final RequestQueue requestQueue;
    private final HashMap<Integer, WaitingList> queueMap;       //<elevatorId,WaitingList>

    public RequestDispatcher(RequestQueue requestQueue,
        HashMap<Integer, WaitingList> queueMap) {
        this.requestQueue = requestQueue;
        this.queueMap = queueMap;
    }

    @Override
    public void run() {
        while (true) {
            if (requestQueue.isEmpty() && requestQueue.isEnd()) {
                for (WaitingList queue : queueMap.values()) {
                    queue.setEnd();
                }
                break;
            }
            Request request = requestQueue.poll();
            if (request == null) {
                continue;
            }
            dispatch(request);
        }
    }

    private void dispatch(Request request) {
        if (request instanceof PersonRequest) {
            Integer elevatorId = ((PersonRequest) request).getElevatorId();
            queueMap.get(elevatorId).offer(request);// 把乘客请求加入相应电梯的WaitingList
        }
    }
}

