package pl.poznan.put.sqldatagenerator.testing;

import com.mifmif.common.regex.Generex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.sqldatagenerator.generators.RandomGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    public static final int SIZE50KK = 50000000;


    public static void main(String[] args) throws Exception {

        Generex generex = new Generex("[a-zA-Z]{5,15}");

        System.out.println("generex = " + generex.random());


//        Generex generex = new Generex("dupa|lala|papa|qwwqdas|dsadasd|wqeqweewq|oppopopo|mnmnmnmnmn|zxzxzx");
//        Generex generex = new Generex("[0-9a-zA-Z]{10,50}");
//        Generex generex = new Generex("([0-3][a-c]{10,20}([x-z]{10,20}|[1-9]{10,20}))*");
//        Generex generex = new Generex("[a-z]*");
//        Generex generex = new Generex(" /^(?=^abc)(?=.*xyz$)(?=.*123)(?=^(?:(?!456).)*$).*$/"); //wywala generator
//        Generex generex = new Generex("abc|xyz$|123|^(?:(?!456).)*");

//        List<String> allowedValues = Arrays.asList("dupa", "lala", "papa", "qwwqdas", "dsadasd", "wqeqweewq", "oppopopo", "mnmnmnmnmn", "zxzxzx");

//        logger.info("S");
//        for(int i = 0; i< SIZE50KK; i++) {
////            String randomStr = generex.random();
////            String randomStr = allowedValues.get(random.nextInt(allowedValues.size()));
//
////            String randomStr = generex.random();
//            String randomStr = RandomGenerator.randomString(10,50);
//
//
////            if(i%1==0) {
////                System.out.println(">>  " + randomStr);
////            }
//        }
//        logger.info("K");
    }
}
