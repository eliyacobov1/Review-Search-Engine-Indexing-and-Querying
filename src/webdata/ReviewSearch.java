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
     * Returns a list of the id-s of the k most highly ranked reviews for the
     * given query, using the language model ranking function, smoothed using a
     * mixture model with the given value of lambda
     * The list should be sorted by the ranking
     */
    public Enumeration<Integer> languageModelSearch(Enumeration<String> query, double lambda, int k) {
        HashMap<Integer, Double> reviewProbabilities = new HashMap<>();
        HashMap<Integer, Integer> sizeReviews = new HashMap<>();
        HashMap<String, Double> tokenCorpusFrequencies = new HashMap<>();

        /* iterate over all query tokens */
        while(query.hasMoreElements()){
            String token = query.nextElement();
            Enumeration<Integer> tokenReviews = ir.getReviewsWithToken(token);
            double tokenCorpusFreq = (1-lambda);
            if(tokenCorpusFrequencies.containsKey(token)) tokenCorpusFreq *= tokenCorpusFrequencies.get(token);
            else{
                tokenCorpusFreq *= (double)ir.getTokenCollectionFrequency(token) / totalNumTokens;
                tokenCorpusFrequencies.put(token, tokenCorpusFreq);
            }

            /* iterate over all reviews containing the current token */
            while(tokenReviews.hasMoreElements()){
                int reviewID = tokenReviews.nextElement();
                int freq = tokenReviews.nextElement();
                double prob = tokenCorpusFreq;
                if(sizeReviews.containsKey(reviewID)) prob += lambda * ((double)freq / sizeReviews.get(reviewID));
                else{
                    int reviewLength = ir.getReviewLength(reviewID);
                    prob += lambda * ((double)freq / reviewLength);
                    sizeReviews.put(reviewID, reviewLength);
                }
                if(reviewProbabilities.containsKey(reviewID))
                    reviewProbabilities.put(reviewID, reviewProbabilities.get(reviewID) + Math.log10(prob));
                else
                    reviewProbabilities.put(reviewID, Math.log10(prob));
            }
        }
        return Utils.getTopKeysFromHashMap(k, reviewProbabilities);
    }
}
