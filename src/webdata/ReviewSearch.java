package webdata;

import java.util.*;

public class ReviewSearch {
    private final IndexReader ir;
    private final int totalNumTokens;

    /**
     * Constructor
     */
    public ReviewSearch(IndexReader iReader) {
        ir = iReader;
        totalNumTokens = ir.getTokenSizeOfReviews();
    }

    /**
     * compute the log term frequency of the given tf
     */
    private double logTermFreq(int rawTf){
        return 1 + Math.log10(rawTf);
    }

    /**
     * compute the standard document frequency of the given df
     */
    private double logDocFreq(int rawIdf){
        return Math.log10((double)ir.getNumberOfReviews()/rawIdf);
    }

    /**
     * computes the vector multiplication between 2 given vectors
     * @return Array list, with the result of vector multiplication of inputs
     */
    private static ArrayList<Double> vectorMul(ArrayList<Double> v1, ArrayList<Double> v2) {
        ArrayList<Double> result = new ArrayList<>(v1.size());
        for (int i = 0; i < v1.size(); i++){
            result.set(i, v1.get(i)* v2.get(i));
        }
        return result;
    }

    /**
     * computes the dot product between 2 given vectors
     * @return double, result of dot product of inputs
     */
    private static double dotProduct(ArrayList<Double> v1, ArrayList<Double> v2){
        double result = 0;
        for (int i = 0; i < v1.size(); i++){
            result += v1.get(i)* v2.get(i);
        }
        return result;
    }

    /**
     * computes the l2 norm of the given vector
     */
    private static double vectorNorm(ArrayList<Double> vector)
    {
        double res = 0;
        for (double val : vector){
            res += Math.pow(val, 2);
        }
        return Math.sqrt(res);
    }

    /**
     * normalizes the given vector with l2 norm
     */
    private static void normalizeVector(ArrayList<Double> vector)
    {
        double norm = vectorNorm(vector);
        // TODO: maybe problem to change vector on the run and in place
        for (int i=0; i < vector.size(); i++){
            vector.set(i, vector.get(i)/norm);
        }
    }

    /**
     * take a posting list of form id-i, freq-i, id-j, freq-j,...
     * and returns only the ids
     * @param resultsForTerm array list of ints with form (id-i, freq-i, id-j, freq-j,...
     * @return array list of ints with the doc ids form the given posting list
     */
    private static ArrayList<Integer> getDocIds(ArrayList<Integer> resultsForTerm)
    {
        ArrayList<Integer> docIds = new ArrayList<>();
        for (int i=0; i < resultsForTerm.size(); i+=2){
            docIds.add(resultsForTerm.get(i));
        }
        return docIds;
    }

    /**
     * take a posting list of form (id-i, freq-i, id-j, freq-j,...)
     * and returns only the frequencies
     * @param resultsForTerm array list of ints with form (id-i, freq-i, id-j, freq-j,...)
     * @return array list of ints with the frequencies form the given posting list
     */
    private static ArrayList<Integer> getDocFrequencies(ArrayList<Integer> resultsForTerm)
    {
        ArrayList<Integer> docFreqs = new ArrayList<>();
        for (int i=1; i < resultsForTerm.size()-1; i+=2){
            docFreqs.add(resultsForTerm.get(i));
        }
        return docFreqs;
    }

    /**
     * returns a copy of the given list without duplicates
     */
    private static ArrayList<String> removeDuplicates(List<String> l){
        HashSet<String> noDups = new HashSet<>(l);
        return new ArrayList<String>(noDups);
    }

    /**
     * computes the query vector for the given query using 'ltc' (log tf, standard df, cosine norm)
     * @param fullQuery list of strings with all the words in the query (including duplicates)
     * @param queryTerms list of strings with all the words in the query, sorted (and without duplicates)
     * @return vector of doubles with the values computed for the query
     */
    private ArrayList<Double> getQueryVector(List<String> fullQuery, List<String> queryTerms) {
        HashMap<String, Integer> termFreqInQuery = new HashMap<>();     // term: how many time term in query
        HashMap<String, Integer> termFreqInDocs = new HashMap<>();      // term: how many docs term is in
        Integer prevVal;

        // first pass over query. compute raw tf and raw idf
        for (String term : fullQuery)
        {
            prevVal = termFreqInQuery.putIfAbsent(term, 1);
            if (prevVal != null){
                termFreqInQuery.put(term, ++prevVal);
            }
            termFreqInDocs.putIfAbsent(term, ir.getTokenFrequency(term));
        }

        // compute tf-wt and idf-wt, save into vectors
        ArrayList<Double> tf_wtVector = new ArrayList<>();
        ArrayList<Double> idf_wtVector = new ArrayList<>();
        double tf_wt, idf_wt;

        for (String term : queryTerms){
            tf_wt = logTermFreq(termFreqInQuery.get(term));
            idf_wt = logDocFreq(termFreqInDocs.get(term));
            tf_wtVector.add(tf_wt);
            idf_wtVector.add(idf_wt);
        }

        ArrayList<Double> queryVector = vectorMul(tf_wtVector, idf_wtVector);       // multiply tf-wt*idf-wt
        normalizeVector(queryVector);       // normalize
        return queryVector;
    }

