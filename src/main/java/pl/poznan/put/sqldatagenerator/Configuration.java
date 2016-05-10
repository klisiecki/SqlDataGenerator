package pl.poznan.put.sqldatagenerator;


public class Configuration {

    private int rowsPerFile = 10000;
    private String outputPath;
    private double selectivity = 0.5;

    private static Configuration instance;

    private Configuration() {
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public int getRowsPerFile() {
        return rowsPerFile;
    }

    public void setRowsPerFile(int rowsPerFile) {
        this.rowsPerFile = rowsPerFile;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public double getSelectivity() {
        return selectivity;
    }

    public void setSelectivity(double selectivity) {
        this.selectivity = selectivity;
    }
}
