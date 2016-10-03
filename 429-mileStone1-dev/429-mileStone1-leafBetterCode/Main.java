public class Main {

	public static void main(String[] args){
		DocumentReader docReader 	= new DocumentReader();
		PositionalInvertedIndex aPII = new PositionalInvertedIndex();
		// Store each articles in the ArrayList of Document.Article
		docReader.read("test.json");
		// Create Positional Inverted Index from the list of articles
		createIndex(docReader, aPII);
		QueryParser querie =  new QueryParser(docReader, aPII);
		//		for (Map.Entry<String, ArrayList<Posting>> eachEntry : aPII.mTermToPostingListHM.entrySet()) {
		//			System.out.print(eachEntry.getKey() + ": ");
		//			System.out.println(eachEntry.getValue());
		//		}
		querie.leafRun();
	}

	public static void createIndex(DocumentReader pDocReader, PositionalInvertedIndex pPII){
		int num_doc = pDocReader.size();
		System.out.println("Number of documents: " + num_doc);
		for(int id = 0; id < num_doc; id++){
			Document.Article article = pDocReader.get(id);
			TokenStream scan = new TokenStream(article.body);
			int positionIndex = 0;
			while(scan.hasNextToken()){
				// Return a processed token (all alpha-numeric characters are removed) (the '-' does not get removed)
				//  The apostropes (single quotes) are removed also.
				//  The token returned are already loweredCase
				String token = scan.nextToken();

				if(token == null){
					continue;
				}
				// Check if token contain hypen
				if(token.contains("-")){
					/* Deal term with hyphen */
					/**
					 * Three outputs from Hewlett-Packard: HewlettPackard, Hewlett, and Packard
					 */
					String tokenWithOutHyphen = token.replace("-", "");
					String firstWord = token.split("-")[0];
					String secondWord = token.split("-")[1];
					String[] listOfProcessedTokens = {tokenWithOutHyphen, firstWord, secondWord};
					for (String eachProcessedToken : listOfProcessedTokens) {
						pPII.addTerm(PorterStemmer.processToken(eachProcessedToken), id, positionIndex);
					}
				} else {
					String stem = PorterStemmer.processToken(token);
					pPII.addTerm(stem, id, positionIndex);
				}
				positionIndex++;
			}
			// Temporary stop indexing
			if(id > 3000){
				break;
			}
		}
		System.out.println("Indexing completed");
	}
}
