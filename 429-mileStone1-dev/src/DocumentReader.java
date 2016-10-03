import java.util.ArrayList;

public class DocumentReader {

//	JSONSIFY marseJSON 	= new JSONSIFY("all-nps-sites.json");
//	Document doc = parseJSON.read();
//
//	ArrayList<Document.Article> document = new ArrayList<Document.Article>();
//
//	ArrayList<Document.Article> articles = doc.getDocument();
//
//	for(Document.Article a: articles){
//		String title 	= a.title;
//		String url 		= a.url;
//		String body 	= a.body;
//
//		TokenStream scan = new TokenStream(a.body);
//		while(scan.hasNextToken()){
//			System.out.print(scan.nextToken() + ',');
//		}
//
//
//
//		break;
//	}

	JSONSIFY mJSONParser;
	ArrayList<Document.Article> mArticles;

	public DocumentReader(){

	}

	public void read(String path){
		mJSONParser = new JSONSIFY(path);
		Document doc = mJSONParser.read();
		mArticles = doc.getDocument();
	}

	public Document.Article get(int index){
		return mArticles.get(index);
	}

	public int size(){
		return mArticles.size();
	}

}
