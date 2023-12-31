package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;

public class BlockHeuristic extends Heuristic{

    public BlockHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        Board board = game.getBoard();
        Color enemy = game.getCurrentPlayer();

        double score = blockOpponent(board, enemy);
        return score * depth;
    }

    private int countPlayerBuildings(Board board, Color player) {
        int playerBuildings = 0;

        var field = board.getField();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (field[row][col] != null && field[row][col] == player) {
                    playerBuildings++;
                }
            }
        }

        return playerBuildings;
    }

    private int countUnclaimedSpaces(Board board, Color player) {
        int unclaimedSpaces = 0;

        var field = board.getField();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (field[row][col] == Color.None) {
                    if (isAdjacentToOpponentBuilding(board, row, col, player.opponent())) {
                        unclaimedSpaces++;
                    }
                }
            }
        }

        return unclaimedSpaces;
    }

    private boolean isAdjacentToOpponentBuilding(Board board, int row, int col, Color opponent) {
        var field = board.getField();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (row + i >= 0 && row + i < 10 &&
                        col + j >= 0 && col + j < 10 &&
                        field[row + i][col + j] != null &&
                        field[row + i][col + j] == opponent) {
                    return true;
                }
            }
        }

        return false;
    }

    private double blockOpponent(Board board, Color opponent) {
        int opponentBuildings = countPlayerBuildings(board, opponent);
        int opponentUnclaimedSpaces = countUnclaimedSpaces(board, opponent);
        if(opponentBuildings == 0)
            return 0;

        double score = 1.0 - (double) opponentUnclaimedSpaces / opponentBuildings;
        if(score < 0){
            score = 0;
        }
        return score;
    }
}
