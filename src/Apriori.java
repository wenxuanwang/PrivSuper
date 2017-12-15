import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 	Author Wenxuan Wang
 * 	Date 2/12/2017
 */

public class Apriori{
	private static long timer;
	private static int MIN_SUPPORT;
	private static List<List<Integer>> buffer = new ArrayList<>();
	private static List<Map<String, Candidate>> list = new ArrayList<>();
	private static Map<String, Candidate> singleItem = new ConcurrentHashMap<>();
	private static Map<String, Candidate> twoItem = new ConcurrentHashMap<>();
	private static Map<String, Candidate> threeItem = new ConcurrentHashMap<>();

	/**
	 * call the Apriori Algorithm and calculate time
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3)
			System.out.println("Usage: java Apriori [input] [int-minimum support] [output]");

		String inputFile = args[0];
		String outputFile = args[2];
		MIN_SUPPORT = Integer.parseInt(args[1]);
		
		try{
			PrintStream terminal = System.out;
			PrintStream redirect = new PrintStream(new FileOutputStream(outputFile));
			System.setOut(redirect);
			timer = new Date().getTime();
			AprioriPruning(inputFile);
			System.setOut(terminal);
			System.out.printf("t = %f seconds", (new Date().getTime()-timer) / 1000.0);
			outputHandler(outputFile, terminal);
		}
		catch(Exception ex) {
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
	public static void AprioriPruning(String inputFile) throws Exception{
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
	 * soring the output based on the sample output format
	 * @param inputFile
	 * @param terminal
	 * @throws Exception
	 */
	public static void outputHandler(String inputFile, PrintStream terminal) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		List<String> outputBuffer = new ArrayList<String>();

		String line;
		while((line = br.readLine()) != null)
			outputBuffer.add(line);

		sortOutput(outputBuffer);

		PrintStream redirect = new PrintStream(new FileOutputStream(inputFile));
		System.setOut(redirect);
		for(int i = 0; i < outputBuffer.size(); i++) {
			System.out.println(outputBuffer.get(i));
		}
		System.setOut(terminal);
	}

	/**
	 * sorter, read each single item and compare
	 * @param outputBuffer
	 * @throws Exception
	 */
	public static void sortOutput(List<String> outputBuffer) throws Exception{
		Collections.sort(outputBuffer, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				List<Integer> list1 = parseLine(s1);
				List<Integer> list2 = parseLine(s2);
				int m = 0, n = 0;
				while(m < list1.size() || n < list2.size()) {
					int val1 = m < list1.size() ? list1.get(m) : 0;
					int val2 = n < list2.size() ? list2.get(n) : 0;
					if(val1 != val2)
						return val1 - val2;
					m++;
					n++;
				}
				return 0;
			}
		});
	}

	/**
	 * parse the unordered output entry and ignore the support count
	 * @param line
	 * @return list containing all numbers in each entry
	 */
	public static List<Integer> parseLine(String line) {
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
	public static void testMinSupport(Map<String, Candidate> nextSet) {
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
	public static Map<String, Candidate> clean(Map<String, Candidate> map) {
		for(Map.Entry<String, Candidate> e : map.entrySet() ) {
			if(e.getValue().support < MIN_SUPPORT)
				map.remove(e.getKey());
			else {
				for(int i : e.getValue().set)
					System.out.printf("%d ",i);
				System.out.printf("(%d)\n",e.getValue().support);
			}
		}
		return map;
	}

	/**
	 * generate candidate set by doing self-joining
	 * @param lastSet
	 * @return map containing all candidate itemset
	 */
	public static Map<String,Candidate> getCandidateSet(Map<String, Candidate> lastSet) {
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
	public static List<List<Integer>> getSubsets(int[] item) {
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
	public static void getSubsetHelper(List<List<Integer>> list, List<Integer> temp, int[] item, int length, int start) {
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
	public static void buildMap(Map<String, Candidate> map, int[] set) {
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
	public static void getOneSet(List<Integer> temp) {
		for(int i = 0; i < temp.size(); i++)
			buildMap(singleItem, new int[]{temp.get(i)});
	}

	/**
	 * generate two-item set
	 * @param temp
	 */
	public static void getTwoSet(List<Integer> temp) {
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
	public static void getThreeSet(List<Integer> temp) {
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
	public static void threeSetHelper(List<Integer> temp, int[] item, int length, int start) {
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