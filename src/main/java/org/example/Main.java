package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedrale.heuristics.*;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new SaintAgent(), new CuteAgent(), new HeuristicalAgent (
                new ZoneHeuristic(25),
                new BuildingHeuristic(7.5),
                new BlockHeuristic(15),
                new GameScoreHeuristic(8)
        ), new SmartAgent(), new Frodo(), new Gandalf());
    }
}