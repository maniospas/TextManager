package auth.eng.textManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import auth.eng.textManager.stemmers.Stemmer;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * Word model, which can split text into sentences and individual words. Besides spaces, camel and hungarian notation are further splitted into words.
 * @author Emmanouil Krasanakis
 */
public abstract class WordModel {
	// ------------- WORD MODEL INSTANTIATIONS
	/** A Porter stemmer instantiation.*/
	private static final Stemmer commonStemmer = new auth.eng.textManager.stemmers.PorterStemmer();
	/** An instantiation of {@link BagOfWords} using the {@link #commonStemmer}. */
	public static final WordModel commonBagOfWords = new BagOfWords(commonStemmer);
	/** An instantiation of {@link BagOfUnstemmedWords} using the {@link #commonStemmer}. */
	public static final WordModel commonBagOfUnstemmedWords = new BagOfUnstemmedWords();
	/** An instantiation of {@link BagOfPredicates} using the {@link #commonStemmer}. */
	public static final WordModel commonBagOfPredicates = new BagOfPredicates();
	/** An instantiation of {@link Bigram} using the {@link #commonStemmer}. */
	public static final WordModel commonBigram = new Bigram(commonStemmer);
	/** An instantiation of {@link Skipgram} using the {@link #commonStemmer}. */
	public static final WordModel commonSkipgram = new Skipgram(commonStemmer);
	/** An instantiation of {@link BigramWithoutStopwords} using the {@link #commonStemmer}. */
	public static final WordModel commonBigramWithoutStopwords = new BigramWithoutStopwords(commonStemmer);
	/** An instantiation of {@link PhraseTrigram} using the {@link #commonStemmer}. */
	public static final WordModel commonPhraseTrigram = new PhraseTrigram(commonStemmer);
	/** An instantiation of {@link BagOfWordNet} using the {@link #commonStemmer}. */
	public static final WordModel commonWordNet = new BagOfWordNet(commonStemmer);
	
	// ------------- STATIC PROPERTIES
	protected static final Pattern wordPattern = Pattern.compile("(?=\\p{Lu})|\\s+");//used to split words
	protected static final Pattern purePattern = Pattern.compile("[^A-Za-z0-9 ]");//used to remove non-textual information
	//TODO: stopwords should be static, but this causes a null pointer exception somehow
	private final String[] stopwords = {"a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"};
	private HashMap<String, Set<String>> stopWordStemmedSetPerStemmer = new HashMap<String, Set<String>>();

	// ------------- WORD MODEL PROPERTIES
	private Stemmer stemmer;
	private Set<String> stopWordStemmedSet;
	private HashMap<String, Integer> wordId = new HashMap<String, Integer>();
	private HashMap<Integer,String> wordText = new HashMap<Integer, String>();
	
