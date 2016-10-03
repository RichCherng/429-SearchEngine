import java.util.ArrayList;

/**
 * Map docID to the list of position that the term occurs
 * @author LeafChernchaosil
 *
 */
public class PositionMap {
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
