package pl.poznan.put.sqldatagenerator.readers;


import com.bpodgursky.jbool_expressions.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Select;
import pl.poznan.put.sqldatagenerator.restriction.types.Restriction;
import pl.poznan.put.sqldatagenerator.sql.AttributesNamesFinder;
import pl.poznan.put.sqldatagenerator.sql.JoinEqualsFinder;
import pl.poznan.put.sqldatagenerator.sql.RestrictionFinder;
import pl.poznan.put.sqldatagenerator.sql.TablesFinder;
import pl.poznan.put.sqldatagenerator.sql.model.AttributesPair;

import java.util.List;

public class SQLData {

    private final Select selectStatement;

    public SQLData(Select selectStatement) {
        this.selectStatement = selectStatement;
    }

    public List<Table> getTables() {
        TablesFinder tablesFinder = new TablesFinder();
        return tablesFinder.getTableList(selectStatement);
    }

    public List<String> getAttributes(Table table) {
        AttributesNamesFinder attributesNamesFinder = new AttributesNamesFinder();
        return attributesNamesFinder.getAttributesList(selectStatement, table);
    }

    public List<AttributesPair> getJoinEquals() {
        JoinEqualsFinder joinEqualsFinder = new JoinEqualsFinder();
        return joinEqualsFinder.findEquals(selectStatement);
    }

    public Expression<Restriction> getCriteria() {
        RestrictionFinder restrictionFinder = new RestrictionFinder(selectStatement);
        return restrictionFinder.getResult();
    }

}
