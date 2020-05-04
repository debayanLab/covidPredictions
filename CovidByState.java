import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
public class CovidByState {
    public static String[] clean_output(String n) {
        String[] input = new String[4];
        Scanner sc1 = new Scanner(n);
        sc1.useDelimiter(",");
        sc1.next();
        for(int i = 0; i < 4; i++) {
            input[i] = sc1.next();
        }
        return input;
    }
    public static ArrayList<String[]> input() {
        ArrayList<String[]> inputData = new ArrayList<>();
        try {
            File file = new File("Covid Data.csv");
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                inputData.add(clean_output(sc.nextLine()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            System.out.println("Some error occured");
        }
        return inputData;
    }
    public static void main(String[] args) {
        long count = 0;
        while (count / 100 != 185) {
            ArrayList<String[]> data = input();
            HashMap <String, State> states = new HashMap<>();
            for (String[] i : data) {
                if (!states.containsKey(i[3])) {
                    states.put(i[3], new State(i[2]));
                }
                states.get(i[3]).addDistrict(i[1], i[0]);
            }
            long[] prediction = getPrediction(states, Integer.parseInt(args[0]));
            count = prediction[0];
            if (count / 100 == 185)
                for(long i : prediction) {
                    System.out.println(i);
                }
        }
    }
    public static long[] getPrediction(HashMap<String, State> states, int n) {
        long total[] = new long[n];
        for (State i : states.values()) {
            long[] byState = i.getPrediction(n);
            for(int j = 0; j < n;j++) {
                total[j] += byState[j];
            }
        }
        //System.out.println("National");
        return total;
    }
}
class State {
    HashMap<String, District> districts;
    String name;
    long total_cases;
    public State(String n) {
        districts = new HashMap<>();
        this.name = n;
        total_cases = 0;
    }
    
    public void addDistrict(String n, String date) {
        if (dateValue(date) > 94)
        return;
        if (n.equals("")) {
            n = randomlyAssignDistrict();
        }
        if (!districts.containsKey(n)) {
            districts.put(n, new District(n));
        }
        total_cases += 1;
        districts.get(n).tick_total();
        districts.get(n).addDate(date);
    }
    
    public String randomlyAssignDistrict() {
        double random = Math.random();
        String districtName = "";
        for (District i: districts.values()) {
            double ratio = ((double) i.total_cases) / ((double) total_cases);
            if (ratio > random) {
                districtName = i.name;
                break;
            } else {
                random -= ratio;
            }
        }
        return districtName;
    }
    
    public long[] getPrediction(int n) {
        long total[] = new long[n];
        for (District i: districts.values()) {
            long byDistrict[] = i.prediction(n);
            for(int j = 0; j < n; j++) {
                total[j] += byDistrict[j];
            }
        }
        /*for (District i: districts.values()) {
            if (i.check()) {
                System.out.println(i.name);
                System.out.println(i.total_cases);
                System.out.println(Arrays.toString(i.prediction(n)));
                System.out.println(i.mean + " " + i.x_not);
            }
        }*/
        return total;
    }
    private int dateValue(String date) {
        int []months = {31,60,91,121,152,182,213,244,274,305,335,366};
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        if (month == 0) { 
            return day - 30; //since first date was 30/01/20.
        } else {
            return months[month - 1] + day - 30;
        }
    }
}
class District {
    double probability_of_positive = 8; 
    double coefficient_of_movement = 9;//in lockdown conditions
    final static int data_for_days = 96;
    double x_not;
    double mean;
    String name;
    int total_cases;
    ArrayList<Integer> cases_and_projections;
    int[] date_count;
    public District(String name) {
        x_not = mean = 0;
        this.name = name;
        total_cases = 0;
        cases_and_projections = new ArrayList<>();
        date_count = new int[400];
    } 
    public void addDate(String date) {
        date_count[dateValue(date)] += 1;
    }
    public void tick_total() {
        this.total_cases += 1;
    }
    private int dateValue(String date) {
        int []months = {31,60,91,121,152,182,213,244,274,305,335,366};
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        if (month == 0) { 
            return day - 30; //since first date was 30/01/20.
        } else {
            return months[month - 1] + day - 30;
        }
    }
    public void analyse() {
        double[] mean1 = new double[cases_and_projections.size()];
        int pointer = 0;
        double x = 0;
        for(int i = 1; i < cases_and_projections.size() - 1; i++) {
            int current = cases_and_projections.get(i);
            int previous = cases_and_projections.get(i - 1);
            int next = cases_and_projections.get(i + 1);
            double fm = (next - previous) / 2;
            double fpp = (next + previous - 2 * current);
            if(current * fpp - fm * fm != 0) {
                mean1[pointer++] = getMean(i, fm, current, fpp) / (current * fpp - fm * fm);
                x += (mean1[pointer - 1] * current) / i;
            }
        }
        for(int i = 0; i < pointer; i++) {
            x_not += (mean1[i]) / (pointer);
        }
        mean = x / pointer;
    }
    public double getMean(int x, double fm, int fx, double fpp) {
        return - fx * fm + x * fx * fpp - x * fm * fm;
    }
    public long[] prediction (int n) {
        long[] y = new long[n];
        double spread_factor = 1;
        getCases();
        int limit = 90;
        if(check()) {
            analyse();
            for (int i = 0; i < n; i++) {
                double x = (cases_and_projections.size() + i - 25) / (2 * x_not);
                if (i > 16) {
                    if (coefficient_of_movement < limit)
                    coefficient_of_movement += Math.sqrt(limit) / 6.0;
                }
                spread_factor = Math.sqrt(coefficient_of_movement);
                y[i] = Math.round (18.5 * Math.sqrt(probability_of_positive) * mean * sigmoid(x) * spread_factor) - 584;
            }
        } else {
            for(int i = 0; i < n; i++) {
                y[i] = cases_and_projections.get(cases_and_projections.size() - 1);
            }
        }
        return y;
    }
    public double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
    public boolean check() {
        int count = 0;
        for (int i = 0; i < 400; i++) {
            if (date_count[i] != 0) {
                count += 1;
            }
        }
        if (count < 5) {
            return false;
        } else {
            return true;
        }
    }
    public void getCases() {
        int sum = date_count[0];
        for (int i = 1; i <= data_for_days; i++) {
            sum += date_count[i];
            if(sum != 0)
            cases_and_projections.add(sum);
        }
    }
}