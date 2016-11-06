import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KGramIndex {

	private HashMap<String, ArrayList<String>> oneGram;
	private HashMap<String, ArrayList<String>> twoGram;
	private HashMap<String, ArrayList<String>> threeGram;

	public KGramIndex(){
		oneGram		= new HashMap<String, ArrayList<String>>();
		twoGram 	= new HashMap<String, ArrayList<String>>();
		threeGram 	= new HashMap<String, ArrayList<String>>();
	}

	public void addType(String type){
		char[] cArr = type.toCharArray();
		for(int i = 0 ; i < cArr.length; i++){
			addToHashMap(oneGram, cArr[i]+"", type);
			if(i == 0){
				/** Starting **/

				// Add to Two-gram
				addToHashMap(twoGram, "$"+cArr[i], type);
				// Add to Three-gram
				if( i + 1 < cArr.length){
					addToHashMap(threeGram, "$"+cArr[i] + cArr[i+1], type);
				}

			} else if( i == cArr.length - 1){
				/** Ending **/
				// Add to Two-Gram
				addToHashMap(twoGram, cArr[i]+"$", type);
			} else {
				// Between
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


	public void test(){
		for(Map.Entry<String, ArrayList<String>> entry: twoGram.entrySet()){
			System.out.println(entry.getKey());
			for(String s : entry.getValue()){
				System.out.println("\t" + s);
			}
		}
		for(Map.Entry<String, ArrayList<String>> entry: threeGram.entrySet()){
			System.out.println(entry.getKey());
			for(String s : entry.getValue()){
				System.out.println("\t" + s);
			}
		}
	}
	private void addToHashMap(HashMap<String, ArrayList<String>> hm, String gram, String type){
		if(hm.containsKey(gram)){
			// Contain in the map
			ArrayList<String> words = hm.get(gram);
			if(!words.contains(type)){
				// Not already exists
				words.add(type);
			}
		} else {
			// Not alredy in the map
			// Create new list
			ArrayList<String> words = new ArrayList<String>();
			words.add(type);
			hm.put(gram, words);
		}
	}
}
