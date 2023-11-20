package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedrale.heuristics.*;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new SaintAgent(), new CuteAgent(), new HeuristicalAgent (
                new ZoneHeuristic(150),
                new GameScoreHeuristic(75)
        ), new SmartAgent());
    }
}