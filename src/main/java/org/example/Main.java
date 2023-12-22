package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedral.core.evaluators.*;
import org.cathedrale.heuristics.*;
import org.cathedrale.heuristics.Heuristic;

public class Main {
    public static void main(String[] args) {
        Heuristic[] heuristic = new Heuristic[]{
                new BlockHeuristic(1),
                new LargeBuildingHeuristic(1),
                new ZoneHeuristic(1),
                new GameScoreHeuristic(1)
        };

        AlphaBeta alphaBeta = new AlphaBeta(heuristic);
        MinMaxQuiescence minMaxQuiescence = new  MinMaxQuiescence(heuristic);
        AlphaBetaTransposition alphaBetaTransposition = new AlphaBetaTransposition(heuristic);
        MinMax minMax = new MinMax(heuristic);
        Negamax negamax = new Negamax(heuristic);

        CathedralGUI.start(
                new HeuristicalAgent(heuristic),
                new Frodo(),
                new Gandalf(),
                new VisualizedAgent(alphaBeta),
                new VisualizedAgent(negamax)
        );
    }
}