package io.github.mayhewsw.utils;

import cz.jirutka.unidecode.Unidecode;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;


import java.util.*;


/**
 * Created by mayhew2 on 6/7/17.
 */
public class Utils {
	//
	private static final int EdlCandidateLimit = 4;

	public static HashMap<String, String> labelcolors;
	static {
		labelcolors = new HashMap<>();
		// put some common label colors here.
		labelcolors.put("PER", "yellow");
		labelcolors.put("LOC", "greenyellow");
		labelcolors.put("GPE", "coral");
		labelcolors.put("MISC", "coral");
		labelcolors.put("ORG", "lightblue");
	}

	/**
	 * Given a label, this will return a standard color, or a random color.
	 * @param label
	 * @return
	 */
	public static String getColorOrRandom(String label){
		String color;
		if(Utils.labelcolors.containsKey(label)){
			color = Utils.labelcolors.get(label);
		}else{
			Random random = new Random();
			int nextInt = random.nextInt(256*256*256);
			color = String.format("#%06x", nextInt);
		}
		return color;
	}

	/**
	 * Given a TextAnnotation, this will return the tokens in a cloned String[] array. If the ROMANIZATION
	 * view is present, the tokens will come from there. Otherwise, this uses unidecode to get a base romanization.
	 *
	 * We recommend using the excellent Uroman library: https://www.isi.edu/~ulf/uroman.html
	 *
	 * @param ta TextAnnotation
	 * @return an array of words, romanized if available.
	 */
	public static String[] getRomanTaToks(TextAnnotation ta){
		String[] text;
		if(ta.hasView("ROMANIZATION")){
			View translit = ta.getView("ROMANIZATION");
			StringBuilder sb = new StringBuilder();
			for(Constituent c : translit.getConstituents()){
				String romantext = c.getLabel().replace(" ", "_");
				if (romantext.length() == 0){
					romantext = "_";
				}
				sb.append(romantext +" ");
			}
			text = sb.toString().trim().split(" ");
		}else {

			Unidecode unidecode = Unidecode.toAscii();

			text = ta.getTokens().clone();
			for(int t = 0; t < text.length; t++){
				text[t] = unidecode.decode(text[t]);
			}
		}

		return text;
	}


	/**
	 * This removes all stems from a word, even if they are stacked.
	 * @param word
	 * @param suffixes
	 * @return
	 */
	public static String stem(String word, List<String> suffixes){
		boolean stemmed = false;
		while(!stemmed) {
			stemmed = true;
			for (String suff : suffixes) {
				if (word.endsWith(suff)) {
					word = word.substring(0, word.length() - suff.length());
					stemmed = false;
				}
			}
		}
		return word;
	}

	public static void main(String[] args) {
		String w = "jungoliqlarning";
		List<String> suf = new ArrayList<>();
		suf.add("liq");
		suf.add("lar");
		suf.add("ning");
		System.out.println(stem(w, suf));
	}

	public static Map<String,CandgenValue> buildCandgenMap(List<Constituent> sentner)
	{
		Map<String,CandgenValue> candgenMap = new HashMap<>(); 


		for (Constituent c : sentner)
		{

			
   	        //print out candgen values
   	        //System.out.println("c.getLabel() is "+c.getLabel());
	   	     //System.out.println("CANDGEN3 is "+c.getSurfaceForm());
	   	       
			 
			String key = c.getSurfaceForm();
			//System.out.println("CANDGEN3 is "+c.getSurfaceForm());
			if(!candgenMap.containsKey(key))
			{
				String label = c.getLabel();
				Map<String,Double> labelScoreMap = c.getLabelsToScores();
				if(label!=null && labelScoreMap!=null)
				{
					CandgenValue cv = new CandgenValue(label);
					System.out.println(key);
					List<String> originalLabels = sortByValue(labelScoreMap);
					// System.out.println("CANDGEN8 is "+));
	
					for(int a=0;a<originalLabels.size() && a<EdlCandidateLimit;a++)
					{
						String candidateLabel = originalLabels.get(a);
						//System.out.println("label is "+label);
						List<String> array = splitOnVerticalLine(candidateLabel);
						//System.out.println("array is "+array);
						String url = array.get(0);
						if(array.size()>2)
							url = array.get(2);
						else
							url = "https://www.geonames.org/"+url;
						//System.out.println("url is "+url);
						cv.addCandidate(candidateLabel, url);
	
					}
					// System.out.println("sortedList is "+sortedList);
					candgenMap.put(key, cv);
				}
			}

		}
		return candgenMap;
	}

	//for some return the string.split function doesn't work for '|' so I implemented my own
	public static List<String> splitOnVerticalLine(String str)
	{
		char splitter = '|';

		List<String> returnValue = new ArrayList<>();
		int b=0;
		for(int i =0;i<str.length();i++)
		{
			if(str.charAt(i)==splitter)
			{
				returnValue.add(str.substring(b, i));
				b=i+1;
			}
		}
		returnValue.add(str.substring(b, str.length()));
		return returnValue;
	}

	// function to sort hashmap by values 
	public static List<String> sortByValue(Map<String, Double> hm) 
	{ 
		// Create a list from elements of HashMap 
		List<Map.Entry<String, Double> > list = 
				new LinkedList<>(hm.entrySet()); 

		// Sorts the list in descending order
		Collections.sort(list, new Comparator<Map.Entry<String, Double> >() { 
			public int compare(Map.Entry<String, Double> o1,  
					Map.Entry<String, Double> o2) 
			{ 
				return (o2.getValue()).compareTo(o1.getValue()); 
			} 
		}); 

		List<String> temp = new LinkedList<>(); 
		for (Map.Entry<String, Double> aa : list) { 
			temp.add(aa.getKey()); 
		} 
		//System.out.println("list is "+list);
		//System.out.println("temp is "+temp);

		// put data from sorted list to hashmap 
		/*
        HashMap<String, Double> temp = new LinkedHashMap<>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
		 */
		return temp; 
	} 
	public static class CandgenValue
	{
		private List<EdlCandidate> edlCandidates = new ArrayList<>();
		private Set<String> urls = new HashSet<>();
		
		private String currentLabel;

		public CandgenValue(String currentLabel) 
		{
			this.currentLabel = currentLabel;
		}

		public void addCandidate(String label, String url)
		{
			if(urls.contains(url))
				return;
			EdlCandidate e = new EdlCandidate(label,url);
			edlCandidates.add(e);
			urls.add(url);
		} 

		public String toString()
		{
			return "[\""+currentLabel+"\","+edlCandidates.toString()+"]";
		}
	}

	public static class EdlCandidate
	{
		private String url,label;

		public EdlCandidate(String label, String url) 
		{
			this.url = url;
			this.label = label;
		}

		public String toString()
		{
			return "[\""+label+"\","+"\""+url+"\"]";
		}
	}
}
