package model;

public class TimeManager {
    private final double totalTime;
    private final double[] moveTimes;
    private final int peakMove;
    private final int earlyGameMoves;
    private final int midGameMoves;
    private final double earlyFactor;
    private final double midFactor;
    private final double endFactor;
    private double remainingTime;

    public TimeManager(double totalTime, int peakMove, int midGameMoves, double earlyFactor, double midFactor, double endFactor) {
        this.totalTime = totalTime;
        this.peakMove = peakMove;
        this.earlyGameMoves = peakMove * 2;
        this.midGameMoves = midGameMoves;
        this.earlyFactor = earlyFactor;
        this.midFactor = midFactor;
        this.endFactor = endFactor;
        this.moveTimes = new double[midGameMoves + earlyGameMoves];
        this.remainingTime = totalTime;
        distributeTime();
    }

    public TimeManager(double totalTime) {
        this.totalTime = totalTime;
        this.peakMove = 7;
        this.earlyGameMoves = peakMove * 2;
        this.midGameMoves = 16;
        this.earlyFactor = 0.75;
        this.midFactor = 0.2;
        this.endFactor = 0.2;
        this.moveTimes = new double[midGameMoves + earlyGameMoves];
        this.remainingTime = totalTime;
        distributeTime();
    }

    private void distributeTime() {
        double earlyTime = totalTime * earlyFactor;
        double[] tempEarlyTimes = new double[earlyGameMoves];
        double sumEarly = 0;
        for (int i = 0; i < earlyGameMoves; i++) {
            if (i < peakMove) {
                tempEarlyTimes[i] = i + 1;
            } else {
                tempEarlyTimes[i] = earlyGameMoves - i;
            }
            sumEarly += tempEarlyTimes[i];
        }
        double scaleFactor = earlyTime / sumEarly;
        for (int i = 0; i < earlyGameMoves; i++) {
            moveTimes[i] = tempEarlyTimes[i] * scaleFactor;
        }
        double midGameTime = totalTime * midFactor;
        double midMoveTime = midGameTime / midGameMoves;

        for (int i = earlyGameMoves; i < (earlyGameMoves + midGameMoves); i++) {
            moveTimes[i] = midMoveTime;
        }
    }

    public double getTimeForMove(int moveNumber) {
        double usedTime;
        if (moveNumber < moveTimes.length && moveTimes[moveNumber] > 0) {
            usedTime = Math.min(moveTimes[moveNumber], remainingTime);
            return usedTime;
        }
        usedTime = remainingTime * endFactor; // Panic mode: endFactor of remaining time
        return usedTime;
    }

    public void updateRemainingTime(double usedTime) {
        remainingTime -= usedTime;
        if (remainingTime < 0) remainingTime = 0;
    }

}
