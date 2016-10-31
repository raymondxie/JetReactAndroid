package club.makeable.jetreactscore;

/**
 * Created by yuhxie on 10/26/16.
 */

public class GameScore implements Comparable<GameScore> {
    String name;
    int score;

    public GameScore(String name, int score) {
        this.name = name;
        this.score = score;
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

