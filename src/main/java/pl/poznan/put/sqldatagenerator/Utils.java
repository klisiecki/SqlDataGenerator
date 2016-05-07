package pl.poznan.put.sqldatagenerator;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class Utils {
    public static String readFile(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public static void intersectRangeSets(RangeSet a, RangeSet b) {
        RangeSet aClone = TreeRangeSet.create(a);
        a.clear();
        for (Range aRange : (Set<Range>) aClone.asRanges()) {
            for (Range bRange:  (Set<Range>) b.asRanges()) {
                try {
                    Range r = bRange.intersection(aRange);
                    a.add(r);
                } catch (IllegalArgumentException ignore) {}
            }
        }
    }
}
