package de.rubymc.packettests.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class ValueUtil {

    public static double getRandomDouble(double min, double max) {

        Random random = new Random();
        return random.nextDouble() * (max - min) + min;

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
