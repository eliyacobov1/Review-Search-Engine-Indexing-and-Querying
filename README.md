# Review Sarch Engine- Indexing and Querying

1. ## **General Explanation and Diagram**
*Our index has 3 parts:*

1) *Dictionary*
1) *inverted inverted index file*
1) *review metadata file*

***Dictionary:***

*Our dictionary holds 3 main components and a few more primitive data members for different purposes. We choose to use the k-1 in k encoding for out dictionary.* 

1. *The first component of the dictionary is a concatenated String with k-1 in k encoding format. i.e the vocabulary found in the input is sorted alphabetically and split into blocks of k words. The first word of each block is written fully, and the following k-1 words are written only with the separating suffix from the previous word.* 
1. *The second component is an array with of pointers to the beginning of the blocks in the concatenated String (pointers to the begging of the first word of the blocks). The size of this array is the number of blocks in the concatenated string (|V|/k).* 
1. *The third component is another array for storing the needed information (word frequency, word length, pointer to inverted index, size of shared prefix with previous word). To be precise the first word in each block has 3 entries: [frequency, length, pointer to inverted index], last word in the block has 3 entries: [frequency, prefix size, pointer to inverted index], and all words in middle of block have 4 entries:  [frequency, length, , prefix size, pointer to inverted index]. So the size of this array is amount\_of\_blocks\*(3+4+3)*
1. *There are a few more integers stored in the dictionary for fast retravel and assistance with reading files from disk.*

***Inverted Index:***

*Our inverted index is a random-access file. The inverted index holds for each word in the vocabulary (of our input) a sequence of numbers representing the reviewIds the word appeared in, and the number of times the word appeared in that review (id1, frequency1, id2, freqency2…). The sequence is encoded with gap encoding (i.e instead of saving the whole id we save the first one and then the difference between previous id and next id). Afterwards the posting list is encoded with Delta encoding for compression and only then written to the inverted index file. So, in practice this is just a file with sequence of 0’s and 1’s. We store in the dictionary the pointer to the beginning of the posting list of each word and when reading from the inverted index we preform decoding of the same process described above. The inverted Index sits on the disk and is read from on demand.*

***Review Metdata File:***

***Dictionary***

*Concatenated string*

*“abcdefghijklmnopqrtuvwxyz….”*

*blockArr – [0,5,12,…]*

*Dictionary data array-*

*[freq1, len1, pointer1,*

*Freq2, len2,  prefix2, pointer2*

*Freq3, len3,  prefix3, pointer3*

*⋮*

*FreqK, prefixK, pointerK,*

*⋮*

*Freq|V|, prefix|V|, pointer|V|]*

***Inverted Index***

*‘word\_1 posting list and frequencies in delta coding’ ‘word\_2 posting list and frequencies in delta coding’ ‘word\_3 posting list and frequencies in delta coding’……..*


***Metadata File***

*‘review\_1’s metadata encoded’ ‘review\_2’s metadata encoded’* 

*‘review\_3’s metadata encoded’ ‘review\_4’s metadata encoded’* 

*‘review\_4’s metadata encoded’ …..*

*.*


` `*Like the inverted index also a random-access file. This file holds for each review from the input it’s metadata: the product id that the review was about, the score of the review, the helpfulness of the review and the length of its text. The data is stored in binary where the product id is broken into chars and each char is written with 1 byte (since we know the bounds of the ascii values that compose productIds), 1 byte for score, 2 bytes for helpfulness and 2 bytes for length, total 15 bytes per review. The constant size of entries allows us to access the metadata of a review without needing to save a pointer to its location.*

--
\*
## **2	Main Memory Versus Disk**
*When an IndexReader is created the dictionary is read into the memory, the inverted index file and metadata file stay on disk and are read from as needed*
## **3	Theoretical Analysis of Size**
*Theoretically analyze the expected size (in bytes) of all of your index structures. In your analysis, the size of the index should be a function of the size of the input. Use the following variables to denote the various input size parameters:*

*N Number of reviews*

*M Total number of tokens (counting duplicates as many times as they appear)*

