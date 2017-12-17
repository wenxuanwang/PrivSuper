import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 	Author Wenxuan Wang
 * 	Date 2/12/2017
 */

public class Apriori{
	private int k;
	private String inputFile;
	private String outputFile;
	private List<List<Integer>> FIM;
	private List<Integer> fim_count;
	
	private long timer;
	private int MIN_SUPPORT;
	private List<List<Integer>> buffer = new ArrayList<>();
	private List<Map<String, Candidate>> list = new ArrayList<>();
	private Map<String, Candidate> singleItem = new ConcurrentHashMap<>();
	private Map<String, Candidate> twoItem = new ConcurrentHashMap<>();
	private Map<String, Candidate> threeItem = new ConcurrentHashMap<>();

	
	/**
	 * Constructor, intialize the parameters
	 * @param inputFile Database
	 * @param outputFile 
	 * @param k
	 */
	public Apriori(String inputFile, int k, List<List<Integer>> FIM, List<Integer> fim_count) {
		this.inputFile = inputFile;
		this.k = k;
		this.FIM = FIM;
		this.fim_count = fim_count;
	}
	
	
	public void run() {
		MIN_SUPPORT = k;

		try{
			AprioriPruning(inputFile);
		}
		catch(IOException ex) {
			System.err.println("File not found");
			System.exit(1);
		}
	}

	/**
	 * Apriori Algorithm
	 * build a buffer from the input file and generate candidate sets
	 * @param inputFile
	 * @throws Exception
	 */
	public void AprioriPruning(String inputFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line;
		while((line = br.readLine()) != null) {
			List<Integer> temp = new ArrayList<Integer>();
			for(String s : line.trim().split(" "))
				temp.add(Integer.parseInt(s));
			getOneSet(temp);
			getTwoSet(temp);
			//getThreeSet(temp);
			buffer.add(temp);
		}
		list.add(clean(singleItem));
		list.add(clean(twoItem));
		//list.add(clean(threeItem));

		Map<String, Candidate> nextSet = getCandidateSet(list.get(list.size() - 1));
		list.add(nextSet);
		while(nextSet.size() > 0) {
			testMinSupport(nextSet);
			clean(nextSet);
			nextSet = getCandidateSet(list.get(list.size() - 1));
			list.add(nextSet);
		}
	}

	/**
	 * parse the unordered output entry and ignore the support count
	 * @param line
	 * @return list containing all numbers in each entry
	 */
	public List<Integer> parseLine(String line) {
		line = line.trim().substring(0, line.lastIndexOf(" "));
		List<Integer> list = new ArrayList<Integer>();
		for(String s : line.split(" "))
			list.add(Integer.parseInt(s));
		return list;
	}

	/**
	 * test for minimum support using buffer cache
	 * @param nextSet
	 */
	public void testMinSupport(Map<String, Candidate> nextSet) {
		for(List<Integer> entry : buffer) {
			Set<Integer> set = new HashSet<Integer>();
			for(Integer n : entry)
				set.add(n);
			for(Map.Entry<String, Candidate> m : nextSet.entrySet()) {
				boolean contain = true;
				int[] itemList = m.getValue().set;
				for(int i : itemList) {
					if(!set.contains(i))
						contain = false;
				}
				if(contain)	m.getValue().support++;
			}
		}
	}

	/**
	 * remove itemset less than minimum support and write output
	 * @param map
	 * @return map containing supported candidate itemset
	 */
	public Map<String, Candidate> clean(Map<String, Candidate> map) {
		for(Map.Entry<String, Candidate> e : map.entrySet() ) {
			if(e.getValue().support < MIN_SUPPORT)
				map.remove(e.getKey());
			else {
				List<Integer> FIM_entry = new ArrayList<Integer>();
				for(int i : e.getValue().set)
					FIM_entry.add(i);
				FIM.add(FIM_entry);
				fim_count.add(e.getValue().support);
			}
		}
		return map;
	}

