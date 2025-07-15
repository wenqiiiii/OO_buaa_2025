import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.Request;

public class InputHandler extends Thread {
    private final RequestQueue requestQueue;

    public InputHandler(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
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
            requestQueue.offer(request);
        }
    }
}
