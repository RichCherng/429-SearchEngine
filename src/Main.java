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

		DocumentReader docReader = new DocumentReader();
		PorterStemmer stemmer = new PorterStemmer();
		PositionalInvertedIndex PII = new PositionalInvertedIndex();

		docReader.read("all-nps-sites.json");
		int num_doc = docReader.size();
		for(int id = 0; id < num_doc; id++){

			Document.Article article = docReader.get(id);
			TokenStream scan = new TokenStream(article.body);
			int positionIndex = 0;
			while(scan.hasNextToken()){
				String token = scan.nextToken();

				// Check if token contain hypen
				if(token.contains("-")){

				} else {


				}

				System.out.print(token + ":");

				positionIndex++;
			}
			break;
		}
	}
}
