import java.util.ArrayList;
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
			Posting newPosting = new Posting();
			mIndex.put(token, newPosting);
			posting = newPosting;
		}
		posting.add(documentID, position);
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

//			if (mPositions.size() == 0){
//				/* Empty */
//
//				// Create a map
//				PositionMap newMap = new PositionMap(docID);
//				newMap.add(position);
//				mPositions.add(newMap);
//			} else {
//
//				// Check if alraedy exist
//				PositionMap pos = mPositions.get(mPositions.size() - 1);
//				if (pos.mDocID == docID){
//					/* Already Exist */
//					// just add
//				} else {
//					// Create a new Map
//
//					PositionMap newMap = new PositionMap(docID);
//					newMap.add(position);
//					mPositions.add(newMap);
//				}
//			}
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
