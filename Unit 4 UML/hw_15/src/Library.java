import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;

import static com.oocourse.library3.LibraryBookState.BOOKSHELF;
import static com.oocourse.library3.LibraryBookState.APPOINTMENT_OFFICE;
import static com.oocourse.library3.LibraryBookState.BORROW_RETURN_OFFICE;
import static com.oocourse.library3.LibraryBookState.USER;
import static com.oocourse.library3.LibraryBookState.HOT_BOOKSHELF;
import static com.oocourse.library3.LibraryBookState.READING_ROOM;
import static com.oocourse.library3.LibraryIO.PRINTER;
import static com.oocourse.library3.LibraryIO.SCANNER;

public class Library implements Runnable {
    private LocalDate today;
    private final BookShelf bookShelf = new BookShelf();
    private final ReadingRoom readingRoom = new ReadingRoom();
    private final AppointmentOffice appointmentOffice = new AppointmentOffice();
    private final BorrowReturnOffice borrowReturnOffice = new BorrowReturnOffice();
    private final InquiryMachine inquiryMachine = new InquiryMachine();
    private final HashMap<String, User> users = new HashMap<>();
    private final HashMap<User, LibraryBookIsbn> orderReq = new HashMap<>();    // 待整理的已预约书籍
    private final ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
    private final HashSet<LibraryBookIsbn> hot = new HashSet<>();   // 记录当天被读的书

    public Library() {
    }

