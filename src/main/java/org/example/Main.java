package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.GodAgent;
import org.cathedral.core.HeroAgent;
import org.cathedral.core.NeuralAgent;
import org.cathedral.core.SaintAgent;

public class Main {
    public static void main(String[] args) {
        var network = new NeuralNetwork(100, 128,  2);
        var neuralAgent = new NeuralAgent(network);
        CathedralGUI.start(new GodAgent(), new SaintAgent(), new HeroAgent(), neuralAgent);
    }
}