import java.util.ArrayList;


public class Posting {
	int mDocID;
	ArrayList<Integer> mPositionArr;

	public Posting(int pDocId) {
		//		mPositions = new ArrayList<PositionMap>();
		mDocID = pDocId;
		mPositionArr = new ArrayList<Integer>();
	}

	public void addPosition(int pPosition){
		mPositionArr.add(pPosition);
	}
	
	public int getSizeOfPositionArray() {
		return mPositionArr.size();
	}

	// Print <1, [0,1,2,3]>
	@Override
	public String toString() {
		return ("<" + mDocID + ", " + mPositionArr + ">");
	}

}
