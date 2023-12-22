package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedral.core.evaluators.*;
import org.cathedrale.heuristics.*;
import org.cathedrale.heuristics.Heuristic;

public class Main {
    public static void main(String[] args) {
        Heuristic[] heuristic = new Heuristic[]{
                new ZoneHeuristic(5.0),
                new LargeBuildingHeuristic(1.0)
        };

        AlphaBeta alphaBeta = new AlphaBeta(heuristic);
        MinMaxQuiescence minMaxQuiescence = new  MinMaxQuiescence(heuristic);
        AlphaBetaTransposition alphaBetaTransposition = new AlphaBetaTransposition(heuristic);
        MinMax minMax = new MinMax(heuristic);
        NegamaxParallelTransposition negamaxParallelTransposition = new NegamaxParallelTransposition(heuristic);

        CathedralGUI.start(
                new HeuristicalAgent(heuristic),
                new Frodo(),
                new Gandalf(),
                new VisualizedAgent(alphaBeta),
                new VisualizedAgent(negamaxParallelTransposition)
        );
    }
}