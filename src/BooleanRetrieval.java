import java.util.ArrayList;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanRetrieval {

	private DiskInvertedIndex 	mDII;
	private BiwordIndex			mBI;

	public BooleanRetrieval(DiskInvertedIndex pDII, BiwordIndex pBI){
		mDII = pDII;
		mBI = pBI;
	}

	public void processQuery(String pQuery){
		// Example of query:  Q1 + Q2 + Q3
		// Q1 can be:   shakes "Jamba Juice"
		// Q1 can be:	shakes + smoothies mango

		// Split all the queries by "+". The array will be [Q1,Q2,Q3,Q4,Qn]
		String[] queriesArr = pQuery.split("\\+");

		// List of Posting for each query
		ArrayList<Posting[]> listOfPostingList 	= new ArrayList<Posting[]>();

		// For each query, store the postingList in the outer ArrayList to do AND/OR merge later
		for (String eachQueryStr : queriesArr) {
			String processedQueryStr 	= eachQueryStr.trim().toLowerCase();

			// Get the final PostingList
			processedQueryStr = processedQueryStr.replaceAll("-", "- "); // normalized for not phrase query
			Posting[] termPostingList 	= returnPostingForQuery(processedQueryStr);

			if (termPostingList != null ) {
				listOfPostingList.add(termPostingList);
			}
		}

		// OR together the Posting for each Q1
		// posting1 OR posting2 OR posting3
//		SortedSet<Integer> docIdSet = orListOfPostingList(listOfPostingList);
//		if (docIdSet.size() > 0){
//			System.out.println("Documents containing the term: ");
//			for(int eachDocId : docIdSet){
//				System.out.println(eachDocId);
//			}
//			System.out.println("Total Results: " + docIdSet.size());
//
//		} else {
//			System.out.println("No document containing the query");
//		}

	}









	/**
	 * Return Posting for the query literal
	 * Each Qi is one of the following: 1. a single token. 2. phrase query (between the "")
	 * @param pQuery - Qi query.
	 * 		  	Example of pQuery: shakes "Jamba Juice"
	 * 			Example of pQuery: shakes
	 * 			Example of pQuery: "Jamba Juice"
	 * 			Example of pQuery: shakes shake shaked
	 * @return - The final Posting list for the Qi
	 */
	private Posting[] returnPostingForQuery(String pQuery){

		// Split string based on space but take quoted substring as one word
		// Ex: Query: shakes "Jamba Juice" -> Q1 (only one query)
		// AND them together at the end
		// Resulting list: [shakes, "Jamba Juice"]
		ArrayList<String> wordList = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(pQuery); // split by space but keep words between ""
		while(m.find()){
			wordList.add(m.group(1));
		}

		// Remove all the NOT query and put it in the separate array
		// Do the query for non-NOT query first then merge it back with the NOT
		ArrayList<String> notQueryList 	= new ArrayList<String>();
		ArrayList<String> newList 		= new ArrayList<String>();
		for (int i = 0; i < wordList.size(); i++) {
			if(wordList.get(i).equals("-")){
				// the next word is for not query
				notQueryList.add(wordList.get(i+1).replaceAll("\"", ""));
				i++;
			}else {
				newList.add(wordList.get(i));
			}
		}
		wordList = newList;
//		System.out.println(notQueryList);
//		System.out.println(newList);
//
//		// List of posting for term in Q1
//		// Ex: Q1: shakes "Jamba Juice"
//		// Ex: Q1: shakes shaked shaking
//		// Store Posting[] for shakes, and another Posting[] for "Jamba Juice"
//		ArrayList<Posting[]> listOfPostingArr = new ArrayList<Posting[]>();
//		// Check whether it's a phrase or AND query
//		// worldList: [shaeks, "Jamba Juice"]
//		for (String eachWord : wordList) {
//			// Ex: "Jamba Juice"
//			// Ex: "Jamba The Juice"
//			if (eachWord.charAt(0) == '\"') {
//				// Do the phrase Query
//				String wordWithOutQuotes = eachWord.replaceAll("\"", ""); // Now it's ---Jamba Juice---
//				String[] wordsArr = wordWithOutQuotes.split("\\s+"); // [Jamba, Juice]
//				// Jamba: [<1,[0,7]>, <2,[3]>, <3,[6]>]
//				// Juice: [<1,[1]>, <2,[0]>, <3,[7]>, <4,[1,4]>]
//				ArrayList<Posting[]> eachWordPostingList = new ArrayList<Posting[]>();
//				// Do the Biword Index
//				if (wordsArr.length == 2) {
//					// Get docIDs for the two word (Biword)
//					String firstWord = PorterStemmer.processToken(wordsArr[0].replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
//					String secondWord = PorterStemmer.processToken(wordsArr[1].replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
//					ArrayList<Integer> docIdArr = mBI.getPosting(firstWord, secondWord);
//					ArrayList<Posting> postingArr = new ArrayList<Posting>();
//					if (docIdArr != null) {
//						// Construct Posting object then add to posting array
//						for (Integer docId : docIdArr) {
//							postingArr.add(new Posting(docId));
//						}
//						Posting[] aPostingList = new Posting[postingArr.size()];
//						eachWordPostingList.add(postingArr.toArray(aPostingList));
//					} else {
//						return null;
//					}
//				}
//				else {
//					// for each of the word in the phrase
//					for (String eachStr : wordsArr) {
//						String str = PorterStemmer.processToken(eachStr);
//						// Get the postingList of each word in the phrase
//						eachWordPostingList.add(mPII.getListOfPosting(str));
//					}
//				}
//				// Now we've got Posting[] of each word
//				listOfPostingArr.add(phraseMergeListOfPostingList(eachWordPostingList, wordList.size()));
//			}
//			else { // Do the word query
//				// Porter stemm here
//				eachWord = PorterStemmer.processToken(eachWord);
//				Posting[] termPostingList = mPII.getListOfPosting(eachWord);
//				if (termPostingList != null) {
//					listOfPostingArr.add(termPostingList);
//				}
//			}
//		}
//
		return null;
	}

}
