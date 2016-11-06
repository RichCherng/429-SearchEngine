
public class Pair<T> {

	private T mFirst;
	private T mSecond;

	public Pair(T f, T s){
		mFirst = f;
		mSecond = s;
	}

	public T getFirst(){
		return mFirst;
	}

	public T getSecond(){
		return mSecond;
	}
}
