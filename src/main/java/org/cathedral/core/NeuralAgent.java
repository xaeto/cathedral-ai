package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.example.NeuralNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

public class NeuralAgent implements Agent {
    private NeuralNetwork neuralNetwork;
    @Override
    public String name(){
        return "NeuralAgent";
    }

    public NeuralAgent(NeuralNetwork network){
        this.neuralNetwork = network;
    }

    private INDArray preprocessGameState(Game game){
        var board = NeuralNetwork.GenerateBoardMatrix(game);
        return Nd4j.create(board);
    }
    private int processOutput(INDArray array) {
        int bestMove = -1;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < array.length(); i++) {
            double predictedValue = array.getDouble(i);

            // Assuming higher predicted values are better (you may need to adjust based on your network)
            if (predictedValue > bestValue) {
                bestValue = predictedValue;
                bestMove = i;
            }
        }

        return bestMove;
    }

    int countOccurences(int [][]array, int id){
        int count = 0;
        for(int x = 0; x < 10; ++x){
            for(int y = 0; y < 10; ++y){
                if(array[x][y] == id){
                    count += 1;
                }
            }
        }

        return count;
    }

    private int calculatePositionScore(Game gameState) {
        // Get the placement of interest (e.g., the last placement in the game)
        Placement placement = gameState.lastTurn().getAction();

        // Extract position coordinates
        int x = placement.position().x();
        int y = placement.position().y();

        // Calculate the distance from the center
        int center = 4;  // Center position
        int distanceFromCenter = Math.abs(x - center) + Math.abs(y - center);

        // Assign a higher score to placements closer to the center
        // Adjust the weights based on your scoring criteria
        int centerScore = 10 * (10 - distanceFromCenter);

        // Assign a lower score to placements closer to the edges
        int edgeScore = 2 * Math.min(Math.min(x, 9 - x), Math.min(y, 9 - y));

        // Total position score
        return centerScore - edgeScore;
    }

    public Placement findBestMove(List<Placement> possiblePlacements, Game gameState) {
        var buildingCount = gameState.getPlacableBuildings(gameState.getCurrentPlayer())
                .stream().map(c-> c.getId())
                .mapToInt(c-> c.intValue())
                .count();
        int owned = gameState.getCurrentPlayer() == Color.Black ? -2 : -1;
        System.out.println(owned);
        var state = NeuralNetwork.GenerateBoardMatrix(gameState);
        long count = buildingCount + countOccurences(state, owned);
        Placement best = null;

        int maxScore = Integer.MIN_VALUE;

        for (var placement : possiblePlacements){
            if(gameState.takeTurn(placement, true)){
                var nextField = NeuralNetwork.GenerateBoardMatrix(gameState);
                NeuralNetwork.printMatrix(nextField);
                // long nextFieldCount = buildingCount + countOccurences(nextField, owned);
                int score = calculatePositionScore(gameState);
                System.out.println("Score: " + score);
                if(score > maxScore){
                    best = placement;
                }
                gameState.undoLastTurn();
            }
        }
        return best;
    }

    public int evaluateMove(Placement placement, Game gameState) {
        // Example scoring criteria: maximize captured opponent pieces
        int opponentPiecesCaptured = countCapturedOpponentPieces(gameState);

        // You can add more criteria and weigh them accordingly

        return opponentPiecesCaptured * 2;
    }
    private int countCapturedOpponentPieces(Game gameState) {
        Board board = gameState.getBoard();
        Color currentPlayer = gameState.getCurrentPlayer();
        Color opponentColor = (currentPlayer == Color.Black) ? Color.White : Color.Black;

        int capturedPiecesCount = 0;

        for (Placement placement : board.getPlacedBuildings()) {
            if (placement.building().getColor() == opponentColor) {
                boolean isCaptured = isSurrounded(placement, board);
                if (isCaptured) {
                    capturedPiecesCount++;
                }
            }
        }

        return capturedPiecesCount;
    }

    private boolean isSurrounded(Placement placement, Board board) {
        List<Building> adjacentBuildings = getAdjacentBuildings(placement, board);
        return adjacentBuildings.isEmpty();
    }

    private List<Building> getAdjacentBuildings(Placement placement, Board board) {
        List<Building> adjacentBuildings = new ArrayList<>();
        for (Placement placed : board.getPlacedBuildings()) {
            if (areBuildingsAdjacent(placement, placed)) {
                adjacentBuildings.add(placed.building());
            }
        }
        return adjacentBuildings;
    }

    public boolean isAdjacentTo(Position first, Position second) {
        int dx = Math.abs(first.x() - second.x());
        int dy = Math.abs(first.y() - second.y());

        // Two positions are adjacent if their differences are at most 1 in either x or y direction
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    private boolean areBuildingsAdjacent(Placement placement1, Placement placement2) {
        List<Position> form1 = placement1.form();
        List<Position> form2 = placement2.form();

        for (Position position1 : form1) {
            for (Position position2 : form2) {
                Position realPosition1 = position1.plus(placement1.position());
                Position realPosition2 = position2.plus(placement2.position());

                if (isAdjacentTo(realPosition1, realPosition2)) {
                    return true;
                }
            }
        }

        return false;
    }


    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        // Preprocess the game state to create input for the neural network
        List<Placement> possiblePlacements = new ArrayList<Placement>();
        for(Building building : game.getPlacableBuildings(game.getCurrentPlayer())){
            for(Direction direction : building.getTurnable().getPossibleDirections()){
                for(int x = 0; x < 10; ++x){
                    for(int y = 0; y < 10; ++y){
                        var placement = new Placement(new Position(x,y), direction, building);
                        if(game.takeTurn(placement, true)){
                            possiblePlacements.add(placement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }

        // Preprocess the game state to create input for the neural network
        INDArray input = preprocessGameState(game);

        // Use the neural network to predict the best move
        INDArray output = neuralNetwork.predict(input);

        // Process the output to determine the best move
        int bestMoveIndex = processOutput(output);

        try{
            if (bestMoveIndex >= 0 && bestMoveIndex < possiblePlacements.size()) {
                // Select the best move based on evaluation criteria
                Placement bestPlacement = findBestMove(possiblePlacements, game);
                System.out.println(bestPlacement);
                neuralNetwork.save("./data.txt");
                return Optional.of(bestPlacement);
            }
        } finally {
            if(game.isFinished()){
                System.out.println("Done");
            }
        }
        // Ensure the index corresponds to a valid placement
        return Optional.empty();
    }
}