    public void run() {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();    // 获取图书馆内所有书籍ISBN号及相应副本数
        bookShelf.loadBook(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) {
                break;
            }
            today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                checkOverdue();
                clearBro();
                clearOverdueOrder("open");
                solveOrder();
                bookShelf.updateHot(today, hot, inquiryMachine, moveInfos);
                PRINTER.move(today, moveInfos);
                moveInfos.clear();
            } else if (command instanceof LibraryCloseCmd) {
                readingRoom.clearReader();  // 闭关赶人,没还书扣信用
                clearRr();
                clearOverdueOrder("close");
                PRINTER.move(today, moveInfos);
                moveInfos.clear();
            } else if (command instanceof LibraryQcsCmd) {
                String studentId = ((LibraryQcsCmd) command).getStudentId();
                User user;
                if (users.containsKey(studentId)) {
                    user = users.get(studentId);
                } else {
                    user = new User(studentId);
                    users.put(studentId, user);
                }
                PRINTER.info(command, user.getCreditScore());
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
                    case READ:
                        tryToRead(user, bookIsbn, command);
                        break;
                    case RESTORED:
                        bookId = req.getBookId();
                        restoreBook(user, bookId, command);
                        break;
                    default:
                }
            }
        }
    }

    private void queryTrace(LibraryBookId bookId) {
        PRINTER.info(today, bookId, inquiryMachine.quiryTrace(bookId));
    }

    @Trigger(from = "BookShelf", to = "User")
    private void tryToBorrow(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canBorrow(bookIsbn) && user.getCreditScore() >= 60
                && bookShelf.hasBook(bookIsbn)) {
            LibraryBookId bookId = bookShelf.takeout(bookIsbn);
            user.getBook(bookId, today);
            PRINTER.accept(command, bookId);
            hot.add(bookIsbn);
            if (bookShelf.isHot(bookIsbn)) {
                inquiryMachine.addTrace(bookId, today, HOT_BOOKSHELF, USER);
            } else {
                inquiryMachine.addTrace(bookId, today, BOOKSHELF, USER);
            }
        } else {
            PRINTER.reject(command);
        }
    }

    private void tryToOrder(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canOrder(bookIsbn) && user.getCreditScore() >= 100) {
            user.setOrderState(true);
            orderReq.put(user, bookIsbn);
            PRINTER.accept(command);
        } else {
            PRINTER.reject(command);
        }
    }

    @Trigger(from = "User", to = "BorrowReturnOffice")
    private void returnBook(User user, LibraryBookId bookId, LibraryCommand command) {
        user.returnBook(bookId);
        borrowReturnOffice.put(bookId);
        inquiryMachine.addTrace(bookId, today, USER, BORROW_RETURN_OFFICE);
        if (user.isOverdue(bookId, today)) {
            PRINTER.accept(command, "overdue");
        } else {
            PRINTER.accept(command, "not overdue");
            user.creditAdd(10);
        }
    }

    @Trigger(from = "AppointmentOffice", to = "User")
    private void tryToPick(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (user.canBorrow(bookIsbn)) {
            LibraryBookId bookId = appointmentOffice.pick(user, bookIsbn);
            if (bookId != null) {
                user.getBook(bookId, today);
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

    @Trigger(from = "BookShelf", to = "ReadingRoom")
    private void tryToRead(User user, LibraryBookIsbn bookIsbn, LibraryCommand command) {
        if (!bookShelf.hasBook(bookIsbn) || readingRoom.hasReader(user)
                || !user.canRead(bookIsbn)) {
            PRINTER.reject(command);
        } else {
            LibraryBookId bookId = bookShelf.takeout(bookIsbn);
            readingRoom.addReader(user, bookId);
            PRINTER.accept(command, bookId);
            hot.add(bookIsbn);
            if (bookShelf.isHot(bookIsbn)) {
                inquiryMachine.addTrace(bookId, today, HOT_BOOKSHELF, READING_ROOM);
            } else {
                inquiryMachine.addTrace(bookId, today, BOOKSHELF, READING_ROOM);
            }
        }
    }

    @Trigger(from = "ReadingRoom", to = "BorrowReturnOffice")
    private void restoreBook(User user, LibraryBookId bookId, LibraryCommand command) {
        readingRoom.restoreBook(user, bookId);
        borrowReturnOffice.put(bookId);
        inquiryMachine.addTrace(bookId, today, READING_ROOM, BORROW_RETURN_OFFICE);
        PRINTER.accept(command);
        user.creditAdd(10);
    }

    @Trigger(from = "ReadingRoom", to = "BookShelf")
    private void clearRr() {
        HashSet<LibraryBookId> books = readingRoom.takeout();
        Iterator<LibraryBookId> iterator = books.iterator();
        while (iterator.hasNext()) {
            LibraryBookId next = iterator.next();
            bookShelf.put(next);
            LibraryBookIsbn bookIsbn = next.getBookIsbn();
            if (bookShelf.isHot(bookIsbn)) {
                inquiryMachine.addTrace(next, today, READING_ROOM, HOT_BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(next, READING_ROOM, HOT_BOOKSHELF));
            } else {
                inquiryMachine.addTrace(next, today, READING_ROOM, BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(next, READING_ROOM, BOOKSHELF));
            }
        }
        books.clear();
    }

    @Trigger(from = "BorrowReturnOffice", to = "BookShelf")
    private void clearBro() {
        HashSet<LibraryBookId> books = borrowReturnOffice.takeout();
        Iterator<LibraryBookId> iterator = books.iterator();
        while (iterator.hasNext()) {
            LibraryBookId next = iterator.next();
            bookShelf.put(next);
            LibraryBookIsbn bookIsbn = next.getBookIsbn();
            if (bookShelf.isHot(bookIsbn)) {
                inquiryMachine.addTrace(next, today, BORROW_RETURN_OFFICE, HOT_BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(next, BORROW_RETURN_OFFICE, HOT_BOOKSHELF));
            } else {
                inquiryMachine.addTrace(next, today, BORROW_RETURN_OFFICE, BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(next, BORROW_RETURN_OFFICE, BOOKSHELF));
            }
        }
        books.clear();
    }

    @Trigger(from = "AppointmentOffice", to = "BookShelf")
    private void clearOverdueOrder(String time) {
        for (LibraryBookId bookId : appointmentOffice.clearOverdue(today, time)) {
            bookShelf.put(bookId);
            LibraryBookIsbn bookIsbn = bookId.getBookIsbn();
            if (bookShelf.isHot(bookIsbn)) {
                inquiryMachine.addTrace(bookId, today, APPOINTMENT_OFFICE, HOT_BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE, HOT_BOOKSHELF));
            } else {
                inquiryMachine.addTrace(bookId, today, APPOINTMENT_OFFICE, BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(bookId, APPOINTMENT_OFFICE, BOOKSHELF));
            }
        }
    }

    @Trigger(from = "BookShelf", to = "AppointmentOffice")
    private void solveOrder() {
        Iterator<User> iterator = orderReq.keySet().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            LibraryBookIsbn book = orderReq.get(user);
            if (bookShelf.hasBook(book)) {
                LibraryBookId bookId = bookShelf.takeout(book);
                appointmentOffice.put(user, bookId, today);
                if (bookShelf.isHot(book)) {
                    inquiryMachine.addTrace(bookId, today, HOT_BOOKSHELF, APPOINTMENT_OFFICE);
                    moveInfos.add(new LibraryMoveInfo(bookId, HOT_BOOKSHELF, APPOINTMENT_OFFICE,
                            user.getId()));
                } else {
                    inquiryMachine.addTrace(bookId, today, BOOKSHELF, APPOINTMENT_OFFICE);
                    moveInfos.add(new LibraryMoveInfo(bookId, BOOKSHELF, APPOINTMENT_OFFICE,
                            user.getId()));
                }
                iterator.remove();
            }
        }
    }

    private void checkOverdue() {    // 检查user的逾期未还行为，并扣除积分
        for (User user : users.values()) {
            user.checkOverdue(today);
        }
    }

    @SendMessage(from = ":Library", to = ":BookShelf")
    public void orderNewBook() {
    }
}
