package auth.eng.textManager.stemmers;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Can be used as a wrapper around a base {{@link Stemmer} so that stems can be converted to words afterwards.
 * @author Emmanouil Krasanakis
 * @see #getBestInterpretation(String)
 * @see #getBestInterpretation(String[])
 */
public class InvertibleStemmer implements Stemmer {
	private Stemmer baseStemmer;
	private HashMap<String, HashMap<String, Integer>> inverseDictionary = new HashMap<String, HashMap<String, Integer>>();
	
	public InvertibleStemmer(Stemmer baseStemmer) {
		this.baseStemmer = baseStemmer;
	}
	public String stem(String word) {
		String ret = baseStemmer.stem(word);
		register(word, ret);
		return ret;
	}
	/**
	 * Registers an occuring stem-word pair.
	 * @param word
	 * @param stem
	 */
	protected void register(String word, String stem) {
		if(stem==null)
			return;
		HashMap<String, Integer> entries = inverseDictionary.get(stem);
		if(entries==null)
			inverseDictionary.put(stem, entries = new HashMap<String, Integer>());
		entries.put(word, entries.getOrDefault(word, 0) + 1);
	}
	/**
	 * @param stem
	 * @return a map between words and their number of occurrences for a given stem
	 */
	public HashMap<String, Integer> getStemInterpretationFrequencies(String stem) {
		return inverseDictionary.get(stem);
	}
	/**
	 * @param stem
	 * @return the word with the highest number of occurrences for the given stem
	 */
	public String getBestInterpretation(String stem) {
		HashMap<String, Integer> entries = getStemInterpretationFrequencies(stem);
		if(entries==null)
			return null;
		int maxVal = 0;
		String ret = null;
		for(Entry<String, Integer> entry : entries.entrySet())
			if(entry.getValue()>maxVal) {
				maxVal = entry.getValue();
				ret = entry.getKey();
			}
		return ret;
	}
	/**
	 * @param stems a list of stems
	 * @return a list of words obtained through {@link #getBestInterpretation(String)} for each stem in the list
	 */
	public String[] getBestInterpretation(String[] stems) {
		String[] ret = new String[stems.length];
		for(int i=0;i<stems.length;i++)
			ret[i] = getBestInterpretation(stems[i]);
		return ret;
	}
}
