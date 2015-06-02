package pl.poznan.put.SqlDataGenerator.restriction;

import java.lang.reflect.Type;

public abstract class Restriction {
    public static <T> Restriction getForObject(Class<T> type) {
//        if (object instanceof Integer) {
//            return new NumberRestriction<Integer>();
//        } else if (object instanceof Float) {
//            return new NumberRestriction<Float>();
//        } else if (object instanceof String) {
//            return new StringRestriction();
//        } else {
//            throw new RuntimeException("Restriction for type " + object.getClass() + " not implemented");
//        }
        return null;
    }
}