    /**
     * computes the doc vectors for the every doc that contains at least one of the query words using 'lnn' (log tf, no df, no norm)
     * @param sortedQuery list of strings with the words in the query, sorted (without duplicates)
     * @return hashmap<int, arrayList<double>> - for each doc the matches at least one word from query - a vector
     */
    private HashMap<Integer, ArrayList<Double>> getDocVectors(List<String> sortedQuery) {
        double tf_wt;
        HashMap<Integer, ArrayList<Double>> docVectors = new HashMap<>();           // docId: docVector
        HashMap<String, ArrayList<Integer>> termPostingList = new HashMap<>();      // term: posting list of term
        HashMap<String, ArrayList<Integer>> termDocIdList = new HashMap<>();        // term: docIds term is in

        // collect all docs with at least one word from query and initiate vectors
        for (String term : sortedQuery){
            ArrayList<Integer> resultForTerm = Collections.list(ir.getReviewsWithToken(term));
            ArrayList<Integer> docIds = getDocIds(resultForTerm);

            // cache posting list and docIds for later
            termPostingList.putIfAbsent(term, resultForTerm);
            termDocIdList.put(term, docIds);

            // initiate vectors for docs (if doc was seen already it won't be re-initiated)
            for (int docId: docIds){
                docVectors.putIfAbsent(docId, new ArrayList<Double>(sortedQuery.size()));
            }
        }

        int rawTf;
        String term;
        // go over each term from query and update tf in the relevant vectors
        for (int termIdx = 0; termIdx < sortedQuery.size(); termIdx++){
            term = sortedQuery.get(termIdx);
            ArrayList<Integer> docIdsForTerm = termDocIdList.get(term); // matching docIds
            ArrayList<Integer> docFreqsForTerm = getDocFrequencies(termPostingList.get(term)); // matching frequencies

            // go over all docs with this term and update the tf
            for (int docIdx = 0; docIdx < docIdsForTerm.size(); docIdx++){
                rawTf = docFreqsForTerm.get(docIdx);
                tf_wt = logTermFreq(rawTf);
                // get the docId x'th vector, set the termId x'th entry = docFreq
                int docId = docIdsForTerm.get(docIdx);
                docVectors.get(docId).set(termIdx, tf_wt);
            }
        }
        return docVectors;
    }

    /**
     * computes the score for each of the matching docs
     * @param queryVector vector computed for the query
     * @param docVectors hashmap<int, arrayList<int>> mapping docId: vector of document
     * @return List of DocScores (pairs of id, score)
     */
    private ArrayList<DocScore> computeDocScores(ArrayList<Double> queryVector, HashMap<Integer, ArrayList<Double>> docVectors) {
        ArrayList<DocScore> docScores = new ArrayList<>();
        for (int docId: docVectors.keySet()){
            double docScore = dotProduct(queryVector, docVectors.get(docId));
            docScores.add(new DocScore(docId, docScore));
        }
        return docScores;
    }

    /**
     * returns list of k docIds with best scores (if there are less than k docs, returns that amount)
     * @param k wanted amount of docs
     * @param docScores list of DocScores (pairs of id,score)
     * @return list of k docIds with best scores
     */
    private ArrayList<Integer> getKBestDocs(int k, ArrayList<DocScore> docScores) {
        // sort scores
        PriorityQueue<DocScore> sortedDocScores = new PriorityQueue<>(docScores);

        // choose k best
        k = Math.min(k, docScores.size());
        ArrayList<Integer> bestDocs = new ArrayList<>();
        for (int i = 0; i < k; i++){
            DocScore doc = sortedDocScores.poll();
            if (doc == null){
                break;
            }
            bestDocs.add(doc.getId());
        }
        return bestDocs;
    }
    /**
     * Returns a list of the id-s of the k most highly ranked reviews for the
     * given query, using the vector space ranking function lnn.ltc (using the
     * SMART notation)
     * The list should be sorted by the ranking
     */
    public Enumeration<Integer> vectorSpaceSearch(Enumeration<String> query, int k) {
        List<String> fullQuery = new ArrayList<>(Collections.list(query));
        ArrayList<String> queryTerms = removeDuplicates(fullQuery);
        Collections.sort(queryTerms); // sort query to have same order always


        ArrayList<Double> queryVector = getQueryVector(fullQuery, queryTerms);

        HashMap<Integer, ArrayList<Double>> docVectors = getDocVectors(queryTerms);

        ArrayList<DocScore> docScores = computeDocScores(queryVector, docVectors);

        ArrayList<Integer> bestDocs = getKBestDocs(k, docScores);
        return Collections.enumeration(bestDocs);
    }

