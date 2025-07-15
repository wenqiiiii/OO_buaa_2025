public class FloorState {
    private int floor = 1;     // B4-F7

    public FloorState() {
    }

    public void up() {
        if (floor < 7) {
            if (floor == -1) {      //没有第0层
                floor = 1;
            } else {
                floor += 1;
            }
        }
    }

    public void down() {
        if (floor > -4) {
            if (floor == 1) {      //没有第0层
                floor = -1;
            } else {
                floor -= 1;
            }
        }
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        int num = floor;
        if (num < 0) {
            num = -num;
            sb.append("B");
        } else {
            sb.append("F");
        }
        sb.append(num);
        return sb.toString();
    }

    public int peek() {
        return floor;
    }
}
