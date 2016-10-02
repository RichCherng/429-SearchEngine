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
			map.add(position);
		}
	}
}
