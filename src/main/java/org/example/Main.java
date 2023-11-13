package org.example;

import de.fhkiel.ki.cathedral.gui.CathedralGUI;
import org.cathedral.core.*;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        CathedralGUI.start(new GodAgent(), new SaintAgent(), new HeroAgent(), new AlphaBetaAgent());
    }
}