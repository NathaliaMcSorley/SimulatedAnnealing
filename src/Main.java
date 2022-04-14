import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {
	
    public static DecimalFormat df1 = new DecimalFormat("0.0"); 	//used for current utility, current weight, total utility, total weight
    public static DecimalFormat df2 = new DecimalFormat("0.00000"); //used for current temperature

    @SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
    	//Write OUTPUT file with results
    	String output = "output.txt";
        File file1 = new File(output);
        if (!file1.exists()) {
            file1.createNewFile();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file1.getAbsoluteFile(), true));
        
    	try {
    		//Read INPUT file with utilities and weights
            String line;
    		File file = new File("Program2Input.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<Double> utilities = new ArrayList<Double>();
            List<Double> weights = new ArrayList<Double>();

            while ((line = br.readLine()) != null)
            {
                String[] values = line.split("\t");
                utilities.add(Double.valueOf(values[0]));
                weights.add(Double.valueOf(values[1]));
            }

            Double capacity = 500d; //maximum weight = 500
            Double initialTemperature = 500d; //select a fairly large value for the initial temperature
            Random rnd = new Random();

            List<Boolean> packing = simulation(utilities, weights, capacity, initialTemperature, rnd);
            Double[] result = totalUtilityWeight(packing, utilities, weights, capacity);

            bw.write("\n---------------PACKING LIST---------------\n");
            //display 10 items per line, 40 lines = 400 items 
            //True = item is packed, False = item is not packed
            int item = 1;
            for(Boolean b: packing) {
            	bw.write(b+", ");
                if(item == 10) {
                	bw.write("\n");
                    item = 0;
                }
                item++;
            }
            
            //Count total items packed
            int totalItems = 0;
            for(Boolean p: packing) {
            	if(p) {
            		totalItems++;
            	}
            }
            
            //Report the number of items packed, total utility, and total weight
            bw.write("\n---------------RESULT---------------\n");
            bw.write("\nTotal items packed: "+ totalItems);
            bw.write("\nTotal Utility: "+ df1.format(result[0]));
            bw.write("\nTotal Weight: "+ df1.format(result[1])+"\n");
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<Boolean> simulation(List<Double> utilities, List<Double> weights, Double capacity, Double initialTemperature, Random rnd) {
        
    	int initialPacking = (int) Math.round(weights.size() * 0.05); //Initially select about 1/20 of the items
        Double currentTemperature = initialTemperature; //initialTemperature = 500d
        
        List<Boolean> currentPacking = new ArrayList<Boolean>(); 
        for(int item = 0 ; item < weights.size() ; item++) {
            if(item < initialPacking) {
                currentPacking.add(true);
            }else {
                currentPacking.add(false);
            }
        }
        Collections.shuffle(currentPacking, rnd);
        Double[] currentValues = totalUtilityWeight(currentPacking, utilities, weights, capacity);

        int interval = 1000;
        int total_iterations = 1; 
        int attempts = 0; 
        int successfulChanges = 0; 

        while(true) {
            List<Boolean> compPacking = comparePacking(currentPacking, rnd);
            Double[] compareValues = totalUtilityWeight(compPacking, utilities, weights, capacity);
            if(compareValues[0] > currentValues[0]) {
                currentPacking = compPacking;
                currentValues = compareValues;
                successfulChanges++;
            }else {
                Double criterion = Math.exp((compareValues[0]-currentValues[0])/currentTemperature) ;
                Double threshold = rnd.nextDouble();
                if(threshold < criterion) {
                    currentPacking = compPacking;
                    currentValues = compareValues;
                    successfulChanges++;
                }
            }
            
            if(attempts%interval == 0){
                System.out.println(
                		"  \tSucessful changes: "+ successfulChanges+
                		", \tAttempts: "+ attempts+
                		", \tCurrent Utility: "+df1.format(currentValues[0])+
                		", \tCurrent Weight: "+df1.format(currentValues[1])+
                		", \tCurent Temperature: "+df2.format(currentTemperature));
            }
            attempts++;

            if((attempts == 40000 || successfulChanges==4000) && successfulChanges == 0) {
                System.out.println("Total Iterations: " + total_iterations);
                return currentPacking;
            }

            if(successfulChanges == 4000 || attempts == 40000) {
                System.out.println("Iteration number: " + total_iterations);
                currentTemperature *= 0.99;
                attempts = 0;
                successfulChanges = 0;
                total_iterations++;
            }

            if(currentTemperature < 0.00001 ) {
                currentTemperature = 0.00001;
            }
        }
    }

    public static Double[] totalUtilityWeight(List<Boolean> packings, List<Double> utilities, List<Double> weights, Double capacity) {
        
    	Double utility = 0d;
        Double weight = 0d;
        Double result[] = new Double[2];
        
        for(int i = 0 ; i < packings.size(); i++) { 
            if(packings.get(i)) { 
                utility += utilities.get(i); 
                weight += weights.get(i);
            }
        }

        if(weight > capacity) {
        	//penalty of -20 utility for every pound over the weight limit
            utility -= ((weight-capacity)*20);
        }

        result[0] = utility;
        result[1] = weight;

        return result;
    }

    public static List<Boolean> comparePacking(List<Boolean> packings, Random rnd) {
        
    	List<Boolean> result = new ArrayList<Boolean>();
        for(Boolean b: packings) {
            result.add(b);
        }

        Integer index = rnd.nextInt(packings.size());
        result.set(index, !result.get(index));

        return result;
    }
}