*D Number of different tokens (counting duplicates once)*

*L Average token length (counting each token once)*

*F Average token frequency, i.e., number of reviews containing a token You can S Average uncommon suffix length*

*R Average number of reviews for each productId*

*k - factor for k-1 in k folding of our dictionary (we chose k=8 but for easier understanding of calculations we will use ‘k’)*

***Dictionary:***

*Since the dictionary is relatively small (compared to the size the inverted index can get to), we decided to spend most of our compressing efforts on the inverted index. So when the dictionary is written to the disk it is done by simply writing ints and chars into a file, without any compression methods. Hence, the size of the dictionary when is on disk or when in main memory are similar – up to constant difference.*  

*Block array – We have D/k blocks, for each block a pointer to its beginning – 4 Bytes per pointer. Total:  Dk\*4*

*Dictionary data array – We have D/k entries of first words of blocks, D/k entries of last words of block, and D – 2D/k entries of words in middle of block. Each entry in the beginning or end of a block has 3 values (as explained above), 4 bytes per value – 12. Each mid-block entry has 4 values -4 bytes per value  - 16. In Total: 2Dk\*12+D-2Dk\*16*

*Concatenated String – D/k word are written fully, D-Dk words only the suffix is written. 2 bytes per char. Total: 2\*Dk\*L+D-Dk\*S*

*Additional data – constant amount of ints stored – not depending on input size, and java object overhead like object headers, array lengths, padding, etc…*

*Total Dictionary size:*

` `*ODk\*4+2Dk\*12+D-2Dk\*16+2\*Dk\*L+D-Dk\*S*

***Metadata File:***

*For each review we have a product ID string with length of 10 chars – we used 1 byte for storing each char (instead of the default of 2), 1 byte for the score, 1 for helpfulness numerator, 1 for helpfulness dominator, and 2 for length of the text of the review. Total: 10+1+1+1+2\*N=15N* 

***InvertedIndex File:***

