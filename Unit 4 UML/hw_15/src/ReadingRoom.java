import com.oocourse.library3.LibraryBookId;

import java.util.HashSet;

public class ReadingRoom {
    private final HashSet<User> reader = new HashSet<>();
    private final HashSet<LibraryBookId> books = new HashSet<>();

    public ReadingRoom() {
    }

    public void addReader(User user, LibraryBookId bookId) {
        reader.add(user);
        books.add(bookId);
    }

    public void restoreBook(User user, LibraryBookId bookId) {
        reader.remove(user);
        books.remove(bookId);
    }

    public void clearReader() {
        for (User user : reader) {
            user.creditAdd(-10);
        }
        reader.clear();
    }

    public boolean hasReader(User user) {   // 查询当天阅读未归还
        return reader.contains(user);
    }

    public HashSet<LibraryBookId> takeout() {
        return books;
    }
}
