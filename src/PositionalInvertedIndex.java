import java.util.HashMap;

public class PositionalInvertedIndex {

	private HashMap<String, Posting> mIndex;

	public PositionalInvertedIndex(){
		mIndex = new HashMap<String, Posting>();
	}

	public void addTerm(String term, int documentID, int position){

	}

	class Posting{

	}
}
