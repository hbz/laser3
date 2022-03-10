package org.gokb

import java.text.Normalizer

class GOKbTextUtils {
  
  private static final List<String> STOPWORDS = [
	"and",
	"the",
	"from"
  ];

  static int levenshteinDistance(String str1, String str2) {
    if ( ( str1 != null ) && ( str2 != null ) ) {
      int str1_len = str1.length()
      int str2_len = str2.length()
      int[][] distance = new int[str1_len + 1][str2_len + 1]
      (str1_len + 1).times { distance[it][0] = it }
      (str2_len + 1).times { distance[0][it] = it }
      (1..str1_len).each { i ->
         (1..str2_len).each { j ->
            distance[i][j] = [distance[i-1][j]+1, distance[i][j-1]+1, str1[i-1]==str2[j-1]?distance[i-1][j-1]:distance[i-1][j-1]+1].min()
         }
      }
      return distance[str1_len][str2_len]
    }

    return 0
  }
  
  static String normaliseString(String s) {

	// Ensure s is not null.
	if (!s) s = "";

	// Normalize to the D Form and then remove diacritical marks.
	s = Normalizer.normalize(s, Normalizer.Form.NFD)
	s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+","");

	// lowercase.
	s = s.toLowerCase();

	// Break apart the string.
	String[] components = s.split("\\s");
	Arrays.sort(components);

	// Re-piece the array back into a string.
	String normstring = "";
	components.each { String piece ->
	  if ( !STOPWORDS.contains(piece)) {

		// Remove all unnecessary characters.
		normstring += piece.replaceAll("[^a-z0-9]", " ") + " ";
	  }
	}

	normstring.trim();
  }
  
  static double cosineSimilarity(String s1, String s2, int degree = 2) {
    if ( ( s1 != null ) && ( s2 != null ) ) {
      return cosineSimilarity(s1.toLowerCase()?.toCharArray(), s2.toLowerCase()?.toCharArray(), degree)
    }

    return 0
  }


  static String generateSortKey(value) {
    // Normalise
    // Trim
    // Lowercase
    // Remove Leading Articles
    String s1 = Normalizer.normalize(value, Normalizer.Form.NFKD).trim().toLowerCase()

    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1

  }
  
  static double cosineSimilarity(char[] sequence1, char[] sequence2, int degree = 2) {
	Map<List, Integer> m1 = countNgramFrequency(sequence1, degree)
	Map<List, Integer> m2 = countNgramFrequency(sequence2, degree)
  
	dotProduct(m1, m2) / Math.sqrt(dotProduct(m1, m1) * dotProduct(m2, m2))
  }
  
  private static Map<List, Integer> countNgramFrequency(char[] sequence, int degree) {
	Map<List, Integer> m = [:]
	
	if (sequence) {
	  int count = sequence.size()

	  for (int i = 0; i + degree <= count; i++) {
		List gram = sequence[i..<(i + degree)]
		m[gram] = 1 + m.get(gram, 0)
	  }
	}
  
	m
  }
  
  private static double dotProduct(Map<List, Integer> m1, Map<List, Integer> m2) {
	m1.keySet().collect { key -> m1[key] * m2.get(key, 0) }.sum()
  }

}
