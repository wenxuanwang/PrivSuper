import java.io.*;
import java.util.*;

/**
 * Created by temp on 12/14/2017.
 */
public class PrivSuper {
    private String database;
    private double eps;
    private double eps1, eps2, eps3;
    private int k;
    private int l;
    private int m;
    private int lambda;
    
    private List<List<Integer>> FIM;
    private List<Integer> exact_support;
    private List<List<Integer>> db = new ArrayList<List<Integer>>();
    private List<Integer> dbEntryLength = new ArrayList<Integer>();
    private Set<Integer> F = new HashSet<Integer>();
    
    
    public static void main(String[] args) throws IOException{
    	String database = "T10I4D100K.dat";
    	double eps = 1;
    	int k = 500;
    	PrivSuper ps = new PrivSuper(database, eps, k);
    	ps.run();
    }
    
    public PrivSuper(String database, double eps, int k) {
        this.database = database;
        this.eps = eps;
        this.k = k;
        FIM = new ArrayList<List<Integer>>();
        exact_support = new ArrayList<Integer>();
    }

    private void run() throws IOException{
    	// step 1.
    	// Split the budget
    	splitBudget();
    	
    	// step 2.
    	// Get top-k frequent itemsets, and exact support
    	Apriori Apriori = new Apriori(database, k, FIM, exact_support);
    	Apriori.run();
    	for(int i = 0; i < FIM.size(); i++) {
    		System.out.println(FIM.get(i));
    		System.out.println(exact_support.get(i));
    	}

    	// step3. step4.
    	// Choose value for l
    	truncateTransaction();
    	
        for(List<Integer> e : db)
            System.out.println(e);
      	
        // step5.
        getResultLength();
      	
        // step6.
        getLambda();
        
        // step7.
        // Compute the set F of frequent items
        computeF();
    	
    	
    }
    
    private void splitBudget() {
        eps1 = eps * 0.15;
        eps2 = eps * 0.5;
        eps3 = eps * 0.35;
    }
    
    private void  truncateTransaction() throws IOException{
    	Set<String> set = new HashSet<String>();
		List<Integer> length_stat = new ArrayList<Integer>();
		List<Integer> temp;
		int length;
		int counter;
		
		length = 0;
		counter = 0;
		
    	BufferedReader br = new BufferedReader(new FileReader(database));
		String line;
		while((line = br.readLine()) != null) {
			temp = new ArrayList<Integer>();
			for(String s : line.trim().split(" ")) {
				temp.add(Integer.parseInt(s));
				set.add(s);
			}
			db.add(temp);
			dbEntryLength.add(temp.size());
		}

		for(int i = 0; i < set.size(); i++) {
			length_stat.add(0);
		}
		
		for(int i = 0; i < dbEntryLength.size(); i++) {
			length_stat.set(dbEntryLength.get(i) - 1, length_stat.get(dbEntryLength.get(i) - 1) + 1);
		}
		
		for(int i = length_stat.size() - 1; i > 0; i--) {
			counter += length_stat.get(i);
			if(counter * 1.0 / db.size() > 0.15) {
				length = i + 1;
				break;
			}
		}
		
		System.out.println(length);
		for(int i = 0; i < db.size(); i++) {
			List<Integer> entry = db.get(i);
			if(entry.size() > length) {
				entry = resample(entry, length);
			}
			db.set(i, entry);
		}
		
		l = length;
    }

    private List<Integer> resample(List<Integer> input, int length) {
    	List<Integer> output = new ArrayList<Integer>();
    	for(int i = 0; i < length; i++) {
	    	Collections.shuffle(input);
	    	output.add(input.remove(0));
    	}
    	return output;
    }
    
    private void getResultLength() {
    	// calculate i
    	int i = 0;
    	Set<Integer> hs = new HashSet<Integer>();
    	
    	for(int j = 0; j < FIM.size(); i++) {
    		List<Integer> itemset = FIM.get(j);
    		hs.add(itemset.size());
    	}
    	i = hs.size();
    	
    }
    
    private void getLambda() {
    	
    }

    private void computeF() {
    	double budget = eps2 / 2.0;
    	
    	if(l < lambda) {
    		
    	}
    }
}
