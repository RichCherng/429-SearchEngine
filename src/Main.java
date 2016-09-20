import java.util.ArrayList;

public class Main {


	public static void main(String[] args){

		JSONSIFY parseJSON = new JSONSIFY("all-nps-sites.json");
		Document doc = parseJSON.read();

		ArrayList<Document.Article> articles = doc.getDocument();

		for(Document.Article a: articles){
			System.out.println(a.title);
		}
	}
}
