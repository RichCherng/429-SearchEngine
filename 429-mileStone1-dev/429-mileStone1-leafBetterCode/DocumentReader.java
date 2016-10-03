import java.util.ArrayList;

public class DocumentReader {
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
