import java.util.ArrayList;

public class DocumentReader {

	private JSONSIFY mJSONParser;
	ArrayList<Article> mArticles;

	public DocumentReader(){
		mArticles = new ArrayList<Article>();
	}

	public void read(String path){
		mJSONParser = new JSONSIFY(path);
		Article aArticle = mJSONParser.read();
		mArticles.add(aArticle);
//		mArticles = doc.getDocument();
	}

	public Article get(int index){
		return mArticles.get(index);
	}

	public int size(){
		return mArticles.size();
	}


	/**
	 * Reset all the document stored in the class
	 */
	public void reset(){
		mArticles.clear();
	}

}
