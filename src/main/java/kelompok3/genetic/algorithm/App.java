package kelompok3.genetic.algorithm;

import com.diogonunes.jcolor.Attribute;
import kelompok3.genetic.algorithm.model.Individual;
import kelompok3.genetic.algorithm.model.Population;

import java.util.*;

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
    public static final Integer totalPopulation = 30;

    public static void main(String[] args) {

        // console color setting for windows
        consoleColorSetup();

        App geneticAlgorithmApp = new App();

        // Initialize population
        geneticAlgorithmApp.population.initializePopulation(totalPopulation);

        // Calculate fitness of each individual
        geneticAlgorithmApp.population.calculateFitness();

        geneticAlgorithmApp.lastFittestIndex = geneticAlgorithmApp.population.fittestIndex;

        System.out.println(colorize("Generation: " + geneticAlgorithmApp.generationCount + ", Fittest: " + geneticAlgorithmApp.population.fittest, YELLOW_TEXT()));
        geneticAlgorithmApp.printAllIndividuals(geneticAlgorithmApp.population.getLeastFittestIndex());

        while (geneticAlgorithmApp.generationCount <= 200) {
            ++geneticAlgorithmApp.generationCount;

            // Get least fittest index
            geneticAlgorithmApp.leastFittestIndex = geneticAlgorithmApp.population.getLeastFittestIndex();

            // Do selection
            geneticAlgorithmApp.selection();

            // Do crossover
            geneticAlgorithmApp.crossover();

            // Do mutation under a random probability
            geneticAlgorithmApp.mutation();

            // Add fittest offspring to population
            geneticAlgorithmApp.addFittestOffspring();

            // Calculate new fitness value
            geneticAlgorithmApp.population.calculateFitness();

            System.out.println(colorize("Generation: " + geneticAlgorithmApp.generationCount + ", Fittest: " + geneticAlgorithmApp.population.fittest, YELLOW_TEXT()));
            geneticAlgorithmApp.printAllIndividuals(geneticAlgorithmApp.leastFittestIndex);
        }

        System.out.println("\nSolution found in generation " + geneticAlgorithmApp.generationCount);
        System.out.println("Fitness: " + geneticAlgorithmApp.population.getFittest().fitness);
        System.out.print("Genes: ");
        for (int i = 0; i < Individual.geneLength; i++) {
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

    private Individual runTournament(Optional<Individual> previousSelectedIndividual) {
        List<Individual> individualList = new ArrayList<Individual>(Arrays.asList(population.individuals));
        Collections.shuffle(individualList);
        List<Individual> selected150Individuals = individualList.subList(0, Individual.geneLength/2);

        Individual selectedBestIndividual = selected150Individuals.stream()
                .min(Comparator.comparing(Individual::getFitness))
                .orElse(null);

        return selectedBestIndividual;
    }

    // Selection
    private void selection() {
        // Select the first individual using Tournament
        firstSelectedIndividual = runTournament(null);

        // Select the second individual using Tournament
        secondSelectedIndividual = runTournament(Optional.ofNullable(firstSelectedIndividual));
    }

    // Get random index between 0 to geneLength-1
    private int getRandomIndex(int geneLength) {
        return (int) (Math.random() * geneLength-1);
    }

    // Crossover
    private void crossover() {
        int temp;
        firstChild = firstSelectedIndividual;
        secondChild = secondSelectedIndividual;

        // Swap values of (2nd half of firstChild) and (1st half of secondChild)
        for (int i = 0; i < Individual.geneLength/2; i++) {
            temp = firstChild.genes[(Individual.geneLength/2) + i];
            firstChild.genes[(Individual.geneLength/2) + i] = secondChild.genes[i];
            secondChild.genes[i] = temp;
        }
    }

    // Mutation
    private void mutation() {
        // Hardcode mutation threshold as 1%
        float mutationThreshold = 0.01f;

        float randomValue = (float) Math.random();

        // do mutation under a random probability by flip the bit in lower and upper bound range
        if (randomValue <= mutationThreshold) {
            int randomBitIndex = (int) Math.floor(Math.random() * (Individual.geneLength + 1));
            firstChild.genes[randomBitIndex] ^= 1;
            secondChild.genes[randomBitIndex] ^= 1;
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
