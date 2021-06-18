package kelompok3.genetic.algorithm.model;

import lombok.Data;

import java.util.Random;

@Data
public class Individual {

    public float fitness = 0;
    public float xAxis = 0;
    public int[] genes = new int[geneLength];
    public static int geneLength = 20;

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

    // Calculate fitness
    public void calcFitness() {
        // Convert array of integer (as binary) to integer result
        int multiplier = 1;
        long result = 0;
        for (int i=geneLength-1; i>=0; i--) {
            result += multiplier * genes[i];
            multiplier <<= 1;
        }

        // Get x value
        xAxis = (float) ((result * 1.0) / (2<<geneLength));

        // Get y value as fitness value
        fitness = (float) (Math.exp(0.005f * Math.sin(xAxis) - Math.sinh(Math.sin(10.005 * xAxis) * xAxis / 2.0))
                * Math.sin(50.5 * xAxis) * Math.cos(xAxis) + Math.cos(0.325 * xAxis)) ;
    }
}
