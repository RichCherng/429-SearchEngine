import java.util.Scanner;

public class TokenStream {

	private Scanner mReader;

	public TokenStream(String text){
		mReader = new Scanner(text);
	}

	/**
	 *
	 * @return true if the stream has tokens remaining.
	 */
	public boolean hasNextToken() {
		return mReader.hasNext();
	}

	/**
	 * @return - a process token (removing all non-alphanumberic characters from the beginning to the end
	 */
	public String nextToken() {
		if (!hasNextToken()){
			return null;
		}
		String pString = process(mReader.next());
		return pString.length() > 0 ? pString : (hasNextToken()? nextToken() : null);
	}

	public static String process(String pWords){
		return pWords.replaceAll("[^a-zA-Z0-9]+" , "").toLowerCase();
	}
}
