package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;
import org.example.NeuralNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralAgent implements Agent {
    private NeuralNetwork neuralNetwork;
    private Heuristic[] heuristics;
    private static final double gamma = 0.9;
    @Override
    public String name(){
        return "NeuralAgent";
    }

    public NeuralAgent(NeuralNetwork network, Heuristic... heuristics){
        this.neuralNetwork = network;
        this.heuristics = heuristics;
    }

    private INDArray preprocessGameState(Game game){
        var board = NeuralNetwork.GenerateBoardMatrix(game);
        var flatten = Nd4j.create(board);
        return flatten.reshape(1, 100);
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
        return (int)Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(gameState, 1) * c.getWeight()).sum();
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

        double maxScore = Double.MIN_VALUE;

        for (var placement : possiblePlacements){
            if(gameState.takeTurn(placement, true)){
                var nextField = NeuralNetwork.GenerateBoardMatrix(gameState);
                NeuralNetwork.printMatrix(nextField);
                // long nextFieldCount = buildingCount + countOccurences(nextField, owned);
                double score = HeuristicsHelper.countFieldById(gameState.getBoard(), gameState.getCurrentPlayer().opponent());
                System.out.println("Score: " + score);
                if(score > maxScore){
                    best = placement;
                }
                gameState.undoLastTurn();
            }
        }
        return best;
    }
    private int evaluateZoneCapture(Placement placement, Game gameState) {
        int zoneCaptureReward = 0;

        // Check if the placement captures any opponent zone
        List<Building> adjacentOpponentBuildings = getAdjacentOpponentBuildings(placement, gameState);
        if (!adjacentOpponentBuildings.isEmpty()) {
            // You can adjust the reward based on the number of captured opponent zones
            zoneCaptureReward = adjacentOpponentBuildings.size() * 5; // Adjust the weight as needed
        }

        return zoneCaptureReward;
    }

    private List<Building> getAdjacentOpponentBuildings(Placement placement, Game game) {
        List<Building> adjacentOpponentBuildings = new ArrayList<>();
        for (Placement placed : game.getBoard().getPlacedBuildings()) {
            if (areBuildingsAdjacent(placement, placed) && placed.building().getColor() != game.getCurrentPlayer()) {
                adjacentOpponentBuildings.add(placed.building());
            }
        }
        return adjacentOpponentBuildings;
    }

    public int evaluateMove(Placement placement, Game gameState) {
        int opponentPiecesCaptured = countCapturedOpponentPieces(gameState);
        int zoneCaptureReward = evaluateZoneCapture(placement, gameState);

        // Get the score of the placed building
        int buildingScore = placement.building().score();

        // Adjust the reward based on the score of the building and captured zones
        int totalReward = (opponentPiecesCaptured + zoneCaptureReward) * 2;

        // Give more weight to higher-scoring buildings and captured zones
        totalReward += buildingScore * 3; // Adjust the weight as needed

        return totalReward;
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
    private int sampleAction(INDArray probabilities) {
        // Assuming probabilities is a row vector representing the probabilities of each action
        int numActions = (int)probabilities.length();

        // Convert probabilities to a cumulative distribution function (CDF)
        double[] cdf = new double[numActions];
        cdf[0] = probabilities.getDouble(0);
        for (int i = 1; i < numActions; i++) {
            cdf[i] = cdf[i - 1] + probabilities.getDouble(i);
        }

        // Sample a value between 0 and 1
        double sample = ThreadLocalRandom.current().nextDouble();

        // Find the action corresponding to the sampled value in the CDF
        for (int i = 0; i < numActions; i++) {
            if (sample < cdf[i]) {
                return i;
            }
        }

        // This should not happen, but return a default action if it does
        return 0;
    }
    private INDArray computeTargetProbabilities(NeuralNetwork neuralNetwork, List<INDArray> states, List<Integer> actions, List<Double> returns) {
        int numStates = states.size();
        int numActions = (int)neuralNetwork.getNumOutputs(); // Assuming the number of outputs is the number of possible actions

        INDArray targetProbabilities = Nd4j.zeros(numStates, numActions);

        for (int i = 0; i < numStates; i++) {
            // Forward pass to get current policy probabilities
            INDArray currentProbabilities = neuralNetwork.predict(states.get(i));

            // Compute the log probability of the selected action
            double logProbability = computeLogProbability(currentProbabilities, actions.get(i));

            // Compute the advantage (returns - baseline, where baseline can be the average return)
            double advantage = returns.get(i) - computeBaseline(returns);

            // Compute the gradient of the log probability with respect to the parameters
            INDArray gradient = computeGradientLogProbability(currentProbabilities, actions.get(i));

            // Compute the target probability (scaled by the advantage)
            INDArray targetProbability = computeScaledGradient(gradient, advantage, numStates);

            // Update the target probabilities matrix
            targetProbabilities.putRow(i, targetProbability);
        }

        return targetProbabilities;
    }

    private double computeLogProbability(INDArray probabilities, int action) {
        return Math.log(probabilities.getDouble(action));
    }

    private double computeBaseline(List<Double> returns) {
        // Simple baseline: average of returns
        return returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private INDArray computeGradientLogProbability(INDArray probabilities, int action) {
        INDArray gradient = probabilities.dup();
        gradient.putScalar(action, gradient.getDouble(action) - 1.0);
        return gradient;
    }

    private INDArray computeScaledGradient(INDArray gradient, double advantage, int numStates) {
        // Negate for gradient ascent
        return gradient.muli(advantage).divi(-numStates);
    }

    private void evaluatePlacement(Placement placement, Game game, List<INDArray> states, List<Integer> actions, List<Double> rewards) {
        if (game.takeTurn(placement, true)) {
            INDArray input = preprocessGameState(game);
            INDArray predictedMoves = neuralNetwork.predict(input);

            int action = sampleAction(predictedMoves);
            game.takeTurn(placement, true);

            double reward = evaluateMove(placement, game);
            System.out.println("Score: " + reward);

            states.add(input);
            actions.add(action);
            rewards.add(reward);

            game.undoLastTurn();
        }
    }

    private List<Placement> generatePossiblePlacements(Game game) {
        List<Placement> possiblePlacements = new ArrayList<>();
        for (Building building : game.getPlacableBuildings(game.getCurrentPlayer())) {
            for (Direction direction : building.getTurnable().getPossibleDirections()) {
                for (int x = 0; x < 10; ++x) {
                    for (int y = 0; y < 10; ++y) {
                        var placement = new Placement(new Position(x, y), direction, building);
                        if (game.takeTurn(placement, true)) {
                            possiblePlacements.add(placement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }
        return possiblePlacements;
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        List<Placement> possiblePlacements = new ArrayList<>();

        for (Building building : game.getPlacableBuildings(game.getCurrentPlayer())) {
            for (Direction direction : building.getTurnable().getPossibleDirections()) {
                for (int x = 0; x < 10; ++x) {
                    for (int y = 0; y < 10; ++y) {
                        var placement = new Placement(new Position(x, y), direction, building);
                        if (game.takeTurn(placement, true)) {
                            possiblePlacements.add(placement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }

        Placement best = null;
        double maxScore = Double.NEGATIVE_INFINITY;

        for (var placement : possiblePlacements) {
            if (game.takeTurn(placement, true)) {
                int positionScore = calculatePositionScore(game);
                double buildingScore = placement.building().score();

                // Prefer placements with higher scores
                double totalScore = positionScore + buildingScore;

                if (totalScore > maxScore) {
                    best = placement;
                    maxScore = totalScore;
                }

                // Observe the reward
                double reward = evaluateMove(placement, game);

                // Forward pass
                INDArray input = preprocessGameState(game);
                INDArray predictedMoves = neuralNetwork.predict(input);

                // Sample an action based on the predicted moves (you may use a softmax function)
                int action = sampleAction(predictedMoves);

                // Adjust the reward based on the total score
                reward *= totalScore;

                // Store the state, action, and adjusted reward for later use in updating the model
                List<INDArray> states = Collections.singletonList(input);
                List<Integer> actions = Collections.singletonList(action);
                List<Double> rewards = Collections.singletonList(reward);

                // Compute returns (cumulative rewards)
                double cumulativeReward = 0;
                List<Double> returns = new ArrayList<>();
                for (int t = rewards.size() - 1; t >= 0; t--) {
                    cumulativeReward = rewards.get(t) + gamma * cumulativeReward;
                    returns.add(cumulativeReward);
                }
                Collections.reverse(returns);

                // Compute target probabilities based on returns
                INDArray targetProbabilities = computeTargetProbabilities(neuralNetwork, states, actions, returns);

                // Train the neural network using policy gradient
                neuralNetwork.train(states.get(0), targetProbabilities.getRow(0));

                game.undoLastTurn();
            }
        }

        return Optional.ofNullable(best);
    }

    private List<Double> calculateReturns(List<Double> rewards, double cumulativeReward) {
        List<Double> returns = new ArrayList<>();
        for (int t = rewards.size() - 1; t >= 0; t--) {
            cumulativeReward = rewards.get(t) + gamma * cumulativeReward;
            returns.add(cumulativeReward);
        }
        Collections.reverse(returns);
        return returns;
    }

    // public Optional<Placement> calculateTurn(Game game, int i, int i1) {
    //     List<Placement> possiblePlacements = new ArrayList<Placement>();
    //     for(Building building : game.getPlacableBuildings(game.getCurrentPlayer())){
    //         for(Direction direction : building.getTurnable().getPossibleDirections()){
    //             for(int x = 0; x < 10; ++x){
    //                 for(int y = 0; y < 10; ++y){
    //                     var placement = new Placement(new Position(x,y), direction, building);
    //                     if(game.takeTurn(placement, true)){
    //                         possiblePlacements.add(placement);
    //                         game.undoLastTurn();
    //                     }
    //                 }
    //             }
    //         }
    //     }

    //     INDArray input = preprocessGameState(game);
    //     INDArray output = neuralNetwork.predict(input);

    //     int bestMoveIndex = processOutput(output);

    //     try{
    //         if (bestMoveIndex >= 0 && bestMoveIndex < possiblePlacements.size()) {
    //             Placement bestPlacement = findBestMove(possiblePlacements, game);
    //             trainNeuralNetwork(game);
    //             return Optional.of(bestPlacement);
    //         }
    //     } finally {
    //         if(game.isFinished()){
    //             System.out.println("Done");
    //         }
    //     }
    //     // Ensure the index corresponds to a valid placement
    //     return Optional.empty();
    // }

    private void trainNeuralNetwork(Game game) {
        INDArray input = preprocessGameState(game);
        INDArray targetOutput = getTargetOutput(game);

        // Assuming you have a method to perform the training
        neuralNetwork.fit(input, targetOutput);
    }

    private INDArray getTargetOutput(Game game) {
        Color winner = determineWinner(game);

        // Assuming you have two output neurons, one for each player
        int numOutputs = 2;
        INDArray targetOutput = Nd4j.zeros(1, numOutputs);

        if (winner != null) {
            // Set the target value for the winner
            if (winner == Color.Black) {
                targetOutput.putScalar(0, 0, 1);  // Neuron 0 represents a win for Black
            } else {
                targetOutput.putScalar(0, 1, 1);  // Neuron 1 represents a win for White
            }
        }

        return targetOutput;
    }

    private Color determineWinner(Game game) {
        if (game.isFinished()) {
            Map<Color, Integer> scores = game.score();
            if (scores.containsKey(Color.Black) && scores.containsKey(Color.White)) {
                int blackScore = scores.get(Color.Black);
                int whiteScore = scores.get(Color.White);

                if (blackScore > whiteScore) {
                    return Color.White;
                } else if (whiteScore > blackScore) {
                    return Color.Black;
                } else {
                    return null;  // It's a draw
                }
            }
        }
        return null;  // Game is not finished yet
    }
}
