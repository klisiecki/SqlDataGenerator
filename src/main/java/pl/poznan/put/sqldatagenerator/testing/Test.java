package pl.poznan.put.sqldatagenerator.testing;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import pl.poznan.put.sqldatagenerator.generator.AttributeTypes;


public class Test {
    public static void main(String[] args) {
        Expression<String> nonStandard = ExprParser.parse("((A | B) & (C | D))");
        System.out.println(nonStandard);

        Expression<String> sopForm = RuleSet.toCNF(nonStandard);
        System.out.println(sopForm);


        Double x = 2.0;
        Double y = 3.0;

        Variable v1 = Variable.of(x);
        Variable v2 = Variable.of(y);

        Expression<Double> e =  And.of(v1, v2);

        System.out.println(e);

        assert false;

        System.out.println("DUPA");

        AttributeTypes.valueOf("Integer");
    }
}
