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

//	   String next = mReader.next().replaceAll("[^\\w-]+" , "").toLowerCase();
	   String next = mReader.next().replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase();
//	   if(next.equals("__")){
//		   System.out.println("yes");
//		   System.out.println(next);
//		   System.out.println("RICH_PONGSA-THO&RN".replaceAll("[^a-zA-Z0-9-]+" , ""));
//	   }

	   return next.length() > 0 ? next : (hasNextToken()? nextToken() : null);

   }
}
