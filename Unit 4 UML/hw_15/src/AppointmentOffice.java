import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AppointmentOffice {  // 预约处
    private final HashMap<User, LibraryBookId> orderList = new HashMap<>();
    private final HashMap<User, LocalDate> orderTime = new HashMap<>();

    public AppointmentOffice() {
    }

    public void put(User user, LibraryBookId bookId, LocalDate date) {
        orderList.put(user, bookId);
        orderTime.put(user, date);
    }

    public LibraryBookId pick(User user, LibraryBookIsbn bookIsbn) {
        if (orderList.containsKey(user)) {
            if (orderList.get(user).getBookIsbn().equals(bookIsbn)) {
                orderTime.remove(user);
                return orderList.remove(user);
            }
        }
        return null;

    }

    public ArrayList<LibraryBookId> clearOverdue(LocalDate date, String time) {
        int between;
        if (time.equals("close")) {
            between = 4;
        } else {
            between = 5;
        }
        ArrayList<LibraryBookId> overdue = new ArrayList<>();
        Iterator<User> iterator = orderTime.keySet().iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            LocalDate orderDate = orderTime.get(user);
            if (ChronoUnit.DAYS.between(orderDate, date) >= between) {
                user.setOrderState(false);  // 逾期后，user可视作未预约
                user.creditAdd(-15);
                overdue.add(orderList.remove(user));
                iterator.remove();
            }
        }
        return overdue;
    }

}
