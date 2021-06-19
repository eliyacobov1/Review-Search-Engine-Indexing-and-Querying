package webdata;

public class DocScore implements Comparable<DocScore>{
    private int id = 0;
    private Double score = 0.0;

    public DocScore(int id, double score){
        this.id = id;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(DocScore other) {
        return this.score.compareTo(other.score);
    }
}
