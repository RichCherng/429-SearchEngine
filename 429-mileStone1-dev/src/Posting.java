import java.util.ArrayList;


public class Posting {
	ArrayList<PositionMap> mPositionMapArr;

	public Posting(){
		mPositionMapArr = new ArrayList<PositionMap>();
	}

	public void add(int docID, int position){

		// Document should be sorted by id already
		// Check only the last document for existing

		PositionMap map = null;
		// If the array of postingMap (term -> [pos1,pos2]) is not empty
		if (mPositionMapArr.size() != 0){
			/* Not empty */
			// Get the last positionMap in the array (either the current docID, or the previous one will return)
			map = mPositionMapArr.get(mPositionMapArr.size() - 1);
		}

		// If the last positionMap is not the same as current docID, create a new positionMap
		if (map == null || map.mDocID != docID){
			/* mPositions is empty or map for this document doesn't exist */
			// Create a new Map

			PositionMap newMap = new PositionMap(docID);
			// Add position to the list of position
			newMap.add(position);
			// Add it to the list of positionMap
			mPositionMapArr.add(newMap);
		} else {
			/* already exist */
			/*
			 * Negative position mean it's mearged posting, has no position
			 */
			if(position >= 0){
				map.add(position);
			}
		}
	}

	/**
	 * The AND Merge
	 * @param Q
	 * @return - The posting
	 */
	public Posting merge(Posting Q){
		// AND merge
		Posting newPosting = new Posting();
		int index_0 = 0, index_1 = 0;
		while(index_0 < mPositionMapArr.size() && index_1 < Q.mPositionMapArr.size()){

			PositionMap P0 = mPositionMapArr.get(index_0);
			PositionMap P1 = Q.mPositionMapArr.get(index_1);

			if(P0.mDocID == P1.mDocID){
				newPosting.add(P0.mDocID, -1);
				index_0++;
				index_1++;
			} else if (P0.mDocID < P1.mDocID){
				index_0++;
			} else {
				index_1++;
			}
		}
		return newPosting;
	}

	public Posting positionalMerge(Posting Q){
		return null;
	}

	public Posting ORMerge(Posting Q){
		return null;
	}

}
