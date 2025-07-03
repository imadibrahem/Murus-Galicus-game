package model.move;

import java.util.ArrayList;
import java.util.List;

public class MoveTypePermutations {
    private MoveType[] startValues = MoveType.values();
    private List<MoveType[]> permutations = new ArrayList<>();

    public MoveTypePermutations() {
        generatePermutations(startValues, 0, permutations);
    }

    public List<MoveType[]> getPermutations() {
        return permutations;
    }

    private void generatePermutations(MoveType[] moveTypes, int start, List<MoveType[]> permutations) {
        if (start == startValues.length - 1) permutations.add(moveTypes.clone());
        else {
            for (int i = start; i < moveTypes.length; i++) {
                swap(moveTypes, start, i);
                generatePermutations(moveTypes, start + 1, permutations);
                swap(moveTypes, start, i);
            }
        }
    }

    private  void swap(MoveType[] array, int i, int j) {
        MoveType temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
