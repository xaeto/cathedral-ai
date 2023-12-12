package org.cathedral.core;


import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//TODO Heuristiken einführen um die listen zu filtern bevor sie simuliert werden

public class Frodo implements Agent {

    private static final int COLORWEIGHT = 50;
    private static final int CONQUERWEIGHT = 51;
    private static final int BLOCKPLACEABLEWEIGHT = 53;
    private static final int PossibleConquers = 1;
    private Placement bestPlacementFromLastTurn;
    private int bestScoreFromLastTurn;

    public Optional<Placement> calculateTurn(Game game, int timeForTurn, int timeBonus) {
        System.out.println(timeForTurn);
        Color currentPlayer = game.getCurrentPlayer();
        GameStage gameStage = GameStage.determineStage(game);
        List<PlacementWithBoard> currentPlayerPlacements = simulatePlacements(game.getBoard(), currentPlayer, gameStage);
        List<List<PlacementWithBoardScore>> theList = new ArrayList<>();
        List<PlacementWithBoardScore> bestTurnsPlayer = new ArrayList<>();

        int bestScoreAfterOpponentTurn = Integer.MIN_VALUE;
        PlacementWithBoard bestPlacement;
        PlacementWithBoardScore bestPlacement1 = null;

        int time = 10;
        int depth = 5;
        int howmanyplacementstoconsider=20;
        int counter = 0;
        while (depth >0) {
            if (theList.size() == 0) {//first turn
                System.out.println("do i exist?");
                for (PlacementWithBoard placementWithBoard : currentPlayerPlacements) {
                    int currentScore = evaluateBoard(placementWithBoard.board, currentPlayer);

                    if (bestTurnsPlayer.size() < howmanyplacementstoconsider) { //if best turns only has less than 3 entries fill it up
                        bestTurnsPlayer.add(new PlacementWithBoardScore(placementWithBoard, currentScore));
                    } else { //if there is a better entry change it
                        for (PlacementWithBoardScore entry : bestTurnsPlayer) {
                            if (currentScore > entry.score()) {
                                bestTurnsPlayer.set(bestTurnsPlayer.indexOf(entry), new PlacementWithBoardScore(placementWithBoard, currentScore));
                                break;
                            }
                        }
                    }
                    if(placementWithBoard.board==null){
                        System.out.println("Hello");
                    }
                }
                theList.add(bestTurnsPlayer);
            }
            else{
                System.out.println("sizeoflist" + theList.size());
                System.out.println("lastTurn"+theList.get(theList.size()-1));

                GameStage gameStage1 = GameStage.MIDGAME;

                theList.add(giveitback(theList.get(theList.size()-1), counter%2, currentPlayer, gameStage1));
                System.out.println("depth" + depth);
                System.out.println("sizeoflist the second" + theList.size());
                theList.set(0, reflect_on_your_choices(theList, theList.get(theList.size()-1), counter % 2));
                System.out.println("sizeoflist3" + theList.size());
                if(theList.get(theList.size()-1).get(0).PlacewithBoard==null){
                    break;
                }
            }
            --depth;
            ++counter;

        }
        if(theList.size() == 0){
            System.out.println("FAILURE");
        }
        else{
            for (PlacementWithBoardScore entry : theList.get(0)) { //return boardPlac of highest score
                if (bestPlacement1 == null) {
                    bestPlacement1 = entry;
                } else {
                    if (bestPlacement1.score < entry.score) {
                        bestPlacement1 = entry;
                    }
                }
            }
        }

        bestPlacement = bestPlacement1.PlacewithBoard;
        //safe best turn and score for later evaluation
        bestPlacementFromLastTurn = bestPlacement.placement;
        bestScoreFromLastTurn = bestScoreAfterOpponentTurn;

        if (bestPlacement == null) {
            System.out.println("placement ist null");
            return Optional.empty();
        } else {
            return Optional.ofNullable(bestPlacement).map(PlacementWithBoard::placement);
        }
    }

    public List<PlacementWithBoardScore> giveitback(List<PlacementWithBoardScore> lastTurn, int whosturn, Color currentPlayer, GameStage gameStage){
        List<PlacementWithBoardScore> newTurn = new ArrayList<>();

        for (PlacementWithBoardScore entry : lastTurn) {
            List<PlacementWithBoard> Placements = new ArrayList<>();

            if (whosturn == 0){//OpponentTurn
                Placements = simulatePlacements(entry.PlacewithBoard().board, currentPlayer, gameStage);

            }
            else{
                Placements = simulatePlacements(entry.PlacewithBoard().board, currentPlayer.opponent(), gameStage);

            }
            PlacementWithBoard hilf =null;
            int opponentScore;
            int BestopponentScore = Integer.MIN_VALUE;
            for (PlacementWithBoard opponentPlacement : Placements) { // searches for best Play by the opponent
                if(opponentPlacement.board==null){
                    System.out.println("Cry");
                }
                if (whosturn == 0) {//OpponentTurn
                    opponentScore = evaluateBoard(opponentPlacement.board, currentPlayer);
                }
                else{
                    opponentScore = evaluateBoard(opponentPlacement.board, currentPlayer.opponent());
                }
                if (opponentScore > BestopponentScore) {
                    BestopponentScore = opponentScore;
                    hilf = opponentPlacement;
                }
            }
            newTurn.add(new PlacementWithBoardScore(hilf, BestopponentScore));
        }
        return newTurn;
    }

