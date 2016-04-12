package pl.poznan.put.SqlDataGenerator;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.Rule;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import java.util.List;


public class Test {
    public static void main(String[] args) {
        Expression<String> nonStandard = ExprParser.parse("((A | B) & (C | D))");
        System.out.println(nonStandard);

        Expression<String> sopForm = RuleSet.toCNF(nonStandard);
        System.out.println(sopForm);


    }
}
