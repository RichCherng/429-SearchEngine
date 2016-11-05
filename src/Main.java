import java.util.Scanner;

public class Main {

	public static void main(String[] args){



		Scanner reader = new Scanner(System.in);
		System.out.print("Put in directory name: ");
		String dir = reader.nextLine();

		PositionalInvertedIndex aPII 		= new PositionalInvertedIndex();
		BiwordIndex aBI 					= new BiwordIndex();
		DocumentReader docReader 			= new DocumentReader(aPII, aBI);
		DirectoryParser aDirectoryParser 	= new DirectoryParser(docReader);
//
		// Store each articles in the ArrayList of Document.Article
		aDirectoryParser.parseDirectory(dir);


		// Check Dictionary
//		for(String term: aPII.getSortedListOfVocab()){
//			aPII.getListOfPosting(term);
//			if(aPII.getListOfPosting(term) == null){
//				System.out.println(term);
//			}
//
//		}


		/*** Write To Disk ****/
		IndexWriter diskWriter = new IndexWriter();
		diskWriter.writeToDisk(dir,aPII);

		/*** Read from Disk ***/
		DiskInvertedIndex aDII = new DiskInvertedIndex(dir);


		// Create Positional Inverted Index from the list of articles
//		QueryParser querie =  new QueryParser(docReader, aPII, aBI, aDirectoryParser);
//		querie.leafRun();

	}
}
