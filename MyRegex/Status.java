package MyRegex;

import java.util.Objects;

/**
 * 返回匹配到的下标i
 */
class Status {
    private int i;

    Status(int i) {
        this.i = i;
    }

    int getI() {
        return i;
    }

    @Override
    public boolean equals(Object o) {
        Status status = (Status) o;
        return i == status.i;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i);
    }
}
