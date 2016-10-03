public class Main {


	public static void main(String[] args){

		//		JSONSIFY parseJSON 	= new JSONSIFY("all-nps-sites.json");
		//		Document doc 		= parseJSON.read();
		//
		//		ArrayList<Document.Article> document = new ArrayList<Document.Article>();
		//
		//		ArrayList<Document.Article> articles = doc.getDocument();



		//		/** For purpose of developing **/
		//		for(Document.Article a: articles){
		//			String title 	= a.title;
		//			String url 		= a.url;
		//			String body 	= a.body;
		//
		//			TokenStream scan = new TokenStream(a.body);
		//			while(scan.hasNextToken()){
		//				System.out.print(scan.nextToken() + ',');
		//			}
		//
		//
		//
		//			break;
		//		}



		DocumentReader docReader 	= new DocumentReader();
		PositionalInvertedIndex PII = new PositionalInvertedIndex();
		QueryParser querie;
		docReader.read("all-nps-sites.json");
		createIndex(docReader, PII);

		System.out.println("Print posting: ");
		PII.PrintPosting("he");

		querie =  new QueryParser( docReader, PII);


		//		System.out.println(docReader.get(1026).body);
		querie.run();


		//		ArrayList<String> vocab = PII.getVocab();
		//		for(String v : vocab){
		//			PII.PrintPosting(v);


		//		}
	}

	public static void createIndex(DocumentReader docReader, PositionalInvertedIndex PII){
		int num_doc = docReader.size();
		System.out.println("Number of documents: " + num_doc);
		for(int id = 0; id < num_doc; id++){

			Document.Article article = docReader.get(id);
			TokenStream scan = new TokenStream(article.body);
			int positionIndex = 0;
			while(scan.hasNextToken()){
				String token = scan.nextToken();

				if(token == null){
					continue;
				}
				// Check if token contain hypen
				if(token.contains("-")){
					/* Deal term with hyphen */

				} else {
					String stem = PorterStemmer.processToken(token);
					//					System.out.print(token + " : ");
					//					System.out.println(stem);
					PII.addTerm(stem, id, positionIndex);
				}
				//				System.out.print(token + ":");
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
