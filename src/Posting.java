import java.util.ArrayList;


public class Posting {
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
			/*
			 * Negative position mean it's mearged posting, has no position
			 */
			if(position >= 0){
				map.add(position);
			}
		}
	}

	public Posting merge(Posting Q){
		// AND merge
		Posting newPosting = new Posting();
		int index_0 = 0, index_1 = 0;
		while(index_0 < mPositions.size() && index_1 < Q.mPositions.size()){

			PositionMap P0 = mPositions.get(index_0);
			PositionMap P1 = Q.mPositions.get(index_1);

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
