import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;

public class InputHandler extends Thread {
    private final RequestQueue requestQueue;

    public InputHandler(RequestQueue queue) {
        requestQueue = queue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                requestQueue.setEnd();
                break;
            }
            if (request instanceof ScheRequest) {       // 临时调度直接通知ElevatorManager进行检修
                ElvManager.Manager().scheElevator((ScheRequest) request);
            } else if (request instanceof PersonRequest) {
                requestQueue.put((PersonRequest) request);
            }
        }
    }
}
