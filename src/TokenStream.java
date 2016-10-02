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
//	   String next = mReader.next().replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase();
	   String pString = process(mReader.next());
//	   if(next.equals("__")){
//		   System.out.println("yes");
//		   System.out.println(next);
//		   System.out.println("RICH_PONGSA-THO&RN".replaceAll("[^a-zA-Z0-9-]+" , ""));
//	   }

	   return pString.length() > 0 ? pString : (hasNextToken()? nextToken() : null);

   }

   public static String process(String words){
	   return words.replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase();
   }
}
