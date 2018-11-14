package auth.eng.textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import auth.eng.textManager.stemmers.Stemmer;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Word model, which can split text into sentences and individual words. Besides spaces, camel and Hungarian notations are further split into words.
 * @author Emmanouil Krasanakis
 * @version Stopword removal is handled through a {@link auth.eng.textManager.stemmers.StopwordRemove} stemmer.
 */
public abstract class WordModel {
	// ------------- STATIC PROPERTIES
	protected static final Pattern wordPattern = Pattern.compile("(?=\\p{Lu})|\\s+");//used to split words
	protected static final Pattern purePattern = Pattern.compile("[^A-Za-z0-9 ]");//used to remove non-textual information

	// ------------- WORD MODEL PROPERTIES
	private Stemmer stemmer;
	private HashMap<String, Integer> wordId = new HashMap<String, Integer>();
	private HashMap<Integer,String> wordText = new HashMap<Integer, String>();
	
	public WordModel(Stemmer stemmer) {
		this.stemmer = stemmer;
		if(stemmer==null)
			throw new RuntimeException("null stemmer no longer supported. Use NoStemmer() instead.");
	}
	/**
	 * @return the {@link Stemmer} used by the model (some models don't use a stemmer)
	 */
	public Stemmer getStemmer() {
		return stemmer;
	}
	/**
	 * Adds additional dimensions to features if features not found for the sentence.
	 * A number of trailing zeros (which may be added by future feature discoveries) may not be present
	 * @param sentence
	 * @return an array of [0,1] representing the features discovered within the word model.
	 */
	public double[] getSentenceFeatureVector(String sentence) {
		ArrayList<Integer> idsDiscovered = new ArrayList<Integer>();
		for(String word : getSentenceFeatures(sentence)) {
			Integer id = wordId.get(word);
			if(id==null) {
				wordId.put(word, id = wordId.size());
				wordText.put(id, word);
			}
			idsDiscovered.add(id);
		}
		double[] vector = new double[wordId.size()];//initializes for zeros
		for(int id : idsDiscovered)
			vector[id] = 1;
		return vector;
	}
	/**
	 * Generates a single string which contains every word corresponding to non-zero components of the designated vector.
	 * @param vector a given vector
	 * @return the generated sentence
	 */
	public String convertVectorToFeatureSentence(double[] vector) {
		StringBuilder ret = new StringBuilder();
		for(int i=0;i<vector.length;i++)
			if(vector[i]!=0)
				ret.append(wordText.get(i)).append(" ");
		return ret.toString().trim();
	}
	/**
	 * @return the length of the feature vector
	 */
	public int getCurrentFeatureVectorLength() {
		return wordId.size();
	}
	
	/**
	 * Different word models implement this function differently.
	 * @param sentence a given sentence
	 * @return the words which comprise the sentence
	 */
	public abstract String[] getSentenceFeatures(String sentence);
	
	/**
	 * Implements a string equality comparison between (possibly <code>null</code>) words.
	 * Some word models may require a more sophisticated comparison.
	 * @param word1
	 * @param word2
	 * @return true, if the given words match
	 */
	public boolean equal(String word1, String word2) {
		if(word1==null || word2==null)
			return false;
		return word1.equals(word2);
	}
	
