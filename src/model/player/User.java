package model.player;

import model.Board;
import model.evaluationFunction.EvaluationFunction;
import model.move.Move;
import model.move.MoveGenerator;
import view.UserInput;

public class User extends Player{
    UserInput userInput;

    public User(boolean isBlue, Board board, EvaluationFunction evaluationFunction, UserInput userInput, double totalTime) {
        super(isBlue, board,evaluationFunction);
        this.userInput = userInput;
        this.remainingTime = totalTime / 1000;
    }

    public User(boolean isBlue, Board board, MoveGenerator moveGenerator, EvaluationFunction evaluationFunction, UserInput userInput, double totalTime) {
        super(isBlue, board, moveGenerator, evaluationFunction);
        this.userInput = userInput;
        this.remainingTime = totalTime / 1000;
    }

    @Override
    public Move decideMove() {
        this.userInput.setPlayer(this);
        userInput.setChoosing(true);
        return userInput.decideMove();
    }


}
