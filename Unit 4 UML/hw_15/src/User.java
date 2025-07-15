import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;

public class User {
    private final String id;
    private final HashSet<LibraryBookId> booksA = new HashSet<>();
    private final HashSet<LibraryBookId> booksB = new HashSet<>();
    private final HashSet<LibraryBookId> booksC = new HashSet<>();
    private boolean hasOrdered = false; // 记录是否预约但没取
    private int creditScore = 100;
    private final HashMap<LibraryBookId, LocalDate> borrowTime = new HashMap<>();
    private final HashMap<LibraryBookId, LocalDate> lastDeductDate = new HashMap<>();

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

    public boolean canRead(LibraryBookIsbn bookIsbn) {
        if (bookIsbn.isTypeA()) {
            return creditScore >= 40;
        } else {
            return creditScore > 0;
        }
    }

    public void setOrderState(boolean ifOrdered) {
        hasOrdered = ifOrdered;
    }

    public void getBook(LibraryBookId bookId, LocalDate getTime) {
        borrowTime.put(bookId, getTime);
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
        lastDeductDate.remove(bookId);
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void creditAdd(int increment) {
        creditScore += increment;
        if (creditScore > 180) {
            creditScore = 180;
        } else if (creditScore < 0) {
            creditScore = 0;
        }
    }

    public boolean isOverdue(LibraryBookId bookId, LocalDate returnTime) {
        int between;
        if (bookId.isTypeB()) {
            between = 30;
        } else {    // type C
            between = 60;
        }
        if (ChronoUnit.DAYS.between(borrowTime.get(bookId), returnTime) > between) {
            return true;
        } else {
            return false;
        }
    }

    public String getId() {
        return id;
    }

    public void checkOverdue(LocalDate today) {    //检查逾期未还行为
        for (LibraryBookId bookId : booksB) {
            LocalDate borrowDate = borrowTime.get(bookId);
            LocalDate dueDate = borrowDate.plusDays(30); // B类书30天期限
            if (today.isAfter(dueDate)) {
                LocalDate startDate = lastDeductDate.getOrDefault(bookId, dueDate);
                long overdueDays = ChronoUnit.DAYS.between(startDate, today);
                if (overdueDays > 0) {
                    creditAdd(-5 * (int) overdueDays);
                    lastDeductDate.put(bookId, today);
                }
            }
        }
        for (LibraryBookId bookId : booksC) {
            LocalDate borrowDate = borrowTime.get(bookId);
            LocalDate dueDate = borrowDate.plusDays(60);
            if (today.isAfter(dueDate)) {
                LocalDate startDate = lastDeductDate.getOrDefault(bookId, dueDate);
                long overdueDays = ChronoUnit.DAYS.between(startDate, today);
                if (overdueDays > 0) {
                    creditAdd(-5 * (int) overdueDays);
                    lastDeductDate.put(bookId, today);
                }
            }
        }
    }
}
