package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Game;
import org.cathedral.heuristics.Heuristic;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class EmulationTask extends Thread {
    public Queue<BoardPlacement> queued = new LinkedBlockingQueue<>();
    public Queue<BoardPlacementScore> finished = new LinkedBlockingQueue<>();
    public volatile boolean stop = false;

    @Override
    public void start(){
        run();
    }

    @Override
    public void run(){
        while(!queued.isEmpty()){
            var boardPlacement = queued.poll();
            double heuristic = Heuristic.calculateZoneHeuristic(new Game(boardPlacement.board()));
            finished.add(new BoardPlacementScore(boardPlacement.placement(), boardPlacement.board(), heuristic));
        }

        stop = true;
    }
}
