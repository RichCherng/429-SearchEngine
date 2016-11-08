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
			String twoKey 	= null;
			String threeKey = null;

			// Two - Gram
			if(i == 0){
				/** Starting **/

				twoKey = "$" + cArr[i];
				addToHashMap(twoGram, twoKey, type);
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
					addToHashMap(threeGram, threeKey, type);
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
				addToHashMap(twoGram, twoKey, type);
			}

			if(threeKey != null){
				addToHashMap(threeGram, threeKey, type);
			}
		}

	}

	public HashMap<String, HashSet<String>> getNGram(int k){
		switch(k){
			case 1:
				return oneGram;
			case 2:
				return twoGram;
			case 3:
				return threeGram;
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
