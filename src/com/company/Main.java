package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

public class Main {

    public static int bestSum = Integer.MAX_VALUE;
    public static int iteration = 0;
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\Kamila\\Desktop\\Berlin2.txt";
        int[][] distances = makeList(filePath);

        int numberOfCities = distances.length;
        int populationSize = 200_000;
        int crossoverChance = 95;
        int mutationChance = 0;
        int numberOfIteration = 600;

        int[][] population  = routeDraw(populationSize, numberOfCities);
        int[] sumOfDistances = routeSum(populationSize, numberOfCities, distances, population);
        bestRoute(sumOfDistances, population);
        for (int i = 0; i < numberOfIteration; i++) {
            iteration = i;
            population = tournamentSelection(sumOfDistances, population, numberOfCities);
            //making crossover rate dynamic
            if(i % 10 == 0 && i != 0 && crossoverChance >= 30) {
                crossoverChance -= 2;
            }
            population = pmxCrossover(population, numberOfCities, crossoverChance);
            sumOfDistances = routeSum(populationSize, numberOfCities, distances, population);
            bestRoute(sumOfDistances, population);
            //making mutation rate dynamic
            if(i % 9 == 0 && i != 0 && mutationChance <= 35) {
                mutationChance += 1;
            }
            population = mutation(population, numberOfCities, mutationChance);
            sumOfDistances = routeSum(populationSize, numberOfCities, distances, population);
            bestRoute(sumOfDistances, population);
        }
    }

    public static int[][] makeList(String filePath) throws IOException {
        BufferedReader file = new BufferedReader(new FileReader(filePath));
        String text = "";
        int size = 0;
        String checkLine = file.readLine();
        while (checkLine != null) {
            text += checkLine + "\n";
            size += 1;
            checkLine = file.readLine();
        }
        String[] splitLines = text.split("\n");
        int[][] list = new int[size][size];
        for (int i = 0; i < size; i++) {
            String[] splitValues = splitLines[i].split(" ");
            for (int j = 0; j < splitValues.length; j++) {
                list[i][j] = convertToInt(splitValues[j]);
            }
        }
        for (int i = size - 1; i > 0; i--) {
            for (int j = 0; j < size; j++) {
                list[j][i] = list[i][j];
            }
        }
        file.close();
        return list;
    }

    public static Integer convertToInt (String object) {
        try {
            return Integer.parseInt(object);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int[][] routeDraw (int populationSize, int numberOfCities) {
        int[][] population = new int[populationSize][numberOfCities];
        Random rand = new Random();
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < numberOfCities; j++) {
                int drawCity = rand.nextInt(numberOfCities);
                if (checkIfRepeated(population[i], drawCity, 0, j - 1) && j != 0) {
                    j--;
                } else {
                    population[i][j] = drawCity;
                }
            }
        }
        return population;
    }

    public static boolean checkIfRepeated (int[] list, int value, int beginningValue,  int endingValue) {
        for (int i = beginningValue; i <= endingValue; i++) {
            if (list[i] == value) {
                return true;
            }
        }
        return false;
    }

    public static int[] routeSum (int populationSize, int numberOfCities, int[][] distances, int[][] population) {
        int routeSum;
        int[] sumsList = new int[populationSize];
        for (int i = 0; i < populationSize; i++) {
            routeSum = 0;
            for (int j = 0; j < numberOfCities; j++) {
                if (j != numberOfCities - 1) {
                    routeSum += distances[population[i][j]][population[i][j+1]];
                } else {
                    routeSum += distances[population[i][j]][population[i][0]];
                }
            }
            sumsList[i] = routeSum;
        }
        return sumsList;
    }

    public static void bestRoute (int[] sumOfDistances, int[][] population) {
        for (int i = 0; i < sumOfDistances.length; i++) {
            if (sumOfDistances[i] < bestSum) {
                bestSum = sumOfDistances[i];
                printList(population[i]);
                System.out.println(bestSum + " iteration number: " + iteration);
            }
        }
    }

    public static int[][] tournamentSelection (int[] sumOfDistances, int[][] population, int numberOfCities) {
        Random rand = new Random();
        int[][] populationAfterSelection = new int[population.length][numberOfCities];
        for (int i = 0; i < population.length; i++) {
//            int firstRoute = rand.nextInt(population.length);
            int firstRoute = i;
            int secondRoute = rand.nextInt(population.length);
            if (sumOfDistances[firstRoute] < sumOfDistances[secondRoute]) {
                populationAfterSelection[i] = population[firstRoute].clone();
            } else {
                populationAfterSelection[i] = population[secondRoute].clone();
            }
        }
        return populationAfterSelection;
    }

    public static int[][] pmxCrossover (int[][] population, int numberOfCities, int crossoverChance) {
        int[][] populationAfterCrossover = new int[population.length][numberOfCities];
        Random rand = new Random();
        for (int i = 0; i < population.length; i++) {
            int draw = rand.nextInt(101);
            int secondParent = rand.nextInt(population.length);
            if(draw <= crossoverChance && !Arrays.equals(population[i], population[secondParent])) {
                populationAfterCrossover[i] = crossRoutes(population[i], population[secondParent], numberOfCities);
            } else {
                populationAfterCrossover[i] = population[i].clone();
            }
        }
        return populationAfterCrossover;
    }

    public static int[] crossRoutes (int[] parent1, int[] parent2, int numberOfCities) {
        Random rand = new Random();
        int[] offspring = new int[numberOfCities];
        Arrays.fill(offspring, -1);
        int endingPoint = 0;
        while (endingPoint < 1) {
            endingPoint = rand.nextInt(numberOfCities);
        }
        int beginningPoint = rand.nextInt(endingPoint);
        while (endingPoint - beginningPoint < 1) {
            endingPoint = rand.nextInt(numberOfCities);
            beginningPoint = rand.nextInt(endingPoint);
        }
        // copying selected argument from parent1 into offspring
        for (int i = beginningPoint; i <= endingPoint; i++) {
            offspring[i] = parent1[i];
        }
        // checking and finding place for values which occur in parent2 but not in offspring
        for (int i = beginningPoint; i <= endingPoint; i++) {
            if(!checkIfRepeated(offspring, parent2[i], beginningPoint, endingPoint)) {
                int index = i;
                while(offspring[index] != -1) {
                    int temporaryValue = parent1[index];
                    int searchedValue = -1;
                    int indexOfSearching = 0;
                    while (temporaryValue != searchedValue) {
                        searchedValue = parent2[indexOfSearching];
                        indexOfSearching++;
                    }
                    index = indexOfSearching - 1;
                }
                offspring[index] = parent2[i];
            }
        }
        // filling rest of the array with values
        for (int i = 0; i < offspring.length; i++) {
            if (offspring[i] == -1) {
                offspring[i] = parent2[i];
            }
        }
        return offspring;
    }

    public static int[][] mutation (int[][] population, int numberOfCities, int mutationChance) {
        Random rand = new Random();
        int[][] populationAfterMutation = new int[population.length][numberOfCities];
        for (int i = 0; i < population.length; i++) {
            Arrays.fill(populationAfterMutation[i], -1);
            int draw = rand.nextInt(101);
            if (draw <= mutationChance) {
                int firstValue = rand.nextInt(numberOfCities);
                int secondValue = rand.nextInt(numberOfCities);
                while (secondValue == firstValue) {
                    secondValue = rand.nextInt(numberOfCities);
                }
                populationAfterMutation[i][firstValue] = population[i][secondValue];
                populationAfterMutation[i][secondValue] = population[i][firstValue];
                for (int j = 0; j < numberOfCities; j++) {
                    if (populationAfterMutation[i][j] == -1) {
                        populationAfterMutation[i][j] = population[i][j];
                    }
                }
            } else {
                populationAfterMutation[i] = population[i].clone();
            }
        }
        return populationAfterMutation;
    }

    public static void printList (int[][] list) {
        for (int i = 0; i < list.length; i++) {
            for (int j = 0; j < list[i].length; j++) {
                System.out.print(list[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void printList (int[] list) {
        for (int i = 0; i < list.length; i++) {
            if (i != list.length - 1) {
                System.out.print(list[i] + "-");
            } else {
                System.out.println(list[i]);
            }
        }
    }
}
