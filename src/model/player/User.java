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
        this.name = "User";
    }

    @Override
    public Move decideMove() {
        this.userInput.setPlayer(this);
        userInput.setChoosing(true);
        return userInput.decideMove();
    }


}
