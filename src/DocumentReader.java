import java.util.ArrayList;

public class DocumentReader {

	private JSONSIFY mJSONParser;
	PositionalInvertedIndex aPII;
	BiwordIndex aBI;
	ArrayList<Article> mArticles;

	public DocumentReader(PositionalInvertedIndex pPII, BiwordIndex pBI){
		mArticles = new ArrayList<Article>();
		aPII = pPII;
		aBI = pBI;
	}

	public void read(String path){
		mJSONParser = new JSONSIFY(path);
		Article aArticle = mJSONParser.read();
		mArticles.add(aArticle);
		index(aArticle, mArticles.indexOf(aArticle));
//		mArticles = doc.getDocument();
	}

	public void index(Article pArticle, int docID){
		TokenStream aTokenStream = new TokenStream(pArticle.body);
		int positionIndex = 0;
		String prevTerm = null; // use for biword indexing to keep track of the previous word
		while(aTokenStream.hasNextToken()){
			// Return a processed token (all alpha-numeric characters are removed) (the '-' does not get removed)
			//  The apostropes (single quotes) are removed also.
			//  The token returned are already loweredCase
			String token = aTokenStream.nextToken();
			String stem = null;

			if(token == null){
				continue;
			}
//			System.out.println(token);
//			System.out.println(aTokenStream.nextToken());
			// Check if token contain hypen
			if(token.contains("-") && token.length() > 3){ //a-
				/* Deal term with hyphen */
				/**
				 * Three outputs from Hewlett-Packard: HewlettPackard, Hewlett, and Packard
				 */

//				String firstWord = token.split("-")[0];
//				String secondWord = token.split("-")[1];
//				String tokenWithOutHyphen = token.replace("-", "");
//				String[] listOfProcessedTokens = {tokenWithOutHyphen, firstWord, secondWord};
//				for (String eachProcessedToken : listOfProcessedTokens) {
//					pPII.addTerm(PorterStemmer.processToken(eachProcessedToken), id, positionIndex);
//				}

			} else {
				stem = PorterStemmer.processToken(token);
				aPII.addTerm(stem, docID, positionIndex);
				if(prevTerm != null){
					aBI.addTerm( prevTerm,stem, docID);
				}

			}
			prevTerm = stem;
			positionIndex++;
		}
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
