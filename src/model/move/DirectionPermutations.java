package model.move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectionPermutations {
    private int[] startValues = {1, 2, 3, 4, 5, 6, 7, 8};
    private List<int[]> permutations = new ArrayList<>();

    public DirectionPermutations() {
        generatePermutations(startValues, 0, permutations);
    }

    public List<int[]> getPermutations() {
        return permutations;
    }

    private void generatePermutations(int[] moveDirections, int start, List<int[]> permutations) {
        if (start == startValues.length - 1) permutations.add(moveDirections.clone());
        else {
            for (int i = start; i < moveDirections.length; i++) {
                swap(moveDirections, start, i);
                generatePermutations(moveDirections, start + 1, permutations);
                swap(moveDirections, start, i);
            }
        }
    }

    private  void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

}
