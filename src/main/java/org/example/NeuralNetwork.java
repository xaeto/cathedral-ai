package org.example;

import com.google.gson.Gson;
import de.fhkiel.ki.cathedral.game.*;
import org.bytedeco.javacv.FrameFilter;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class NeuralNetwork {
    private MultiLayerNetwork model;
    public static MultiLayerConfiguration createConfiguration(int numInputs, int numHidden, int numOutputs) {
        return new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(org.nd4j.linalg.learning.config.Adam.builder().learningRate(0.02).build())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(numHidden)  // Number of neurons in the hidden layer
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(numHidden)  // Number of neurons in the hidden layer
                        .nOut(numOutputs)  // Number of output classes
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();
    }
    public NeuralNetwork(int numInputs, int numHidden, int numOutputs){

        var cfg = createConfiguration(numInputs, numHidden, numOutputs);

        model = new MultiLayerNetwork(cfg);
        load("./data.txt");
        model.init();
        System.out.println(model.summary());
    }

    public INDArray predict(INDArray input) {
        return model.output(input);
    }

    public void save(String filePath) {
        System.out.println("Saving network model.");
        try{
            System.out.println(model.summary());
            ModelSerializer.writeModel(model, filePath, true);
        } catch(Exception ex){
        }
    }

    public void load(String filePath) {
        try{
            model = ModelSerializer.restoreMultiLayerNetwork(new File(filePath));
        } catch(Exception ex){
        }
    }

    public static void printMatrix(int [][]matrix){
        for(int x = 0; x < matrix.length; x++){
            printRow(matrix[x]);
        }
    }
    public static void printRow(int[] row) {
        for (int i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }

    public static int[][] GenerateBoardMatrix(Game game){
        int matrix[][] = new int[10][10];
        var field = game.getBoard().getField();

        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++){
                if(field[y][x] == Color.Black_Owned){
                    matrix[y][x] = -1;
                }
                else if(field[y][x] == Color.White_Owned){
                    matrix[y][x] = -2;
                } else {
                    matrix[y][x] = 0;
                }
            }
        }

        for(Placement p: game.getBoard().getPlacedBuildings()){
            for(Position pos: p.form()){
                int x = p.x() + pos.x();
                int y = p.y() + pos.y();
                matrix[y][x] = p.building().getId();
            }
        }
        return matrix;
    }
}