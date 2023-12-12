package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedrale.heuristics.*;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new SaintAgent(), new CuteAgent(), new HeuristicalAgent (
                new LargeBuildingHeuristic(8),
                new BlockHeuristic(3),
                new ZoneHeuristic(8),
                new AvoidGrayAreaHeuristic(10)
        ), new SmartAgent(), new Frodo(), new Gandalf());
    }
}