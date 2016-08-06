package pl.poznan.put.sqldatagenerator.writers;

import com.opencsv.CSVWriter;
import pl.poznan.put.sqldatagenerator.configuration.Configuration;
import pl.poznan.put.sqldatagenerator.configuration.ConfigurationKeys;
import pl.poznan.put.sqldatagenerator.generator.BaseTable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVTableWriter implements TableWriter {

    private final Configuration configuration = Configuration.getInstance();
    private final int rowsPerFile = configuration.getIntegerProperty(ConfigurationKeys.MAX_ROWS_PER_FILE, 100000);
    private final String path = configuration.getOutputPath();

    private CSVWriter writer;
    private int fileNum = 0;
    private final BaseTable baseTable;

    public CSVTableWriter(BaseTable baseTable) {
        this.baseTable = baseTable;
        initWriter();
    }

    private void initWriter() {
        if (writer != null) {
            closeWriter();
        }
        try {
            writer = new CSVWriter(new FileWriter(path + "/" + baseTable.getName() + "_" + fileNum + ".csv"), ';');
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeList(baseTable.getAttributesNames());
    }

    private void writeList(List<String> list) {
        writer.writeNext(list.stream().toArray(String[]::new), false);
    }

    @Override
    public void save(List<String> values) {
        if (baseTable.getDataCount() % rowsPerFile == 0) {
            initWriter();
            fileNum++;
        }
        writeList(values);
    }

    @Override
    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
