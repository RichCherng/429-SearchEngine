import java.util.ArrayList;

public class Main {

	public static void main(String[] args){


		DocumentReader docReader 			= new DocumentReader();
		DirectoryParser aDirectoryParser 	= new DirectoryParser(docReader);
		PositionalInvertedIndex aPII 		= new PositionalInvertedIndex();
		BiwordIndex aBI 					= new BiwordIndex();
		// Store each articles in the ArrayList of Document.Article
//		docReader.read("all-nps-sites.json");
		aDirectoryParser.parseDirectory();
		// Create Positional Inverted Index from the list of articles
		createIndex(docReader, aPII, aBI);
		QueryParser querie =  new QueryParser(docReader, aPII);
		querie.leafRun();


	}

	public static void createIndex(DocumentReader pDocReader, PositionalInvertedIndex pPII, BiwordIndex pBI){
		int num_doc = pDocReader.size();
		System.out.println("Number of documents: " + num_doc);
		for(int id = 0; id < num_doc; id++){
			Article article = pDocReader.get(id);
			TokenStream aTokenStream = new TokenStream(article.body);
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
//				System.out.println(token);
//				System.out.println(aTokenStream.nextToken());
				// Check if token contain hypen
				if(token.contains("-") && token.length() > 3){ //a-
					/* Deal term with hyphen */
					/**
					 * Three outputs from Hewlett-Packard: HewlettPackard, Hewlett, and Packard
					 */

//					String firstWord = token.split("-")[0];
//					String secondWord = token.split("-")[1];
//					String tokenWithOutHyphen = token.replace("-", "");
//					String[] listOfProcessedTokens = {tokenWithOutHyphen, firstWord, secondWord};
//					for (String eachProcessedToken : listOfProcessedTokens) {
//						pPII.addTerm(PorterStemmer.processToken(eachProcessedToken), id, positionIndex);
//					}

				} else {
					stem = PorterStemmer.processToken(token);
					pPII.addTerm(stem, id, positionIndex);
					if(prevTerm != null){
						pBI.addTerm( prevTerm,stem, id);
					}

				}
				prevTerm = stem;
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
