package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.cathedrale.heuristics.*;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new SaintAgent(), new CuteAgent(), new HeuristicalAgent (
                new ZoneHeuristic(150.0),
                new BlockHeuristic(100.0),
                new GameScoreHeuristic(10.0),
                new PlayerAreaHeuristic(7.5)
        ));
    }
}