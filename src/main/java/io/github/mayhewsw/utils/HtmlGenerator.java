package io.github.mayhewsw.utils;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import io.github.mayhewsw.Dictionary;

import org.apache.commons.lang3.StringUtils;


import java.util.*;

/**
 * Created by stephen on 8/31/17.
 */
@SuppressWarnings("ALL")
public class HtmlGenerator {


//    public static String getHTMLfromTA(TextAnnotation ta, boolean showdefs) {
//        return getHTMLfromTA(ta, new IntPair(-1, -1), ta.getId(), "", null, showdefs);
//    }

    public static String getHTMLfromTA(TextAnnotation ta, Dictionary dict, boolean showdefs) {
        return getHTMLfromTA(ta, new IntPair(-1, -1), ta.getId(), "", dict, showdefs, false, false);
    }

    public static String getHTMLfromTA(TextAnnotation ta, String query, Dictionary dict, boolean showdefs) {
        return getHTMLfromTA(ta, new IntPair(-1, -1), ta.getId(), query, dict, showdefs, false, false);
    }


    public static String getHTMLfromTA(TextAnnotation ta, Dictionary dict, boolean showdefs, boolean showroman) {
        return getHTMLfromTA(ta, new IntPair(-1, -1), ta.getId(), "", dict, showdefs, showroman, false);
    }


    public static String getHTMLfromTA(TextAnnotation ta, Dictionary dict, boolean showdefs, boolean showroman, boolean allowcopy) {
        return getHTMLfromTA(ta, new IntPair(-1, -1), ta.getId(), "", dict, showdefs, showroman, allowcopy);
    }

    // this is basically read only
    public static String getCopyableHTMLFromTA(TextAnnotation ta, Dictionary dict, boolean showdefs, boolean showroman){
        IntPair sentspan = new IntPair(-1, -1);
        String id = ta.getId();

        // required to have one view or another...
        View ner;
        if(ta.hasView(ViewNames.NER_ONTONOTES)){
            ner = ta.getView(ViewNames.NER_ONTONOTES);
        } else{
            ner = ta.getView(ViewNames.NER_CONLL);
        }

        View nersugg = null;
        if(ta.hasView("NER_SUGGESTION")) {
            nersugg = ta.getView("NER_SUGGESTION");
        }else{
            // create a dummy view!
            nersugg = new SpanLabelView("NER_SUGGESTION", ta);
            ta.addView("NER_SUGGESTION", nersugg);
        }

        String[] nonroman_text = ta.getTokens().clone();

        // We clone the text so that when we modify it (below) the TA is unchanged.
        String[] text;
        if(showroman) {
            text = Utils.getRomanTaToks(ta);
        }else {
            text = ta.getTokens().clone();
        }

        if(sentspan.getFirst() != -1) {
            text = Arrays.copyOfRange(text, sentspan.getFirst(), sentspan.getSecond());
        }

        // add spans to every word that is not a constituent.
        for (int t = 0; t < text.length; t++) {
            String def = null;
            if (dict != null && dict.containsKey(nonroman_text[t])) {
                def = dict.get(nonroman_text[t]).get(0);
            }

            if (showdefs && def != null) {
                text[t] = def;
            }
        }

        List<Constituent> sentner;
        List<Constituent> sentnersugg;
        int startoffset;
        if(sentspan.getFirst() == -1){
            sentner = ner.getConstituents();
            sentnersugg = nersugg.getConstituents();
            startoffset = 0;
        }else {
            sentner = ner.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            sentnersugg = ner.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            startoffset = sentspan.getFirst();
        }

        for (Constituent c : sentner) {

            int start = c.getStartSpan() - startoffset;
            int end = c.getEndSpan() - startoffset;

            // important to also include 'cons' class, as it is a keyword in the html
            text[start] = String.format("<span class='%s cons' id='cons-%d-%d' title='%s'>%s", c.getLabel(), start, end, c.getLabel(), text[start]);
            text[end - 1] += "</span>";
        }

        // Then add sentences.
        List<Constituent> sentlist;
        View sentview = ta.getView(ViewNames.SENTENCE);
        if(sentspan.getFirst() == -1){
            sentlist = sentview.getConstituents();
            startoffset = 0;
        }else {
            sentlist = sentview.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            startoffset = sentspan.getFirst();
        }

        for (Constituent c : sentlist) {

            int start = c.getStartSpan() - startoffset;
            int end = c.getEndSpan() - startoffset;

            text[start] = "<p>" + text[start];
            text[end - 1] += "</p>";
        }

        String html = StringUtils.join(text, " ");

        String htmltemplate = "<div class=\"card\">" +
                    "<div class=\"card-header\">%s</div>" +
                    "<div class=\"card-body text\" dir=\"auto\" id=%s>%s</div></div>";
        String out = String.format(htmltemplate, id, id, html) + "\n";


        return out;
    }


