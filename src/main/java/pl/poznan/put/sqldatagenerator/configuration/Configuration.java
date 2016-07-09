package pl.poznan.put.sqldatagenerator.configuration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private String outputPath;
    private double selectivity = 0.5;
    private Properties properties;

    private static Configuration instance;

    private Configuration() {
        properties = new Properties();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
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

    public void setPropertiesLocation(String propertiesLocation) {
        try {
            properties.load(new FileInputStream(propertiesLocation));
        } catch (IOException e) {
            logger.warn("Unable to load properties from file " + propertiesLocation);
        }
    }

    public Integer getIntegerProperty(ConfigurationKey key, Integer defaultValue) {
        if (properties.containsKey(key.toString())) {
            try {
                return Integer.parseInt((String) properties.get(key.toString()));
            } catch (ClassCastException | NumberFormatException e) {
                logger.warn("Unable to parse value for key " + key);
            }
        }
        return defaultValue;
    }

    public String getStringProperty(ConfigurationKey key, String defaultValue) {
        if (properties.containsKey(key.toString())) {
            try {
                return (String) properties.get(key.toString());
            } catch (ClassCastException e) {
                logger.warn("Unable to parse value for key " + key);
            }
        }
        return defaultValue;
    }
}
