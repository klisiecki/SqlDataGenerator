package pl.poznan.put.SqlDataGenerator.generator;


import java.util.Random;

public class RandomGenerator {
    public static Integer getInteger(int from, int to) {
        int min = 0;
        int max = to - from;
        Random rand = new Random();
        int randomNum = rand.nextInt(max);
        return randomNum + from;
    }
}
