package placement;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {

        try {

            // Read JSON file
            String json = Files.readString(Path.of("testcase.json"));

            // Read n and k
            int n = getNumber(json, "\"n\"\\s*:\\s*(\\d+)");
            int k = getNumber(json, "\"k\"\\s*:\\s*(\\d+)");

            System.out.println("n = " + n);
            System.out.println("k = " + k);
            System.out.println();

            // Read all roots from JSON
            ArrayList<Point> points = readPoints(json);

            // Sort points according to x
            points.sort(Comparator.comparing(p -> p.x));

            System.out.println("Decoded Points:");

            for (Point p : points) {
                System.out.println("(" + p.x + ", " + p.y + ")");
            }

            System.out.println();

            // Check enough points are available
            if (points.size() < k) {
                System.out.println("Not enough points.");
                return;
            }

            // Use k points
            BigInteger answer = findConstantTerm(points, k);

            System.out.println("Degree = " + (k - 1));
            System.out.println("Constant coefficient = " + answer);

        } catch (IOException e) {

            System.out.println("Error reading testcase.json");
            System.out.println(e.getMessage());

        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
        }
    }


    // Read an integer from JSON
    static int getNumber(String json, String regex) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        throw new RuntimeException("Could not find value in JSON");
    }


    // Read all root objects
    static ArrayList<Point> readPoints(String json) {

        ArrayList<Point> points = new ArrayList<>();

        /*
         * Matches:
         *
         * "1": {
         *     "base": "6",
         *     "value": "12345"
         * }
         */

        String regex =
                "\"(\\d+)\"\\s*:\\s*\\{\\s*" +
                "\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*" +
                "\"value\"\\s*:\\s*\"([^\"]+)\"\\s*\\}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {

            BigInteger x =
                    new BigInteger(matcher.group(1));

            int base =
                    Integer.parseInt(matcher.group(2));

            String value =
                    matcher.group(3);

            // Convert value from given base
            BigInteger y =
                    new BigInteger(value, base);

            points.add(new Point(x, y));
        }

        return points;
    }


    // Find f(0) using Lagrange interpolation
    static BigInteger findConstantTerm(
            List<Point> points,
            int k) {

        BigInteger numerator = BigInteger.ZERO;
        BigInteger denominator = BigInteger.ONE;

        for (int i = 0; i < k; i++) {

            BigInteger termNumerator =
                    points.get(i).y;

            BigInteger termDenominator =
                    BigInteger.ONE;

            for (int j = 0; j < k; j++) {

                if (i != j) {

                    // -xj
                    termNumerator =
                            termNumerator.multiply(
                                    points.get(j).x.negate()
                            );

                    // xi - xj
                    termDenominator =
                            termDenominator.multiply(
                                    points.get(i).x.subtract(
                                            points.get(j).x
                                    )
                            );
                }
            }

            // Add fractions
            numerator =
                    numerator.multiply(termDenominator)
                    .add(
                        termNumerator.multiply(denominator)
                    );

            denominator =
                    denominator.multiply(termDenominator);

            // Simplify fraction
            BigInteger gcd =
                    numerator.gcd(denominator);

            numerator =
                    numerator.divide(gcd);

            denominator =
                    denominator.divide(gcd);
        }

        return numerator.divide(denominator);
    }
}