import java.util.ArrayList;
import java.util.HashMap;

public class BiwordIndex {

	HashMap<String, ArrayList<Integer>> mTermToPostingListHM;

	public BiwordIndex(){
		mTermToPostingListHM = new HashMap<String, ArrayList<Integer>>();
	}

	/**
	 * Index Biword with document id
	 * @param pFirstToken - first token
	 * @param pSecondToken - second token
	 * @param pDocumentID - Document ID
	 */
	public void addTerm(String pFirstToken, String pSecondToken, int pDocumentID){
		String key = pFirstToken + "-" + pSecondToken;

		if(mTermToPostingListHM.containsKey(key)){
			/*Check if the key already exists*/
			ArrayList<Integer> posting = mTermToPostingListHM.get(key);


			// If the document ID already exists in this posting, skip
			// doesn't have to add same document ID twice if the biword occurs more than once in the document
			if(posting.get(posting.size() - 1) != pDocumentID){
				posting.add(pDocumentID);
			}

		} else {
			/* Create posting*/

			ArrayList<Integer> posting = new ArrayList<Integer>();
			posting.add(pDocumentID);
			mTermToPostingListHM.put(key, posting);
		}
	}


	/**
	 * Return t he posting of the Biword
	 * @param pFirstToken - first token
	 * @param pSecondToken - second token in the phase query
	 * @return List of document ID
	 */
	public ArrayList<Integer> getPosting(String pFirstToken, String pSecondToken){
		String key = pFirstToken + "-" + pSecondToken;
		return mTermToPostingListHM.get(key);
	}
}