    private double calcProductScore(int reviewID) {
        int helpfulnessNumerator = ir.getReviewHelpfulnessNumerator(reviewID);
        int helpfulnessDenominator = ir.getReviewHelpfulnessDenominator(reviewID);
        int score = ir.getReviewScore(reviewID);
        if(helpfulnessNumerator != 0 && helpfulnessDenominator != 0){
            return score * ((double)helpfulnessNumerator / helpfulnessDenominator);
        }
        else return score;
    }

    /**
     * Returns a list of the id-s of the k most highly ranked reviews for the
     * given query, using the language model ranking function, smoothed using a
     * mixture model with the given value of lambda
     * The list should be sorted by the ranking
     */
    public Enumeration<Integer> languageModelSearch(Enumeration<String> query, double lambda, int k) {
        List<String> queryTokens = Collections.list(query); // convert query to list to allow multiple iterations
        HashMap<Integer, Double> reviewProbabilities = new HashMap<>();
        HashMap<Integer, Integer> reviewSizes = new HashMap<>();
        HashMap<String, Double> tokenCorpusFrequencies = new HashMap<>();
        HashMap<Integer, HashSet<String>> tokenQueriesInReviews  =new HashMap<>();

        /* iterate over all query tokens */
        for(String token: queryTokens){
            Enumeration<Integer> tokenReviews = ir.getReviewsWithToken(token);

            /* (i) evaluate the term (1-lambda)*(tokenFreq/numTokensInCorpus) */
            double tokenCorpusFreq = (1-lambda);
            if(!tokenCorpusFrequencies.containsKey(token))
                tokenCorpusFrequencies.put(token, (double)ir.getTokenCollectionFrequency(token) / totalNumTokens);
            tokenCorpusFreq *= tokenCorpusFrequencies.get(token);

            /* iterate over all reviews containing the current token */
            while(tokenReviews.hasMoreElements()) {
                int reviewID = tokenReviews.nextElement();
                int freq = tokenReviews.nextElement();

                /* add token to this review's query token-list */
                if(!tokenQueriesInReviews.containsKey(reviewID))
                    tokenQueriesInReviews.put(reviewID, new HashSet<>());
                tokenQueriesInReviews.get(reviewID).add(token);

                /* (ii) evaluate the term lambda*(tokenFrequencyInReview / numTokensInReview) */
                double prob = tokenCorpusFreq;
                if(!reviewSizes.containsKey(reviewID)) reviewSizes.put(reviewID, ir.getReviewLength(reviewID));
                /* the probability for this token is the sum of (i) and (ii) */
                prob += lambda * ((double)freq / reviewSizes.get(reviewID));

                /* add log10(probability) to the sum of probabilities of the current review */
                if(reviewProbabilities.containsKey(reviewID))
                    reviewProbabilities.put(reviewID, reviewProbabilities.get(reviewID) + Math.log10(prob));
                else
                    reviewProbabilities.put(reviewID, Math.log10(prob));
            }
        }

        /* add log((1-lambda)*(tokenFreq/numTokensInCorpus) for each query-token that does not appear in a review */
        for(int reviewID: tokenQueriesInReviews.keySet()){
            for(String token: queryTokens) {
                if(!tokenQueriesInReviews.get(reviewID).contains(token)){
                    double tokenCorpusFreq = (1-lambda) * tokenCorpusFrequencies.get(token);
                    reviewProbabilities.put(reviewID, reviewProbabilities.get(reviewID) + Math.log10(tokenCorpusFreq));
                }
            }
        }

        return Utils.getTopKeysFromHashMap(k, reviewProbabilities);
    }

    /**
     * Returns a list of the id-s of the k most highly ranked productIds for the
     1
     * given query using a function of your choice
     * The list should be sorted by the ranking
     */
    public Collection<String> productSearch(Enumeration<String> query, int k) {
        HashMap<String, Double> productScores = new HashMap<>();

        /* find all reviews that contain tokens form the given query */
        while(query.hasMoreElements()){
            String token = query.nextElement();
            Enumeration<Integer> tokenReviews = ir.getReviewsWithToken(token);
            while(tokenReviews.hasMoreElements()){
                int reviewID = tokenReviews.nextElement();
                int freq = tokenReviews.nextElement();
                String productID = ir.getProductId(reviewID);
                if(!productScores.containsKey(productID)) productScores.put(productID, 0.0);
                productScores.put(productID, productScores.get(productID) + calcProductScore(reviewID));
            }
        }

        return Collections.list(Utils.getTopKeysFromHashMap(k, productScores));
    }
}
