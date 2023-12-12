package org.board.fast;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitBoard {
    private static final int BOARD_SIZE = 10;
    private static final int BITS_PER_CELL = 2; // Assuming 2 bits for each cell
    private static final int BIT_MASK = (1 << BITS_PER_CELL) - 1;

    private long[] bitboard;

    public BitBoard() {
        int arraySize = (int) Math.ceil((double) (BOARD_SIZE * BOARD_SIZE * BITS_PER_CELL) / Long.SIZE);
        bitboard = new long[arraySize];
    }

    public int[][] getBitboardAsIntArray() {
        int[][] result = new int[BOARD_SIZE][BOARD_SIZE];

        for (int y = 0; y < BOARD_SIZE; ++y) {
            for (int x = 0; x < BOARD_SIZE; ++x) {
                int bitOffset = (y * BOARD_SIZE + x) * BITS_PER_CELL;
                int index = bitOffset / Long.SIZE;
                int offsetWithinLong = bitOffset % Long.SIZE;

                long bits = (bitboard[index] >>> offsetWithinLong) & BIT_MASK;
                result[y][x] = (int) bits;
            }
        }

        return result;
    }

    public List<Placement> getPossiblePlacements(Building building) {
        List<Placement> possiblePlacements = new ArrayList<>();

        for (int y = 0; y < BOARD_SIZE; ++y) {
            for (int x = 0; x < BOARD_SIZE; ++x) {
                for (Direction direction : Direction.values()) {
                    Placement placement = new Placement(x, y, direction, building);

                    if (canPlaceBuilding(placement)) {
                        possiblePlacements.add(placement);
                    }
                }
            }
        }

        return possiblePlacements;
    }

    private boolean canPlaceBuilding(Placement placement) {
        // Check if the building can be placed at the specified position on the Bitboard
        for (Position position : placement.form()) {
            int x = placement.x() + position.x();
            int y = placement.y() + position.y();

            if (!isValidPosition(x, y) || isOccupied(x, y)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    private boolean isOccupied(int x, int y) {
        // Assuming BITS_PER_CELL = 2
        int bitOffset = (y * BOARD_SIZE + x) * BITS_PER_CELL;
        int index = bitOffset / Long.SIZE;
        int offsetWithinLong = bitOffset % Long.SIZE;

        long mask = BIT_MASK << offsetWithinLong;

        // Check if the corresponding bits are not set
        return (bitboard[index] & mask) != 0;
    }

    public void undoSetBuilding(Placement placement) {
        for (Position position : placement.form()) {
            int x = placement.x() + position.x();
            int y = placement.y() + position.y();

            if (isValidPosition(x, y)) {
                int bitOffset = (y * BOARD_SIZE + x) * BITS_PER_CELL;
                int index = bitOffset / Long.SIZE;
                int offsetWithinLong = bitOffset % Long.SIZE;

                long mask = BIT_MASK << offsetWithinLong;

                // Clear the bits at the specified position
                bitboard[index] &= ~mask;
            }
        }
    }

    public void setBuilding(Placement placement) {
        for (Position position : placement.form()) {
            int x = placement.x() + position.x();
            int y = placement.y() + position.y();

            if (isValidPosition(x, y)) {
                int bitOffset = (y * BOARD_SIZE + x) * BITS_PER_CELL;
                int index = bitOffset / Long.SIZE;
                int offsetWithinLong = bitOffset % Long.SIZE;

                long mask = BIT_MASK << offsetWithinLong;
                long value = placement.building().getColor().getId() << offsetWithinLong;

                // Clear the bits at the specified position and set the new value
                bitboard[index] &= ~mask;
                bitboard[index] |= value;
            }
        }
    }
}