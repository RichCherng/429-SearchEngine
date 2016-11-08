import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SpellingCorrection {

	private HashMap<String, HashSet<String>> oneGram;
	private HashMap<String, HashSet<String>> twoGram;
	private HashMap<String, HashSet<String>> threeGram;

	public SpellingCorrection(KGramIndex pKGram){
		oneGram 	= pKGram.getNGram(1);
		twoGram 	= pKGram.getNGram(2);
		threeGram 	= pKGram.getNGram(3);
	}

	/**
	 * Retrieve all the possible words that in common with pWord in k-gram.
 	 * Calculate Jaccard Coefficient for each possible words, select those that
 	 * have JC over (0.1) and calculate edit-distance. Select words with the lowest edit-distance
	 * @param pWord correcting word
	 * @return List of all possible words as ArrayList<String>
	 */
	public String[] correct(String pWord){
		// All vocab types that have k-gram in common with the misspelled term.
		HashSet<String> commons = getCommonList(pWord);

		// Calculate the Jaccard coefficient for each type in the selection
		HashMap<String, HashSet<String>> wKGram = getKGram(pWord);

		ArrayList<String> correctionList = new ArrayList<String>();
		int minED = 0;
		for(String c : commons){

			// # of time c appear in word's kgram
			double hit = 0;
			for(Map.Entry<String, HashSet<String>> entry: wKGram.entrySet()){
				if(entry.getValue().contains(c)){
					hit++;
				}
			}
			HashMap<String, HashSet<String>> commonKGram = getKGram(c);
			if(hit > 0){
				// Calculate Jaccard Coefficient
				double jc = hit/((double)commonKGram.size() + (double)wKGram.size() - (double)hit);

				// select those that are about jc threshold
				if( jc >= 0.1){
					System.out.print(c+" : ");
					System.out.println(jc);
					int ED = calcEditDistance(pWord,c);

					if(correctionList.size() == 0){
						correctionList.add(c);
						minED = ED;
					} else {
						if(minED > ED){
							minED = ED;
							correctionList.clear();
							correctionList.add(c);
						} else if(minED == ED){
							correctionList.add(c);
						}
					}
					System.out.println(ED);
				}
			}
		}
		return (String[]) correctionList.toArray();
	}

	private int calcEditDistance(String source, String target){
		int s_length = source.length();
		int t_length = target.length();

		int[] dp0 = new int[t_length + 1];
		int[] dp1 = new int[t_length + 1];

		for(int i = 0; i <= t_length; i++){
			dp0[i] = i;
			dp1[i] = i;
		}

		// iterate through source and target

		int[] top = dp0, cur = dp1;
		for(int i = 0; i < s_length; i++){
			char s = source.charAt(i);
			cur[0] = i + 1;
			for(int j = 0; j < t_length; j++){
				char t = target.charAt(j);

				if(s == t){
					// No Change require
					cur[j+1] = top[j];
				}else {
					int replace = top[j] + 1;
					int insert = top[j + 1] + 1;
					int delete = cur[j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					cur[j+1] = min;
				}
			}
			int[] temp = top;
			top = cur;
			cur = temp;
		}
		return top[t_length];
	}

	private HashMap<String, HashSet<String>> getKGram(String word){
		// 3-gram
		HashMap<String, HashSet<String>> kgram = new HashMap<String, HashSet<String>>();
		char[] cArr = word.toCharArray();
		for(int i = 0; i < cArr.length; i++){
			String key = null;
			if( i == 0){
				/** Starting **/

				// $ + follow by next 2 char
				if( i + 1 < cArr.length){
					key = "$" + cArr[i] + cArr[i+1];
					HashSet<String> temp = threeGram.get(key);
					if(temp != null){
						kgram.put(key, temp);
					} else {
						kgram.put(key, new HashSet<String>());
					}
				}

				// first 3 chars
				if( i + 2 < cArr.length){
					key = "" + cArr[i] + cArr[i+1] + cArr[i+2];
				}

			} else if(i + 1 == cArr.length - 1){
				/** Ending **/

				key = "" + cArr[i] + cArr[i+1] + "$";
			} else {
				/** Between **/
				if(i + 2 < cArr.length){
					key = "" + cArr[i] + cArr[i+1] + cArr[i+2];
				}
			}
			if(key == null){
				break;
			}
			HashSet<String> temp = threeGram.get(key);
			if(temp != null) {
				kgram.put(key, temp);
			}
			else {
				kgram.put(key, new HashSet<String>());
			}
		}
		return kgram;
	}

	private HashSet<String> getCommonList(String word){
		HashSet<String> common = new HashSet<String>();
		char[] cArr = word.toCharArray();
		for(int i = 0 ; i < cArr.length; i++){

			String twoKey 	= null;
			String threeKey = null;

			// Two - Gram
			if(i == 0){
				/** Starting **/

				twoKey = "$" + cArr[i];
				combineSet(common, twoGram.get(twoKey));
				twoKey = null;
				if(i + 1 < cArr.length) { twoKey = "" + cArr[i] + cArr[i+1]; }// type[0:2]
			} else if(i == cArr.length - 1){
				/** Ending for 2-gram**/
				twoKey = "" + cArr[i]+"$";
			} else {
				/** Between **/
				if(i + 1 < cArr.length){
					// Add to Two-gram
					twoKey = "" + cArr[i] + cArr[i+1];
				}
			}

			// Three - Gram
			if(i == 0){
				/** Starting **/

				if( i + 1 < cArr.length){
					threeKey = "$"+cArr[i] + cArr[i+1];
					combineSet(common, threeGram.get(threeKey));
					threeKey = null;
					if(i + 2 < cArr.length) { threeKey = "" + cArr[i] + cArr[i+1] + cArr[i+2]; } // type[0:3]
				}
			} else if( i + 1 == cArr.length - 1){
				/** Ending for 3-gram **/
				threeKey = "" + cArr[i] + cArr[i+1] + "$";
			}else {
				/** Between **/
				if(i + 2 < cArr.length){
					// Add to Three-gram
					threeKey = "" + cArr[i] + cArr[i+1] + cArr[i+2];
				}
			}

			if(twoKey != null){
				combineSet(common, twoGram.get(twoKey));
			}

			if(threeKey != null){
				combineSet(common, threeGram.get(threeKey));
			}

		}
		return common;
	}

	private void combineSet(HashSet<String> set, HashSet<String> add){
		HashSet<String> temp = add;
		if(temp != null){
			set.addAll(temp);
		}
	}

}
