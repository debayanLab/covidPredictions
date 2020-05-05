import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
class DeathsToActive {
    static double mean = 0;
    static double x_not = 0;
    static int n = 0;
    static int deaths[];
    public static void main (String args[]) {
        input();
        analyse();
        System.out.println(mean);
        long prediction[] = prediction(Integer.parseInt(args[0]));
        for (long i: prediction) {
            System.out.println(i + "\t" + get_active_case_projection(i));
        }
    }
    public static void input() {
        try {
            File file = new File ("Deaths.csv");
            Scanner sc = new Scanner(file);
            Scanner sc1 = new Scanner(file);
            int count = 0;
            while(sc.hasNextInt()) {
                count ++;
                sc.nextLine();
            }
            n = count;
            deaths = new int[n];
            for (int i = 0; i < n; i++) {
                deaths[i] = sc1.nextInt();
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
            int current = deaths[i];
            int previous = deaths[i - 1];
            int next = deaths[i + 1];
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
        return fx * fm + x * fx * fpp - x * fm * fm;
    }
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
    public static long[] prediction(int k) {
        long y[] = new long[k];
        for (int i = - 80; i < k - 80; i++) {
                double x = (n + i) / (x_not);
                y[i + 80] = Math.round (50 * mean * sigmoid(x))-2930;
        }
        return y;
    } 
    public static long get_active_case_projection(long i) {
        double x = i;
        return Math.round(-0.0041 * x * x + 25.387 * x - 31.946); 
    }
}