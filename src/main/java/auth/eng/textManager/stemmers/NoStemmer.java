package auth.eng.textManager.stemmers;

/**
 * A {@link Stemmer} whose {@link #stem(String)} function returns given words as-is.
 * @author Emmanouil Krasanakis
 */
public class NoStemmer implements Stemmer {
	public String getName() {
		return "NoStemmer";
	}
	public String stem(String word) {
		return word;
	}

}
