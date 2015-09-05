package pl.poznan.put.SqlDataGenerator.restriction;

import org.apache.commons.lang3.StringUtils;

public class MyString implements Comparable<MyString> {
    public static final MyString MIN_VALUE = new MyString("b");
    public static final MyString MAX_VALUE = new MyString(StringUtils.repeat('z', 128));



    String string;

    public MyString(String string) {
        this.string = string;
    }

    public MyString(char c, int length) {
        this.string = StringUtils.repeat(c, length);
    }

    public int lenght() {
        return string.length();
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public int compareTo(MyString o) {
        if (lenght() == o.lenght()) {
            return string.compareTo(o.toString());
        } else {
            return string.length() < o.lenght() ? -1 : 1;
        }
    }
}
