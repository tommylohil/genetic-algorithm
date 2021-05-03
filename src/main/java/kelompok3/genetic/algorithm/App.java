package kelompok3.genetic.algorithm;

import com.diogonunes.jcolor.Attribute;
import kelompok3.genetic.algorithm.model.Individual;
import kelompok3.genetic.algorithm.model.Population;

import java.util.Optional;
import java.util.Random;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

/**
 * Genetic Algorithm
 *
 */
public class App {

    Population population = new Population();
    Individual firstSelectedIndividual;
    Individual secondSelectedIndividual;
    Individual firstChild;
    Individual secondChild;
    private int generationCount = 0;
    private int leastFittestIndex;
    private int lastFittestIndex;
    private int counter = 0;
    public static final Integer totalPopulation = 10;

    public static void main(String[] args) {

        // console color setting for windows
        consoleColorSetup();

        Random random = new Random();

        App geneticAlgorithmApp = new App();

        //Initialize population
        geneticAlgorithmApp.population.initializePopulation(totalPopulation);

        //Calculate fitness of each individual
        geneticAlgorithmApp.population.calculateFitness();

        geneticAlgorithmApp.lastFittestIndex = geneticAlgorithmApp.population.fittestIndex;
        geneticAlgorithmApp.counter = 1;

        System.out.println(colorize("Generation: " + geneticAlgorithmApp.generationCount + ", Fittest: " + geneticAlgorithmApp.population.fittest + ", Counter = " + geneticAlgorithmApp.counter, YELLOW_TEXT()));
        geneticAlgorithmApp.printAllIndividuals(geneticAlgorithmApp.population.getLeastFittestIndex());

        while (geneticAlgorithmApp.counter <= 10) {
            ++geneticAlgorithmApp.generationCount;

            // Get least fittest index
            geneticAlgorithmApp.leastFittestIndex = geneticAlgorithmApp.population.getLeastFittestIndex();

            //Do selection
            geneticAlgorithmApp.selection();

            //Do crossover
            geneticAlgorithmApp.crossover();

            //Do mutation under a random probability
            geneticAlgorithmApp.mutation();

            //Add fittest offspring to population
            geneticAlgorithmApp.addFittestOffspring();

            //Calculate new fitness value
            geneticAlgorithmApp.population.calculateFitness();

            System.out.println(colorize("Generation: " + geneticAlgorithmApp.generationCount + ", Fittest: " + geneticAlgorithmApp.population.fittest + ", Counter = " + geneticAlgorithmApp.counter, YELLOW_TEXT()));
            geneticAlgorithmApp.printAllIndividuals(geneticAlgorithmApp.leastFittestIndex);

            if (geneticAlgorithmApp.lastFittestIndex == geneticAlgorithmApp.population.fittestIndex) {
                geneticAlgorithmApp.counter++;
            } else {
                geneticAlgorithmApp.counter = 0;
                geneticAlgorithmApp.lastFittestIndex = geneticAlgorithmApp.population.fittestIndex;
            }
        }

        System.out.println("\nSolution found in generation " + geneticAlgorithmApp.generationCount);
        System.out.println("Fitness: " + geneticAlgorithmApp.population.getFittest().fitness);
        System.out.print("Genes: ");
        for (int i = 0; i < 5; i++) {
            System.out.print(geneticAlgorithmApp.population.getFittest().genes[i]);
        }

        System.out.println("");

    }

    // Get All Fitness Probability percentage
    private float[] getAllFitnessProbability() {
        float[] result = new float[totalPopulation];
        float sum = 0;

        for (int i=0; i<totalPopulation-1; i++) {
            result[i] = sum + population.individuals[i].fitness / population.totalFitness;
            sum = result[i];
        }
        result[totalPopulation-1] = 1f;

        return result;
    }

