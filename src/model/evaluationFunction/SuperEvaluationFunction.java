package model.evaluationFunction;

import model.Board;
import model.bit.BitBoard;

public class SuperEvaluationFunction extends EvaluationFunction{
    public SuperEvaluationFunction() {
        super(new BitBoard("tttttttt/8/8/8/8/8/TTTTTTTT,b"), new int[]{0, 1, 0, 0, 1, 6, 0}, new int[]{2, 2, 11, -1}, new int[]{3, 0, 1, 0, 5, 4, 0}, new int[]{6, 8, 13, 3}, new int[]{6, 3, 1, 1, 6, 0, 12, 7}, new int[]{-171, -170, 0, -2, 0, 2, 170, 171}, 24, 40, 2);
    }
    public SuperEvaluationFunction(Board board) {
        super(board, new int[]{0, 1, 0, 0, 1, 6, 0}, new int[]{2, 2, 11, -1}, new int[]{3, 0, 1, 0, 5, 4, 0}, new int[]{6, 8, 13, 3}, new int[]{6, 3, 1, 1, 6, 0, 12, 7}, new int[]{-171, -170, 0, -2, 0, 2, 170, 171}, 24, 40, 2);
    }
}
