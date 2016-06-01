package pl.poznan.put.sqldatagenerator.generator;

import pl.poznan.put.sqldatagenerator.exception.InvalidInternalStateException;
import pl.poznan.put.sqldatagenerator.exception.SQLSyntaxNotSupportedException;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DataTypesConverter;
import pl.poznan.put.sqldatagenerator.generator.datatypes.DatabaseType;
import pl.poznan.put.sqldatagenerator.generator.datatypes.InternalType;

public class Attribute {

    private final String name;
    private final DatabaseType databaseType;
    private Attribute baseAttribute;
    private final TableInstance tableInstance;

    private boolean isClear;

    public Attribute(TableInstance tableInstance, String name, DatabaseType databaseType) {
        this.name = name;
        this.tableInstance = tableInstance;
        this.databaseType = databaseType;
        this.isClear = true;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (baseAttribute != null) {
            return baseAttribute.getValue();
        }
        return tableInstance.getState().getValue(name);
    }

    public String getDatabaseValue() {
        return DataTypesConverter.getDatabaseType(getValue(), getInternalType(), getDatabaseType());
    }

    public void setValue(String value) {
        if (baseAttribute != null) {
            throw new InvalidInternalStateException("Attempt to set value of dependent attribute");
        }
        if (!isClear) {
            throw new InvalidInternalStateException("Value for attribute '" + name + "' already set");
        }
        isClear = false;
        tableInstance.getState().setValue(name, value);
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public InternalType getInternalType() {
        return databaseType.getInternalType();
    }

    public String getBaseTableName() {
        return tableInstance.getBase().getName();
    }

    public String getTableAliasName() {
        return tableInstance.getAliasName();
    }

    public boolean canBeGenerated() {
        return baseAttribute == null && isClear;
    }

    public void setClear(boolean isClear) {
        if (baseAttribute == null) {
            this.isClear = isClear;
        }
    }

    public void setBaseAttribute(Attribute baseAttribute) {
        if (this.baseAttribute != null) {
            throw new InvalidInternalStateException("Base attribute already set");
        }
        if (getInternalType() != baseAttribute.getInternalType()) {
            throw new SQLSyntaxNotSupportedException("Dependent attribute must have the same internalType as base attribute");
        }
        this.baseAttribute = baseAttribute;
    }

    @Override
    public String toString() {
        return tableInstance.getAliasName() + "." + name;
    }
}
