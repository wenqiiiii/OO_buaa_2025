import com.oocourse.library2.LibraryBookId;

import java.util.HashSet;

public class BorrowReturnOffice {
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public void put(LibraryBookId bookId) {
        books.add(bookId);
    }

    public HashSet<LibraryBookId> takeout() {
        return books;
    }
}
