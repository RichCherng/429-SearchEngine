import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PositionalInvertedIndex {

	// Map term to list of Posting:   New --> [<1,[0.7]>, <2,[3], <3,[6]>]
	public HashMap<String, ArrayList<Posting>> mTermToPostingListHM;

	public PositionalInvertedIndex(){
		mTermToPostingListHM = new HashMap<String, ArrayList<Posting>>();
	}

	/**
	 * Add term to the Positional Inverted Index
	 * @param pToken - A processed token
	 * @param pDocumentId - current document ID
	 * @param pPosition - The position in that docID that the token occurs
	 */
	public void addTerm(String pToken, int pDocumentId, int pPosition){
		// If contains the term
		if (mTermToPostingListHM.containsKey(pToken)) {
			// Get the List of the Posting of the current token
			ArrayList<Posting> postingArr = mTermToPostingListHM.get(pToken);
			// Get the last Posting, could be the same docID, or it could not
			Posting currentPosting = postingArr.get(postingArr.size() - 1);
			// If it's the same id then just add a new position
			if (currentPosting.mDocID == pDocumentId) {
				currentPosting.addPosition(pPosition);
			}
			// If it's not the same docID, create a new Posting object, add position
			else {
				currentPosting = new Posting(pDocumentId);
				currentPosting.addPosition(pPosition);
				postingArr.add(currentPosting);
			}
		}
		// If the HM does not contain the term
		else {
			// Create an empty ArrayList of posting, create new posting object and add it to the arrayList
			ArrayList<Posting> postingArr = new ArrayList<Posting>();
			Posting newPosting = new Posting(pDocumentId);
			newPosting.addPosition(pPosition);
			postingArr.add(newPosting);
			mTermToPostingListHM.put(pToken, postingArr);
		}
	}

	/**
	 *
	 * @param token - The term
	 * @return - List of Posting
	 */
	public Posting[] getListOfPosting(String token){
		String stem = PorterStemmer.processToken(token);
		Posting[] listOfPostingObj = new Posting[mTermToPostingListHM.get(stem).size()];
		return mTermToPostingListHM.get(stem).toArray(listOfPostingObj);
	}

	public boolean hasTerm(String pTerm) {
		return mTermToPostingListHM.containsKey(pTerm);
	}

	/**
	 *
	 * @return - List of vocab in the PII
	 */
	public String[] getSortedListOfVocab(){
		ArrayList<String> vocabArrList = new ArrayList<String>();
		for(String key: mTermToPostingListHM.keySet()){
			vocabArrList.add(key);
		}
		Collections.sort(vocabArrList);
		String[] vocab = new String[vocabArrList.size()];
		return vocabArrList.toArray(vocab);

	}
}
