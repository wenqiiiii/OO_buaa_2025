import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashSet;

public class User {
    private final String id;
    private final HashSet<LibraryBookId> booksA = new HashSet<>();
    private final HashSet<LibraryBookId> booksB = new HashSet<>();
    private final HashSet<LibraryBookId> booksC = new HashSet<>();
    private boolean hasOrdered = false; // 记录是否预约但没取

    public User(String studentId) {
        this.id = studentId;
    }

    public boolean canBorrow(LibraryBookIsbn bookIsbn) {
        if (bookIsbn.isTypeA()) {
            return false;
        } else if (bookIsbn.isTypeB()) {
            return booksB.isEmpty();
        } else if (bookIsbn.isTypeC()) {
            for (LibraryBookId book : booksC) {
                if (book.getBookIsbn().equals(bookIsbn)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean canOrder(LibraryBookIsbn bookIsbn) {
        if (hasOrdered) {
            return false;
        } else {
            if (bookIsbn.isTypeA()) {
                return false;
            } else if (bookIsbn.isTypeB()) {
                return booksB.isEmpty();
            } else if (bookIsbn.isTypeC()) {
                for (LibraryBookId book : booksC) {
                    if (book.getBookIsbn().equals(bookIsbn)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void setOrderState(boolean ifOrdered) {
        hasOrdered = ifOrdered;
    }

    public void getBook(LibraryBookId bookId) {
        switch (bookId.getType()) {
            case A:
                booksA.add(bookId);
                break;
            case B:
                booksB.add(bookId);
                break;
            case C:
                booksC.add(bookId);
                break;
            default:
        }
    }

    public void returnBook(LibraryBookId bookId) {
        switch (bookId.getType()) {
            case A:
                booksA.remove(bookId);
                break;
            case B:
                booksB.remove(bookId);
                break;
            case C:
                booksC.remove(bookId);
                break;
            default:
        }
    }

    public String getId() {
        return id;
    }
}
