
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DiskInvertedIndex {

//	private String mPath;
	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostings;
	private RandomAccessFile mDocWeights;
	private long[] mVocabTable;
	private List<String> mFileNames;


	public DiskInvertedIndex(String path){
		try{
//			mPath = path;
			mVocabList 	= new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostings 	= new RandomAccessFile(new File(path, "postings.bin"), "r");
			mDocWeights	= new RandomAccessFile(new File(path, "docWeights.bin"), "r");
			mVocabTable 			= readVocabTable(path);
			mFileNames 				= readFileNames(path);

		} catch (FileNotFoundException ex){
			System.out.println(ex.toString());
		}
	}

	public double getDocWeight(int pDocID){
		Long position = (long)8 * (long)pDocID;
		return readDocWeight(mDocWeights, position);
	}

	private double readDocWeight(RandomAccessFile pDocWeight, long position ){
		try {
			if(position < pDocWeight.length()){
				pDocWeight.seek(position);
				byte[] buffer = new byte[8];
				pDocWeight.read(buffer, 0, buffer.length);
				return ByteBuffer.wrap(buffer).getDouble();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public Posting[] getPositionPostings(String term){
		long postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition >= 0){
			return readPostingsFromFile(mPostings, postingsPosition);
		}

		return null;
	}

	/**
	 * Get list of docIDs and the frequency of each document
	 * @param term
	 * @return Array of Pair object, <document ID, termFrequency>
	 */
	@SuppressWarnings("unchecked")
	public Pair<Integer>[] getDocListPosting(String term){

		long postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition >= 0){
			Posting[] posting = readPostingsFromFile(mPostings, postingsPosition);
			Pair<Integer>[] pairs = new Pair[posting.length];
			for(int i = 0; i < posting.length; i++){
				pairs[i] = new Pair<Integer>(posting[i].mDocID, posting[i].mPositionArr.size());
//				System.out.println(posting[i]);
//				System.out.println(pairs[i]+ " ");
			}
			return pairs;
		}
		return null;
	}

	public boolean hasTerm(String term){
		long postingsPosition = binarySearchVocabulary(term);
		return postingsPosition > -1;
	}

	private static Posting[] readPostingsFromFile(RandomAccessFile postings, long postingsPosition){
		try{
			// seek to the position in the file where the postings start.
			postings.seek(postingsPosition);

			// read the 4 bytes for the document frequency
			byte[] buffer = new byte[4];
			postings.read(buffer, 0, buffer.length);

			// use ByteBuffer to convert the 4 bytes into an int.
			int documentFrequency = ByteBuffer.wrap(buffer).getInt();

			// initialize the array that will hold the postings.
//			int[] docIDs = new int[documentFrequency];
			Posting[] postingList = new Posting[documentFrequency];
			int documentID = 0; // Handling gap encoding
			for(int d = 0; d < documentFrequency; d++){

				// Read document ID
				postings.read(buffer, 0, buffer.length);
				int docID = ByteBuffer.wrap(buffer).getInt();

				Posting readPosting = new Posting(docID + documentID); // Handling gap encoding
				postingList[d] = readPosting;

				// Read term frequency
				postings.read(buffer, 0, buffer.length);
				int termFreq = ByteBuffer.wrap(buffer).getInt();

				// Read term positions
				for(int ti = 0; ti < termFreq; ti++){
					postings.read(buffer, 0, buffer.length);
					int position = ByteBuffer.wrap(buffer).getInt();
					readPosting.addPosition(position);
				}
				documentID = docID + documentID; // Handling gap encoding
			}
			return postingList;

		} catch(IOException ex){
			System.out.println(ex.toString());
		}
		return null;
	}



   private long binarySearchVocabulary(String term) {
	      // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
	      int i = 0, j = mVocabTable.length / 2 - 1;
	      while (i <= j) {
	         try {
	            int m = (i + j) / 2;
	            long vListPosition = mVocabTable[m * 2];
	            int termLength;
	            if (m == mVocabTable.length / 2 - 1){
	               termLength = (int)(mVocabList.length() - mVocabTable[m*2]);
	            }
	            else {
	               termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
	            }

	            mVocabList.seek(vListPosition);

	            byte[] buffer = new byte[termLength];
	            mVocabList.read(buffer, 0, termLength);
	            String fileTerm = new String(buffer, "ASCII");

	            int compareValue = term.compareTo(fileTerm);
	            if (compareValue == 0) {
	               // found it!
	               return mVocabTable[m * 2 + 1];
	            }
	            else if (compareValue < 0) {
	               j = m - 1;
	            }
	            else {
	               i = m + 1;
	            }
	         }
	         catch (IOException ex) {
	            System.out.println(ex.toString());
	         }
	      }
	      return -1;
	   }


	private static List<String> readFileNames(String indexPath){
		try{
			final List<String> names = new ArrayList<String>();
			final Path currentWorkingPath = Paths.get(indexPath).toAbsolutePath();

			Files.walkFileTree(currentWorkingPath, new SimpleFileVisitor<Path>() {
	            int mDocumentID = 0;

	            public FileVisitResult preVisitDirectory(Path dir,
	             BasicFileAttributes attrs) {
	               // make sure we only process the current working directory
	               if (currentWorkingPath.equals(dir)) {
	                  return FileVisitResult.CONTINUE;
	               }
	               return FileVisitResult.SKIP_SUBTREE;
	            }

	            public FileVisitResult visitFile(Path file,
	             BasicFileAttributes attrs) {
	               // only process .txt files
	               if (file.toString().endsWith(".json")) {
	                  names.add(file.toFile().getName());
	                  mDocumentID++;
	               }
//	               System.out.println(mDocumentID);
	               return FileVisitResult.CONTINUE;
	            }


	            // don't throw exceptions if files are locked/other errors occur
	            public FileVisitResult visitFileFailed(Path file,
	             IOException e) {

	               return FileVisitResult.CONTINUE;
	            }

	         });
			return names;
		} catch(IOException ex){
			System.out.println(ex.toString());
		}
		return null;
	}

	private static long[] readVocabTable(String indexPath){
		long[] vocabTable;
		try{
			RandomAccessFile tableFile = new RandomAccessFile(new File(indexPath, "vocabTable.bin"), "r");

			byte[] byteBuffer = new byte[4];
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			int tableIndex = 0;
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
			byteBuffer = new byte[8];

			while(tableFile.read(byteBuffer, 0, byteBuffer.length) > 0){ // while we keep reading 4 bytes
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;
			}
			tableFile.close();
			return vocabTable;

		} catch (FileNotFoundException ex){
			System.out.println(ex);
		} catch (IOException ex){
			System.out.println(ex);
		}
		return null;
	}


	public void printVocab(){
		int count = 0;
		try{
			mVocabList.seek(0);
			for(int i = 0; i < mVocabTable.length; i += 2){

				long vListPosition = mVocabTable[i];
	            int termLength;
	            if(i+2 == mVocabTable.length){
	            	termLength = (int) (mVocabList.length() - vListPosition);
	            }else {
	            	termLength = (int) (mVocabTable[(i + 2)] - vListPosition);
	            }

				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);
				System.out.println(new String(buffer, "ASCII"));
				count++;
			}
		}catch(IOException e){

		}
		System.out.println("Total Number of vocabs: " + count);
	}

	public List<String> getFileName(){
		return mFileNames;
	}

	public int getTermCount(){
		return mVocabTable.length / 2;
	}

}
