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
		accumulatorHM	= new HashMap<Integer, Float>();					// Map docID ---> A(docID), The accumulator score for each doc for term in query
		PQsort pqs 		= new PQsort();										// The Comparator for the priority queue
		topDocOnScorePQ 	 	= new PriorityQueue<DocAndScorePair>(pqs);	// Construct a priority queue to rank the document by the score L(d)
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
		String[] queriesArr = pQuery.split("\\s+");
		for (String eachTerm : queriesArr) {
			// W(q,t), Importance of the term in the query, W(q,t) = ln( 1 + (N/df(t) )
			float weightOfTermInQuery 	= getWeightOfTermInQuery(eachTerm);
			Pair<Integer>[]	postings 	= mDII.getDocListPosting(eachTerm);
			// Get the Postings list of the term
			Posting[] postingList 		= new Posting[postings.length];
			for(int i = 0; i < postings.length; i++){
				postingList[i] = new Posting(postings[i].getFirst());
			}
			// For each document in t's posting lists
			for (Posting eachPost : postingList) {
				// Get the size of position array (tells us the # of occurrence of term in the doc
				int termFreqInDoc 		= 	eachPost.getSizeOfPositionArray();				// tf(t,d)
				float weightOfTermInDoc;
				if (termFreqInDoc == 0) {
					weightOfTermInDoc 	= 	0.0f;											// If termFreqInDoc of term is 0 (no term occur in document), weight is 0
				}
				else {
					weightOfTermInDoc = 	(1 + (float)Math.log(termFreqInDoc));			// W(d,t) = 1 + ln(tf(t,d))
				}
				float result			= 	(weightOfTermInDoc * weightOfTermInQuery);		// W(d,t) X W(q,t)
				addAccumulator(eachPost.mDocID, result);									// A(d) += W(d,t) X W(q,t)
			}

		}


		// Now Diving A(d) by L(d) for each non-zero A(d)
		for (Map.Entry<Integer, Float> eachEntry : accumulatorHM.entrySet()) {
			int docID				= eachEntry.getKey();
			float accumulator 		= eachEntry.getValue();										// A(d)
			if (accumulator != 0.0f) {
				double weightOfDoc 	= 	mDII.getDocWeight(eachEntry.getKey());					// L(d)
				float result 		=	(float) (accumulator / weightOfDoc);					// A(d) / L(d)
				topDocOnScorePQ.offer(new DocAndScorePair(docID, result));							// Add the DocAndScorePair to the priortyQueue (ranked by the score)
				accumulatorHM.put(eachEntry.getKey(), result);
			}
		}


		// Print the top ten document
		int rank = 1;
		while(!topDocOnScorePQ.isEmpty()) {
			if (rank > 10) {
				break;
			}
			else {
				DocAndScorePair anObj = topDocOnScorePQ.poll();
				System.out.print(rank + ". " + anObj.mDocID + "with weight: " + anObj.mScore);
			}
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
	 * Return the weight of the term in the query W(q,t)
	 * Using the formula W(q,t) = ln(1 + (N / dt(t)) )
	 * @param pTerm
	 * @return W(q,t)
	 */
	private float getWeightOfTermInQuery(String pTerm) {
		// The the document frequency - counting the size of the posting of the term
		int docFreq = mDII.getDocListPosting(pTerm).length;						// df(t) - Document frequency of the term (how many documents contain the term)
		int N 		= mDII.getFileName().size();							// N - the total number of documents in collection
		float weightOfTermInQuery = (float) Math.log(1 + (N / docFreq));	// ln( 1 + (N / df(t)) )
		return weightOfTermInQuery;
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