package pl.poznan.put.sqldatagenerator.configuration;

public enum ConfigurationKeys implements ConfigurationKey {

    PRINT_PROGRESS_DELAY("print_progress_delay"),

    MAX_ROWS_PER_FILE("max_rows_per_file"),
    HISTORY_SIZE("history_size"),
    DATABASE_TYPES_DESCRIPTION("database_types_description"),

    INPUT_DATE_FORMAT("input_date_format"),
    OUTPUT_DATE_FORMAT("output_date_format"),

    DATABASE_NULL_VALUE("database_null_value"),

    MIN_STRING_LENGTH("min_string_length"),
    MAX_STRING_LENGTH("max_string_length"),

    DOUBLE_EPSILON("double_epsilon"),

    RANDOM_KEYS_GENERATION("random_keys_generation"),
    ONLY_QUERY_ATTRIBUTES("only_query_attributes");

    private final String key;

    ConfigurationKeys(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
