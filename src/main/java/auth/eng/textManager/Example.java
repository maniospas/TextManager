package auth.eng.textManager;

import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class Example 
{
    public static void main( String[] args )
    {
    	auth.eng.textManager.stemmers.InvertibleStemmer stemmer = new auth.eng.textManager.stemmers.InvertibleStemmer(new auth.eng.textManager.stemmers.StopwordRemove(new auth.eng.textManager.stemmers.PorterStemmer()));
    	WordModel wordModel = new WordModel.BagOfWords(stemmer);
    	args = new String[]{"getActionSheetPanesPane", "getActionToPanes"};
    	
    	if(args.length!=2)
    		System.out.println("Two string arguments produce their cosine similarity under WordNet features");
    	else {
    		System.out.println("----- Features");
    		System.out.println(args[0]+" --> "+Arrays.toString(wordModel.getSentenceFeatures(args[0])));
    		System.out.println(args[1]+" --> "+Arrays.toString(wordModel.getSentenceFeatures(args[1])));
    		System.out.println("----- Binary");
    		System.out.println(Arrays.toString(wordModel.getSentenceFeatureVector(args[0])));
    		System.out.println(Arrays.toString(wordModel.getSentenceFeatureVector(args[1])));
    		System.out.println("----- Similarity");
			System.out.println(similarity(wordModel.getSentenceFeatureVector(args[0]), wordModel.getSentenceFeatureVector(args[1])));
    		System.out.println("----- Inverse Features");
    		System.out.println(args[0]+" --> "+Arrays.toString(((auth.eng.textManager.stemmers.InvertibleStemmer)wordModel.getStemmer()).getBestInterpretation(wordModel.getSentenceFeatures(args[0]))));
    		System.out.println(args[1]+" --> "+Arrays.toString(((auth.eng.textManager.stemmers.InvertibleStemmer)wordModel.getStemmer()).getBestInterpretation(wordModel.getSentenceFeatures(args[1]))));
    	}
    }
    
    public static double similarity(double[] v1, double[] v2) {
    	return dot(v1,v2)/Math.sqrt(dot(v1,v1)*dot(v2,v2));//it's fine to divide, since larger feature sets associate to more cognitive relations
    }
    
    public static double dot(double[] v1, double[] v2) {
    	double ret = 0;
    	for(int i=0;i<v1.length && i<v2.length;i++)
    		ret += v1[i]*v2[i];
    	return ret;
    }
}
