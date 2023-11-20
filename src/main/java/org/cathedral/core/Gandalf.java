package org.cathedral.core;
import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import java.util.*;
import java.util.stream.Collectors;

//TODO Heuristiken einführen um die listen zu filtern bevor sie simuliert werden

public class Gandalf implements Agent {

    private static final int COLORWEIGHT = 1;
    private static final int CONQUERWEIGHT = 2;
    private static final int BLOCKPLACEABLEWEIGHT = 3;

    private Placement bestPlacementFromLastTurn;
    private int bestScoreFromLastTurn;

    public Optional<Placement> calculateTurn(Game game, int timeForTurn, int timeBonus) {

        Color currentPlayer = game.getCurrentPlayer();
        GameStage gameStage = GameStage.determineStage(game);
        List<PlacementWithBoard> currentPlayerPlacements = simulatePlacements(game.getBoard(), currentPlayer,gameStage);

        PlacementWithBoard bestPlacement = null;
        int bestScoreAfterOpponentTurn = Integer.MIN_VALUE;

        for (PlacementWithBoard placementWithBoard : currentPlayerPlacements) {
            int currentScore = evaluateBoard(placementWithBoard.board, currentPlayer);

            int opponentBestScore = Integer.MIN_VALUE;
            List<PlacementWithBoard> opponentPlacements = simulatePlacements(placementWithBoard.board, currentPlayer.opponent(),gameStage);

            for (PlacementWithBoard opponentPlacement : opponentPlacements) {
                int opponentScore = evaluateBoard(opponentPlacement.board, currentPlayer.opponent());


                if (opponentScore > opponentBestScore) {
                    opponentBestScore = opponentScore;
                }
            }

            //game.undoLastTurn();
            int scoreAfterOpponentTurn = currentScore - opponentBestScore;


            if (scoreAfterOpponentTurn > bestScoreAfterOpponentTurn) {
                bestScoreAfterOpponentTurn = scoreAfterOpponentTurn;
                bestPlacement = placementWithBoard;
            }
        }

        //safe best turn and score for later evaluation
        bestPlacementFromLastTurn = bestPlacement.placement;
        bestScoreFromLastTurn = bestScoreAfterOpponentTurn;

        return Optional.ofNullable(bestPlacement).map(PlacementWithBoard::placement);
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
            evaluation = "Perfekt! Das war der beste mögliche Zug mit einem Score von " + bestScoreFromLastTurn +
                    " mit dem Gebäude " + bestPlacementFromLastTurn +".";
        } else if (scoreDifference > 0) {
            evaluation = "Ein besserer Zug war möglich. Du hast " + scoreDifference + " Punkte verpasst." +
                    bestPlacementFromLastTurn + " wäre wahrscheinlich besser gewesen.";
        } else {
            evaluation = "Mehr Punkte hättest du nicht machen können. Es kann aber sein, dass das setzen von"
                    + bestPlacementFromLastTurn + " besser gewesen wäre" ;
        }

        return evaluation;
    }


    //placementsimulation

    private List<PlacementWithBoard> simulatePlacements(Board board, Color player, GameStage gameStage) {
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
        newOnes.sort((p1, p2) -> Integer.compare(p2.placement().building().score(), p1.placement().building().score()));
        return newOnes;
    }




    //helpfunktion, true if buildings cornerdistance exactly "int distance"
    private boolean isCornerExactlyDistanceFromWall(Position corner, int width, int height, int distance) {
        return (corner.x() == distance || corner.x() == width - 1 - distance) ||
                (corner.y() == distance || corner.y() == height - 1 - distance);
    }

    // biggest buildings
    private List<Placement> getPlacementsOfHighestScoringBuildings(Game game) {

        int highestScore = game.getPlacableBuildings().stream()
                .mapToInt(Building::score)
                .max()
                .orElse(-1);

        if (highestScore == -1) {
            return Collections.emptyList();
        }

        List<Placement> results = game.getPlacableBuildings().stream()
                .filter(building -> building.score() == highestScore)
                .flatMap(building -> building.getPossiblePlacements(game).stream())
                .collect(Collectors.toList());

        return results;
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

    private int evaluateBoard(Board board, Color currentPlayer) {

        int playerColorCount = getColorOnBoard(board, currentPlayer);
        int playerAreaControl = getConqueredOnBoard(board, currentPlayer);

        int opponentColorCount = getColorOnBoard(board, currentPlayer.opponent());
        int opponentAreaControl = getConqueredOnBoard(board, currentPlayer.opponent());

        List<Building> opponentNotPlaceableBuildings = findNotPlaceableBuildings(board, currentPlayer.opponent());

        // sum the score of notplacable buildings
        int lostOpponentScore = opponentNotPlaceableBuildings.stream()
                .mapToInt(Building::score)
                .sum();

        int score = COLORWEIGHT * (playerColorCount - opponentColorCount)
                + CONQUERWEIGHT * (playerAreaControl - opponentAreaControl)
                + BLOCKPLACEABLEWEIGHT * lostOpponentScore;

        return score;
    }


    private record PlacementWithBoard(Placement placement, Board board){
    }

    private record weigthedPlacement(Placement placement, int weight){
    }

}