    public List<PlacementWithBoardScore> reflect_on_your_choices (List<List<PlacementWithBoardScore>> theList, List<PlacementWithBoardScore> lastTurns, int whosturn ){    //reflect on your choices
        System.out.println("board2");
        if(lastTurns.get(0).PlacewithBoard==null){
            System.out.println("board");
            return theList.get(0);
        }
        System.out.println("board2");
        List<PlacementWithBoardScore> bestTurnsplayer =new ArrayList<>();
        bestTurnsplayer = theList.get(0);
        for (PlacementWithBoardScore entry : bestTurnsplayer) {
            PlacementWithBoardScore entry2 = lastTurns.get(bestTurnsplayer.indexOf(entry));
            switch(whosturn){
                case 0:
                    bestTurnsplayer.set(bestTurnsplayer.indexOf(entry), new PlacementWithBoardScore(entry.PlacewithBoard, entry.score + entry2.score));
                    break;
                case 1:
                    bestTurnsplayer.set(bestTurnsplayer.indexOf(entry), new PlacementWithBoardScore(entry.PlacewithBoard, entry.score - entry2.score));
            }
        }
        return bestTurnsplayer;
    }
    @Override
    public String evaluateLastTurn(Game game) {
        Placement lastPlacement = game.lastTurn().getAction();
        String evaluation;

        if (lastPlacement == null) {
            return "Kein Zug gemacht.";
        }

        if (bestPlacementFromLastTurn == null) {
            return "Es wurde kein bester Zug berechnet.";
        }

        int lastTurnScore = evaluateBoard(game.getBoard(), game.getCurrentPlayer());

        int scoreDifference = bestScoreFromLastTurn - lastTurnScore;

        if (lastPlacement.equals(bestPlacementFromLastTurn)) {
            evaluation = "Perfekt! Das war der beste mögliche Zug mit einem Score von " + bestScoreFromLastTurn + ".";
        } else if (scoreDifference > 0) {
            evaluation = "Ein besserer Zug war möglich. Du hast " + scoreDifference + " Punkte verpasst.";
        } else {
            evaluation = "Mehr Punkte hättest du nicht machen können.";
        }

        return evaluation;
    }


    //placementsimulation

    private List<PlacementWithBoard> simulatePlacements(Board board, Color player, GameStage gameStage) {// can be optimized by ai
        Board current = board.copy();

        List<PlacementWithBoard> newOnes = new ArrayList<>();
        List<Building> placableBuildings = current.getPlacableBuildings(player);

        for (Building free : placableBuildings) {

            // Preselect objects based on gamestate
            boolean considerBuilding = false;
            switch (gameStage) {
                case OPENER:
                    if (free.getName().contains("Tower") || free.getName().contains("Cathedral") || free.getName().contains("Academy")){
                        considerBuilding = true;
                    }
                    break;
                case EARLYGAME:
                    if (free.score() > 4) {
                        considerBuilding = true;
                    }
                    break;
                case MIDGAME:
                    if (free.score() > 2) {
                        considerBuilding = true;
                    }
                    break;
                case LATEGAME:
                    considerBuilding = true;
                    break;
            }

            if (!considerBuilding) {
                continue; //continue without adding the object
            }

            for (Direction direct : free.getTurnable().getPossibleDirections()) {

                for (int y = 0; y < 10; ++y) {
                    for (int x = 0; x < 10; ++x) {
                        Placement possible = new Placement(x, y, direct, free);
                        if (current.placeBuilding(possible)) {
                            newOnes.add(new PlacementWithBoard(possible, current));
                            current = board.copy();
                        }
                    }
                }
            }
        }
        return newOnes;
    }



    //helpfunktion, true if buildings cornerdistance exactly "int distance"
    private boolean isCornerExactlyDistanceFromWall(Position corner, int width, int height, int distance) {
        return (corner.x() == distance || corner.x() == width - 1 - distance) ||
                (corner.y() == distance || corner.y() == height - 1 - distance);
    }



    // count colored buildings for player
    private int getColorOnBoard(Board board, Color currentPlayer) {
        int myColorCount = 0;

        for (int y = 0; y < board.getField().length; ++y) {
            for (int x = 0; x < board.getField()[y].length; ++x) {
                Color cellColor = board.getField()[y][x];
                // Zähle die Farbenanzahl
                if (cellColor == currentPlayer) {
                    myColorCount++;
                }
            }
        }
        return myColorCount;
    }

