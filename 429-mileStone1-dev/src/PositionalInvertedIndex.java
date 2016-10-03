import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PositionalInvertedIndex {

	/**
	 * Map the term to the Posting
	 */
	private HashMap<String, Posting> mIndex;

	public PositionalInvertedIndex(){
		mIndex = new HashMap<String, Posting>();
	}

	public void addTerm(String token, int documentID, int position){

		Posting posting = null;

		// Check if term already exist
		if(mIndex.containsKey(token)){
			/* Retreive Posting */
			posting = mIndex.get(token);

		} else {
			/* Create Posting */
			Posting newPosting 	= new Posting();
			mIndex.put(token, newPosting);
			posting = newPosting;
		}
		// Add position to the arraylist of position in each docID
		posting.add(documentID, position);
	}

	public Posting getPosting(String token){
		String stem = PorterStemmer.processToken(token);
		return mIndex.get(stem);
	}

	public ArrayList<String> getVocab(){
		ArrayList<String> vocab = new ArrayList<String>();
		for(String key: mIndex.keySet()){
			vocab.add(key);
		}
		Collections.sort(vocab);
		return vocab;

	}

	public void PrintPosting(String token){
		Posting p = mIndex.get(token);
		System.out.print(token + ": ");
		for(PositionMap m: p.mPositionMapArr){
			System.out.print("< " + m.mDocID + ",[ ");
			for(int i: m.mPosition){
				System.out.print(i+", ");
			}
			System.out.print("]>, ");
		}
		System.out.println();
	}


}
