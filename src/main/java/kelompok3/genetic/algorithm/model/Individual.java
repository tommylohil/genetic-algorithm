package kelompok3.genetic.algorithm.model;

import java.util.Random;

public class Individual {

    public float fitness = 0;
    public int[] genes = new int[geneLength];
    public static int geneLength = 5;

    public Individual() {
        Random rn = new Random();

        //Set genes randomly for each individual
        for (int i = 0; i < genes.length; i++) {
            genes[i] = Math.abs(rn.nextInt() % 2);
        }

        fitness = 0;
    }

    public Individual(Individual individual) {
        //Set genes randomly for each individual
        for (int i = 0; i < genes.length; i++) {
            genes[i] = individual.genes[i];
        }

        calcFitness();
    }

    //Calculate fitness
    public void calcFitness() {
        // Convert array of integer (as binary) to integer result
        int multiplier = 1;
        int result = 0;
        for (int i=geneLength-1; i>=0; i--) {
            result += multiplier * genes[i];
            multiplier <<= 1;
        }

        // Get x value
        float xAxis = (float) ((result * 1.0) / ((2<<geneLength)-1));

        // Get y value as fitness value
        fitness = (float) (Math.sin(xAxis) + Math.cos(xAxis));
    }
}
