import java.util.Scanner;

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
		mRRO 		= new RankRetrievalsObject(pDII);
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

				// Check for spelling
				/*
				 * Any time a user runs a query using a term that is either missing from the
				 * vocab or whose document freq is below some threshold value
				 */
				String suggestQuery = checkQuery(inputLine);

				switch(mMode){
					case 1:
						// Boolean Retrieval
						mBR.processQuery(inputLine);
						break;
					case 2:
						mRRO.processQuery(inputLine);
						break;
				}

				if(!suggestQuery.equals(inputLine)){
					// there are modified query
					System.out.println("Suggeted Query: " + suggestQuery);
					System.out.println("Search again with suggested query? (y/n)");
					inputLine = mScanner.nextLine();
					if(inputLine.equals("y")){
						inputLine = suggestQuery;
						switch(mMode){
							case 1:
								// Boolean Retrieval
								mBR.processQuery(inputLine);
								break;
							case 2:
								mRRO.processQuery(inputLine);
								break;
						}
					}

				}
				// View document
				viewDocument();
			}

			System.out.println("Enter queries: ");
		}
	}

	/**
	 * Check if term missing from dictionary or document frequency is below soem threshold value
	 * @return "" if no suggestion, other a bettwe query
	 */
	private String checkQuery(String pQuery){
		String[] orgQuery = pQuery.split("\\s+");
//		String modQuery = pQuery.replaceAll("[^a-zA-Z0-9- ]+", "").toLowerCase();// Normalize query

		for(int i = 0; i < orgQuery.length; i++){
			String word = orgQuery[i].replaceAll("[^a-zA-Z0-9-]+", "").toLowerCase();
			String[] sugList = null;
			String stem = PorterStemmer.processToken(word);
			if(mDII.hasTerm(stem)){
				// word in vocab
			} else {
				sugList = mSC.correct(word);
				//Select the word with the highest document frequency
				int max = -1;
				String maxTerm = "";
				for(String s: sugList){
					int docFreq = mDII.getDocListPosting(PorterStemmer.processToken(s)).length;
					if(docFreq > max){
						max = docFreq;
						maxTerm = s;
					}
				}
				if(maxTerm.length() > 0){
					orgQuery[i] = orgQuery[i].replaceAll(word, maxTerm);
				}
			}
		}

		return String.join(" ", orgQuery);
	}

	private void viewDocument(){
		System.out.println("Insert document ID to view document's name or press enter to skip: ");
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
}

