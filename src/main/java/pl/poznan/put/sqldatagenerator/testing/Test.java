package pl.poznan.put.sqldatagenerator.testing;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import pl.poznan.put.sqldatagenerator.generator.RandomGenerator;


public class Test {
    public static void main(String[] args) {
//        Expression<String> nonStandard = ExprParser.parse("((A | B) & (C | D))");
//        System.out.println(nonStandard);
//
//        Expression<String> sopForm = RuleSet.toCNF(nonStandard);
//        System.out.println(sopForm);
//
//
//        Double x = 2.0;
//        Double y = 3.0;
//
//        Variable v1 = Variable.of(x);
//        Variable v2 = Variable.of(y);
//
//        Expression<Double> e =  And.of(v1, v2);
//
//        System.out.println(e);
//
//        assert false;
//
//        System.out.println("DUPA");
//
//        AttributeType.valueOf("INTEGER");

        Long a = 1L;
        Long b = 3L;
        Range x = Range.open(a, b);
        Range y = Range.closed(a, b);
        TreeRangeSet treeRangeSet = TreeRangeSet.create();
        TreeRangeSet treeRangeSet1 = TreeRangeSet.create();
        treeRangeSet.add(x);
        treeRangeSet1.add(y);
        for (int i = 0; i < 10; i++) {
            System.out.println(RandomGenerator.getLong(treeRangeSet));
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.println(RandomGenerator.getLong(treeRangeSet1));
        }
    }
}
