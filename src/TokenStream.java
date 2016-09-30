import java.util.Scanner;

public class TokenStream {

	private Scanner mReader;

	public TokenStream(String text){
		mReader = new Scanner(text);
	}

	/**
   Returns true if the stream has tokens remaining.
   */

   public boolean hasNextToken() {
      return mReader.hasNext();
   }

   public String nextToken() {

	   if (!hasNextToken()){
		   return null;
	   }

	   String next = mReader.next().replaceAll("[^\\w-]+" , "").toLowerCase();

	   return next.length() > 0 ? next : (hasNextToken()? nextToken() : null);

   }
}
