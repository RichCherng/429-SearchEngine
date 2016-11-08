import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Write an inverted indexing of a directory to disk.
 * @author Rich Cherngchaosil
 *
 */

public class IndexWriter {


	public IndexWriter(){

	}

	/**
	 * Wtite the in-memory PII to disk
	 * Saving index, generating three files: Posting Index, vocabulary list, and vocabulary table.
	 * @param pPII
	 */
	public void writeToDisk(String pDirectory, PositionalInvertedIndex pPII, double[] pLds){

		String[] dictionary 	= pPII.getSortedListOfVocab();
		long[] vocabPositions 	= new long[dictionary.length];

		buildVocabFile(pDirectory, dictionary, vocabPositions);
		buildPostingFile(pDirectory, pPII, dictionary, vocabPositions);
		buildDocWeightFile(pDirectory, pLds);
		System.out.println("Saved Index to Disk");
	}

	private void buildDocWeightFile(String pDIr, double[] pLds){
		FileOutputStream docWeightFileWriter = null;

		try {
			docWeightFileWriter = new FileOutputStream(new File(pDIr, "docWeights.bin"));
			for(double d: pLds){
				byte[] LDBytes = ByteBuffer.allocate(8).putDouble(d).array();
				docWeightFileWriter.write(LDBytes, 0, LDBytes.length);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try{
				docWeightFileWriter.close();
 			} catch (IOException ex){
 				System.out.println(ex.toString());
 			}
 		}
	}


	private void buildPostingFile(String pDir, PositionalInvertedIndex index, String[] dictionary, long[] vocabPositions){
		FileOutputStream postingFileWriter = null;

		try{
			postingFileWriter = new FileOutputStream(new File(pDir, "postings.bin"));

			// Simultaneously build the vocabulary table on disk, mapping a term index to a
			// file location in the posting file.
			FileOutputStream vocabTableWriter = new FileOutputStream(new File(pDir, "vocabTable.bin"));

			// Write the number of vocab terms to vocabTable
			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length).array();
			vocabTableWriter.write(tSize, 0, tSize.length);

			int vocabI = 0;
			for(String s: dictionary){
				// Retrieve Posting for each string
				Posting[] postings = index.getListOfPosting(s);

				// write the vocab table entry for this term: the byte location of the term in the vocab list file,
				// and the byte lcoation of the posting for the term in the posting file.
				byte[] vPositionBytes = ByteBuffer.allocate(8).putLong(vocabPositions[vocabI]).array();
				vocabTableWriter.write(vPositionBytes, 0, vPositionBytes.length);

				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postingFileWriter.getChannel().position()).array();
				vocabTableWriter.write(pPositionBytes, 0, pPositionBytes.length);

				// Write the postings file for this term. First, the document frequency for the term, then
				// the document IDs, encoded as gaps
				byte[] docFreqBytes = ByteBuffer.allocate(4).putInt(postings.length).array();
				postingFileWriter.write(docFreqBytes, 0, docFreqBytes.length);

				int lastDocID = 0;
				for(Posting posting : postings){
					int docID = posting.mDocID;

					// Write doc id
					byte[] docIDByte = ByteBuffer.allocate(4).putInt(docID - lastDocID).array();
					postingFileWriter.write(docIDByte, 0, docIDByte.length);

					lastDocID = docID;

					// Write term frequency
					byte[] termFreqByte = ByteBuffer.allocate(4).putInt(posting.mPositionArr.size()).array();
					postingFileWriter.write(termFreqByte, 0, termFreqByte.length);

					// Write term positions in this doc
					for(int pos: posting.mPositionArr){
						// For each position
						byte[] termPosByte = ByteBuffer.allocate(4).putInt(pos).array();
						postingFileWriter.write(termPosByte, 0, termPosByte.length);
					}
				}
				vocabI++;
			}
			vocabTableWriter.close();
			postingFileWriter.close();

		} catch(FileNotFoundException ex){
			System.out.println(ex.toString());
		} catch(IOException ex){

		} finally{
			try{
				postingFileWriter.close();
 			} catch (IOException ex){
 				System.out.println(ex.toString());
 			}
 		}
	}


	/**
	 * Create a vocabulary list file and their associated postion
	 * @param pDir 			- Folder that will be write to
	 * @param dictionary 	- List of Vocabs
	 * @param vocabPosition - Array of position of vocab in the file
	 */
	private void buildVocabFile(String pDir, String[] dictionary, long[] vocabPosition){
		OutputStreamWriter vocabListWriter = null;
		try{

			// 1. Build the vocabulary list: a file of each vocab word concatenated together.
			// 2. Build an array associating each term with its byte location in this file.
			int vocabI = 0;
			vocabListWriter = new OutputStreamWriter(new FileOutputStream(new File(pDir, "vocab.bin")), "ASCII");
			int vocabPos = 0;
			for(String vocabWord : dictionary){

				// For each String in dictionary, save the byte position where that term will start in the vocab file.
				vocabPosition[vocabI] = vocabPos;
				vocabListWriter.write(vocabWord); // Write the string
				vocabI++;
				vocabPos += vocabWord.length();
			}
		} catch(FileNotFoundException ex){

			System.out.println(ex.toString());
		} catch(UnsupportedEncodingException ex){

			System.out.println(ex.toString());
		} catch(IOException ex){

			System.out.println(ex.toString());
		} finally{

			try{
				vocabListWriter.close();
			} catch(IOException ex){
				System.out.println(ex.toString());
			}
		}
	}

}
