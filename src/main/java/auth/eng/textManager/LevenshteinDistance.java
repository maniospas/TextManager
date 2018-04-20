package auth.eng.textManager;

/**
 * Simple class which computes the Levenshtein distance. Use {{@link #levenshteinSimilarity(String, String)} to obtain 
 * a string similarity metric.
 * @author Emmanouil Krasanakis
 */
public class LevenshteinDistance {
	private static int minimum(int a, int b, int c) {                            
		return Math.min(Math.min(a, b), c);                                      
	}
	/**
	 * Calculates the Levenshtein similarity between two strings by diving the Levenshtein distance with its
	 * supremum and subtracting it from 1.
	 * @param lhs the first string
	 * @param rhs the second string
	 * @return a value in the range [0,1]
	 */
	public static double levenshteinSimilarity(String lhs, String rhs) {
		return 1-(double)computeLevenshteinDistance(lhs, rhs)/Math.max(lhs.length(),rhs.length());
	}                                   
	public static int computeLevenshteinDistance(String lhs, String rhs) {      
		int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];
		for (int i = 0; i <= lhs.length(); i++)                                 
		distance[i][0] = i;                                                  
		for (int j = 1; j <= rhs.length(); j++)                                 
		distance[0][j] = j;                      
		for (int i = 1; i <= lhs.length(); i++)                                 
		for (int j = 1; j <= rhs.length(); j++)                             
		distance[i][j] = minimum(                                        
			distance[i - 1][j] + 1,                                  
			distance[i][j - 1] + 1,                                  
			distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));                 
		return distance[lhs.length()][rhs.length()];                           
	}        
	public static int computeLevenshteinSentenceDistance(String[] lhs, String[] rhs) {
		int[][] distance = new int[lhs.length + 1][rhs.length + 1];        
		for (int i = 0; i <= lhs.length; i++)                                 
			distance[i][0] = i;                                                  
		for (int j = 1; j <= rhs.length; j++)                                 
			distance[0][j] = j;                
		for (int i = 1; i <= lhs.length; i++)                                 
			for (int j = 1; j <= rhs.length; j++)                             
				distance[i][j] = minimum(                                        
					distance[i - 1][j] + 1,                                  
					distance[i][j - 1] + 1,                                  
					distance[i - 1][j - 1] + (lhs[i - 1].equalsIgnoreCase(rhs[j - 1]) ? 0 : 1));
		return distance[lhs.length][rhs.length];
	}
	public static double levenshteinSentenceSimilarity(String[] lhs, String[] rhs) {
		if(rhs.length==0 || lhs.length==0)
			return 0;
		return 1-(double)computeLevenshteinSentenceDistance(lhs, rhs)/Math.max(lhs.length,rhs.length);
	}
}