    /**
     * Given a sentence, produce the HTML for display. .
     * @return
     */
    public static String getHTMLfromTA(TextAnnotation ta, IntPair span, String id, String query, Dictionary dict, boolean showdefs, boolean showroman, boolean allowcopy) {

        IntPair sentspan = span;
       
        // required to have one view or another...
        View ner;
        if(ta.hasView(ViewNames.NER_ONTONOTES)){
            ner = ta.getView(ViewNames.NER_ONTONOTES);
        } else{
            ner = ta.getView(ViewNames.NER_CONLL);
        }

        View nersugg = null;
        if(ta.hasView("NER_SUGGESTION")) {
            nersugg = ta.getView("NER_SUGGESTION");
        }else{
            // create a dummy view!
            nersugg = new SpanLabelView("NER_SUGGESTION", ta);
            ta.addView("NER_SUGGESTION", nersugg);
        }

        String[] nonroman_text = ta.getTokens().clone();

        // We clone the text so that when we modify it (below) the TA is unchanged.
        String[] text;
        if(showroman) {
            text = Utils.getRomanTaToks(ta);
        }else {
            text = ta.getTokens().clone();
        }

        if(sentspan.getFirst() != -1) {
            text = Arrays.copyOfRange(text, sentspan.getFirst(), sentspan.getSecond());
            nonroman_text = Arrays.copyOfRange(nonroman_text, sentspan.getFirst(), sentspan.getSecond());

        }

        // add spans to every word that is not a constituent.
        for (int t = 0; t < text.length; t++) {
            String def = null;
            if (dict != null && dict.containsKey(nonroman_text[t])) {
                def = dict.get(nonroman_text[t]).get(0);
            }

            String tokid = String.format("tok-%s-%s", id, t);


            // The orig attribute is used in the dictionary.
            if (showdefs && def != null) {
                text[t] = "<span class='token pointer def' orig=\""+nonroman_text[t]+"\" id='"+tokid+"'>" + def + "</span>";
            } else {
                // FIXME: this will only work for single word queries.
                if (query.length() > 0 && text[t].startsWith(query)) {
                    text[t] = "<span class='token pointer emph' orig=\""+nonroman_text[t]+"\" id='"+tokid+"'>" + text[t] + "</span>";
                } else {
                    text[t] = "<span class='token pointer' orig=\""+nonroman_text[t]+"\" id='"+tokid+"'>" + text[t] + "</span>";
                }
            }
        }
        System.out.println(ta.getAvailableViews());
        System.out.println("ner is "+ner);
        System.out.println(ner.getConstituents());
        System.out.println(ner.getRelations());
        List<Constituent> sentner;
        List<Constituent> sentnersugg;
        int startoffset;
        if(sentspan.getFirst() == -1){
            sentner = ner.getConstituents();
            sentnersugg = nersugg.getConstituents();
            startoffset = 0;
        }else {
            sentner = ner.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            sentnersugg = ner.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            startoffset = sentspan.getFirst();
        }

        for (Constituent c : sentner) {

            int start = c.getStartSpan() - startoffset;
            int end = c.getEndSpan() - startoffset;


            
            
            // important to also include 'cons' class, as it is a keyword in the html
            text[start] = String.format("<span class='%s pointer cons' id='cons-%d-%d' title='%s'>%s", c.getLabel(), start, end, c.getLabel(), text[start]);
            text[end - 1] += "</span>";
        }

    
       
//        for (Constituent c : sentnersugg) {
//
//            int start = c.getStartSpan() - startoffset;
//            int end = c.getEndSpan() - startoffset;
//
//            // important to also include 'cons' class, as it is a keyword in the html
//            text[start] = String.format("<strong>%s", text[start]);
//            text[end - 1] += "</strong>";
//        }

        // Then add sentences.
        List<Constituent> sentlist;
        View sentview = ta.getView(ViewNames.SENTENCE);
        if(sentspan.getFirst() == -1){
            sentlist = sentview.getConstituents();
            startoffset = 0;
        }else {
            sentlist = sentview.getConstituentsCoveringSpan(sentspan.getFirst(), sentspan.getSecond());
            startoffset = sentspan.getFirst();
        }

        for (Constituent c : sentlist) {

            int start = c.getStartSpan() - startoffset;
            int end = c.getEndSpan() - startoffset;

            text[start] = "<p>" + text[start];
            text[end - 1] += "</p>";
        }

        String sep = "";
        if(allowcopy){
            sep = " ";
        }
        String html = StringUtils.join(text, sep);

        String htmltemplate;
        if(allowcopy){
            htmltemplate = "<div class=\"card\">" +
                    "<div class=\"card-header\">%s</div>" +
                    "<div class=\"card-body text\" dir=\"auto\" id=%s>%s</div></div>";
        }else{
            htmltemplate = "<div class=\"card\">" +
                    "<div class=\"card-header\">%s</div>" +
                    "<div class=\"card-body text nocopy\" dir=\"auto\" id=%s>%s</div></div>";
        }
        if(ta.hasView("CANDGEN"))
        {
        	sentner = ta.getView("CANDGEN").getConstituents();
        	 Map<String,List<String>> candgenMap = new HashMap<>(); 
        	 int limit = 4;
        	 for (Constituent c : sentner)
        	 {

        		 /*
        	        //print out candgen values
        	        System.out.println("c.getLabel() is "+c.getLabel());
        	       
        	        System.out.println("CANDGEN is "+c2.getLabel());
        	        System.out.println("CANDGEN2 is "+c2.toSExpression());
        	        System.out.println("CANDGEN5 is "+c2.getNumberOfTokens());
        	        System.out.println("CANDGEN6 is "+c2.getTokenizedSurfaceForm());
        	        System.out.println("CANDGEN3 is "+c2.getSurfaceForm());
        	        System.out.println("CANDGEN7 is "+c2.getAttributeKeys());
        	     */
        		 	String key = c.getSurfaceForm();
        	        System.out.println("CANDGEN3 is "+c.getSurfaceForm());
        	       if(!candgenMap.containsKey(key))
        	       {
        	    	   List<String> sortedList = new ArrayList<>();
        	    	   List<String> originalLabels = sortByValue(c.getLabelsToScores());
        	    	  // System.out.println("CANDGEN8 is "+));
        	    	   
        	    	   for(int a=0;a<originalLabels.size() && a<limit;a++)
        	    	   {
        	    		   String label = originalLabels.get(a);
        	    		   //System.out.println("label is "+label);
        	    		   List<String> array = mySplit(label);
  //      	    		   System.out.println("array is "+array);
        	    		   String url = array.get(0);
        	    		   if(array.size()>2)
        	    			   url = array.get(2);
        	    		   else
        	    			   url = "https://www.geonames.org/"+url;
        	    		   System.out.println("url is "+url);
        	    		   sortedList.add(url);
        	    		   
        	    	   }
        	    	  // System.out.println("sortedList is "+sortedList);
        	    	   candgenMap.put(key, sortedList);
        	       }
        	       
             }
        	 StringBuilder sb = new StringBuilder();
        	 for(Map.Entry<String, List<String>> entry:candgenMap.entrySet())
        	 {
        		 sb.append("[ \'"+entry.getKey()+"\', [");
        		 
        		 for(String v:entry.getValue())
        		 {
        			 sb.append("\""+v+"\",");
            		 
        		 }
        		 //delete last comma
        		 sb.deleteCharAt(sb.length()-1);
        		 
        		 sb.append("] ],");
        	 }
        	 //delete last comma
    		 sb.deleteCharAt(sb.length()-1);
    		 
    		 String mikeHTMLTemp = "<script>"
    		 		+ " var candgenMap = new Map(["+sb.toString()+"]);"
    		 		+ "console.log(\"testing\");</script>";
    		 
    		 htmltemplate+=mikeHTMLTemp;

        }
        String out = String.format(htmltemplate, id, id, html) + "\n";

        System.out.println(out);

        return out;
    }
    
    //for some return the string.split function doesn't work for '|' so I implemented my own
	public static List<String> mySplit(String str)
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
        System.out.println("list is "+list);
        System.out.println("temp is "+temp);
        
        // put data from sorted list to hashmap 
        /*
        HashMap<String, Double> temp = new LinkedHashMap<>(); 
        for (Map.Entry<String, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        */
        return temp; 
    } 
}
