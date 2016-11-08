import java.util.HashMap;

public class RankRetrievalsObject {
	// The whole query
	String mQuery;
	// Reference to the in-use PII
	PositionalInvertedIndex mPII;
	// Reference to the DocumentReader object
	DocumentReader mDocReader;
	// Accumulator HashMap for storing the score of each document for the query
	HashMap<Integer, Float> accumulatorHM;
	public RankRetrievalsObject() {
		accumulatorHM	= new HashMap<Integer, Float>();	// Map docID ---> A(docID), The accumulator score for each doc for term in query
	}
	
	public void setQuery(String pQuery) {
		mQuery = pQuery;
	}
	
	public void setPII(PositionalInvertedIndex pPII) {
		mPII = pPII;
	}
	
	public void setDocReader(DocumentReader pDocReader) {
		mDocReader = pDocReader;
	}
	
	public void processQuery() {
		String[] queriesArr = mQuery.split("\\+");
		for (String eachTerm : queriesArr) {
			float weightOfTermInQuery 	= getWeightOfTermInQuery(eachTerm); 				// W(q,t), Importance of the term in the query, W(q,t) = ln( 1 + (N/df(t) )
			Posting[] postingList 		= mPII.getListOfPosting(eachTerm);					// Get the Postings list of the term
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
	}
	
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
		int docFreq = mPII.getListOfPosting(pTerm).length;					// df(t) - Document frequency of the term (how many documents contain the term)
		int N 		= mDocReader.size();									// N - the total number of documents in collection
		float weightOfTermInQuery = (float) Math.log(1 + (N / docFreq));	// ln( 1 + (N / df(t)) )
		return weightOfTermInQuery;
	}
}
