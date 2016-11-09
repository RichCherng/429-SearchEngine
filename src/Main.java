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
				SpellingCorrection  aSC	 = null;
				System.out.println("Enter the name of an index to read:");
				String dir  = reader.nextLine();
				aDII 		= new DiskInvertedIndex(dir);

//				for(String f: aDII.getFileName()){
				for(int i = 0; i < aDII.getFileName().size(); i++){
	                  if(aDII.getFileName().get(i).equals("28151BadlandsNationalParkLandscapes.json")){
	                	  System.out.println(aDII.getFileName().get(i) + " " + aDII.getDocWeight(i));
	                  }
				}
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
					aSC = new SpellingCorrection(aKGI); // Initialize Spelling Correction
					in.close();
				} catch (IOException e) {
					System.out.println("Failed Processing Serialized files");
				} catch (ClassNotFoundException e) {
					System.out.println("Failed Processing Serialized files");
				}
				System.out.println("Loading index completed");
//				int docFreq = aDII.getDocListPosting("fire").length;
//				System.out.println(Math.log(1 + ((float)aDII.getFileName().size())/(float)docFreq));
				QueryParser querie =  new QueryParser(aDII, aBI, aSC, dir);
				querie.richRun();

				break;
		}


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

		// Clearing 1-gram and 2-gram for speed. (Not currently being use)
		aKGI.clearGram(1);
		aKGI.clearGram(2);

		/*** Write To Disk ****/
		IndexWriter diskWriter = new IndexWriter();
		diskWriter.writeToDisk(folder, aPII, docReader.getListOfLD());
//		System.out.println(docReader.getListOfLD().size());

		try {
			System.out.println("Saving bi-word index to disk...");
			// Write Bi-Word
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(folder+"/biword.bin"));
//			out.writeObject(aBI);
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
