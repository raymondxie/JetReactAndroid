package club.makeable.jetreactscore;

/**
 * Created by yuhxie on 10/26/16.
 */

public class GameScore implements Comparable<GameScore> {

    int record;
    String name;
    int score;

    public GameScore(int recId, String name, int score) {
        this.record = recId;
        this.name = name;
        this.score = score;
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    @Override
    public int compareTo(GameScore another) {
        // descending order
        return another.getScore() - this.score;
    }
}

