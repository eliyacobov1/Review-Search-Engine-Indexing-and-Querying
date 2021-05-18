package webdata;

public class IntPair implements Comparable<IntPair>
{
    int termId;
    int docId;

    public IntPair(int termId, int docId)
    {
        this.termId = termId;
        this.docId = docId;
    }

    void setVals(int termId, int docId){
        this.termId = termId;
        this.docId = docId;
    }


    @Override
    public int compareTo(IntPair other) {
        if (this.termId < other.termId) {
            return -1;
        }

        if (this.termId == other.termId)
        {
            return Integer.compare(this.docId, other.docId);
        }
        return 1;
    }
}