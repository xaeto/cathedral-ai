package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.*;
import org.cathedral.core.StageBuildings;

import java.util.Arrays;
import java.util.Comparator;

public abstract class Heuristic {
    private double weight;
    public Heuristic(double weight){
        this.weight = weight;
    }

    public double getWeight(){return this.weight;}
    public abstract double eval(final Game game, int depth);

    public static int countFieldsByPlayerId(Board board, Color color){
        var field = board.getField();
        int count = 0;
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                if(field[y][x] == color){
                    count++;
                }
            }
        }

        return count;
    }

    private static boolean isValidPosition(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }


    private static double calculateCenterControlHeuristic(Board board, Color color) {
        var field = board.getField();
        int centerX = board.getField().length / 2;
        int centerY = board.getField().length / 2;
        int centerSize = 3; // Größe des Zentrums (3x3)

        int centerControl = 0;
        int totalCenterPositions = 0;

        for (int y = centerY - centerSize / 2; y <= centerY + centerSize / 2; y++) {
            for (int x = centerX - centerSize / 2; x <= centerX + centerSize / 2; x++) {
                if (isValidPosition(x, y)) {
                    totalCenterPositions++;

                    if (field[y][x] == color) {
                        centerControl++;
                    }
                }
            }
        }

        // Wenn das Zentrum leer ist, bewerte es neutral
        if (totalCenterPositions == 0) {
            return 0.0;
        }

        // Berechne den Anteil der Kontrolle über das Zentrum
        double centerControlRatio = (double) centerControl / totalCenterPositions;

        // Skaliere den Wert auf eine Skala zwischen -1 und 1 (neutral in der Mitte)
        double scaledCenterControl = (centerControlRatio - 0.5) * 2.0;

        // Belohne die Kontrolle über das Zentrum
        double centerControlBonus = 1.5; // Experimentiere mit diesem Wert

        return scaledCenterControl * centerControlBonus;
    }

    private static double buildingHeuristic(Placement placement){
        if(placement.building() == Building.White_Stable || placement.building() == Building.Black_Stable){
            return placement.building().score()*10;
        }
        return 0;
    }
    private static double preferNearEnemyBuildingsHeuristic(Game game) {
        var currentPlayer = game.getCurrentPlayer();
        var enemy = currentPlayer.opponent();
        var board = game.getBoard();

        double nearEnemyBonus = 0.0;

        // Iteriere über die Gebäude des aktuellen Spielers
        for (var playerBuilding : game.getPlacableBuildings(currentPlayer)) {
            for (var placement : playerBuilding.getPossiblePlacements(board)) {
                // Simuliere das Platzieren des Gebäudes
                var copyBoard = board.copy();
                copyBoard.placeBuilding(placement, true);

                // Iteriere über die Gebäude des Gegners
                for (var enemyBuilding : board.getPlacableBuildings(enemy)) {
                    for (var enemyPlacement : enemyBuilding.getPossiblePlacements(copyBoard)) {
                        // Berechne die Entfernung zwischen den Gebäuden
                        double distance = calculateDistance(placement.position(), enemyPlacement.position());

                        // Berücksichtige die Entfernung in der Bewertung
                        nearEnemyBonus += 1.0 / (distance + 1);
                    }
                }
            }
        }

        return nearEnemyBonus;
    }

    // Hilfsfunktion zur Berechnung der Entfernung zwischen zwei Positionen
    private static double calculateCenterHeuristic(Game game){
        var turn = game.lastTurn().getAction();
        if(turn.x() > 3 && turn.x() < 6 && turn.y() < 3 && turn.y() < 6){
            return 5;
        }

        return 0;
    }

    private static double edgeBuildingHeuristic(Placement placement) {
        if(placement == null)
            return 0.0;
        int x = placement.position().x();
        int y = placement.position().y();
        int boardSize = 10;
        int edgeBonus = 2; // Bonus für Randplatzierungen

        if (x == 0 || x == boardSize - 1 || y == 0 || y == boardSize - 1) {
            return edgeBonus;
        }

        return 0;
    }

    private static double createsNewZone(Game game){
        if(game.lastTurn().hasAction()){
            var lastTurn = game.lastTurn().copy();
            game.undoLastTurn();

            double score = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
            game.takeTurn(lastTurn.getAction(), true);

            double nextScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
            game.takeTurn(lastTurn.getAction(), true);

            double res = 0;
            if((nextScore - score) == 1){
                res = -1.0;
            }
            if((nextScore - score) > 1){
                res = -0.5;
            }
            if((nextScore - score) > 4){
                res = 1.0;
            }
            if((nextScore - score) >= 8){
                res = 5.0;
            }
            if((nextScore - score) >= 10){
                res = 50;
            }
            return res;
        }

        return 0;
    }

    public static double normalize(double value, double min, double max) {
        return 1 - ((value - min) / (max - min));
    }

    private static double placedInGrayArea(Game game){
        if(game.lastTurn().hasAction()){
            var action = game.lastTurn().copy().getAction();
            game.undoLastTurn();
            var field = game.getBoard().getField();
            if(field[action.y()][action.x()] == game.getCurrentPlayer().opponent().subColor()){
                return 1 * action.building().score();
            }
            game.takeTurn(action, false);
        }

        return 1;
    }

    private static double distanceToNearestEnemyBuilding(Game game, Position position) {
        var currentPlayer = game.getCurrentPlayer();
        var enemy = currentPlayer.opponent();
        var board = game.getBoard();
        var placedByEnemy = board.getPlacedBuildings();

        double minDistance = Double.MAX_VALUE;

        for (var placed : placedByEnemy) {
            var silhouette = placed.building().silhouette(placed.direction());
            for (var s : silhouette) {
                double distance = calculateDistance(position, s);
                minDistance = Math.min(minDistance, distance);
            }
        }

        return minDistance;
    }

    // Helper function to calculate distance between two positions
    private static double calculateDistance(Position pos1, Position pos2) {
        int dx = pos1.x() - pos2.x();
        int dy = pos1.y() - pos2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double calculateScoreDiff(Game game){
        return game.score().getOrDefault(game.getCurrentPlayer(), 0)
                - game.score().getOrDefault(game.getCurrentPlayer().opponent(), 0);
    }

    private static double cathedralLost(Game game){
        boolean hasCathedral = game.getBoard().getPlacedBuildings().stream().anyMatch(c-> c.building() == Building.Blue_Cathedral);
        if(game.getCurrentPlayer() == Color.Black){
            return hasCathedral ? 0 : 1;
        } else {
            return hasCathedral ? 1 : -1;
        }
    }

    private static double preferCornersHeuristic(Game game) {
        if(!game.lastTurn().hasAction())
            return 0;

        var action = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        double result = 0;
        var max = game.getPlacableBuildings(game.getCurrentPlayer())
                .stream().max(Comparator.comparingDouble(Building::score));

        game.takeTurn(action, false);
        if(max.isPresent()){
            for(var maxPos : max.get().getAllPossiblePlacements()){
                if(!game.takeTurn(maxPos, false)){
                    result += 1;
                } else {
                    result -= 1;
                    game.undoLastTurn();
                }
            }
        }
        return result;
    }

    private static double discourageBigBuildingsHeuristic(Game game) {
        if(!game.lastTurn().hasAction())
            return 0;

        var action = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        double result = 0;
        var max = game.getPlacableBuildings(game.getCurrentPlayer())
                .stream().max(Comparator.comparingDouble(Building::score));

        game.takeTurn(action, false);
        if(max.isPresent()){
            for(var maxPos : max.get().getAllPossiblePlacements()){
                if(!game.takeTurn(maxPos, false)){
                    result += 1;
                } else {
                    result -= 1;
                    game.undoLastTurn();
                }
            }
        }
        return result;
    }

    private static double earlyGameHeuristics(Game game, Color color){
        double heuristic = 0;
        heuristic += block(game) * 50;
        heuristic += countFieldsByPlayerId(game.getBoard(), color)*25;
        heuristic -= countFieldsByPlayerId(game.getBoard(), color.opponent())*100;
        heuristic -= countFieldsByPlayerId(game.getBoard(), color.opponent().subColor())*100;
        heuristic += countFieldsByPlayerId(game.getBoard(), color.subColor())*1000;
        heuristic += calculateScoreDiff(game)*75;
        heuristic -= placedInGrayArea(game)*100;
        return heuristic;
    }

    private static double midGameHeuristics(Game game, Color color){
        double heuristic = 0;
        heuristic += block(game) * 150;
        heuristic += countFieldsByPlayerId(game.getBoard(), color)*25;
        heuristic -= countFieldsByPlayerId(game.getBoard(), color.opponent())*100;
        heuristic += countFieldsByPlayerId(game.getBoard(), color.subColor())*10000;
        heuristic -= placedInGrayArea(game)*100;
        heuristic += calculateScoreDiff(game)*100;
        heuristic += cathedralLost(game)*100;
        return heuristic;
    }

    private static double endGameHeuristics(Game game, Color color){
        double heuristic = 0;
        heuristic += block(game) * 500;
        heuristic += countFieldsByPlayerId(game.getBoard(), color)*25;
        heuristic -= countFieldsByPlayerId(game.getBoard(), color.opponent())*100;
        heuristic -= countFieldsByPlayerId(game.getBoard(), color.subColor())*15;
        heuristic += calculateScoreDiff(game)*50;
        heuristic += cathedralLost(game)*200;
        return heuristic;
    }

    private static double calculatePlayerHeuristic(Game game, Color color){
        var stage = StageBuildings.getStage(game, game.getCurrentPlayer());
        double heuristic = Double.NEGATIVE_INFINITY;
        switch (stage){
            case None -> heuristic = 0;
            case BlackEarlyGameBuildings, WhiteEarlyGameBuildings -> heuristic = earlyGameHeuristics(game, game.getCurrentPlayer());
            case BlackMidGameBuildings, WhiteMidGameBuildings -> heuristic = midGameHeuristics(game, game.getCurrentPlayer());
            case BlackEndgameGameBuildings, WhiteEndgameGameBuildings -> heuristic = endGameHeuristics(game, game.getCurrentPlayer());
        }

        return heuristic;
    }

    private static double block(Game game){
        if(game.lastTurn().hasAction()){
            var placement = game.lastTurn().copy().getAction();
            game.undoLastTurn();

            double score = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().subColor());
            game.takeTurn(placement, false);
            double nextScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());

            if(score > nextScore){
                return -(nextScore - score);
            }

            // blocked Score
            if(score == nextScore){
                return 10;
            }

            if(nextScore < score){
                return 100*(score * nextScore);
            }
        }

        return 0;
    }


    public static double calculateZoneHeuristic(Game game){
        double heuristic = calculatePlayerHeuristic(game, game.getCurrentPlayer());
        return heuristic;
    }
}
