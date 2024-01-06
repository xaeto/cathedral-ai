package org.example;

import de.fhkiel.ki.cathedral.game.*;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.util.*;

public class NeuralNetwork {
    private MultiLayerNetwork network;

    public static MultiLayerConfiguration createConfiguration(int numInputs, int numHidden, int numOutputs) {
        return new NeuralNetConfiguration.Builder()
                .seed(new Random().nextInt(1, 1000000))
                .weightInit(WeightInit.ZERO)
                .updater(org.nd4j.linalg.learning.config.Adam.builder().learningRate(0.1).build())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .activation(Activation.RELU)
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(numHidden)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(numHidden)
                        .nOut(numHidden)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .activation(Activation.TANH)  // Use softmax for multiclass classification
                        .nIn(numHidden)
                        .nOut(numOutputs)
                        .build())
                .build();
    }

    public NeuralNetwork(int numInputs, int numHidden, int numOutputs){
        var cfg = createConfiguration(numInputs, numHidden, numOutputs);
        network = new MultiLayerNetwork(cfg);
        load("./data.zip");
        network.init();
        System.out.println(network.summary());
    }

    public void setListener(StatsListener listener){
        this.network.setListeners(listener);
    }

    public INDArray predict(INDArray input) {
        return network.output(input);
    }

    public void save(String filePath) {
        try{
            ModelSerializer.writeModel(network, filePath, true);
        } catch(Exception ex){
        }
    }

    public void load(String filePath) {
        try{
            network = ModelSerializer.restoreMultiLayerNetwork(new File(filePath));
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

    public static int[] flatten(Game game){
        var field = NeuralNetwork.GenerateBoardMatrix(game);
        return Arrays.stream(field).flatMapToInt(Arrays::stream)
                .toArray();
    }

    public void fit(INDArray input, INDArray output){
        network.fit(input, output);
         //System.out.println(network.feedForward());
        // save("./data.zip");
    }

    public long getNumOutputs() {
        // Assuming the output layer is the last layer in your neural network configuration
        return network.getLayer(network.getLayers().length - 1).getParam("W").size(1);
    }

    public void train(INDArray input, INDArray target) {
        // Forward pass to get current predictions
        target = target.reshape(1, 2);
        INDArray predictions = network.output(input);

        // Compute the loss between predictions and target probabilities
        double loss = computeLoss(predictions, target);
        System.out.println("Loss: " + loss);

        // Backpropagation to compute gradients
        network.fit(input, target);

        // Update the weights using the optimizer
        network.update(network.gradient());
        // Save the trained model after training
        save("data.zip");
    }

    private double computeLoss(INDArray predictions, INDArray target) {
        // Assuming a simple mean squared error loss for illustration
        return predictions.squaredDistance(target) / predictions.size(0);
    }

    public static int[][] GenerateBoardMatrix(Game game){
        int matrix[][] = new int[10][10];
        var field = game.getBoard().getField();

        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++){
                if(field[y][x] == Color.Black_Owned){
                    matrix[y][x] = Color.Black_Owned.getId();
                }
                else if(field[y][x] == Color.White_Owned){
                    matrix[y][x] = Color.White_Owned.getId();
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