	/**
	 * Splits into sentences without splitting any of the common parenthesis or brackets (i.e. (), [], {})
	 * @param text
	 * @return a table of found sentences
	 */
	public static String[] getTextSentences(String text) {
		String[] allSentences = text.split("(\\n|(\\.\\s+($(|\\n|\\s+(?=[A-Z0-9])))))");
		ArrayList<String> sentences = new ArrayList<String>();
		String currentString = "";
		int accumulation = 0;
		for(String sentence : allSentences) {
			int localAccumulation = countOccurrences(sentence, '(')+countOccurrences(sentence, '{')+countOccurrences(sentence, '[')
									-countOccurrences(sentence, ')')-countOccurrences(sentence, '}')-countOccurrences(sentence, ']');
			accumulation += localAccumulation;
			if(accumulation>0) 
				currentString += sentence+" ";
			else {
				currentString += sentence;
				sentences.add(currentString);
				currentString = "";
			}
		}
		if(!currentString.isEmpty()) {
			sentences.add(currentString);
		}
		return (String[])sentences.toArray(new String[sentences.size()]);
	}
	private static int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	        if (haystack.charAt(i) == needle)
	             count++;
	    return count;
	}
	final protected static String prepareAcronyms(String sentence) {
		char[] s = sentence.toCharArray();
		char[] sPrepared = sentence.toCharArray();
		for(int i=0;i<s.length;i++)
			if((i==0||!Character.isAlphabetic(s[i-1])||Character.isUpperCase(s[i-1])) && (i+1>=s.length||Character.isUpperCase(s[i+1])||!Character.isAlphabetic(s[i+1])||i==s.length-1) && Character.isUpperCase(s[i]))
				sPrepared[i] = Character.toLowerCase(s[i]);
		return new String(sPrepared);
	}
	final protected String[] splitStemSentenceWords(String sentence) {
		sentence = prepareAcronyms(sentence);
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//sentence.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			if(!word.isEmpty()) {
				String w = stemmer.stem(word);
				if(w!=null)
					words.add(w);
			}
		return (String[])words.toArray(new String[words.size()]);
	}
	final protected String[] splitStemSentenceWordsWithoutPreparation(String sentence) {
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//sentence.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			if(!word.isEmpty()) {
				String w = stemmer.stem(word);
				if(w!=null)
					words.add(w);
			}
		return (String[])words.toArray(new String[words.size()]);
	}
	final protected static String[] splitSentenceWords(String sentence) {
		sentence = prepareAcronyms(sentence);
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			if(word.length()>=1)
				words.add(word.toLowerCase());
		return (String[])words.toArray(new String[words.size()]);
	}
	final protected static String[] splitSentencePredicates(String sentence) {
		sentence = prepareAcronyms(sentence);
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			words.add(word.toLowerCase().trim());
		return (String[])words.toArray(new String[words.size()]);
	}
	final protected String stem(String word) {
		return  stemmer.stem(word);
	}
	final protected String[] stem(String[] words) {
		String[] stemmed = new String[words.length];
		for(int i=0;i<words.length;i++)
			stemmed[i] =  stemmer.stem(words[i]);
		return stemmed;
	}
	final protected String[] stem(String[] words, Stemmer stemmer) {
		String[] stemmed = new String[words.length];
		for(int i=0;i<words.length;i++)
			stemmed[i] = stemmer.stem(words[i]);
		return stemmed;
	}
	public static String mergeToSentence(String[] words) {
		if(words.length==0)
			return "";
		StringBuilder ret = new StringBuilder();
		ret.append(words[0]);
		for(int i=1;i<words.length;i++)
			ret.append(" ").append(words[i]);
		return ret.toString();
	}
	
	/**
	 * Splits into stemmed words and adds stemmed WordNet synonyms as additional features.
	 * Should be preferred for more informed contextual matching.
	 * @author Emmanouil Krasanakis
	 */
	public static class BagOfWordNet extends WordModel {
		private Dictionary dictionary;
		public BagOfWordNet(Stemmer stemmer) {
			super(stemmer);
		}
		public String[] getSentenceFeatures(String sentence) {
			if(dictionary==null) {
				try {
					dictionary = Dictionary.getDefaultResourceInstance();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			ArrayList<String> ret = new ArrayList<String>();
			try{
				for(String word : splitSentenceWords(sentence)) {
					ret.add(word);//may yield duplicate entries
					IndexWord indexWord;
					PointerTargetNodeList hypernyms;
					indexWord = dictionary.lookupIndexWord(POS.VERB, word);
					hypernyms = (indexWord!=null && indexWord.getSenses()!=null && indexWord.getSenses().size()>0)?PointerUtils.getDirectHypernyms(indexWord.getSenses().get(0)):null;
					if(hypernyms!=null && hypernyms.size()>0)
						for(int i=0;i<hypernyms.get(0).getSynset().getWords().size();i++)//get synonym set
							for(String subword : (splitStemSentenceWordsWithoutPreparation(hypernyms.get(0).getSynset().getWords().get(i).getLemma())))
								ret.add(subword);
					if(indexWord!=null && indexWord.getSenses().size()!=0 &&  indexWord.getSenses().size()!=0)
						for(int i=0;i<indexWord.getSenses().get(0).getWords().size();i++)
							for(String subword : (splitStemSentenceWordsWithoutPreparation(indexWord.getSenses().get(0).getWords().get(i).getLemma())))
								ret.add(subword);
					indexWord = dictionary.lookupIndexWord(POS.ADJECTIVE, word);
					hypernyms = (indexWord!=null && indexWord.getSenses()!=null && indexWord.getSenses().size()>0)?PointerUtils.getDirectHypernyms(indexWord.getSenses().get(0)):null;
					if(hypernyms!=null && hypernyms.size()>0)
						for(int i=0;i<hypernyms.get(0).getSynset().getWords().size();i++)//get synonym set
							for(String subword : (splitStemSentenceWordsWithoutPreparation(hypernyms.get(0).getSynset().getWords().get(i).getLemma())))
								ret.add(subword);
					if(indexWord!=null && indexWord.getSenses().size()!=0 &&  indexWord.getSenses().size()!=0)
						for(int i=0;i<indexWord.getSenses().get(0).getWords().size();i++)
							for(String subword : (splitStemSentenceWordsWithoutPreparation(indexWord.getSenses().get(0).getWords().get(i).getLemma())))
								ret.add(subword);
					indexWord = dictionary.lookupIndexWord(POS.ADVERB, word);
					hypernyms = (indexWord!=null && indexWord.getSenses()!=null && indexWord.getSenses().size()>0)?PointerUtils.getDirectHypernyms(indexWord.getSenses().get(0)):null;
					if(hypernyms!=null && hypernyms.size()>0)
						for(int i=0;i<hypernyms.get(0).getSynset().getWords().size();i++)//get synonym set
							for(String subword : (splitStemSentenceWordsWithoutPreparation(hypernyms.get(0).getSynset().getWords().get(i).getLemma())))
								ret.add(subword);
					if(indexWord!=null && indexWord.getSenses().size()!=0 &&  indexWord.getSenses().size()!=0)
						for(int i=0;i<indexWord.getSenses().get(0).getWords().size();i++)
							for(String subword : (splitStemSentenceWordsWithoutPreparation(indexWord.getSenses().get(0).getWords().get(i).getLemma())))
								ret.add(subword);
					indexWord = dictionary.lookupIndexWord(POS.NOUN, word);
					hypernyms = (indexWord!=null && indexWord.getSenses()!=null && indexWord.getSenses().size()>0)?PointerUtils.getDirectHypernyms(indexWord.getSenses().get(0)):null;
					if(hypernyms!=null && hypernyms.size()>0)
						for(int i=0;i<hypernyms.get(0).getSynset().getWords().size();i++)//get synonym set
							for(String subword : (splitStemSentenceWordsWithoutPreparation(hypernyms.get(0).getSynset().getWords().get(i).getLemma())))
								ret.add(subword);
					if(indexWord!=null && indexWord.getSenses().size()!=0 &&  indexWord.getSenses().size()!=0)
						for(int i=0;i<indexWord.getSenses().get(0).getWords().size();i++)
							for(String subword : (splitStemSentenceWordsWithoutPreparation(indexWord.getSenses().get(0).getWords().get(i).getLemma())))
								ret.add(subword);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return ret.toArray(new String[ret.size()]);
		}
	}
	/**
	 * Splits into words and stems them.
	 * @author Emmanouil Krasanakis
	 */
	public static class BagOfWords extends WordModel {
		public BagOfWords(Stemmer stemmer) {
			super(stemmer);
		}
		public String[] getSentenceFeatures(String sentence) {
			return splitStemSentenceWords(sentence);
		}
	}
	/**
	 * Stems words and groups consecutive ones (e.g. the words ABCDEF for yields the 4-Grams ABCD, BCDE, CDEF). 
	 * Edge checks make this implementation a bit slower than the {@link Bigram} implementation.
	 * @author Emmanouil Krasanakis
	 * @see Bigram
	 * @see PhraseTrigram
	 */
	public static class NGram extends WordModel {
		private int N;
		public NGram(int N, Stemmer stemmer) {
			super(stemmer);
			this.N = N;
		}
		protected static String[] getGrams(String[] words, int N) {
			String[] grams = new String[words.length-N+1];
			for(int i=0;i<grams.length;i++) {
				StringBuilder builder = new StringBuilder();
				builder.append(words[i]);
				for(int j=1;j<N;j++)
					builder.append(" ").append(words[i+j]);
				grams[i] = builder.toString();
			}
			return grams;
		}
		public String[] getSentenceFeatures(String sentence) {
			String[] words = splitStemSentenceWords(sentence);
			if(words.length<N) {
				if(words.length==0)
					return new String[0];
				String[] ret = new String[1];
				StringBuilder builder = new StringBuilder();
				builder.append(words[0]);
				for(int i=1;i<words.length;i++)
					builder.append(" ").append(words[i]);
				ret[0] = builder.toString();
				return ret;
			}
			return getGrams(words, N);
		}
	}
	/**
	 * Combines every {@link NGram} up to the given N (e.g. for N=3, it includes 1-Grams, 2-Gram and 3-Grams as features).
	 * @author Emmanouil Krasanakis
	 */
	public static class MultiGram extends WordModel {
		private int N;
		public MultiGram(int N, Stemmer stemmer) {
			super(stemmer);
			this.N = N;
		}
		@Override
		public String[] getSentenceFeatures(String sentence) {
			ArrayList<String> words = new ArrayList<String>();
			for(int n=1;n<=N;n++) {
				NGram ngram = new NGram(n, getStemmer());
				for(String word : ngram.getSentenceFeatures(sentence))
					words.add(word);
			}
			String[] grams = new String[words.size()];
			for(int i=0;i<grams.length;i++)
				grams[i] = words.get(i);
			return grams;
		}
	} 
	/**
	 * Generates bigrams by grouping every pair of consecutive words
	 * (e.g. the words ABCD form the bigrams AB, BC, CD).
	 * Words are stemmed before grouping.
	 * Does not employ the {@link NGram} implementation to speed up computations.
	 * @author Emmanouil Krasanakis
	 * @see NGram
	 * @see BigramWithoutStopwords
	 */
	public static class Bigram extends WordModel {
		public Bigram(Stemmer stemmer) {
			super(stemmer);
		}
		public String[] getSentenceFeatures(String sentence) {
			String[] words = splitStemSentenceWords(sentence);
			if(words.length==0)
				return new String[0];
			String[] grams = new String[words.length-1];
			for(int i=0;i<grams.length;i++)
				grams[i] = words[i]+" "+words[i+1];
			return grams;
		}
	}
	/**
	 * Generates bigrams by grouping every pair of words within a window.
	 * @author Emmanouil Krasanakis
	 */
	public static class Skipgram extends WordModel {
		private int window;
		public Skipgram(Stemmer stemmer) {
			this(stemmer, 5);
		}
		public Skipgram(Stemmer stemmer, int window) {
			super(stemmer);
			this.window = window;
		}
		public String[] getSentenceFeatures(String sentence) {
			String[] words = splitStemSentenceWords(sentence);
			if(words.length==0 || words.length*window-window*(window-1)/2<=0)
				return new String[0];
			String[] grams = new String[words.length*window-window*(window-1)/2];
			for(int i=0;i<words.length-1;i++)
				for(int j=0;j<window && i+j+1<words.length;j++)
					grams[i*window+j] = words[i]+" "+words[i+j+1];
			return grams;
		}
	}
}
