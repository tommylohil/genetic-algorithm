package kelompok3.genetic.algorithm.model;

import kelompok3.genetic.algorithm.App;

public class Population {

    public Individual[] individuals = new Individual[App.totalPopulation];
    public float fittest = 0;
    public int fittestIndex = -1;
    public float totalFitness = 0;

    //Initialize population
    public void initializePopulation(int size) {
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual();
        }
    }

    //Get the fittest individual
    public Individual getFittest() {
        float maxFit = Integer.MIN_VALUE;
        int maxFitIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (maxFit <= individuals[i].fitness) {
                maxFit = individuals[i].fitness;
                maxFitIndex = i;
            }
            totalFitness += individuals[i].fitness;
        }
        fittest = individuals[maxFitIndex].fitness;
        fittestIndex = maxFitIndex;
        return individuals[maxFitIndex];
    }

    //Get index of least fittest individual
    public int getLeastFittestIndex() {
        float minFitVal = Integer.MAX_VALUE;
        int minFitIndex = 0;
        for (int i = 0; i < individuals.length; i++) {
            if (minFitVal >= individuals[i].fitness) {
                minFitVal = individuals[i].fitness;
                minFitIndex = i;
            }
        }
        return minFitIndex;
    }

    //Calculate fitness of each individual
    public void calculateFitness() {
        for (int i = 0; i < individuals.length; i++) {
            individuals[i].calcFitness();
        }
        getFittest();
    }
}
