package auth.eng.textManager.stemmers;


/**
 * Extends the {@link LovinsStemmer} to iteratively perform stemming.
 * @author Eibe Frank
 */
public class IteratedLovinsStemmer extends LovinsStemmer {
  public String stem(String str) {

    if (str.length() <= 2) {
      return str;
    }
    String stemmed = super.stem(str);
    while (!stemmed.equals(str)) {
      str = stemmed;
      stemmed = super.stem(stemmed);
    }
    return stemmed;
  }


}
    