	public WordModel(Stemmer stemmer) {
		this.stemmer = stemmer;
		if(stemmer==null)
			return;
		stopWordStemmedSet = stopWordStemmedSetPerStemmer.get(stemmer.getName());
		if(stopWordStemmedSet==null) 
			stopWordStemmedSetPerStemmer.put(stemmer.getName(), new HashSet<String>(Arrays.asList(stem(stopwords))));
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
			if((i==0||!Character.isAlphabetic(s[i-1])||Character.isUpperCase(s[i-1])) && (Character.isUpperCase(s[i+1])||!Character.isAlphabetic(s[i+1])||i==s.length-1) && Character.isUpperCase(s[i]))
				sPrepared[i] = Character.toLowerCase(s[i]);
		return new String(sPrepared);
	}
	final protected String[] splitStemSentenceWords(String sentence) {
		sentence = prepareAcronyms(sentence);
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//sentence.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			if(!word.isEmpty() && word.length()>=2) {
				words.add( stemmer.stem(word.toLowerCase()));
			}
		return (String[])words.toArray(new String[words.size()]);
	}
	final protected String[] splitStemSentenceWordsWithoutPreparation(String sentence) {
		String[] allWords = wordPattern.split(purePattern.matcher(sentence).replaceAll(" "));//sentence.split("(?=\\p{Lu})|(\\_|\\,|\\.|\\s|\\n|\\#|\\\"|\\{|\\}|\\@|\\(|\\)|\\;|\\-|\\:|\\*|\\\\|\\/)+");
		ArrayList<String> words = new ArrayList<String>();
		for(String word : allWords)
			if(!word.isEmpty() && word.length()>=2) {
				words.add( stemmer.stem(word.toLowerCase()));
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
	final protected boolean isStopword(String word) {//optimized for lower-case words
		if(word.isEmpty())
			return true;
		if(word.charAt(0) >= '0' && word.charAt(0) <= '9')
			return true;
		if(stopWordStemmedSet.contains(stem(word)))
			return true;
		return false;
	}
	final protected boolean isStemmedStopword(String word) {//optimized for lower-case words
		if(word.isEmpty())
			return true;
		if(word.charAt(0) >= '0' && word.charAt(0) <= '9')
			return true;
		if(stopWordStemmedSet.contains(word))
			return true;
		return false;
	}
	final protected String[] removeStopwords(String[] words) {
		ArrayList<String> ret = new ArrayList<String>(words.length);
		for(String word : words)
			if(word.length()>=2 && !isStopword(word))
				ret.add(word);
		return ret.toArray(new String[ret.size()]);
	}
	final protected String[] removeStemmedStopwords(String[] words) {
		ArrayList<String> ret = new ArrayList<String>(words.length);
		for(String word : words)
			if(word.length()>=2 && !isStemmedStopword(word))
				ret.add(word);
		return ret.toArray(new String[ret.size()]);
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
	 * @see BagOfUnstemmedWords
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
	 * Splits into words without performing stemming.
	 * This may result to larger and more sparse feature spaces compared to {@link BagOfWords}.
	 * @author Emmanouil Krasanakis
	 * @see BagOfWords
	 */
	public static class BagOfUnstemmedWords extends WordModel {
		public BagOfUnstemmedWords() {
			super(null);
		}
		public String[] getSentenceFeatures(String sentence) {
			return splitSentenceWords(sentence);
		}
	}
	/**
	 * Splits a string into words but keeps even empty words.
	 * @author Emmanouil Krasanakis
	 * @see BagOfUnstemmedWords
	 */
	public static class BagOfPredicates extends WordModel {
		public BagOfPredicates() {
			super(null);
		}
		public String[] getSentenceFeatures(String sentence) {
			return splitSentencePredicates(sentence);
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
	/**
	 * Removes all stopwords (including words whose stem is less than 2 characters long) and then generates bigrams.
	 * @author Emmanouil Krasanakis
	 * @see Bigram
	 */
	public static class BigramWithoutStopwords extends WordModel {
		public BigramWithoutStopwords(Stemmer stemmer) {
			super(stemmer);
		}
		public String[] getSentenceFeatures(String sentence) {
			String[] words = splitStemSentenceWords(sentence);
			words = removeStemmedStopwords(words);
			if(words.length==0)
				return new String[0];
			String[] grams = new String[words.length-1];
			for(int i=0;i<grams.length;i++)
				grams[i] = words[i]+" "+words[i+1];
			return grams;
		}
	}
	/**
	 * Generates phrase trigrams, which essentially expand trigrams so as not to end on stopwords.
	 * <b>Phrase trigrams are compared using {@link LevenshteinDistance}</b>.
	 * @author Emmanouil Krasanakis
	 * @see NGram
	 */
	public static class PhraseTrigram extends WordModel {
		public PhraseTrigram(Stemmer stemmer) {
			super(stemmer);
		}
		public String[] extractPhrases(String sentence) {
			//System.out.println(sentence);
			String[] words = splitSentenceWords(sentence);
			String[] stemmedWords = stem(words);
			if(words.length<=2) {
				if(words.length==0)
					return new String[0];
				String[] ret = new String[1];
				StringBuilder builder = new StringBuilder();
				builder.append(stemmedWords[0]);
				if(words.length>1)
					builder.append(" ").append(stemmedWords[1]);
				ret[0] = builder.toString();
				return ret;
			}
			String[] grams = new String[words.length-2];
			for(int i=0;i<words.length-2;i++){
				int first = i;
				int last = i+2;
				while(isStopword(words[last]) && last<words.length-1)
					last++;
				while(isStopword(words[first]) && first>0)
					first--;
				StringBuilder builder = new StringBuilder();
				builder.append(stemmedWords[first]);
				for(int j=first+1;j<=last;j++)
					builder.append(" ").append(stemmedWords[j]);
				grams[i] = builder.toString();
				//System.out.println(grams[i]);
				i = last-1;
			}
			return grams;
		}
		@Override
		public String[] getSentenceFeatures(String sentence) {
			return extractPhrases(sentence);
		}
		@Override
		public boolean equal(String word1, String word2) {
			if(word1==null || word2==null)
				return false;
			int distance = LevenshteinDistance.computeLevenshteinSentenceDistance(word1.split("\\s"), word2.split("\\s"));
			return distance<=1;
		}
		
	}
}
