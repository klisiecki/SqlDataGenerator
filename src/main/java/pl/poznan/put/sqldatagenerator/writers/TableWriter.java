package pl.poznan.put.sqldatagenerator.writers;

import java.util.List;

public interface TableWriter {
    void save(List<String> values);

    void closeWriter();
}
