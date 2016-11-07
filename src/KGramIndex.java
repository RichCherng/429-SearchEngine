import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("serial")
public class KGramIndex implements Serializable {

	private HashMap<String, HashSet<String>> oneGram;
	private HashMap<String, HashSet<String>> twoGram;
	private HashMap<String, HashSet<String>> threeGram;

	public KGramIndex(){
		oneGram		= new HashMap<String, HashSet<String>>();
		twoGram 	= new HashMap<String, HashSet<String>>();
		threeGram 	= new HashMap<String, HashSet<String>>();
	}

	public void addType(String type){
		char[] cArr = type.toCharArray();
		for(int i = 0 ; i < cArr.length; i++){
			addToHashMap(oneGram, cArr[i]+"", type); // Add to one-gram
			if(i == 0){
				/** Starting **/

				// Add to Two-gram
				addToHashMap(twoGram, "$"+cArr[i], type);
				// Add to Three-gram
				if( i + 1 < cArr.length){
					addToHashMap(threeGram, "$"+cArr[i] + cArr[i+1], type);
				}

			} else if( i == cArr.length - 1){
				/** Ending for 2-gram **/
				// Add to Two-Gram
				addToHashMap(twoGram, "" + cArr[i]+"$", type);
			} else if( i + 1 == cArr.length - 1){
				/** Ending for 3-gram **/
				// Add to Three-Gram
				addToHashMap(threeGram, "" + cArr[i] + cArr[i+1] + "$", type);
			}else {
				/** Between **/
				if( i + 2 < cArr.length){
					// Add to Three-gram
					addToHashMap(threeGram, "" + cArr[i] + cArr[i+1] + cArr[i+2], type);
				}
				if(i + 1 < cArr.length){
					// Add to Two-gram
					addToHashMap(twoGram, "" + cArr[i] + cArr[i+1], type);
				}
			}
		}
	}

	public HashSet<String> getPossibleList(String pWord){
		HashSet<String> posList = new HashSet<String>();

		char[] cArr = pWord.toCharArray();
		for(int i = 0; i < cArr.length; i++){

			// Retrieve from one-gram
			posList.addAll(oneGram.get(cArr[i]+""));

			if(i == 0){
				/** Starting **/

				// Retrieve from Two-Gram
				posList.addAll(twoGram.get("$"+cArr[i]));

				// Retrieve from Three-Gram
				if( i + 1 < cArr.length){
					posList.addAll(threeGram.get("$"+cArr[i]+cArr[i+1]));
				}

			} else if (i == cArr.length - 1){
				/** Ending **/

			} else {
				/** Between **/

			}
		}


		return null;
	}


	private void addToHashMap(HashMap<String, HashSet<String>> hm, String gram, String type){
		if(hm.containsKey(gram)){
			// Contain in the map
			HashSet<String> words = hm.get(gram);
			if(!words.contains(type)){
				// Not already exists
				words.add(type);
			}
		} else {
			// Not alredy in the map
			// Create new list
			HashSet<String> words = new HashSet<String>();
			words.add(type);
			hm.put(gram, words);
		}
	}

	public void print(){
		for (Map.Entry<String, HashSet<String>> entry : oneGram.entrySet()) {
			System.out.println(entry.getKey());
			for(String s: entry.getValue()){
				System.out.println("\t" + s);
			}
		}
		for (Map.Entry<String, HashSet<String>> entry : twoGram.entrySet()) {
			System.out.println(entry.getKey());
			for(String s: entry.getValue()){
				System.out.println("\t" + s);
			}
		}
		for (Map.Entry<String, HashSet<String>> entry : threeGram.entrySet()) {
			System.out.println(entry.getKey());
			for(String s: entry.getValue()){
				System.out.println("\t" + s);
			}
		}
	}
}
