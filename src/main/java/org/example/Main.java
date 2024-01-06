package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.Frodo;
import org.cathedral.core.HeuristicalAgent;
import org.cathedral.core.NeuralAgent;
import org.cathedral.core.VisualizedAgent;
import org.cathedral.core.evaluators.*;
import org.cathedral.heuristics.*;

public class Main {
    public static void main(String[] args) {
        Heuristic[] heuristic = new Heuristic[]{
                new ZoneHeuristic(10),
                new BlockHeuristic(2),
                new GameScoreHeuristic(5),
                new LargeBuildingHeuristic(1),
                new AvoidGrayAreaHeuristic(1)
        };

        AlphaBeta alphaBeta = new AlphaBeta(heuristic);
        MinMaxQuiescence minMaxQuiescence = new  MinMaxQuiescence(heuristic);
        AlphaBetaTransposition alphaBetaTransposition = new AlphaBetaTransposition(heuristic);
        MinMax minMax = new MinMax(heuristic);
        Negamax negamax = new Negamax(heuristic);
        NegaScout negaScout = new NegaScout(heuristic);
        NegamaxParallel negamaxParallel = new NegamaxParallel(heuristic);
        //NegamaxParallelTransposition negamaxParallelTransposition = new NegamaxParallelTransposition(heuristic);

        CathedralGUI.start(
                new HeuristicalAgent(heuristic),
                new VisualizedAgent(alphaBeta),
                new NeuralAgent(new NeuralNetwork(100, 10, 2)),
                new VisualizedAgent(negamax),
                new VisualizedAgent(negaScout),
                new VisualizedAgent(negamaxParallel)
        );
    }
}