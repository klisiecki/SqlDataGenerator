package pl.poznan.put.SqlDataGenerator.restriction;

import org.apache.commons.lang3.StringUtils;

public class CustomString implements Comparable<CustomString> {
    public static final CustomString MIN_VALUE = new CustomString("A");
    public static final CustomString MAX_VALUE = new CustomString(StringUtils.repeat('z', 128));

    private final String string;

    public CustomString(String string) {
        this.string = string;
    }

    public CustomString(char c, int length) {
        this.string = StringUtils.repeat(c, length);
    }

    public int length() {
        return string.length();
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public int compareTo(CustomString o) {
        if (length() == o.length()) {
            return string.compareTo(o.toString());
        } else {
            return string.length() < o.length() ? -1 : 1;
        }
    }
}
