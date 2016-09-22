import java.util.ArrayList;

public class Main {


	public static void main(String[] args){

		JSONSIFY parseJSON = new JSONSIFY("all-nps-sites.json");
		Document doc = parseJSON.read();

		ArrayList<Document.Article> articles = doc.getDocument();

		/** For purpose of developing **/
		for(Document.Article a: articles){
			String title 	= a.title;
			String url 		= a.url;
			String body 	= a.body;

			TokenStream scan = new TokenStream(a.body);
			while(scan.hasNextToken()){
				System.out.print(scan.nextToken() + ',');
			}



			break;
		}
	}
}
