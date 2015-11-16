package pl.poznan.put.SqlDataGenerator;


public class Configuration {

    private int rowsPerFile = 1000;
    private String instanceName;

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

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
