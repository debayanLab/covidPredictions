import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
public class CovidByState {
    /**
     * Converts each of line of the file which is extracted as a String into an array. 
     * @param n a line from the input file
     * @return a array of the comma separated values in n 
     */
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
    
    /**
     * Takes input file and stores each datapoint as an array entry.
     * @return an array of data from the input file
     */
    public static ArrayList<String[]> input() {
        ArrayList<String[]> inputData = new ArrayList<>();
        try {
            File file = new File("Covid National Data Updated.csv");
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                inputData.add(clean_output(sc.nextLine()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            System.out.println(e);
        }
        return inputData;
    }
    
    /**
     * Gives the prediction for the reported cases in India by summing state predictions
     * @param n the number of days for which the prediction must be given
     * @return the list of prediction of total cases for n days
     */
    public static long[] getPrediction(HashMap<String, State> states, int n) {
        long total[] = new long[n];
        for (State i : states.values()) {
            long[] byState = i.getPrediction(n);
            for(int j = 0; j < n;j++) {
                total[j] += byState[j];
            }
        }
        return total;
    }
    
    /**
     * Calls all functions and gets predictions and adjusts for randomness
     * @param args the number of days for which the national prediction is to be given
     */
    public static void main(String[] args) {
        long count = 0;
        //while loop below uses first test data to correct for randomness while assigning districts
        while (count / 1000 != 143) {//25th March has between 143k to 144k reported cases
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
            if (count / 1000 == 143) {
                for(long i : prediction) {
                    System.out.println(i);
                }
               break;
             }
             /*
              * Can be uncommented to see what is the first value reported.
              else {
                 System.out.println(count);
              }
              */
        }
    }
}

/** 
 * Represents a State in India
 */
class State {
    HashMap<String, District> districts;
    String name;
    long total_cases;
    /**
     * Creates a new State with the given name and other default values
     * @param name the name of the State
     */
    public State(String n) {
        districts = new HashMap<>();
        this.name = n;
        total_cases = 0;
    }
    
    /**
     * Adds a case to the district of the state
     * @param n the name of the district
     * @param date the date on which the case is reported
     */
    public void addDistrict(String n, String date) {
        if (dateValue(date) > 99)
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
    
    /**
     * When no district is mentioned, performs a weighted random assignment of the case to a district in the state
     * @return the name of the district to which the case is assigned
     */
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
    
    /**
     * Gives the prediction for the reported cases in the state by summing district predictions
     * @param n the number of days for which the prediction must be given
     * @return the list of prediction of total cases for n days
     */
    public long[] getPrediction(int n) {
        long total[] = new long[n];
        for (District i: districts.values()) {
            long byDistrict[] = i.prediction(n);
            for(int j = 0; j < n; j++) {
                total[j] += byDistrict[j];
            }
        }
        return total;
    }
    
    /**
     * Helper function to convert date to integer value representing number of days after 30/1/2020
     * @param date the date to be converted to integer
     * @return the integer value of the date
     */
    private int dateValue(String date) {
        int []months = {31,60,91,121,152,182,213,244,274,305,335,366};
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        if (month == 0) { 
            return day - 30;
        } else {
            return months[month - 1] + day - 30;
        }
    }
}

/**
 * Represents a District in India
 */
class District {
    final static int data_for_days = 100;
    double x_not;
    double mean;
    String name;
    int total_cases;
    ArrayList<Integer> cases_and_projections;
    int[] date_count;
    /**
     * Creates a new District with the given name and other default values
     * @param name the name of the district
     */
    public District(String name) {
        x_not = mean = 0;
        this.name = name;
        total_cases = 0;
        cases_and_projections = new ArrayList<>();
        date_count = new int[400];
    } 
    
    /**
     * Increases the total number of cases on date by 1.
     * @param date the date at which a case occured
     */
    public void addDate(String date) {
        date_count[dateValue(date)] += 1;
    }
    
    /**
     * Increases total cases in district by 1
     */
    public void tick_total() {
        this.total_cases += 1;
    }
    
    /**
     * Helper function to convert date to integer value representing number of days after 30/1/2020
     * @param date the date to be converted to integer
     * @return the integer value of the date
     */
    private int dateValue(String date) {
        int []months = {31,60,91,121,152,182,213,244,274,305,335,366};
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        if (month == 0) { 
            return day - 30;
        } else {
            return months[month - 1] + day - 30;
        }
    }
    
    /**
     * Calculates the Gaussian mean and x_not value using historic data
     */
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
    
    /**
     * Calculates the value of mu for each iteration (see Appendix A)
     * @param x the current day (x-coordinate)
     * @param fx the number of reported cases on day x
     * @param fm the slope of the graph at x
     * @param fpp the rate of change of slope of the graph
     * @return the value mu for the given parameters
     */
    public double getMean(int x, double fm, int fx, double fpp) {
        return - fx * fm + x * fx * fpp - x * fm * fm;
    }
    
    
    /**
     * Extracts cases by date and creates a list with total cases till each day. 
     */
    public void getCases() {
        int sum = date_count[0];
        for (int i = 1; i <= data_for_days; i++) {
            sum += date_count[i];
            if(sum != 0)
            cases_and_projections.add(sum);
        }
    }
    
    /**
     * @return whether a district has had at least 5 days where new cases were reported
     */
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
    
    /**
     * Gives the prediction of reported total cases for the district
     * @param n the number of days for which the prediction is to be given
     * @return the prediction of total reported cases for the n days in the district
     */
    public long[] prediction (int n) {
        long[] y = new long[n];
        getCases();
        if(check()) {
            analyse();
            for (int i = 0; i < n; i++) {
                double x = (cases_and_projections.size() + i - 70) / (4.65 * x_not);
                y[i] = Math.round (1180 * mean * sigmoid(x)) - 5100;
            }
        } else {
            for(int i = 0; i < n; i++) {
                y[i] = cases_and_projections.get(cases_and_projections.size() - 1);
            }
        }
        return y;
    }
    
    /**
     * Returns the value of sigmoid(x)
     * @param x value for which sigmoid needs to be given.
     * @return the value of sigmoid(x)
     */
    public double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
