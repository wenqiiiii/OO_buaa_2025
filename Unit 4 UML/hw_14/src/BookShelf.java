import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryMoveInfo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;

import static com.oocourse.library2.LibraryBookState.HOT_BOOKSHELF;
import static com.oocourse.library2.LibraryBookState.BOOKSHELF;

public class BookShelf {
    private HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books = new HashMap<>();
    private HashSet<LibraryBookIsbn> hotBooks = new HashSet<>(); // 记录了热门书籍

    public BookShelf() {
    }

    public void loadBook(Map<LibraryBookIsbn, Integer> map) {
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

    public boolean isHot(LibraryBookIsbn bookIsbn) {
        return hotBooks.contains(bookIsbn);
    }

    public void updateHot(LocalDate today, HashSet<LibraryBookIsbn> newHot,
        InquiryMachine inquiryMachine, ArrayList<LibraryMoveInfo> moveInfos) {
        Iterator<LibraryBookIsbn> iterator = hotBooks.iterator();
        while (iterator.hasNext()) {
            LibraryBookIsbn bookIsbn = iterator.next();
            if (!newHot.contains(bookIsbn)) {   // 过气书籍
                ArrayList<LibraryBookId> outdatedBooks = books.get(bookIsbn);
                for (LibraryBookId bookId : outdatedBooks) {
                    inquiryMachine.addTrace(bookId, today, HOT_BOOKSHELF, BOOKSHELF);
                    moveInfos.add(new LibraryMoveInfo(bookId, HOT_BOOKSHELF, BOOKSHELF));
                }
                iterator.remove();
            }
        }
        for (LibraryBookIsbn bookIsbn : newHot) {
            if (!hotBooks.contains(bookIsbn)) {   // 新热门
                ArrayList<LibraryBookId> newHotBooks = books.get(bookIsbn);
                for (LibraryBookId bookId : newHotBooks) {
                    inquiryMachine.addTrace(bookId, today, BOOKSHELF, HOT_BOOKSHELF);
                    moveInfos.add(new LibraryMoveInfo(bookId, BOOKSHELF, HOT_BOOKSHELF));
                }
                hotBooks.add(bookIsbn);
            }
        }
        newHot.clear();
    }
}
