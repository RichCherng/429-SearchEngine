import java.util.ArrayList;

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
