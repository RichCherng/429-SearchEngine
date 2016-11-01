import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

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
	public void writeToDisk(String pDirectory, PositionalInvertedIndex pPII){

		String[] dictionary 	= pPII.getSortedListOfVocab();
		long[] vocabPositions 	= new long[dictionary.length];

		buildVocabFile(pDirectory, dictionary, vocabPositions);
		buildPostingFile(pDirectory, pPII, dictionary, vocabPositions);
	}


	private void buildPostingFile(String pDir, PositionalInvertedIndex index, String[] dictionary, long[] vocabPosition){
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
