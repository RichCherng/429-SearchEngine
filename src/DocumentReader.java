import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentReader {

	private JSONSIFY 		mJSONParser;
	PositionalInvertedIndex aPII;
	BiwordIndex 			aBI;
	KGramIndex				aKGI;
	ArrayList<Article> 		mArticles;
	private final int 		MIN_LENGTH = 3;
	ArrayList<Double> 		mListOfLD;
	/** For the weight of document **/
//	LinkedHashMap<Integer, HashMap<String,Integer>> docIdToTermToTermFreq; // docID --> (term --> termFreq)

	public DocumentReader(PositionalInvertedIndex pPII, BiwordIndex pBI, KGramIndex pKGI){
		mArticles 				= new ArrayList<Article>();
		aPII 					= pPII;
		aBI 					= pBI;
		aKGI 					= pKGI;
		mListOfLD				= new ArrayList<Double>();
//		docIdToTermToTermFreq 	= new LinkedHashMap<Integer, HashMap<String,Integer>>(); // docID --> (term --> termFreq)
	}

	public void read(String path){
		mJSONParser 		= new JSONSIFY(path);
		Article aArticle 	= mJSONParser.read();

		HashMap<String, Integer> termFreqs = new HashMap<String, Integer>();
		mArticles.add(aArticle);
		index(aArticle, mArticles.indexOf(aArticle), termFreqs);

		/** Calculate LD **/
		double sum = 0;
		for(Map.Entry<String, Integer> entry: termFreqs.entrySet()){
			double termWeight =  1 + Math.log((double)entry.getValue());
			sum += Math.pow(termWeight, 2);
		}
		mListOfLD.add(Math.sqrt(sum));
	}

	public void index(Article pArticle, int docID, HashMap<String, Integer> pTF){
		TokenStream aTokenStream 	= new TokenStream(pArticle.body);
		int positionIndex 			= 0;
		String prevTerm 			= null; // use for biword indexing to keep track of the previous word
		while(aTokenStream.hasNextToken()){
			// Return a processed token (all alpha-numeric characters are removed) (the '-' does not get removed)
			//  The apostropes (single quotes) are removed also.
			//  The token returned are already loweredCase
			String token 	= aTokenStream.nextToken();
			String stem 	= null;

			if(token == null){
				continue;
			}

			aKGI.addType(token);

			// Check if token contain hypen
			if(token.contains("-") && token.length() > MIN_LENGTH){


				Pattern p = Pattern.compile("^[a-zA-Z0-9]+(-[a-zA-Z0-9]+)?$"); // Pattern to look for character or number before and after hyphen
				Matcher m = p.matcher(token);
				if (m.find()){

					/* Deal term with hyphen */

					// hree outputs from Hewlett-Packard: HewlettPackard, Hewlett, and Packard

					String firstWord 				= token.split("-")[0];
					String secondWord	 			= token.split("-")[1];
					String tokenWithOutHyphen 		= token.replace("-", "");
					String[] listOfProcessedTokens 	= {tokenWithOutHyphen, firstWord, secondWord};

					for (String eachProcessedToken : listOfProcessedTokens) {
						String pStem = PorterStemmer.processToken(eachProcessedToken);
						aPII.addTerm(pStem, docID, positionIndex);
//						addTermFreq(docID, PorterStemmer.processToken(eachProcessedToken));
						addTermFreq(pTF, pStem);
					}
					String stemFirst 	= PorterStemmer.processToken(firstWord.replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
					String stemSecond 	= PorterStemmer.processToken(secondWord.replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
					aBI.addTerm(stemFirst, stemSecond, docID); // Added Biword for the hyphened word
					aBI.addTerm(prevTerm,  stemFirst,  docID); // Added Biword for previous word and first word
					stem = stemSecond; // This will eventually make second word a prev for the next word to do Biword indexing

				}
				else {
					stem = PorterStemmer.processToken(token.replaceAll("-", ""));
					aPII.addTerm(stem, docID, positionIndex);
//					addTermFreq(docID, stem);
					addTermFreq(pTF, stem);
					if(prevTerm != null){
						aBI.addTerm( prevTerm,stem, docID);
					}
				}


			} else {
				stem = PorterStemmer.processToken(token);
				aPII.addTerm(stem, docID, positionIndex);
//				addTermFreq(docID, stem);
				addTermFreq(pTF, stem);
				if(prevTerm != null){
					aBI.addTerm( prevTerm,stem, docID);
				}

			}
			prevTerm = stem;
			positionIndex++;
		}
	}

	private void addTermFreq(HashMap<String, Integer> tf,String term){
		if(tf.containsKey(term)){
			tf.put(term,  tf.get(term)+1);
		} else {
			tf.put(term, 1);
		}
	}

	public Article get(int index){
		return mArticles.get(index);
	}

	public int size(){
		return mArticles.size();
	}

	public ArrayList<Double> getListOfLD(){
		return mListOfLD;
	}
//	/** Weight Ranking Methods (Ld) starts **/
//
//	/**
//	 * Get array of Ld all of documents
//	 * @return
//	 */
//	public double[] getListOfLd() {
//		double[] listOfLd 	= new double[docIdToTermToTermFreq.size()];
//		int index 			= 0;
//
//		for (Map.Entry<Integer, HashMap<String, Integer>> eachEntry : docIdToTermToTermFreq.entrySet()) {
//			listOfLd[index] = getWeightOfDocument(eachEntry.getKey());
//			System.out.printf("Index: %d, Ld: %f\n", index, getWeightOfDocument(eachEntry.getKey()));
//			index++;
//		}
//		return listOfLd;
//	}

//	/**
//	 * Get the Ld of each document (Ld)
//	 * @param pDocID
//	 * @return The Ld of the pDocID
//	 */
//	private double getWeightOfDocument(int pDocID) {
//
//		double sumOfWeightOfAllTerm 		= 0;
//		HashMap<String, Integer> termFreqHM = docIdToTermToTermFreq.get(pDocID); 			// Get all the HM of term --> termFreq
//
//		for (Map.Entry<String, Integer> eachEntry : termFreqHM.entrySet()) { 				// For each term in the document
//
//			int termFreqOfTermInDoc 		= eachEntry.getValue(); 						// Get the termFreq tf(t,d)
////			System.out.printf("tf(%s,%d): %d\n", eachEntry.getKey(), pDocID, termFreqOfTermInDoc);
//			double weightOfDocOfTerm 		= 1.0 + Math.log(termFreqOfTermInDoc); 			// Get the W(d,t) = 1 + ln( tf(t,d) )
////			System.out.printf("W(%d, %s): %f\n", pDocID, eachEntry.getKey(), weightOfDocOfTerm);
//			double weightOfDocOfTermSquare 	= Math.pow(weightOfDocOfTerm, 2.0);				// Get the W(d,t)^2
////			System.out.printf("W(%d, %s)^2: %f\n", pDocID, eachEntry.getKey(), weightOfDocOfTermSquare);
//			sumOfWeightOfAllTerm 		   += weightOfDocOfTermSquare;
////			System.out.printf("Sum of all weight: %f\n", sumOfWeightOfAllTerm);
//		}
//
//		double Ld = Math.sqrt(sumOfWeightOfAllTerm);
////		System.out.printf("L(%d): %f\n", pDocID, Ld);
//		return Ld;
//	}

//	public int getTermFreqOfDoc(String pTerm, int pDocID) {
//		HashMap<String, Integer> termFreqHM = docIdToTermToTermFreq.get(pDocID);
//		System.out.printf("term: %s, termFreq: %d", pTerm, termFreqHM.get(pTerm));
//		return termFreqHM.get(pTerm);
//	}

//	/**
//	 * Add Term Frequency in the HashMap to the given pDocID
//	 * @param pDocID
//	 * @param pTerm
//	 */
//	private void addTermFreq(int pDocID, String pTerm) {
//		if (docIdToTermToTermFreq.containsKey(pDocID)) { // If the HM contains the docID already
//			HashMap<String, Integer> termToFreq = docIdToTermToTermFreq.get(pDocID); // Get the hashmap of term --> termFreq
//			if (termToFreq.containsKey(pTerm)) {
//				termToFreq.put(pTerm, termToFreq.get(pTerm) + 1); // Increment the termFreq of the term by 1
//			}
//			else {
//				termToFreq.put(pTerm, 1); // Set the termFreq of the term by 1
//			}
//		}
//		else {  // If HM does not contains docID already
//			HashMap<String, Integer> termToFreq = new HashMap<String, Integer>(); // Construct a new HM and put the term --> 1
//			termToFreq.put(pTerm, 1);
//			docIdToTermToTermFreq.put(pDocID, termToFreq);
//		}
//	}

	/** Weight Ranking Method (Ld) ends **/


	/**
	 * Reset all the document stored in the class
	 */
	public void reset(){
		mArticles.clear();
	}

}
