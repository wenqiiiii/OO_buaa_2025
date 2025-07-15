import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class InquiryMachine {
    private final HashMap<LibraryBookId, ArrayList<LibraryTrace>> traces = new HashMap<>();

    public InquiryMachine() {
    }

    public void addTrace(LibraryBookId bookId, LocalDate date,
        LibraryBookState start, LibraryBookState end) {
        LibraryTrace trace = new LibraryTrace(date, start, end);
        if (!traces.containsKey(bookId)) {
            traces.put(bookId, new ArrayList<>());
        }
        traces.get(bookId).add(trace);
    }

    public ArrayList<LibraryTrace> quiryTrace(LibraryBookId bookId) {
        if (traces.containsKey(bookId)) {
            return traces.get(bookId);
        }
        return new ArrayList<>();
    }
}
