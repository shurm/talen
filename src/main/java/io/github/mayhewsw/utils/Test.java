package io.github.mayhewsw.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Test {
	public static Map<String, Map<String, Double>> buildCandgenKeyAndLabelsMap(String candgenString) throws ParseException
	{
		Map<String,Map<String,Double>> candgenMap = new HashMap<>(); 
		JSONParser parser = new JSONParser(); 
		JSONArray jsonarray = (JSONArray) parser.parse(candgenString);
		Iterator iterator= jsonarray.iterator();
		while(iterator.hasNext())
		{
			JSONArray labelsAndScores = (JSONArray) iterator.next();
			String surfaceForm = labelsAndScores.get(0).toString();
			
			
			Map<String,Double> map = new HashMap<>();
			JSONArray candidates = (JSONArray) ((JSONArray) labelsAndScores.get(1)).get(0);
			String selectedLabel = candidates.get(0).toString();
			candidates = (JSONArray) candidates.get(1);
			for(int a=0;a<candidates.size();a++)
			{
				JSONArray candidate = (JSONArray) candidates.get(a);
				String label = candidate.get(0).toString();
				map.put(label,  0.0);
			}
			if(!map.isEmpty())
				map.put(selectedLabel,  1.0);
			
			candgenMap.put(surfaceForm, map);
			System.out.println(candidates);
			
		}
		return candgenMap;
	}
	public static void main(String [] args)
	{
		String f = "[[\"George Washington\",[[\"3558342|George Washington\",[[\"3558342|George Washington\",\"https://www.geonames.org/3558342\"],[\"3558343|George Washington|https://en.wikipedia.org/wiki/George_Washington\",\"https://en.wikipedia.org/wiki/George_Washington\"]]]]],[\"Washington\",[[\"dummy_id|Washington|https://ff\",[[\"dummy_id|Washington|https://test\",\"https://test\"],[\"4140963|Washington,_D.C.\",\"https://www.geonames.org/4140963\"],[\"5815135|Washington_(state)\",\"https://www.geonames.org/5815135\"],[\"5218069|Washington,_Pennsylvania\",\"https://www.geonames.org/5218069\"],[\"dummy_id|Washington|https://ff\",\"https://ff\"]]]]]]\r\n";
		try {
			buildCandgenKeyAndLabelsMap(f);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
