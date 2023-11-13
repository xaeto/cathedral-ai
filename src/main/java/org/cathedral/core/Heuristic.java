package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class Heuristic {
    public static double calculatePlacementScore(Placement placement){
        if(placement.building() == Building.White_Tower || placement.building() == Building.Black_Tower){
            return placement.building().score() * 2;
        }
        return placement.building().score();
    }

    public static int getFieldCountByColor(Board board, Color color) {
        var field = board.getField();

        return Arrays.stream(field)
                .flatMap(Arrays::stream)
                .filter(cell -> cell == color)
                .mapToInt(c -> 1)
                .sum();
    }

    private static boolean isValidPosition(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    private static double buildingHeuristic(Placement placement){
        if(placement.building() == Building.White_Stable || placement.building() == Building.Black_Stable){
            return placement.building().score()*10;
        }
        return 0;
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

    private static double block(Game game) {
        var placement = game.lastTurn().getAction();
        game.undoLastTurn();

        var enemyPossiblePlacements = game.getBoard().getPlacableBuildings(game.getCurrentPlayer().opponent());
        double currentEnemyFieldScore = getFieldCountByColor(game.getBoard(), game.getCurrentPlayer().opponent().subColor());

        boolean reset = false;
        double blockedScore = 0.0;

        for (var enemyBuildings : enemyPossiblePlacements){
            for(var enemyPlacement : enemyBuildings.getPossiblePlacements(game.getBoard())){
                game.takeTurn(placement);
                game.takeTurn(enemyPlacement);
                double eval = getFieldCountByColor(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
                if(eval + 3 > currentEnemyFieldScore){
                    reset = true;
                    blockedScore = eval*placement.building().score();
                    break;
                }
            }
        }

        if(reset){
            game.undoLastTurn();
        }

        return blockedScore;
    }
    private static double blockEnemyBuildings(Game game){
        if(game.lastTurn() != null){
            var cp = game.copy();
            var turn = cp.lastTurn().getAction();
            Placement previousEnemyPlacement = null;

            double currentScore = getFieldCountByColor(cp.getBoard(), cp.getCurrentPlayer());
            var buildings = cp.getPlacableBuildings(game.getCurrentPlayer());
            for (Building b : buildings){
                for(var placement : b.getPossiblePlacements(cp)){
                    var placementGameCopy = cp.copy();
                    placementGameCopy.takeTurn(placement, true);
                    double score = getFieldCountByColor(placementGameCopy.getBoard(), cp.getCurrentPlayer());
                    if(score >= currentScore){
                        previousEnemyPlacement = placement;
                    }
                }
            }
            cp.undoLastTurn();

            if(previousEnemyPlacement != null){
                cp.undoLastTurn();
                if(cp.takeTurn(turn, true)){
                    if(!cp.takeTurn(previousEnemyPlacement)){
                        return 10;
                    }
                }
            }
        }

        return 0;
    }

    private static double calculatePlayerHeuristic(Game game, Color color){
        var board = game.getBoard();
        double playerFields = getFieldCountByColor(board, color) * 2;

        if (game.lastTurn().getTurnNumber() <= 2) {
            return calculateCenterHeuristic(game);
        }

        // 1. Gebietskampf (Gebiete einnehmen, Gegner davon abhalten)
        // 2. Gebäude in der mitte platzieren (Welche Gebäude hat der Gegner zur Verfügung), gegnerische Gebaeude sollen
        // in eingenomme Zonen platziert werden.
        // 3. Auffuellen mit kleinen Gebaeuden
        // 4. Auffuellen der eingenommenen Gebiete
        return playerFields
                + playerFields + game.lastTurn().getAction().building().score() * 2
                + getFieldCountByColor(board, color.subColor())*10
                + calculateScoreHeuristic(game)
                + calculateDistanceToCenter(game.lastTurn().getAction().position())
                + block(game)*100;

                // + capturedFields
                // - enemyFields
                // - capturedEnemyFields
                // + buildingHeuristic(game.lastTurn().getAction())
                // + surroundEmptyFieldsHeuristic(game)
                // + edgeBuildingHeuristic(game.lastTurn().getAction());
    }

    private static double evaluateMaterial(Game game){
        return game.score().getOrDefault(game.getCurrentPlayer(), 0)
               - game.score().getOrDefault(game.getCurrentPlayer().opponent(), 0);
    }

    private static double calculateScoreHeuristic(Game game) {
        int currentPlayerScore = game.score().getOrDefault(game.getCurrentPlayer(), 0);
        int opponentScore = game.score().getOrDefault(game.getCurrentPlayer().opponent(), 0);
        return currentPlayerScore - opponentScore;
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
    private static double calculateDistanceToCenter(Position position) {
        // Implementiere eine Logik zur Berechnung der Entfernung zum Zentrum
        int centerX = 5; // Annahme: Spielfeldgröße ist 10x10
        int centerY = 5;
        return Math.abs(position.x() - centerX) + Math.abs(position.y() - centerY);
    }

    public static double calculateHeuristics(Game game){
        double heuristic = calculatePlayerHeuristic(game, game.getCurrentPlayer());
        return heuristic;
    }
}
