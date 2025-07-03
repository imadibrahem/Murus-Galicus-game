package model;

public class Starter {
    PlayerType blueType;
    PlayerType redType;
    double playTime;

    public Starter(PlayerType blueType, PlayerType redType, double playTime) {
        this.blueType = blueType;
        this.redType = redType;
        this.playTime = playTime;
    }
    public void makeGame(){
        new GameMaker(blueType, redType, playTime);
    }
}