	/**
	 * generate candidate set by doing self-joining
	 * @param lastSet
	 * @return map containing all candidate itemset
	 */
	public Map<String,Candidate> getCandidateSet(Map<String, Candidate> lastSet) {
		ConcurrentHashMap<String, Candidate> nextSet = new ConcurrentHashMap<>();
		boolean valid;
		for(Candidate c1 : lastSet.values()) {
			int[] item1 = c1.set;
			for(Candidate c2 : lastSet.values()) {
				if(c1 == c2)	continue;
				int[] item2 = c2.set;
				valid = true;

				for(int i = 0; i < item2.length - 1; i++) {
					if(item1[i] != item2[i]){
						valid = false;
						break;
					}
				}
				if(valid) {
					boolean isNext = true;
					int[] nextCandidate = new int[item1.length + 1];
					int k = 0;
					while(k < item1.length) {
						nextCandidate[k] = item1[k++];
					}
					nextCandidate[k] = item2[item2.length - 1];
					Arrays.sort(nextCandidate);

					List<List<Integer>> subset = getSubsets(nextCandidate);
					for (int i = 0; i < subset.size(); i++) {
						int[] tempoararyList = subset.get(i).stream().mapToInt(x -> x).toArray();
						if (!lastSet.containsKey(Arrays.toString(tempoararyList)))
							isNext = false;
					}
					if (isNext)
						nextSet.put(Arrays.toString(nextCandidate), new Candidate(0, nextCandidate));
				}
			}
		}
		return nextSet;
	}

	/**
	 * calculate the subset of a given input itemset
	 * @param item
	 * @return
	 */
	public List<List<Integer>> getSubsets(int[] item) {
		List<List<Integer>> list = new ArrayList<List<Integer>>();
		getSubsetHelper(list, new ArrayList<>(), item, item.length - 1,0);
		return list;
	}

	/**
	 * getSubsets helper function
	 * @param list
	 * @param temp
	 * @param item
	 * @param length
	 * @param start
	 */
	public void getSubsetHelper(List<List<Integer>> list, List<Integer> temp, int[] item, int length, int start) {
		if(temp.size() == length)	list.add(new ArrayList<Integer>(temp));
		else {
			for(int i = start; i < item.length; i++) {
				temp.add(item[i]);
				getSubsetHelper(list,temp,item,length,i+1);
				temp.remove(temp.size() - 1);
			}
		}
	}

	/**
	 * put Candidate into HashMap
	 * @param map
	 * @param set
	 */
	public void buildMap(Map<String, Candidate> map, int[] set) {
		String key = Arrays.toString(set);
		if(!map.containsKey(key))
			map.put(key, new Candidate(1, set));
		else
			map.get(key).support++;
	}

	/**
	 * generate one-item set
	 * @param temp
	 */
	public void getOneSet(List<Integer> temp) {
		for(int i = 0; i < temp.size(); i++)
			buildMap(singleItem, new int[]{temp.get(i)});
	}

	/**
	 * generate two-item set
	 * @param temp
	 */
	public void getTwoSet(List<Integer> temp) {
		for(int i = 0; i < temp.size() - 1; i++) {
			for(int j = i+1; j < temp.size(); j++) {
				buildMap(twoItem, new int[]{temp.get(i), temp.get(j)});
			}
		}
	}

	/**
	 * generate three-item set
	 * @param temp
	 */
	public void getThreeSet(List<Integer> temp) {
		int[] arr = temp.stream().mapToInt(x -> x).toArray();
		threeSetHelper(new ArrayList<>(), arr, 3, 0);
	}

	/**
	 * getThreeSet helper
	 * @param temp
	 * @param item
	 * @param length
	 * @param start
	 */
	public void threeSetHelper(List<Integer> temp, int[] item, int length, int start) {
		if(temp.size() == length)	buildMap(threeItem, temp.stream().mapToInt(x->x).toArray());
		else {
			for(int i = start; i < item.length; i++) {
				temp.add(item[i]);
				threeSetHelper(temp,item,length,i+1);
				temp.remove(temp.size() - 1);
			}
		}
	}
}

class Candidate {
	int support;
	int[] set;

	Candidate(int support, int[] set) {
		this.support = support;
		this.set = set;
	}
}