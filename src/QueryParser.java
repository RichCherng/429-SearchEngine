import java.util.ArrayList;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

	private BiwordIndex 			mBI;
	private Scanner 				mScanner;
	private RankRetrievalsObject 	mRRO;
	private DiskInvertedIndex 		mDII;
	private SpellingCorrection 		mSC;
	private BooleanRetrieval        mBR;
	private int						mMode; // 1 - Boolean, 2 - rank

	public QueryParser(DiskInvertedIndex pDII, BiwordIndex pB, SpellingCorrection pSC){
		mBI			= pB;
		mDII		= pDII;
		mSC			= pSC;
		mScanner 	= new Scanner(System.in);
		mRRO 		= new RankRetrievalsObject();
		mBR			= new BooleanRetrieval(mDII, mBI);
		mMode 		= selectMode(mScanner);


	}

	// run
	public void richRun(){
		String inputLine;
		System.out.println("Enter queries: ");
		while(mScanner.hasNextLine()){
			inputLine = mScanner.nextLine();

			//Check for empty input
			if(inputLine.length() < 1){
				System.out.println("Enter queries: ");
				continue;
			}

			// Special queries
			if(inputLine.charAt(0) == ':'){
				// ":q", exit the program
				if(inputLine.substring(1).equals("q")){
					System.out.println("Exiting the program");
				} else {
					// Do the Special query command
					specialQuery(inputLine.substring(1));
				}
			} else {
				// Normal Query
				switch(mMode){
					case 1:
						// Boolean Retrieval
						mBR.processQuery(inputLine);
						break;
				}

				// View document
				viewDocument();
			}

			System.out.println("Enter queries: ");
		}
	}

	private void viewDocument(){
		System.out.println("Insert document ID to view document or press enter to skip: ");
		String input = mScanner.nextLine();
		if(input.length() > 0){
			// Not empty = not skip
			int viewID = Integer.parseInt(input);
			if(viewID < mDII.getFileName().size()){
				mDII.getFileName().get(viewID);
				System.out.println("Document Name: " + mDII.getFileName().get(viewID));
			} else {
				System.out.println("No document containing the query");
			}
		}
	}

	private void specialQuery(String pQuery){

		if(pQuery.equals("vocab")){
			mDII.printVocab();
		}else {
			// "stem"
			String[] queryList 	= pQuery.split("\\s+");
			String command 		= queryList[0];
			String argument		= queryList[1];
			if(command.equals("stem")){
				System.out.println(PorterStemmer.processToken(argument.replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase()));
			}
		}
	}

	private int selectMode(Scanner pReader){
		System.out.println("Select Mode:\n\t1) Boolean Retrieval\n\t2) Rank Retrieval\nChoose a selection:");
		return Integer.parseInt(pReader.nextLine());
	}
