package pl.poznan.put.sqldatagenerator.configuration;

public enum ConfigurationKeys implements ConfigurationKey {
    MAX_ROWS_PER_FILE("max_rows_per_file"),

    INPUT_DATE_FORMAT("input_date_format"),
    OUTPUT_DATE_FORMAT("output_date_format"),

    MIN_STRING_LENGTH("min_string_length"),
    MAX_STRING_LENGTH("max_string_length");

    private String key;

    ConfigurationKeys(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
