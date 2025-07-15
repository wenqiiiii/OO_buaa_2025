import com.oocourse.elevator2.TimableOutput;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();  // 初始化时间戳

        RequestQueue requests = RequestQueue.Queue();
        ElvManager manager = ElvManager.Manager();
        manager.assemble();
        Dispatcher dispatcher = new Dispatcher(requests, manager.getlists());
        dispatcher.start();
        InputHandler inputHandler = new InputHandler(requests);
        inputHandler.start();
    }
}