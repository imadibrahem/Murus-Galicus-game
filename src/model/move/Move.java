package model.move;

import java.util.Objects;

public class Move {

    private final short value;

    public Move(short value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return (value >> 7) + "=> |" + getDirection() + "| (" +getTargetType() + ") | ";
    }


    public short getValue() {
        return value;
    }

    public int getDirection() {
        return (value >> 3) & 15;
    }

    public int getInitialLocation(boolean isBlue) {
        return isBlue ? (value >> 7)  : 55 - (value >> 7);
    }

    public int getInitialRow(boolean isBlue) {
        int location = getInitialLocation(isBlue);
        return 7 - (location / 8);
    }

    public int getInitialCol(boolean isBlue) {
        int location = getInitialLocation(isBlue);
        return (location % 8) + 1;
    }
    public int getTargetRow(boolean isBlue) {
        int initialRow = getInitialRow(isBlue);
        if (getDirection() == 3 || getDirection() == 7) return initialRow;
        if (isTargetEnemy()){
            if (getDirection() == 1 || getDirection() == 2 || getDirection() == 8) return (isBlue ? initialRow + 1 : initialRow - 1) ;
            else return (isBlue ? initialRow - 1 : initialRow + 1) ;
        }
        else{
            if (getDirection() == 1 || getDirection() == 2 || getDirection() == 8) return (isBlue ? initialRow + 2 : initialRow - 2) ;
            else return (isBlue ? initialRow - 2 : initialRow + 2) ;
        }
    }

    public int getTargetRowSorting() {
        int initialRow =  (((55 - (value >> 7)) / 8) + 1);
        if (getDirection() == 3 || getDirection() == 7) return initialRow;
        if (isTargetEnemy()){
            if (getDirection() == 1 || getDirection() == 2 || getDirection() == 8) return (initialRow + 1) ;
            else return (initialRow - 1) ;
        }
        else{
            if (getDirection() == 1 || getDirection() == 2 || getDirection() == 8) return (initialRow + 2) ;
            else return (initialRow - 2) ;
        }
    }


    public int getTargetType() {
        return value & 7;
    }

    public boolean isTargetEmpty() {
        return getTargetType() == 0;
    }

    public boolean isTargetNearFriendly() {
        return getTargetType() == 1;
    }

    public boolean isTargetFarFriendly() {
        return getTargetType() == 2;
    }

    public boolean isTargetBothFriendly() {
        return getTargetType() == 3;
    }

    public boolean isTargetEnemy() {
        return getTargetType() == 4;
    }

    public boolean isWinnerMove(){
       return ((((value >> 7) < 24 && (value >> 7) > 15) && ((value & 7) < 4)) || (((value >> 7) < 16) && ((value & 7) > 3)))
               && ((((value >> 3) & 15) == 1) || (((value >> 3) & 15) == 2) || (((value >> 3) & 15) == 8));
    }
    public boolean isThreateningMove(){
        return ((((value >> 7) < 40 && (value >> 7) > 31) && ((value & 7) < 4)  && ((value & 7) > 1))
                || (((value >> 7) < 32 && (value >> 7) > 23) && (((value & 7) == 3)  || ((value & 7) == 1))))
                && ((((value >> 3) & 15) == 1) || (((value >> 3) & 15) == 2) || (((value >> 3) & 15) == 8));
    }


    /*

    public int getTargetLocation() {
        // TODO: 10/19/2024
        return 0;
    }

    public int getTargetRow() {
        // TODO: 10/19/2024
        return 0;
    }

    public int getTargetCol() {
        // TODO: 10/19/2024
        return 0;
    }

 */


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move that = (Move) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
