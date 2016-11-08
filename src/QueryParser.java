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
				switch(mMode){
					case 1:
						// Boolean Retrieval
						mBR.processQuery(inputLine);
						break;
					case 2:
						mRRO.processQuery(inputLine);
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
}

