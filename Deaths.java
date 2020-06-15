import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
class Deaths {
    static double mean = 0;
    static double x_not = 0;
    static int n = 0;
    static int death[];
    
    /**
     * calls all functions and displays results
     * @param args the number of waits for which the prediction is to be given
     */
    public static void main (String args[]) {
        input();
        analyse();
        System.out.println(mean + " " + x_not);
        long prediction[] = prediction(Integer.parseInt(args[0]));
        for (long i: prediction) {
            System.out.println(i);
        }
    }
    
    /**
     * inputs the values from the data source and stores it in an array
     */
    public static void input() {
        try {
            File file = new File ("Deaths2.csv");
            Scanner sc = new Scanner(file);
            Scanner sc1 = new Scanner(file);
            int count = 0;
            while(sc.hasNextInt()) {
                count ++;
                sc.nextLine();
            }
            n = count;
            death = new int[n];
            for (int i = 0; i < n; i++) {
                death[i] = sc1.nextInt();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            System.out.println("Exception");
        }
    }
    
    /**
     * Calculates the Gaussian mean and x_not value using historic data
     */
    public static void analyse() {
        double[] mean1 = new double[n - 2];
        int pointer = 0;
        double x = 0;
        double mean_calc = 0;
        for(int i = 1; i < n - 1; i++) {
            int current = death[i];
            int previous = death[i - 1];
            int next = death[i + 1];
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
    
    /**
     * Calculates the value of mu for each iteration (see Appendix A)
     * @param x the current day (x-coordinate)
     * @param fx the number of reported cases on day x
     * @param fm the slope of the graph at x
     * @param fpp the rate of change of slope of the graph
     * @return the value mu for the given parameters
     */
    public static double getMean(int x, double fm, int fx, double fpp) {
        return - fx * fm + x * fx * fpp - x * fm * fm;
    }
    
    /**
     * returns the prediction for the death cases of Covid-19 in India for specified number of days
     * @params days the number of days including and after 25/5 for which prediction is required
     * 
     * @return an array with the prediction for the given number of days.
     */
    public static long[] prediction(int days) {
        long y[] = new long[days];
        for (int i = 0; i < days; i++) {
            double x = (n + i - 116) / (0.9*x_not);
            y[i] = Math.round (27 * Math.sqrt(2) * mean * sigmoid(x)) - 5400;
        }
        return y;
    } 
    
    /**
     * Returns the value of sigmoid(x)
     * @param x value for which sigmoid needs to be given.
     * @return the value of sigmoid(x)
     */
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
