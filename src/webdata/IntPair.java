package webdata;

public class IntPair implements Comparable<IntPair>
{
    public final int termId;
    public final int docId;

    public IntPair(int termId, int docId)
    {
        this.termId = termId;
        this.docId = docId;
    }



    @Override
    public int compareTo(IntPair other)
    {
        if (this.termId < other.termId)
        {
            return -1;
        }

        if (this.termId == other.termId)
        {
            if (this.docId < other.docId)
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }

        else
        {
            return 1;
        }
    }
}
