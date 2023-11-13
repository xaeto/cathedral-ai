package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.*;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class Heuristic {
    public static double calculatePlacementScore(Placement placement){
        if(placement.building() == Building.White_Tower || placement.building() == Building.Black_Tower){
            return placement.building().score() * 2;
        }
        return placement.building().score();
    }

    private static int countFieldsByPlayerId(Board board, Color color){
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
    private static double calculateDistance(Position pos1, Position pos2) {
        int dx = pos1.x() - pos2.x();
        int dy = pos1.y() - pos2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

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

    private static double blockEnemyBuildings(Game game){
        if(game.lastTurn() != null){
            var cp = game.copy();
            var turn = cp.lastTurn();
            Placement previousEnemyPlacement;

            double currentScore = countFieldsByPlayerId(cp.getBoard(), cp.getCurrentPlayer());
            var buildings = cp.getPlacableBuildings(game.getCurrentPlayer());
            for (Building b : buildings){
                for(var placement : b.getPossiblePlacements(cp)){
                    var placementGameCopy = cp.copy();
                    placementGameCopy.takeTurn(placement, true);
                    double score = countFieldsByPlayerId(placementGameCopy.getBoard(), cp.getCurrentPlayer());
                    if(score >= currentScore){
                        previousEnemyPlacement = placement;
                    }
                }
            }
            cp.undoLastTurn();
        }

        return 0;
    }

    private static double calculatePlayerHeuristic(Game game, Color color){
        var board = game.getBoard();
        double playerFields = countFieldsByPlayerId(board, color) * 2;
        double capturedFields = countFieldsByPlayerId(board, color.subColor());
        double enemyFields = countFieldsByPlayerId(board, color.opponent());
        double capturedEnemyFields = countFieldsByPlayerId(board, color.opponent().subColor());
        if (game.lastTurn() != null && game.lastTurn().getTurnNumber() <= 2) {
            return calculateCenterHeuristic(game);
        }
        return playerFields
                + capturedFields * 10
                - enemyFields
                - capturedEnemyFields
                + buildingHeuristic(game.lastTurn().getAction())
                + surroundEmptyFieldsHeuristic(game)
                + edgeBuildingHeuristic(game.lastTurn().getAction());
    }

    private static double surroundEmptyFieldsHeuristic(Game game) {
        var board = game.getBoard();

        int emptyFieldBonus = 1;

        long emptyFields = Arrays.stream(board.getField())
                .flatMap(Arrays::stream)
                .filter(color -> color == Color.None)
                .count();

        return emptyFields * emptyFieldBonus;
    }

    public static double calculateZoneHeuristic(Game game){
        double heuristic = calculatePlayerHeuristic(game, game.getCurrentPlayer());
        return heuristic;
    }
}
