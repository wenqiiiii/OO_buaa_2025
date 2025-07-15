import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryMoveInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;

import static com.oocourse.library1.LibraryBookState.BOOKSHELF;
import static com.oocourse.library1.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library1.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library1.LibraryBookState.USER;
import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryIO.SCANNER;

public class Library implements Runnable {
    private LocalDate today;
    private BookShelf bookShelf;
    private final AppointmentOffice appointmentOffice = new AppointmentOffice();
    private final BorrowReturnOffice borrowReturnOffice = new BorrowReturnOffice();
    private final InquiryMachine inquiryMachine = new InquiryMachine();
    private final HashMap<String, User> users = new HashMap<>();
    private final HashMap<User, LibraryBookIsbn> orderReq = new HashMap<>();    // 待整理的已预约书籍
    private final ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();

    public Library() {
    }

    public void run() {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();    // 获取图书馆内所有书籍ISBN号及相应副本数
        bookShelf = new BookShelf(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                clearOverdue("open");
                solveOrder();
                PRINTER.move(today, moveInfos);
                moveInfos.clear();
            } else if (command instanceof LibraryCloseCmd) {
                clearBro();
                clearOverdue("close");
                PRINTER.move(today, moveInfos);
                moveInfos.clear();
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType(); // 指令对应的类型
                LibraryBookIsbn bookIsbn = req.getBookIsbn();
                LibraryBookId bookId;
                String studentId = req.getStudentId();
                User user;
                if (users.containsKey(studentId)) {
                    user = users.get(studentId);
                } else {
                    user = new User(studentId);
                    users.put(studentId, user);
                }
                switch (type) {
                    case QUERIED:
                        bookId = req.getBookId();
                        queryTrace(bookId);
                        break;
                    case BORROWED:
                        tryToBorrow(user, bookIsbn, command);
                        break;
                    case ORDERED:
                        tryToOrder(user, bookIsbn, command);
                        break;
                    case RETURNED:
                        bookId = req.getBookId();
                        returnBook(user, bookId, command);
                        break;
                    case PICKED:
                        tryToPick(user, bookIsbn, command);
                        break;
                    default:
                }
            }
        }
    }

    private void queryTrace(LibraryBookId bookId) {
        PRINTER.info(today, bookId, inquiryMachine.quiryTrace(bookId));
    }

    private void tryToBorrow(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canBorrow(bookIsbn) && bookShelf.hasBook(bookIsbn)) {
            LibraryBookId bookId = bookShelf.takeout(bookIsbn);
            user.getBook(bookId);
            PRINTER.accept(command, bookId);
            inquiryMachine.addTrace(bookId, today, BOOKSHELF, USER);
        } else {
            PRINTER.reject(command);
        }
    }

    private void tryToOrder(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canOrder(bookIsbn)) {
            user.setOrderState(true);
            orderReq.put(user, bookIsbn);
            PRINTER.accept(command);
        } else {
            PRINTER.reject(command);
        }
    }

    private void returnBook(User user, LibraryBookId bookId, LibraryCommand command) {
        user.returnBook(bookId);
        borrowReturnOffice.put(bookId);
        inquiryMachine.addTrace(bookId, today, USER, BORROW_RETURN_OFFICE);
        PRINTER.accept(command);
    }

    private void tryToPick(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canBorrow(bookIsbn)) {
            LibraryBookId bookId = appointmentOffice.pick(user, bookIsbn);
            if (bookId != null) {
                user.getBook(bookId);
                user.setOrderState(false);
                inquiryMachine.addTrace(bookId, today, APPOINTMENT_OFFICE, USER);
                PRINTER.accept(command, bookId);
            } else {
                PRINTER.reject(command);
            }
        } else {
            PRINTER.reject(command);
        }
    }

    private void clearBro() {
        HashSet<LibraryBookId> books = borrowReturnOffice.takeout();
        Iterator<LibraryBookId> iterator = books.iterator();
        while (iterator.hasNext()) {
            LibraryBookId next = iterator.next();
            bookShelf.put(next);
            inquiryMachine.addTrace(next, today, BORROW_RETURN_OFFICE, BOOKSHELF);
            moveInfos.add(new LibraryMoveInfo(next, BORROW_RETURN_OFFICE, BOOKSHELF));
        }
        books.clear();
    }

    private void solveOrder() {
        // 为预约准备书籍
        Iterator<User> iterator = orderReq.keySet().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            LibraryBookIsbn book = orderReq.get(user);
            if (bookShelf.hasBook(book)) {
                LibraryBookId bookId = bookShelf.takeout(book);
                appointmentOffice.put(user, bookId, today);
                inquiryMachine.addTrace(bookId, today, BOOKSHELF, APPOINTMENT_OFFICE);
                moveInfos.add(new LibraryMoveInfo(bookId, BOOKSHELF, APPOINTMENT_OFFICE,
                        user.getId()));
                iterator.remove();
            }
        }
    }

    private void clearOverdue(String time) {
        // 清理预期预约
        for (LibraryBookId bookId : appointmentOffice.clearOverdue(today, time)) {
            bookShelf.put(bookId);
            inquiryMachine.addTrace(bookId, today, APPOINTMENT_OFFICE, BOOKSHELF);
            moveInfos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE, BOOKSHELF));
        }
    }

}
