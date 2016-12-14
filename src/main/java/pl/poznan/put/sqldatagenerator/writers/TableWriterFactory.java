package pl.poznan.put.sqldatagenerator.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generator.BaseTable;

public class TableWriterFactory {
    private static final Logger logger = LoggerFactory.getLogger(TableWriterFactory.class);

    public static TableWriter createTableWriter(Class<? extends TableWriter> writerClass, BaseTable baseTable) {
        try {
            return writerClass.getConstructor(BaseTable.class).newInstance(baseTable);
        } catch (Exception e) {
            logger.warn("Error while creating " + writerClass + " instance. Using default writer");
            return new CSVTableWriter(baseTable);
        }
    }
}
