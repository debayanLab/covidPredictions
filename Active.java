
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
class Active {
    static double mean = 0;
    static double x_not = 0;
    static int n = 0;
    static int active[];
    static double coefficient_of_movement = 9;
    public static void main (String args[]) {
        input();
        analyse();
        System.out.println(mean + " " + x_not);
        long prediction[] = prediction(Integer.parseInt(args[0]), 9);
        for (long i: prediction) {
            System.out.println(i);
        }
    }
    public static void input() {
        try {
            File file = new File ("Active.csv");
            Scanner sc = new Scanner(file);
            Scanner sc1 = new Scanner(file);
            int count = 0;
            while(sc.hasNextInt()) {
                count ++;
                sc.nextLine();
            }
            n = count;
            active = new int[n];
            for (int i = 0; i < n; i++) {
                active[i] = sc1.nextInt();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            System.out.println("Exception");
        }
    }
    public static void analyse() {
        double[] mean1 = new double[n - 2];
        int pointer = 0;
        double x = 0;
        double mean_calc = 0;
        for(int i = 1; i < n - 1; i++) {
            int current = active[i];
            int previous = active[i - 1];
            int next = active[i + 1];
            double fx = (next - previous) / 2;
            double fpp = (next + previous - 2 * current);
            if(current * fpp - fx * fx != 0) {
                mean1[pointer++] = getMean(i, fx, current, fpp) / (current * fpp - fx * fx);
                x += (mean1[pointer - 1] * current) / i;
            }
        }
        for(int i = 0; i < pointer; i++) {
            x_not += (mean1[i]) / (pointer);
        }
        mean = x / pointer;
    }
    public static double getMean(int x, double fm, int fx, double fpp) {
        return - fx * fm + x * fx * fpp - x * fm * fm;
    }
    /**
     * function that returns the prediction for the active cases of Covid-19 in India for specified number of days
     * @params days the number of days after 15/4 for which prediction is required
     * @params percent_movement the percentage of movement of people after lockdown ends on 3/5
     * 
     * @return an array with the prediction for the given number of days.
     */
    public static long[] prediction(int days, int percent_movement) {
        long y[] = new long[days];
        double spread_factor, probability_of_positive = 8, coefficient_of_movement = 9;
        for (int i = 0; i < days; i++) {
            double x = (n + 1.1 * (i - 47)) / x_not;
            /*if (i > 46) {
                if (coefficient_of_movement < percent_movement)
                coefficient_of_movement += Math.sqrt(percent_movement) / 4.0;
            }*/
            spread_factor = Math.sqrt(coefficient_of_movement);
            y[i] = Math.round (10.5 * Math.sqrt(probability_of_positive) * mean * sigmoid(x) * spread_factor) - 6250;
        }
        return y;
    } 
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}