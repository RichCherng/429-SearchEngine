import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

	private DocumentReader mDocReader;
	private PositionalInvertedIndex mPII;
	private Scanner mScanner;

	public QueryParser(DocumentReader reader, PositionalInvertedIndex p){
		mDocReader 	= reader;
		mPII 		= p;
		mScanner = new Scanner(System.in);
	}

	public void leafRun() {
		String inputLine;
		System.out.print("Enter queries: ");
		while(mScanner.hasNextLine()) {
			inputLine = mScanner.nextLine();
			// Special queries, don't have to worry about error checking
			if (inputLine.charAt(0) == ':') {
				// If the query is ":q", then exit the program
				if (inputLine.substring(1).equals("q")) {
					System.out.println("Exiting the program");
					break;
				}
				// Otherwise, do the special query command
				else {
					this.specialQuery(inputLine.substring(1));
				}
			}
			// Normal query
			else {
				// Example of query:  Q1 + Q2 + Q3
				// Q1 can be:   shakes "Jamba Juice"
				// Q1 can be:	shakes + smoothies mango
				String[] queriesArr = inputLine.split("\\+"); // Split all the queries by "+"
				ArrayList<Posting[]> listOfPostingList = new ArrayList<Posting[]>(); // List of Posting for each query
				for (String eachQueryStr : queriesArr) { // For each query, store the postingList in the outer ArrayList to do AND/OR merge later
					Posting[] termPostingList = returnPostingForQuery(eachQueryStr); // Get the final PostingList
					listOfPostingList.add(termPostingList);
				}
				System.out.println("Documents containing the term:");
				Posting[] aPostingArr = listOfPostingList.get(0);
				for (Posting eachPosting : aPostingArr) {
					System.out.println(eachPosting.mDocID);
				}
				// Needs to OR together the Posting for each Qi
				// posting1 OR posting2 OR posting3
			}
			System.out.print("Enter queries: ");
		}
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
	public Posting[] returnPostingForQuery(String pQuery) {
		if (pQuery.charAt(0) == '-') { // If it's a NOT query
			return handleNotOperator(pQuery);
		}
		// Normal Query
		else {
			// Split string based on space but take quoted substring as one word
			// Ex: Query: shakes "Jamba Juice" -> Q1 (only one query)
			// AND them together at the end
			// Resulting list: [shakes, "Jamba Juice"]
			ArrayList<String> wordList = new ArrayList<String>();
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(pQuery);
			while(m.find()){
				wordList.add(m.group(1));
			}
			// List of posting for term in Q1
			// Ex: Q1: shakes "Jamba Juice"
			// Ex: Q1: shakes shaked shaking
			// Store Posting[] for shakes, and another Posting[] for "Jamba Juice"
			ArrayList<Posting[]> listOfPostingArr = new ArrayList<Posting[]>();
			// Check whether it's a phrase or AND query
			for (String eachWord : wordList) {
				if (eachWord.charAt(0) == '\"') {
					// Do the phrase Query
				}
				else { // Do the word query
					System.out.println("doing the word: " + eachWord);
					Posting[] termPostingList = mPII.getListOfPosting(eachWord);
					listOfPostingArr.add(termPostingList);
				}
			}
			// Merge all the Posting[] together using AND operator
			return mergeListOfPostingList(listOfPostingArr);
		}
	}

	public Posting[] mergeListOfPostingList(ArrayList<Posting[]> pListOfPosting) {
		Posting[] finalPostingArr = pListOfPosting.get(0);
		for (int i = 1; i < pListOfPosting.size(); i++) {
			Posting[] currentPostingArr = pListOfPosting.get(i);
			finalPostingArr = mergeTwoPostingArr(finalPostingArr, currentPostingArr);
		}
		return finalPostingArr;
	}

	public Posting[] mergeTwoPostingArr(Posting[] pFirstPostingArr, Posting[] pSecPostingArr) {
		ArrayList<Posting> mergedPostingList = new ArrayList<Posting>();
		int firstIndex = 0;
		int secIndex = 0;
		while (firstIndex < pFirstPostingArr.length && secIndex < pSecPostingArr.length) {
			Posting firstPosting = pFirstPostingArr[firstIndex];
			Posting secPosting = pSecPostingArr[secIndex];
			if (firstPosting.mDocID == secPosting.mDocID) {
				mergedPostingList.add(new Posting(firstPosting.mDocID));
				firstIndex++;
				secIndex++;
			}
			else if (firstPosting.mDocID < secPosting.mDocID) {
				firstIndex++;
			}
			else {
				secIndex++;
			}
		}
		Posting[] mergedPostingArr = new Posting[mergedPostingList.size()];
		return mergedPostingList.toArray(mergedPostingArr);
	}


	/**
	 * Handle NOT operator. Get the posting list of the word,
	 * 	loop thru each docId, check if it's not equal to the
	 * 	 docId that the word occur, add to the new postingList and return it.
	 * @param pQuery
	 * @return
	 */
	public Posting[] handleNotOperator(String pQuery) {
		if (mPII.hasTerm(pQuery.substring(1))) { // if PII has the term
			Posting[] postingList = mPII.getListOfPosting(pQuery.substring(1)); // The array of Posting object for this query, should be sorted by docID already
			ArrayList<Posting> listOfDocIdNotQuery = new ArrayList<Posting>(); // The list of docId's that do not contain the query (NOT query)
			int postingListIndex = 0; // Starting index of the query's postingList
			int postingListDocIdNum = postingList[postingListIndex].mDocID; // the starting docId in the query's posting list
			for (int iDocId = 0; iDocId < mDocReader.size(); iDocId++) { // Looping through all docID, if the docID matches, skip,
				if (iDocId == postingListDocIdNum) { // If the same, increment the point
					postingListIndex++;
				}
				else { // If different, add to the listOfDocIdNotQuery
					listOfDocIdNotQuery.add(new Posting(iDocId));
				}
				if (postingListIndex < postingList.length) {
					postingListDocIdNum = postingList[postingListIndex].mDocID;
				}
			}
			Posting[] ans = new Posting[listOfDocIdNotQuery.size()];
			return listOfDocIdNotQuery.toArray(ans);
		}
		else {
			return new Posting[0];
		}
	}


	/**
	 * Handle special query
	 * @param pQuery - The query not including the ":"
	 */
	public void specialQuery(String pQuery) {
		// If special query is vocab then print list of vocabs
		if (pQuery.equals("vocab")) {
			String[] vocabList = mPII.getSortedListOfVocab();
			for (String eachVocab : vocabList) {
				System.out.println(eachVocab);
			}
		}
		// if it's "stem" or "index"
		else {
			// Split the query by space
			// :stem token, :index directoryname
			String[] queryList = pQuery.split("\\s+");
			String command = queryList[0];
			String argument = queryList[1];
			if (command.equals("stem")) {
				System.out.println(PorterStemmer.processToken(argument));
			}
			else {
				// Do the index directory
			}
		}
	}
}
