package auth.eng.textManager.stemmers;

/**
 * Implementations of the {@link #stem(String)} function deprive words from their suffixes.
 * @author Emmanouil Krasanakis
 */
public interface Stemmer {
	/**
	 * @return a common name for the stemmer
	 */
	public String getName();
	/**
	 * Discovers the given word's stem, which translates to removing suffixes (such as endings, -ing or past terms).
	 * @param word a given word (i.e. must not contain spaces)
	 * @return the discovered stem
	 */
	public String stem(String word);
}
