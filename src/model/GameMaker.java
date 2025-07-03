package model;

import model.bit.BitBoard;
import model.evaluationFunction.EvaluationFunction;
import model.evaluationFunction.SuperEvaluationFunction;
import model.player.*;
import view.UserInput;

public class GameMaker {
    Board blueBoard;
    Board redBoard ;
    EvaluationFunction blueEvaluationFunction;
    EvaluationFunction redEvaluationFunction;
    UserInput userInput;
    Player blue;
    Player red;
    double playTime;
    Game game;

    public GameMaker(PlayerType blueType, PlayerType redType, double playTime) {
        this.blueBoard = new BitBoard("tttttttt/8/8/8/8/8/TTTTTTTT");
        this.redBoard = new BitBoard("tttttttt/8/8/8/8/8/TTTTTTTT");
        this.blueEvaluationFunction = new SuperEvaluationFunction(blueBoard);
        this.redEvaluationFunction = new SuperEvaluationFunction(redBoard);
        this.userInput = new UserInput();
        this.playTime = playTime;
        this.blue = playerMaker(true, blueType);
        this.red = playerMaker(false, redType);
        this.game = new Game(userInput, red, blue, "tttttttt/8/8/8/8/8/TTTTTTTT,b", playTime);
        startGame();

    }

    public Player playerMaker(boolean isBlue, PlayerType type){
        if (isBlue){
            return switch (type) {
                case USER -> new User(true, blueBoard, blueEvaluationFunction, userInput,(playTime * 1000));
                case EASY -> new EasyPlayer(true, blueBoard, blueEvaluationFunction, (playTime * 1000));
                case MEDIUM -> new MediumPlayer(true, blueBoard, blueEvaluationFunction, (playTime * 1000));
                case CAESAR -> new Caesar(true, blueBoard, blueEvaluationFunction, (playTime * 1000));
                default -> new Vercingetorix(true, blueBoard, blueEvaluationFunction, (playTime * 1000));
            };
        }
        else {
            return switch (type) {
                case USER -> new User(false, redBoard, redEvaluationFunction, userInput,(playTime * 1000));
                case EASY -> new EasyPlayer(false, redBoard, redEvaluationFunction, (playTime * 1000));
                case MEDIUM -> new MediumPlayer(false, redBoard, redEvaluationFunction, (playTime * 1000));
                case CAESAR -> new Caesar(false, redBoard, redEvaluationFunction, (playTime * 1000));
                default -> new Vercingetorix(false, redBoard, redEvaluationFunction, (playTime * 1000));
            };
        }
    }

    public void startGame(){
        this.game.playGame();
    }

}
