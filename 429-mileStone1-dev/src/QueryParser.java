import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {

	private DocumentReader docReader;
	private PositionalInvertedIndex PII;
	private Scanner scanner;

	public QueryParser(DocumentReader reader, PositionalInvertedIndex p){

		docReader 	= reader;
		PII 		= p;
		scanner = new Scanner(System.in);

	}

	/**
	 *	Start accepting user input and parsing queries
	 *	@return None
	 */
	public void run(){

		String line;
		System.out.print("Enter querie: ");
		while(scanner.hasNextLine()){
			line = scanner.nextLine();
			if(line.length() == 0){
				System.out.println("Empty Query");
				continue;
			}

			if(line.charAt(0) == ':'){
				/* Check for special query */

				if(line.length() == 1){
					/* empty arguments*/

					System.out.println("empty argument");
				} else if (specialQuerie(line)){ // True = exit
					/* Special Querie*/

					System.out.println("Exit Program");
					break;
				}
			} else{
				/* Normal Query */

				String[] ORQueries = line.split("\\+"); // Split by OR to merge those first
				ArrayList<Posting> QList = new ArrayList<Posting>();
				for(String q : ORQueries){
					QList.add(querie(q.replaceAll("^\\s+", ""))); // get rid of leading space
				}

				/* Do ORMerge with all QList*/


				for(Posting p : QList){
					if(p == null){
						continue;
					}
					for(PositionMap pm : p.mPositionMapArr){
						System.out.println(pm.mDocID);
					}
				}


//				ArrayList<String> list = new ArrayList<String>();
//				Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(line);
//				while (m.find()){
//					list.add(m.group(1));
//				}
//
//
//				for(String s: list){
//					System.out.println(s); // Add .replaceAll("\"", "") to remove surrounding quotes.
//				}
//				querie(list);

			}

			System.out.print("Enter querie: ");
		}
	}

	/**
	 * Get posting and do positionalMerge if it's a phase query
	 * @param q
	 * @return posting
	 */
	public Posting querie(String q){

		Posting rePosting = null;

		ArrayList<String> wordList = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(q);
		while(m.find()){
			wordList.add(m.group(1));
		}

		for(String s: wordList){
//			System.out.println(s.replaceAll("\"", ""));
			if(s.contains("\"")){
				/* Phase Query */

			} else {
				/* Word Query */
				Posting p = PII.getPosting(s);
				if(rePosting == null){
					rePosting = p;
				} else {
					rePosting.merge(p);
				}
			}

			/**
			 * For each string in this list, get posting and do positionial merge
			 */
		}

		return rePosting;

//		ArrayList<PositionalInvertedIndex.Posting> posting = new ArrayList<PositionalInvertedIndex.Posting>();
//		while(qLiteral.size() > 0){
//			String literal = qLiteral.remove(qLiteral.size() - 1);
//
//			if(literal.contains(" ")){
//				/* Phase Query */
//				literal = literal.replaceAll("\"", "");
//				String[] phase = literal.split("\\s+");
//			} else {
//				/* IFF the literal is a word */
//				posting.add(PII.getPosting(literal));
//
//			}
//			// if literal is a word, do querie
//		}
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