*First, let us notice that for a given number n, the delta encoding of n takes: (**2\*log(log(n))+1)**+(**log(n)-1)** (where the left argument is the amount of bit required for the gamma encoding of the length of n in binary representation of n, and the right argument is the length of the binary representation of n without the MSB). The sum of these arguments is **2\*log(log(n)**+**log(n).** Next, since the number of different tokens in D, and the average token frequency is F, using the calculation shown above it can be concluded that the total size of the inverted index is:*

` `*2\*D\*F\*( log(log(N/F)+log(N/F))***  

*(D words, each with average of F values in the inverted index, and N/F average value of difference between the different review indexes, and 2 values for each review- the review id and the number of occurrences in that review.)*

## **4	Theoretical Analysis of Runtime**
*Using the same variables, theoretically analyze the runtime of the functions of IndexReader. Include both the runtime and a short explanation.*

- IndexReader(String dir):

In this function we load the dictionary from the disk into the main memory for fast access to the index. This is done in a straightforward manner of reading the data written into a file in the given directory. So the runtime of this function is determined by the size of the dictionary as described above. *ODk\*4+2Dk\*12+D-2Dk\*16+2\*Dk\*L+D-Dk\*S*

- getProductId(int reviewId):

Constant time – depending mainly on the time it takes to read from the disk. 

We calculate the offset in the metadata file by REVIEW\_DATA\_SIZE \* (REVIEWID-1), where REVIEW\_DATA\_SIZE = 15. Then read from that location of the metadata file.

- getReviewScore(int reviewId):

Constant time – depending mainly on the time it takes to read from the disk.

We calculate the offset in the metadata file by REVIEW\_DATA\_SIZE \* (REVIEWID-1), where REVIEW\_DATA\_SIZE = 15. Then read from that location of the metadata file.

- getReviewHelpfulnessNumerator(int reviewId):

Constant time – depending mainly on the time it takes to read from the disk.

We calculate the offset in the metadata file by REVIEW\_DATA\_SIZE \* (REVIEWID-1), where REVIEW\_DATA\_SIZE = 15. Then read from that location of the metadata file.

- getReviewHelpfulnessDenominator(int reviewId):

Constant time – depending mainly on the time it takes to read from the disk.

We calculate the offset in the metadata file by REVIEW\_DATA\_SIZE \* (REVIEWID-1), where REVIEW\_DATA\_SIZE = 15. Then read from that location of the metadata file.

- getReviewLength(int reviewId):

Constant time – depending mainly on the time it takes to read from the disk.

We calculate the offset in the metadata file by REVIEW\_DATA\_SIZE \* (REVIEWID-1), where REVIEW\_DATA\_SIZE = 15. Then read from that location of the metadata file.

- getTokenFrequency(String token):

To do this we decided not to store this extra data separately but rather to use the fact we stored the list of reviewIds ‘token’ was in and to compute the length of the list. So first we need to find the pointer to the inverted index, for this we do binary search on the dictionary (in memory). The search is done in 2 stages: first find the block where token should be (if it is indeed in the dictionary). Then search the block, each time reconstructing the word form the common prefix and the written suffix and comparing with token. So, first stage takes O(log(D/k)), and second stage O(k\*time\_of\_string\_compare). Then can find the correct place in the dictionary where we have the pointer to the inverted index – read the needed bytes (reading from the beginning of token’s posting list to the beginning of the next word’s posting list). Then we must decode from the delta codding, otherwise it is a meaningless sequence of 0’s and 1’s. Finally, we have the posting list and now we compute and return the length of the list (divided by 2 to ignore the review-frequency). 

\*We are aware this is not the most efficient why the achieve this data. A simple alternative that would be more efficient in runtime would be the store this data in the dictionary saving us the need to read from the disk and decode the list. The price would be increasing the size of the in-memory dictionary structure by D\*4 Bytes (if simply storing this data as an int).

Total: *OlogDkfind block+k\*time\_of\_string\_comparefind in block+accecs\_to\_diskread from disk+Fdelta decoding timeof posting list* 

- getTokenCollectionFrequency(String token):

We stored this data in the dictionary, so all me must do is find the location of the word, i.e what block is it in and what is the offset of the word in the block. The why we do this is described in detail in getTokenFrequency above. After we know what block and the offset we calculate the location in Dictionary data array where we stored the token’s collection frequency.

Total: *O(logDk+k\*time\_of\_string\_compare)* 

- getReviewsWithToken(String token):

The operation of this function is very similar to that of getTokenFrequency only that here we have another step after decoding the delta coding we also decode the gap encoding of the posting list, this takes *OF*. 

Total:  *OlogDkfind block+k\*time\_of\_string\_comparefind in block+accecs\_to\_diskread from disk+Fdelta decoding timeof posting list+Fgap decoding*

- getNumberOfReviews():

Constant runtime. This is a data member of our dictionary, so we simply return its value.

- getTokenSizeOfReviews():

Constant runtime. This is a data member of our dictionary, so we simply return its value.

- getProductReviews(String productId):

To deal with this challenge we decided to treat productIds as tokens, leading us to a simple solution for retrieving this data, simply running the same algorithm as in getReviewsWithToken only here returning only the reviewIds without the frequency. Same runtime as get ReviewsWithToken only here use the variable R instead of F and we have an extra *O(R)* for taking only the reviewIds without the frequencies. 

The 2 main downsides to this approach are 1) we store in our inverted index redundant information – for each review of productId we have a value for the frequency of product id in the review even though we don’t need this, but lucky for us this is only 1 bit (for the count of 1) and when returning the data from this function we must take into account the frequency count and remove it before returning the enumeration. 2) We are working under the assumption that productId’s don’t appear in the review text (or at least don’t appear in texts of reviews that aren’t about productId). This seems like a safe assumption.

We might decide to change this in the future. The alternative we though of was the have a separate invertedIndex file for the productId’s posting list storing only the reviewIds. For this we would also need a separate dictionary structure similar to the one we use for the regular index (i.e concatenated string, blockarray, etc).

Total: *OlogDkfind block+k\*time\_of\_string\_comparefind in block+accecs\_to\_diskread from disk+Rdelta decoding timeof posting list+Rgap decoding*

