import com.oocourse.elevator1.TimableOutput;

public class MainClass {
    public static void main(String[] args) throws Exception {
        TimableOutput.initStartTimestamp();  // 初始化时间戳

        RequestQueue requests = new RequestQueue();
        ElevatorManager manager = ElevatorManager.getManager();
        RequestDispatcher dispatcher = new RequestDispatcher(requests, manager.getWaitinglistMap());
        dispatcher.start();
        InputHandler inputHandler = new InputHandler(requests);
        inputHandler.start();
    }
}