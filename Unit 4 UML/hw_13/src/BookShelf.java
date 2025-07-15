import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookShelf {
    private HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books = new HashMap<>();

    public BookShelf(Map<LibraryBookIsbn, Integer> map) {
        for (LibraryBookIsbn next : map.keySet()) {
            for (int i = 1; i <= map.get(next); i++) {
                String copyId;
                if (i >= 10) {
                    copyId = ((Integer) i).toString();
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(0);
                    sb.append(i);
                    copyId = sb.toString();
                }
                LibraryBookId bookId = new LibraryBookId(next.getType(), next.getUid(), copyId);
                if (books.containsKey(next)) {
                    books.get(next).add(bookId);
                } else {
                    ArrayList<LibraryBookId> list = new ArrayList<>();
                    list.add(bookId);
                    books.put(next, list);
                }
            }
        }
    }

    public boolean hasBook(LibraryBookIsbn bookIsbn) {
        if (books.containsKey(bookIsbn)) {
            if (!(books.get(bookIsbn)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public LibraryBookId takeout(LibraryBookIsbn bookIsbn) {
        return (books.get(bookIsbn)).remove(0);
    }

    public void put(LibraryBookId bookId) {
        books.get(bookId.getBookIsbn()).add(bookId);
    }

}
