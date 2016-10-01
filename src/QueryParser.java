import java.util.ArrayList;
import java.util.Scanner;

public class QueryParser {

	private DocumentReader docReader;
	private PositionalInvertedIndex PII;
	private Scanner scanner;

	public QueryParser(DocumentReader reader, PositionalInvertedIndex p){

		docReader 	= reader;
		PII 		= p;
		scanner = new Scanner(System.in);

	}

	public void run(){

		String line;
		System.out.print("Enter querie: ");
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			if(line.charAt(0) == ':'){

				if(line.length() == 1){
					/* empty arguments*/
					System.out.println("empty argument");
				} else if (specialQuerie(line)){ // True = exit
					/* Special Querie*/
					System.out.println("Exit Program");
					break;
				}
			} else{
				System.out.println("Need to be implemented");
			}

			System.out.print("Enter querie: ");
		}
	}


	/**
	 * Handle special queries
	 * @param querie
	 * @return true is program exit, oitherwise false
	 */

	public boolean specialQuerie(String queries){
		String arg = queries.substring(1);
		if(arg.equals("q")){
			return true;
		} else {
//			System.out.println(arg);
			String[] args 	= arg.split("\\s++");
			String cmd 		= args[0];

			if(cmd.equals("stem")){
				if(args.length != 2){
					/* Wrong number of arguments */
					System.out.println("Wrong number of argument(s)");
				} else {
					/* Print stemmed token */
					System.out.println(PorterStemmer.processToken(args[1]));
				}
			} else if (cmd.equals("index")){
				/* Index the folder specified by directoryname and then begin querying it. */
			} else if (cmd.equals("vocab")){
				/* Print all terms in the vocabulary of the corpus, one term per line.
				 * Then print the count of the total number of vocabulary terms. */
				ArrayList<String> vocab = PII.getVocab();
				for(String v : vocab){
					System.out.println(v);
				}
				System.out.println("Total number of vocabulary terms: " + vocab.size());

			} else {
				System.out.println("Command Not Found.");
			}
		}
		return false;
	}
}