    // count conquered areal for player
    private int getConqueredOnBoard(Board board, Color currentPlayer) {
        int myArea = 0;
        Color subColor = currentPlayer.subColor();

        for (int y = 0; y < board.getField().length; ++y) {
            for (int x = 0; x < board.getField()[y].length; ++x) {
                Color cellColor = board.getField()[y][x];
                if (cellColor == subColor) {
                    myArea++;
                }
            }
        }

        return myArea;
    }

    private List<Building> findNotPlaceableBuildings(Board board, Color player) {
        List<Building> notPlaceableBuildings = new ArrayList<>();
        List<Building> placableBuildings = board.getPlacableBuildings(player);

        for (Building building : placableBuildings) {
            boolean canBePlaced = false;

            for (Direction direction : building.getTurnable().getPossibleDirections()) {
                if (canBePlaced) {
                    break; // end if placeable
                }
                for (int y = 0; y < board.getField().length; y++) {
                    for (int x = 0; x < board.getField()[y].length; x++) {
                        Placement placement = new Placement(x, y, direction, building);
                        Board tempBoard = board.copy(); // copy board for every placement
                        if (tempBoard.placeBuilding(placement)) {
                            canBePlaced = true;
                            break; // end if placable
                        }
                    }
                    if (canBePlaced) {
                        break; // end if placeable
                    }
                }
            }

            if (!canBePlaced) {
                notPlaceableBuildings.add(building); // add building to list of not placeable
            }
        }
        return notPlaceableBuildings;
    }
    private int[][] board_into_matrice(Board board) {
        int[][] a = new int[10][10]; //board into matrix

        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                a[x][y] = board.getField()[x][y].getId();
            }

        }
        return a;
    }

    private int getplayerconqueries3(Board board, Color player){
        int value = 0;
        int[][] matrix = board_into_matrice(board);
        int playerid = player.getId();

        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                if (matrix[x][y] == playerid){
                    // Überprüfe jede Richtung um den aktuellen Punkt
                    value += checkDirection(matrix, x, y, -1, 0, playerid); // Links
                    value += checkDirection(matrix, x, y, 1, 0, playerid);  // Rechts
                    value += checkDirection(matrix, x, y, 0, -1, playerid); // Oben
                    value += checkDirection(matrix, x, y, 0, 1, playerid);  // Unten
                }
            }
        }
        return value;
    }

    private int checkDirection(int[][] matrix, int x, int y, int dx, int dy, int playerid){
        int value = 0;
        int counter = 0;
        while (true) {
            x += dx;
            y += dy;
            counter++;

            // Überprüfe die Bereichsgrenzen nach der Aktualisierung von x und y
            if (x < 0 || x >= 10 || y < 0 || y >= 10) break;

            if (matrix[x][y] != playerid) {
                // Bewertung der Distanz zur nächsten nicht-eigenen Zelle
                value += value_conquerer(counter, matrix[x][y] == playerid ? 0 : 2, counter);
                break;
            }
        }
        return value;
    }

    private int value_conquerer(int distance, int enum_art, int counter){
        int max_wert = 10;
        int factor_own = 2;
        int factor_side = 3;
        int factor_enemy = 1;
        int value = 0;

        switch(enum_art){
            case 0: // Nächste Zelle ist vom Spieler
                value = (max_wert - distance) * factor_own;
                break;
            case 2: // Nächste Zelle ist außerhalb des Bretts oder vom Gegner
                value = (max_wert - distance) * (distance % 2 == 0 ? factor_side : factor_enemy);
                break;
            default:
                value = 0;
        }
        return value / counter;
    }



    private int evaluateBoard(Board board, Color currentPlayer) {

        int playerColorCount = getColorOnBoard(board, currentPlayer);
        int playerAreaControl = getConqueredOnBoard(board, currentPlayer);

        int opponentColorCount = getColorOnBoard(board, currentPlayer.opponent());
        int opponentAreaControl = getConqueredOnBoard(board, currentPlayer.opponent());

        int playerconqueries = getplayerconqueries3(board, currentPlayer);
        int opponentconqueries = getplayerconqueries3(board, currentPlayer.opponent());

        List<Building> opponentNotPlaceableBuildings = findNotPlaceableBuildings(board, currentPlayer.opponent());

        // sum the score of notplacable buildings
        int lostOpponentScore = opponentNotPlaceableBuildings.stream()
                .mapToInt(Building::score)
                .sum();

        int score = COLORWEIGHT * (playerColorCount - opponentColorCount)
                + CONQUERWEIGHT * (playerAreaControl - opponentAreaControl)
                + BLOCKPLACEABLEWEIGHT * lostOpponentScore
                + PossibleConquers * (playerconqueries-opponentconqueries);


        return score;
    }


    private record PlacementWithBoard(Placement placement, Board board){
    }
    private record PlacementWithBoardScore(PlacementWithBoard PlacewithBoard, int score){
    }

}