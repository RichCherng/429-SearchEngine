import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PositionalInvertedIndex {

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
			posting 			= newPosting;
		}
		posting.add(documentID, position);
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
		for(PositionMap m: p.mPositions){
			System.out.print("< " + m.mDocID + ",[ ");
			for(int i: m.mPosition){
				System.out.print(i+", ");
			}
			System.out.print("]>, ");
		}
		System.out.println();
	}

	class Posting{

		ArrayList<PositionMap> mPositions;

		public Posting(){
			mPositions = new ArrayList<PositionMap>();
		}

		public void add(int docID, int position){

			// Document should be sorted by id already
			// Check only the last document for existing

			PositionMap map = null;
			if (mPositions.size() != 0){
				/* Not empty */
				map = mPositions.get(mPositions.size() - 1);
			}

			if (map == null || map.mDocID != docID){
				/* mPositions is empty or map for this document doesn't exist */
				// Create a new Map

				PositionMap newMap = new PositionMap(docID);
				newMap.add(position);
				mPositions.add(newMap);
			} else {
				/* already exist */
				map.add(position);
			}
		}
	}

	class PositionMap{
		int mDocID;
		ArrayList<Integer> mPosition;

		public PositionMap(int id){
			mDocID 		= id;
			mPosition 	= new ArrayList<Integer>();
		}

		public void add(int position){
			mPosition.add(position);
		}


	}
}