    private Individual runRoulette(Optional<Individual> previousSelectedIndividual) {
        // Generate value between 0 and 1
        float generatedRouletteValue = (float) Math.random();
        int individualIndex = 0;

        // Check if roulette result points to which individual index
        for (float upperBoundProbability: getAllFitnessProbability()) {
            if (generatedRouletteValue <= upperBoundProbability) {
                break;
            }
            individualIndex++;
        }

        Individual result = population.individuals[individualIndex];

        // if previous selected individual is equal to result, runRoulette again!
        if ((previousSelectedIndividual != null) && previousSelectedIndividual.equals(result)) {
            return runRoulette(previousSelectedIndividual);
        }
        return result;
    }

    //Selection
    private void selection() {
        //Select the first individual using Roulette
        firstSelectedIndividual = runRoulette(null);

        //Select the second individual using Roulette
        secondSelectedIndividual = runRoulette(Optional.ofNullable(firstSelectedIndividual));
    }

    // Get random index between 0 to geneLength-1
    private int getRandomIndex(int geneLength) {
        return (int) (Math.random() * geneLength-1);
    }

    //Crossover
    private void crossover() {
        // Get 2 random index value
        int lowerBoundConstraint = getRandomIndex(Individual.geneLength);
        int upperBoundConstraint = getRandomIndex(Individual.geneLength);

        // Swap lower and upper if the value incorrect
        if (lowerBoundConstraint > upperBoundConstraint) {
            int temp = lowerBoundConstraint;
            lowerBoundConstraint = upperBoundConstraint;
            upperBoundConstraint = temp;
        }

        firstChild = new Individual(firstSelectedIndividual);
        secondChild = new Individual(secondSelectedIndividual);

        // Swap values among children
        for (int i = lowerBoundConstraint; i < upperBoundConstraint; i++) {
            int temp = firstChild.genes[i];
            firstChild.genes[i] = secondChild.genes[i];
            secondChild.genes[i] = temp;
        }
    }

    // Mutation
    private void mutation() {
        // Hardcode mutation index and mutation threshold
        int lowerBoundConstraint = 2;
        int upperBoundConstraint = 3;
        float mutationThreshold = 0.3f;

        float randomValue = (float) Math.random();

        // do mutation under a random probability by flip the bit in lower and upper bound range
        if (randomValue <= mutationThreshold) {
            for (int i = lowerBoundConstraint; i < upperBoundConstraint; i++) {
                firstChild.genes[i] ^= 1;
                secondChild.genes[i] ^= 1;
            }
        }
    }

    // Get fittest offspring
    private Individual getFittestOffspring() {
        return (firstChild.fitness > secondChild.fitness) ? firstChild : secondChild;
    }

    // Replace least fittest individual from most fittest offspring
    private void addFittestOffspring() {
        // Get index of least fit individual
        int leastFittestIndex = population.getLeastFittestIndex();

        // Replace least fittest individual from most fittest offspring
        population.individuals[leastFittestIndex] = getFittestOffspring();
    }

    private void printAllIndividuals(int leastFittestIndex) {
        for (int i=0; i<totalPopulation; i++) {
            Attribute lineColor = (i == leastFittestIndex) ? GREEN_TEXT() : WHITE_TEXT();

            System.out.print(colorize("Individual " + i + ", genes=[", lineColor));
            for (int j=0; j<Individual.geneLength; j++) {
                if (j>0) {
                    System.out.print(colorize(", ", lineColor));
                }
                System.out.print(colorize(String.valueOf(population.individuals[i].genes[j]), lineColor));
            }
            System.out.println(colorize("], fitness=" + String.format("%.3f", population.individuals[i].fitness), lineColor));
        }
        System.out.println();
    }

    private static void consoleColorSetup() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Set output mode to handle virtual terminal sequences
            Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
            DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
            HANDLE hOut = (HANDLE)GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

            DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
            Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
            GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});

            int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
            DWORD dwMode = p_dwMode.getValue();
            dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
            Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
            SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
        }
    }
}
