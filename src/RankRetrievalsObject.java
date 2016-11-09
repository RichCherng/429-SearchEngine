import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class RankRetrievalsObject {
	// Reference to the DiskInvertedIndex
	DiskInvertedIndex mDII;
	// Accumulator HashMap for storing the score of each document for the query
	HashMap<Integer, Float> accumulatorHM;

	// PriorityQueue for keeping the documents with the highest score sorted
	PriorityQueue<DocAndScorePair> topDocOnScorePQ;
	// Class for the Priority Queue - Sort the Object by its score
	static class PQsort implements Comparator<DocAndScorePair> {
		public int compare(DocAndScorePair one, DocAndScorePair two) {
			// Inverse compare, sort by highest score
			return Float.compare(two.mScore, one.mScore);
		}
	}

	public RankRetrievalsObject(DiskInvertedIndex pDII) {
		mDII			= pDII;
		// Map docID ---> A(docID), The accumulator score for each doc for term in query
		accumulatorHM	= new HashMap<Integer, Float>();
		// The Comparator for the priority queue
		PQsort pqs 		= new PQsort();
		// Construct a priority queue to rank the document by the score L(d)
		topDocOnScorePQ 	 	= new PriorityQueue<DocAndScorePair>(pqs);
	}

	/**
	 * Set the reference to the DiskInvertedIndex
	 * @param pDII
	 */
	public void setDiskInvertedIndex(DiskInvertedIndex pDII) {
		mDII = pDII;
	}

	/**
	 * Rank the document based on the terms in the query and print the top 10 document according to the query
	 * @param pQuery - The input query
	 */
	public void processQuery(String pQuery) {
		topDocOnScorePQ.clear();
		accumulatorHM = new HashMap<Integer, Float>();
		String[] queriesArr = pQuery.split("\\s+");
		for (String eachTerm : queriesArr) {
			String stemEachTerm = PorterStemmer.processToken(eachTerm);
			// W(q,t), Importance of the term in the query, W(q,t) = ln( 1 + (N/df(t) )
			float weightOfTermInQuery 	= getWeightOfTermInQuery(stemEachTerm);
			if (weightOfTermInQuery < 0.0f) {
				continue;
			}
			 System.out.printf("W(q,%s): " + weightOfTermInQuery + "\n", eachTerm);
			Pair<Integer>[]	postings 	= mDII.getDocListPosting(stemEachTerm);
			System.out.println(postings.length);

//			// Get the Postings list of the term
//			Posting[] postingList 		= new Posting[postings.length];
//			for(int i = 0; i < postings.length; i++){
//				postingList[i] = new Posting(postings[i].getFirst());
//			}
			// For each document in t's posting lists
			for (Pair eachPair : postings) {
				// Get the size of position array (tells us the # of occurrence of term in the doc
				// tf(t,d)
				int termFreqInDoc 		= 	(int)eachPair.getSecond();
				// System.out.printf("tf(%s, %d): %d\n", eachTerm, eachPair.getFirst(), termFreqInDoc);
				float weightOfTermInDoc;
				if (termFreqInDoc == 0) {
					// If termFreqInDoc of term is 0 (no term occur in document), weight is 0
					weightOfTermInDoc 	= 	0.0f;
				}
				else {
					// W(d,t) = 1 + ln(tf(t,d))
					weightOfTermInDoc 	= 	(1 + (float)Math.log(termFreqInDoc));
					// System.out.printf("W(%d, %s): %f\n", eachPair.getFirst(), eachTerm, weightOfTermInDoc);
				}
//				 A(d) += W(d,t) X W(q,t)
				float result			= 	(weightOfTermInDoc * weightOfTermInQuery);
				// System.out.printf("A(%d), Adding result of: %f to it\n", eachPair.getFirst(), result);
//				System.out.printf("A(%d): %f\n", eachPair.getFirst(), result);
				addAccumulator((int)eachPair.getFirst(), result);
			}

		}


		// Now Diving A(d) by L(d) for each non-zero A(d)
		for (Map.Entry<Integer, Float> eachEntry : accumulatorHM.entrySet()) {
			int docID				= 	eachEntry.getKey();
			// A(d)
			float accumulator 		= 	eachEntry.getValue();
//			 System.out.printf("A(%d): %f\n", docID, accumulator);
			if (accumulator != 0.0f) {
				// L(d)
				double weightOfDoc 	= 	mDII.getDocWeight(eachEntry.getKey());
//				 System.out.printf("L(%d): %f\n", docID, weightOfDoc);
				// A(d) / L(d)
				float result 		=	(float) (accumulator / weightOfDoc);
				// System.out.printf("result: %f\n", result);
				// Add the DocAndScorePair to the priortyQueue (ranked by the score)
				topDocOnScorePQ.offer(new DocAndScorePair(docID, result));
				accumulatorHM.put(eachEntry.getKey(), result);
			}
		}


		if (topDocOnScorePQ.size() == 0) {
			// If there is no result for rank retrieval
			System.out.println("No document found");
		}
		else {
			// Print the top ten document
			int rank = 1;
			while(!topDocOnScorePQ.isEmpty()) {
				if (rank > 10) {
					break;
				}
				else {
					DocAndScorePair anObj = topDocOnScorePQ.poll();
//					System.out.println(rank + ". Document" + anObj.mDocID + " with accumulator value: " + anObj.mScore);
					System.out.println(mDII.getFileName().get(anObj.mDocID) + " with accumulator value: " + anObj.mScore);

					rank++;
				}
			}
		}
	}

	/**
	 * Return the weight of the term in the query W(q,t)
	 * Using the formula W(q,t) = ln(1 + (N / dt(t)) )
	 * @param pTerm
	 * @return W(q,t)
	 */
	private float getWeightOfTermInQuery(String pTerm) {
		// The the document frequency - counting the size of the posting of the term
		// df(t) - Document frequency of the term (how many documents contain the term)
		// If the term does not exist in the query
		if (mDII.getDocListPosting(pTerm) == null) {
			return -5.0f;
		}
		else {
			int docFreq = mDII.getDocListPosting(pTerm).length;
			System.out.print("docFreq: " + docFreq);
			// System.out.printf("df(%s) : %d\n", pTerm, docFreq);
			// N - the total number of documents in collection
			int N 		= mDII.getFileName().size();
			 System.out.printf("N: %d\n", N);
			// ln( 1 + (N / df(t)) )
			float weightOfTermInQuery = (float) Math.log1p((float)N / (float)docFreq);
			return weightOfTermInQuery;
		}
	}

	/**
	 * Update the accumulator HashMap that contains documentID and its score
	 * @param pDocID
	 * @param pValue
	 */
	private void addAccumulator(int pDocID, float pValue) {
		if (accumulatorHM.containsKey(pDocID)) {
			accumulatorHM.put(pDocID, accumulatorHM.get(pDocID) + pValue);
		}
		else {
			accumulatorHM.put(pDocID, pValue);
		}
	}

	/**
	 * Pair Class that maps the documentID with the score (A(d))
	 * @author LeafChernchaosil
	 *
	 */
	class DocAndScorePair {
		int mDocID;
		float mScore;
		public DocAndScorePair(int pDocID, float pScore) {
			mDocID = pDocID;
			mScore = pScore;
		}
	}
}