//	// run
//	public void leafRun() {
//		String inputLine;
//		System.out.print("Enter queries: ");
//		while(mScanner.hasNextLine()) {
//			inputLine = mScanner.nextLine();
//			if(inputLine.length() < 1){
//				System.out.print("Enter queries: ");
//				continue;
//			}
//			mRRO.setQuery(inputLine);
//			mRRO.setPII(mPII);
//			mRRO.setDocReader(mDocReader);
//			// Special queries, don't have to worry about error checking
//			if (inputLine.charAt(0) == ':') {
//				// If the query is ":q", then exit the program
//				if (inputLine.substring(1).equals("q")) {
//					System.out.println("Exiting the program");
//					break;
//				}
//				// Otherwise, do the special query command
//				else {
//					this.specialQuery(inputLine.substring(1));
//				}
//			}
//			// Normal query
//			else {
//				// Example of query:  Q1 + Q2 + Q3
//				// Q1 can be:   shakes "Jamba Juice"
//				// Q1 can be:	shakes + smoothies mango
//				String[] queriesArr = inputLine.split("\\+"); // Split all the queries by "+". The array will be [Q1,Q2,Q3,Q4,Qn]
//				ArrayList<Posting[]> listOfPostingList = new ArrayList<Posting[]>(); // List of Posting for each query
//				for (String eachQueryStr : queriesArr) { // For each query, store the postingList in the outer ArrayList to do AND/OR merge later
//					String processedQueryStr = eachQueryStr.trim().toLowerCase();
//					Posting[] termPostingList = returnPostingForQuery(processedQueryStr); // Get the final PostingList
//					if (termPostingList != null ) {
//						listOfPostingList.add(termPostingList);
//					}
//				}
//				// OR together the Posting for each Qi
//				// posting1 OR posting2 OR posting3
//				SortedSet<Integer> docIdSet = orListOfPostingList(listOfPostingList);
//				if (docIdSet.size() > 0) {
//					System.out.println("Documents containing the term:");
//					for (int eachDocId : docIdSet) {
//						System.out.println(eachDocId);
//					}
//					System.out.println("total results: " + docIdSet.size());
//
//					System.out.print("Insert document ID to view document or insert negative number to skip: ");
//					int viewID = Integer.parseInt(mScanner.nextLine());
//					if(viewID >= 0){
//						if(viewID < mDocReader.size()){
//							System.out.println(mDocReader.get(viewID).body);
//						} else {
//							System.out.println("Document ID doesn't exists");
//						}
//
//					}
//				}
//				else {
//					System.out.println("No document containing the query");
//				}
//			}
//			System.out.print("Enter queries: ");
//		}
//	}
//
//	/**
//	 * Return Posting for the query literal
//	 * Each Qi is one of the following: 1. a single token. 2. phrase query (between the "")
//	 * @param pQuery - Qi query.
//	 * 		  	Example of pQuery: shakes "Jamba Juice"
//	 * 			Example of pQuery: shakes
//	 * 			Example of pQuery: "Jamba Juice"
//	 * 			Example of pQuery: shakes shake shaked
//	 * @return - The final Posting list for the Qi
//	 */
//	public Posting[] returnPostingForQuery(String pQuery) {
//		// Split string based on space but take quoted substring as one word
//		// Ex: Query: shakes "Jamba Juice" -> Q1 (only one query)
//		// AND them together at the end
//		// Resulting list: [shakes, "Jamba Juice"]
//		ArrayList<String> wordList = new ArrayList<String>();
//		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(pQuery);
//		while(m.find()){
//			wordList.add(m.group(1));
//		}
//
//		// Remove all the NOT query and put it in the separate array
//		// Do the query for non-NOT query first then merge it back with the NOT
//		ArrayList<String> removeStrArr = new ArrayList<String>();
//		ArrayList<String> notQueryList = new ArrayList<String>();
//		for (int i = 0; i < wordList.size(); i++) {
//			if (wordList.get(i).contains(("-\""))) {
//				String newWord = wordList.get(i) + " " + wordList.get(i + 1);
//				notQueryList.add(newWord);
//				removeStrArr.add(wordList.get(i));
//				removeStrArr.add(wordList.get(i + 1));
//				i++;
//			}
//			// If it's a not query like -mango
//			if (wordList.get(i).charAt(0) == '-') {
//				notQueryList.add(wordList.get(i));
//				removeStrArr.add(wordList.get(i));
//			}
//		}
//
//		for (String eachIndex : removeStrArr) {
//			wordList.remove(eachIndex);
//		}
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
//		// Merge all the Posting[] together using AND operator
//		Posting[] finalNormalPosting = new Posting[0];
//		if (wordList.size() > 0) {
//			finalNormalPosting = mergeListOfPostingList(listOfPostingArr, wordList.size());
//		}
//
//		ArrayList<Posting[]> listOfNotPostingArr = new ArrayList<Posting[]>();
//		for (String eachNotWord : notQueryList) {
//			if (eachNotWord.contains("-\"")) {
//				// remove the quotation marks
//				String wordWithOutQuotes = eachNotWord.substring(1).replaceAll("\"", "");
//				// Split the word by space
//				String[] wordsArr = wordWithOutQuotes.split("\\s+");
//				ArrayList<Posting[]> eachWordPostingList = new ArrayList<Posting[]>();
//				for (String eachStr : wordsArr) {
//					eachWordPostingList.add(mPII.getListOfPosting(PorterStemmer.processToken(eachStr)));
//				}
//				// Get the list of posting for each word in phrase, do positional merge them and do NOT operator on it
//				listOfNotPostingArr.add(returnNotPosting(phraseMergeListOfPostingList(eachWordPostingList, wordList.size())));
//			}
//			// normal NOT query (non-quote) (just a word)
//			else {
//				Posting[] termPostingList = handleNotOperator(PorterStemmer.processToken(eachNotWord));
//				if (termPostingList != null) {
//					listOfNotPostingArr.add(termPostingList);
//				}
//			}
//		}
//		if (notQueryList.size() > 0) {
//			Posting[] finalNotPosting = mergeListOfPostingList(listOfNotPostingArr, notQueryList.size());
//			//			System.out.println("finalNotPosting: ");
//			//			for (Posting aPosting : finalNotPosting) {
//			//				System.out.println(aPosting);
//			//			}
//			if (wordList.size() > 0)
//				return mergeTwoPostingArr(finalNormalPosting, finalNotPosting);
//			else
//				return finalNotPosting;
//		}
//		else {
//			return finalNormalPosting;
//		}
//	}
//
//	public Posting[] phraseMergeListOfPostingList(ArrayList<Posting[]> pListOfPosting, int numOfPostingList) {
//		//		if (pListOfPosting.size() != numOfPostingList) {
//		//			return null;
//		//		}
//		// If the phrase query is just one word then return this one
//		Posting[] finalPostingArr = pListOfPosting.get(0);
//		// Combine each 2 into 1 PostingList until the second to last
//		for (int i = 1; i < pListOfPosting.size(); i++) {
//			Posting[] currentPostingArr = pListOfPosting.get(i);
//			finalPostingArr = phraseMergeTwoPostingArr(finalPostingArr, currentPostingArr);
//		}
//		return finalPostingArr;
//	}
//
//	public Posting[] phraseMergeTwoPostingArr(Posting[] pFirstPostingArr, Posting[] pSecPostingArr) {
//		// If one of the posting doesn't exists, cannot merge
//		if(pFirstPostingArr == null || pSecPostingArr == null){
//			return null;
//		}
//		ArrayList<Posting> phraseMergedPostingList = new ArrayList<Posting>();
//		int firstIndex = 0;
//		int secIndex = 0;
//		while (firstIndex < pFirstPostingArr.length && secIndex < pSecPostingArr.length) {
//			Posting firstPosting = pFirstPostingArr[firstIndex];
//			Posting secPosting = pSecPostingArr[secIndex];
//			// When the two docs are the same
//			if (firstPosting.mDocID == secPosting.mDocID) {
//				// Create a new Posting with no position
//				Posting mergedPosting = new Posting(firstPosting.mDocID);
//				// firstPosition: [0,7]
//				// SecPosition: [1]
//				ArrayList<Integer> firstPositionArr = firstPosting.mPositionArr;
//				ArrayList<Integer> secPositionArr = secPosting.mPositionArr;
//				int firstPositionIndex = 0;
//				int secPositionIndex = 0;
//				// loop through each position in first postions array
//				while (firstPositionIndex < firstPositionArr.size() && secPositionIndex < secPositionArr.size()) {
//					int currentFirstPosition = firstPositionArr.get(firstPositionIndex);
//					int currentSecPosition = secPositionArr.get(secPositionIndex);
//					// while the first position is greater than the second position, move the second position
//					while (currentFirstPosition >= currentSecPosition) {
//						secPositionIndex++;
//						if (secPositionIndex >= secPositionArr.size()) {
//							break;
//						}
//						currentSecPosition = secPositionArr.get(secPositionIndex);
//					}
//					// Check if postion2 is right after position1
//					if (currentSecPosition == (currentFirstPosition + 1)) {
//						mergedPosting.addPosition(currentSecPosition);
//					}
//					firstPositionIndex++;
//				}
//				if (mergedPosting.mPositionArr.size() > 0) {
//					phraseMergedPostingList.add(mergedPosting);
//				}
//				firstIndex++;
//				secIndex++;
//			}
//			else if (firstPosting.mDocID > secPosting.mDocID) {
//				secIndex++;
//			}
//			else {
//				firstIndex++;
//			}
//		}
//		Posting[] ans = new Posting[phraseMergedPostingList.size()];
//		return phraseMergedPostingList.toArray(ans);
//	}
//
//	public Posting[] mergeListOfPostingList(ArrayList<Posting[]> pListOfPosting, int numOfPostingList) {
//		if (pListOfPosting.size() != numOfPostingList) {
//			return null;
//		}
//		Posting[] finalPostingArr = pListOfPosting.get(0);
//		for (int i = 1; i < pListOfPosting.size(); i++) {
//			Posting[] currentPostingArr = pListOfPosting.get(i);
//			finalPostingArr = mergeTwoPostingArr(finalPostingArr, currentPostingArr);
//		}
//		return finalPostingArr;
//	}
//
//	public Posting[] mergeTwoPostingArr(Posting[] pFirstPostingArr, Posting[] pSecPostingArr) {
//		// If one of the posting doesn't exists, cannot merge
//		if(pFirstPostingArr == null || pSecPostingArr == null){
//			return null;
//		}
//		ArrayList<Posting> mergedPostingList = new ArrayList<Posting>();
//		int firstIndex = 0;
//		int secIndex = 0;
//		while (firstIndex < pFirstPostingArr.length && secIndex < pSecPostingArr.length) {
//			Posting firstPosting = pFirstPostingArr[firstIndex];
//			Posting secPosting = pSecPostingArr[secIndex];
//			if (firstPosting.mDocID == secPosting.mDocID) {
//				mergedPostingList.add(new Posting(firstPosting.mDocID));
//				firstIndex++;
//				secIndex++;
//			}
//			else if (firstPosting.mDocID < secPosting.mDocID) {
//				firstIndex++;
//			}
//			else {
//				secIndex++;
//			}
//		}
//		Posting[] mergedPostingArr = new Posting[mergedPostingList.size()];
//		return mergedPostingList.toArray(mergedPostingArr);
//	}
//
//	/**
//	 * Return array of docId from applying OR operator to the Posting Lists
//	 * @param pListOfPosting
//	 * @return Sorted Set of docID
//	 */
//	public SortedSet<Integer> orListOfPostingList(ArrayList<Posting[]> pListOfPosting) {
//		SortedSet<Integer> docIdSet = new TreeSet<Integer>();
//		for (Posting[] eachPostingList : pListOfPosting) {
//			for (Posting eachPosting : eachPostingList) {
//				docIdSet.add(eachPosting.mDocID);
//			}
//		}
//		return docIdSet;
//	}
//	/**
//	 * Handle NOT operator. Get the posting list of the word,
//	 * 	loop thru each docId, check if it's not equal to the
//	 * 	 docId that the word occur, add to the new postingList and return it.
//	 * @param pQuery
//	 * @return - Posting list of the query after NOT operator
//	 */
//	public Posting[] handleNotOperator(String pQuery) {
//		if (mPII.hasTerm(pQuery.substring(1))) { // if PII has the term
//			Posting[] postingList = mPII.getListOfPosting(pQuery.substring(1)); // The array of Posting object for this query, should be sorted by docID already
//			ArrayList<Posting> listOfDocIdNotQuery = new ArrayList<Posting>(); // The list of docId's that do not contain the query (NOT query)
//			int postingListIndex = 0; // Starting index of the query's postingList
//			int postingListDocIdNum = postingList[postingListIndex].mDocID; // the starting docId in the query's posting list
//			for (int iDocId = 0; iDocId < mDocReader.size(); iDocId++) { // Looping through all docID, if the docID matches, skip,
//				if (iDocId == postingListDocIdNum) { // If the same, increment the point
//					postingListIndex++;
//				}
//				else { // If different, add to the listOfDocIdNotQuery
//					listOfDocIdNotQuery.add(new Posting(iDocId));
//				}
//				if (postingListIndex < postingList.length) {
//					postingListDocIdNum = postingList[postingListIndex].mDocID;
//				}
//			}
//			Posting[] ans = new Posting[listOfDocIdNotQuery.size()];
//			return listOfDocIdNotQuery.toArray(ans);
//		}
//		else {
//			return null;
//		}
//	}
//	public Posting[] returnNotPosting(Posting[] pPosting) {
//		// If posting is null, return the postingList with all docId (revert of empty)
//		if(pPosting == null){
//			Posting aPosting;
//			ArrayList<Posting> tempPost = new ArrayList<Posting>();
//			for(int i = 0; i < mDocReader.size(); i++){
//				aPosting = new Posting(i);
//				tempPost.add(aPosting);
//			}
//			Posting[] ans = new Posting[tempPost.size()];
//			return tempPost.toArray(ans);
//		}
//		Posting[] postingList = pPosting;
//		ArrayList<Posting> listOfDocIdNotQuery = new ArrayList<Posting>(); // The list of docId's that do not contain the query (NOT query)
//		int postingListIndex = 0; // Starting index of the query's postingList
//		int postingListDocIdNum = postingList[postingListIndex].mDocID; // the starting docId in the query's posting list
//		for (int iDocId = 0; iDocId < mDocReader.size(); iDocId++) { // Looping through all docID, if the docID matches, skip,
//			if (iDocId == postingListDocIdNum) { // If the same, increment the point
//				postingListIndex++;
//			}
//			else { // If different, add to the listOfDocIdNotQuery
//				listOfDocIdNotQuery.add(new Posting(iDocId));
//			}
//			if (postingListIndex < postingList.length) {
//				postingListDocIdNum = postingList[postingListIndex].mDocID;
//			}
//		}
//		Posting[] ans = new Posting[listOfDocIdNotQuery.size()];
//		return listOfDocIdNotQuery.toArray(ans);
//	}
//
//
//	/**
//	 * Handle special query
//	 * @param pQuery - The query not including the ":"
//	 */
//	public void specialQuery(String pQuery) {
//		// If special query is vocab then print list of vocabs
//		if (pQuery.equals("vocab")) {
//			String[] vocabList = mPII.getSortedListOfVocab();
//			for (String eachVocab : vocabList) {
//				System.out.println(eachVocab);
//			}
//			System.out.println("Total Number of vocabs: " + vocabList.length);
//		}
//		// if it's "stem" or "index"
//		else {
//			// Split the query by space
//			// :stem token, :index directoryname
//			String[] queryList = pQuery.split("\\s+");
//			String command = queryList[0];
//			String argument = queryList[1];
//			if (command.equals("stem")) {
//				System.out.println(PorterStemmer.processToken(argument.replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase()));
//			}
//			else {
//				// Do the index directory
//				mPII.reset();
//				mBI.reset();
//				mDir.parseDirectory(argument);
//
//			}
//		}
//	}
}

