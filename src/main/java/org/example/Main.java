package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedrale.heuristics.*;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new SaintAgent(), new CuteAgent(), new HeuristicalAgent (
                new LargeBuildingHeuristic(4),
                new BlockNewHeuristic(0.5),
                new AvoidGrayAreaHeuristic(2),
                new ZoneHeuristic(2)
        ), new SmartAgent(), new Frodo(), new Gandalf());
    }
}