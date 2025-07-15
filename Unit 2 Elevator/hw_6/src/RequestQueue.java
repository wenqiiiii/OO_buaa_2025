import com.oocourse.elevator2.PersonRequest;

import java.util.ArrayList;

public class RequestQueue {
    private final ArrayList<PersonRequest> requests = new ArrayList<>();
    private boolean isEnd = false;
    private static RequestQueue requestQueue = null;
    private static final Object requestQueueLock = new Object();

    private RequestQueue() {
    }

    public static RequestQueue Queue() {
        synchronized (requestQueueLock) {
            if (requestQueue == null) {
                requestQueue = new RequestQueue();
            }
        }
        return requestQueue;
    }

    public synchronized void put(PersonRequest pr) {
        requests.add(pr);
        notifyAll();
    }

    public synchronized PersonRequest poll() {
        if (requests.isEmpty() && !isEnd) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (requests.isEmpty()) {
            return null;
        }
        notifyAll();
        return requests.remove(0);
    }

    public synchronized void setEnd() {
        isEnd = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        notifyAll();
        return isEnd;
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return requests.isEmpty();
    }

}
