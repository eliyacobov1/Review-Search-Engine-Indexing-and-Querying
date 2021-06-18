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

    private double calcProductScore(int reviewID) {
        int helpfulness = ir.getReviewHelpfulnessNumerator(reviewID); // TODO which component of the helpfulness should we use
        int score = ir.getReviewScore(reviewID);
        return score * helpfulness;
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
//        HashSet<Integer> queryReviews = new HashSet<>();
        HashMap<String, Double> productScores = new HashMap<>();

        /* find all reviews that contain tokens form the given query */
        while(query.hasMoreElements()){
            String token = query.nextElement();
            Enumeration<Integer> tokenReviews = ir.getReviewsWithToken(token);
            while(tokenReviews.hasMoreElements()){
                int reviewID = tokenReviews.nextElement();
                int freq = tokenReviews.nextElement(); // TODO decide if frequency should be a part of score calculation
//                queryReviews.add(reviewID);
                String productID = ir.getProductId(reviewID);
                if(!productScores.containsKey(productID)) productScores.put(productID, 0.0);
                productScores.put(productID, productScores.get(productID) + calcProductScore(reviewID));
            }
        }

        return Collections.list(Utils.getTopKeysFromHashMap(k, productScores));
    }
}
