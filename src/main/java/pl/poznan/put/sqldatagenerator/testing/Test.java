package pl.poznan.put.sqldatagenerator.testing;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.util.concurrent.SimpleTimeLimiter;

import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws Exception {

        SimpleTimeLimiter simpleTimeLimiter = new SimpleTimeLimiter();
        String s = simpleTimeLimiter.callWithTimeout(() -> {
            while (true) {
                System.out.println("while");
                Thread.sleep(1000);
                return "aaaa";
            }
        }, 5, TimeUnit.SECONDS, true);

        System.out.println(s);
        Expression<String> nonStandard = ExprParser.parse("(q & (r & s ) | t & (u & v ) ) | w & x & ( y | z ) | a1 & ( b1 & c1 & (d1 | e1) | f1 & (g1 & h1) ) | j1 & (k1 & (l1 & m1) | n1 & (o1 | p1) ) | q1 & ( r1 & (s1 & t1))");
        Expression<String> cnf = RuleSet.simplify(nonStandard);
        System.out.println(cnf.toString().length());
        System.out.println(cnf);
    }
}
