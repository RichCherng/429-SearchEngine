import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanRetrieval {

	private DiskInvertedIndex 	mDII;
	private BiwordIndex			mBI;
	private boolean BI_WORD_ENABLED = false;

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
		SortedSet<Integer> docIdSet = orListOfPostingList(listOfPostingList);
		if (docIdSet.size() > 0){
			System.out.println("Documents containing the term: ");
			for(int eachDocId : docIdSet){
				System.out.println(eachDocId);
			}
			System.out.println("Total Results: " + docIdSet.size());

		} else {
			System.out.println("No document containing the query");
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

		// List of posting for term in Q1
		// Ex: Q1: shakes "Jamba Juice"
		// Ex: Q1: shakes shaked shaking
		// Store Posting[] for shakes, and another Posting[] for "Jamba Juice"
		ArrayList<Posting[]> listOfPostingArr = new ArrayList<Posting[]>();
		// Check whether it's a phrase or AND query
		// worldList: [shaeks, "Jamba Juice"]
		for (String eachWord : wordList) {
			// Ex: "Jamba Juice"
			// Ex: "Jamba The Juice"
//			if (eachWord.charAt(0) == '\"') {
			if(eachWord.contains(" ")){

				// Do the phrase Query
				listOfPostingArr.add(phraseMergeListOfPostingList(phrasePosting(eachWord), wordList.size()));
			}
			else {
				// Do the word query
				eachWord = PorterStemmer.processToken(eachWord);
				Posting[] termPostingList = mDII.getPositionPostings(eachWord);
				if (termPostingList != null) {
					listOfPostingArr.add(termPostingList);
				}
			}
		}

		// Merge all the Posting[] together using AND operator, not including NOT Posting
		Posting[] finalNormalPosting = new Posting[0];
		if (wordList.size() > 0) {
			finalNormalPosting = mergeListOfPostingList(listOfPostingArr, wordList.size());
		}

		ArrayList<Posting[]> listOfNotPostingArr = new ArrayList<Posting[]>();
		for (String eachNotWord : notQueryList) {

			if(eachNotWord.contains(" ")){
				/** Phrase Query **/
				String[] wordsArr = eachNotWord.split("\\s+");
				ArrayList<Posting[]> eachWordPostingList = new ArrayList<Posting[]>();
				for(String eachStr : wordsArr){
					eachWordPostingList.add(mDII.getPositionPostings(PorterStemmer.processToken(eachStr)));
					// Get the list of posting for each word in phrase, do positional merge them and do NOT operator on it
					listOfNotPostingArr.add(returnNotPosting(phraseMergeListOfPostingList(eachWordPostingList, wordList.size())));
				}

			}else {
				// normal NOT query (non-quote) (just a word)
				Posting[] termPostingList = handleNotOperator(PorterStemmer.processToken(eachNotWord));
				if (termPostingList != null) {
					listOfNotPostingArr.add(termPostingList);
				}
			}
		}
		if (notQueryList.size() > 0) {
			Posting[] finalNotPosting = mergeListOfPostingList(listOfNotPostingArr, listOfNotPostingArr.size());
			//			System.out.println("finalNotPosting: ");
			//			for (Posting aPosting : finalNotPosting) {
			//				System.out.println(aPosting);
			//			}
			if (wordList.size() > 0)
				return mergeTwoPostingArr(finalNormalPosting, finalNotPosting);
			else
				return finalNotPosting;
		}
		else {
			return finalNormalPosting;
		}
	}

	/**
	 * Return array of docId from applying OR operator to the Posting Lists
	 * @param pListOfPosting
	 * @return Sorted Set of docID
	 */
	private SortedSet<Integer> orListOfPostingList(ArrayList<Posting[]> pListOfPosting) {
		SortedSet<Integer> docIdSet = new TreeSet<Integer>();
		for (Posting[] eachPostingList : pListOfPosting) {
			for (Posting eachPosting : eachPostingList) {
				docIdSet.add(eachPosting.mDocID);
			}
		}
		return docIdSet;
	}


	/**
	 * Handle NOT operator. Get the posting list of the word,
	 * 	loop thru each docId, check if it's not equal to the
	 * 	 docId that the word occur, add to the new postingList and return it.
	 * @param pQuery
	 * @return - Posting list of the query after NOT operator
	 */
	public Posting[] handleNotOperator(String pQuery) {
		if (mDII.hasTerm(pQuery)) { // if PII has the term
			Posting[] postingList = mDII.getPositionPostings(pQuery); // The array of Posting object for this query, should be sorted by docID already
			ArrayList<Posting> listOfDocIdNotQuery = new ArrayList<Posting>(); // The list of docId's that do not contain the query (NOT query)
			int postingListIndex = 0; // Starting index of the query's postingList
			int postingListDocIdNum = postingList[postingListIndex].mDocID; // the starting docId in the query's posting list
			for (int iDocId = 0; iDocId < mDII.getFileName().size(); iDocId++) { // Looping through all docID, if the docID matches, skip,
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
			return null;
		}
	}


	private Posting[] returnNotPosting(Posting[] pPosting) {
		// If posting is null, return the postingList with all docId (revert of empty)
		if(pPosting == null){
			Posting aPosting;
			ArrayList<Posting> tempPost = new ArrayList<Posting>();
			for(int i = 0; i < mDII.getFileName().size(); i++){
				aPosting = new Posting(i);
				tempPost.add(aPosting);
			}
			Posting[] ans = new Posting[tempPost.size()];
			return tempPost.toArray(ans);
		}
		Posting[] postingList = pPosting;
		ArrayList<Posting> listOfDocIdNotQuery = new ArrayList<Posting>(); // The list of docId's that do not contain the query (NOT query)
		int postingListIndex = 0; // Starting index of the query's postingList
		int postingListDocIdNum = postingList[postingListIndex].mDocID; // the starting docId in the query's posting list
		for (int iDocId = 0; iDocId < mDII.getFileName().size(); iDocId++) { // Looping through all docID, if the docID matches, skip,
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


	private Posting[] mergeListOfPostingList(ArrayList<Posting[]> pListOfPosting, int numOfPostingList) {
		if (pListOfPosting.size() != numOfPostingList) {
			return null;
		}
		Posting[] finalPostingArr = pListOfPosting.get(0);
		for (int i = 1; i < pListOfPosting.size(); i++) {
			Posting[] currentPostingArr = pListOfPosting.get(i);
			finalPostingArr = mergeTwoPostingArr(finalPostingArr, currentPostingArr);
		}
		return finalPostingArr;
	}

	private Posting[] mergeTwoPostingArr(Posting[] pFirstPostingArr, Posting[] pSecPostingArr) {
		// If one of the posting doesn't exists, cannot merge
		if(pFirstPostingArr == null || pSecPostingArr == null){
			return null;
		}
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

	private ArrayList<Posting[]> phrasePosting(String pPhrase){

		String wordWithOutQuotes = pPhrase.replaceAll("\"", ""); // Now it's ---Jamba Juice---
		String[] wordsArr = wordWithOutQuotes.split("\\s+"); // [Jamba, Juice]
		// Jamba: [<1,[0,7]>, <2,[3]>, <3,[6]>]
		// Juice: [<1,[1]>, <2,[0]>, <3,[7]>, <4,[1,4]>]
		ArrayList<Posting[]> eachWordPostingList = new ArrayList<Posting[]>();
		// Do the Biword Index
		if (wordsArr.length == 2 && BI_WORD_ENABLED) { // Set this to fail to disabled bi-word
			// Get docIDs for the two word (Biword)
			String firstWord = PorterStemmer.processToken(wordsArr[0].replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
			String secondWord = PorterStemmer.processToken(wordsArr[1].replaceAll("[^a-zA-Z0-9-]+" , "").toLowerCase());
			ArrayList<Integer> docIdArr = mBI.getPosting(firstWord, secondWord);
			ArrayList<Posting> postingArr = new ArrayList<Posting>();
			if (docIdArr != null) {
				// Construct Posting object then add to posting array
				for (Integer docId : docIdArr) {
					postingArr.add(new Posting(docId));
				}
				Posting[] aPostingList = new Posting[postingArr.size()];
				eachWordPostingList.add(postingArr.toArray(aPostingList));
			} else {
				return null;
			}
		}
		else {
			// for each of the word in the phrase
			for (String eachStr : wordsArr) {
				String str = PorterStemmer.processToken(eachStr);
				// Get the postingList of each word in the phrase
				eachWordPostingList.add(mDII.getPositionPostings(str));
			}
		}

		// Now we've got Posting[] of each word
		return eachWordPostingList;
	}

	private Posting[] phraseMergeListOfPostingList(ArrayList<Posting[]> pListOfPosting, int numOfPostingList) {
		//		if (pListOfPosting.size() != numOfPostingList) {
		//			return null;
		//		}

		// If the phrase query is just one word then return this one
		Posting[] finalPostingArr = pListOfPosting.get(0);
		// Combine each 2 into 1 PostingList until the second to last
		for (int i = 1; i < pListOfPosting.size(); i++) {
			Posting[] currentPostingArr = pListOfPosting.get(i);
			finalPostingArr = phraseMergeTwoPostingArr(finalPostingArr, currentPostingArr);
		}
		return finalPostingArr;
	}

	public Posting[] phraseMergeTwoPostingArr(Posting[] pFirstPostingArr, Posting[] pSecPostingArr) {
		// If one of the posting doesn't exists, cannot merge
		if(pFirstPostingArr == null || pSecPostingArr == null){
			return null;
		}
		ArrayList<Posting> phraseMergedPostingList = new ArrayList<Posting>();
		int firstIndex = 0;
		int secIndex = 0;
		while (firstIndex < pFirstPostingArr.length && secIndex < pSecPostingArr.length) {
			Posting firstPosting = pFirstPostingArr[firstIndex];
			Posting secPosting = pSecPostingArr[secIndex];
			// When the two docs are the same
			if (firstPosting.mDocID == secPosting.mDocID) {
				// Create a new Posting with no position
				Posting mergedPosting = new Posting(firstPosting.mDocID);
				// firstPosition: [0,7]
				// SecPosition: [1]
				ArrayList<Integer> firstPositionArr = firstPosting.mPositionArr;
				ArrayList<Integer> secPositionArr = secPosting.mPositionArr;
				int firstPositionIndex = 0;
				int secPositionIndex = 0;
				// loop through each position in first postions array
				while (firstPositionIndex < firstPositionArr.size() && secPositionIndex < secPositionArr.size()) {
					int currentFirstPosition = firstPositionArr.get(firstPositionIndex);
					int currentSecPosition = secPositionArr.get(secPositionIndex);
					// while the first position is greater than the second position, move the second position
					while (currentFirstPosition >= currentSecPosition) {
						secPositionIndex++;
						if (secPositionIndex >= secPositionArr.size()) {
							break;
						}
						currentSecPosition = secPositionArr.get(secPositionIndex);
					}
					// Check if postion2 is right after position1
					if (currentSecPosition == (currentFirstPosition + 1)) {
						mergedPosting.addPosition(currentSecPosition);
					}
					firstPositionIndex++;
				}
				if (mergedPosting.mPositionArr.size() > 0) {
					phraseMergedPostingList.add(mergedPosting);
				}
				firstIndex++;
				secIndex++;
			}
			else if (firstPosting.mDocID > secPosting.mDocID) {
				secIndex++;
			}
			else {
				firstIndex++;
			}
		}
		Posting[] ans = new Posting[phraseMergedPostingList.size()];
		return phraseMergedPostingList.toArray(ans);
	}
}
