package org.springboot.generator;

import java.util.Random;

public class EANGenerator {

    public static String generateRandomEAN13() {
        Random random = new Random();

        int countryCode = 400 + random.nextInt(41);

        StringBuilder eanCode = new StringBuilder(String.valueOf(countryCode));
        for (int i = 0; i < 9; i++) {
            eanCode.append(random.nextInt(10));
        }

        int checkDigit = calculateEAN13CheckDigit(eanCode.toString());
        eanCode.append(checkDigit);

        return eanCode.toString();
    }

    private static int calculateEAN13CheckDigit(String eanWithoutCheckDigit) {
        int sum = 0;
        for (int i = 0; i < eanWithoutCheckDigit.length(); i++) {
            int digit = Character.getNumericValue(eanWithoutCheckDigit.charAt(i));
            if (i % 2 == 0) {
                sum += digit;
            } else {
                sum += digit * 3;
            }
        }

        int checkDigit = 10 - (sum % 10);
        if (checkDigit == 10) {
            return 0;
        }
        return checkDigit;
    }
}
