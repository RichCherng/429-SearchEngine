import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class Main {

	public static void main(String[] args){

		Scanner reader = new Scanner(System.in);
		System.out.println("Menu:");
		System.out.println("1) Build index");
		System.out.println("2) Read and query indexd");
		System.out.println("Choose a selection:");

		int menuChoice = Integer.parseInt(reader.nextLine());
		switch(menuChoice){
			case 1:
				/** Index directory and write to disk **/
				indexDirectory(reader);
				break;
			case 2:
				/*** Read from Disk ***/

				DiskInvertedIndex	aDII = null;
				BiwordIndex 		aBI  = null;
				KGramIndex			aKGI = null;
				System.out.println("Enter the name of an index to read:");
				String dir  = reader.nextLine();
				aDII 		= new DiskInvertedIndex(dir);

				/** Read Serialized Objects **/
				try {
					System.out.println("Loading Bi-word index...");
					// Read bi-word
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(dir+"/biword.bin"));
//					aBI = (BiwordIndex) in.readObject();
					in.close();

					System.out.println("Loading K-gram index...");
					// Read k-gram
					in = new ObjectInputStream(new FileInputStream(dir + "/kgram.bin"));
					aKGI = (KGramIndex) in.readObject();
					in.close();
				} catch (IOException e) {
//					e.printStackTrace();
					System.out.println("Failed Processing Serialized files");
				} catch (ClassNotFoundException e) {
//					e.printStackTrace();
					System.out.println("Failed Processing Serialized files");
				}
				System.out.println("Loading index completed");


//				aKGI.print();

//				QueryParser querie =  new QueryParser(aDII, aBI);
//				querie.leafRun();

				/** Testing Spelling Correction **/
				SpellingCorrection aSC = new SpellingCorrection(aKGI);
				System.out.println(aSC.correct("parg"));
				break;
		}



//		PositionalInvertedIndex aPII 		= new PositionalInvertedIndex();
//		BiwordIndex aBI 					= new BiwordIndex();
//		DocumentReader docReader 			= new DocumentReader(aPII, aBI);
//		DirectoryParser aDirectoryParser 	= new DirectoryParser(docReader);
////
//		// Store each articles in the ArrayList of Document.Article
//		aDirectoryParser.parseDirectory(dir);


		// Check Dictionary
//		for(String term: aPII.getSortedListOfVocab()){
//			aPII.getListOfPosting(term);
//			if(aPII.getListOfPosting(term) == null){
//				System.out.println(term);
//			}
//
//		}

//		/*** Write To Disk ****/
//		IndexWriter diskWriter = new IndexWriter();
//		diskWriter.writeToDisk(dir,aPII);
//
//		/*** Read from Disk ***/
//		DiskInvertedIndex aDII = new DiskInvertedIndex(dir);
//
//
//		// compare
//		Posting[] fromDisk = aDII.getPostings(PorterStemmer.processToken("park"));
//		Posting[] fromIndex = aPII.getListOfPosting(PorterStemmer.processToken("park"));
//		for(int i = 0; i < fromDisk.length; i++){
////			System.out.println(fromDisk[i]);
////			System.out.println(fromIndex[i]);
//		}
//
//		Pair<Integer>[] pairs= aDII.getDocList(PorterStemmer.processToken("park"));
//		for(Pair<Integer> p: pairs){
//			System.out.println(p.getFirst() + " : " + p.getSecond());
//		}

		// Create Positional Inverted Index from the list of articles
//		QueryParser querie =  new QueryParser(docReader, aPII, aBI, aDirectoryParser);
//		querie.leafRun();

	}


	public static void indexDirectory(Scanner reader){
		System.out.println("Enter the name of a directory to index:");
		String folder = reader.nextLine();

		PositionalInvertedIndex aPII 			 = new PositionalInvertedIndex();
		BiwordIndex 			aBI 			 = new BiwordIndex();
		KGramIndex 				aKGI			 = new KGramIndex();
		DocumentReader 			docReader 		 = new DocumentReader(aPII, aBI, aKGI);
		DirectoryParser 		aDirectoryParser = new DirectoryParser(docReader);
		// Store each articles in the ArrayList of Document.Article
		aDirectoryParser.parseDirectory(folder);


		/*** Write To Disk ****/
		IndexWriter diskWriter = new IndexWriter();
		diskWriter.writeToDisk(folder, aPII, docReader.getListOfLd());

		try {
			System.out.println("Saving bi-word index to disk...");
			// Write Bi-Word
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(folder+"/biword.bin"));
			out.writeObject(aBI);
			out.close();

			System.out.println("Saving k-gram index to disk...");
			// Write K-Gram
			out = new ObjectOutputStream(new FileOutputStream(folder+"/kgram.bin"));
			out.writeObject(aKGI);
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Saving indexs completed");


	}
}
