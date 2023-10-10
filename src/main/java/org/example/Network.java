package org.example;

import com.google.gson.Gson;
import de.fhkiel.ki.cathedral.game.*;
import java.io.FileWriter;
import java.util.*;

public class Network {
    private static List<Pair<Placement, int[][]>> _turns = new ArrayList<>();
    public static void Add(Game game, Placement placement, Board board) {
        var matrix = GenerateBoardMatrix(game, board);
        _turns.add(new Pair(placement, matrix));
    }

    public static void Save(String name){
        Gson gson = new Gson();
        var matrices = _turns.stream().map(c -> c.second).toArray();
        String json = gson.toJson(matrices);

        try{
            FileWriter writer = new FileWriter("./" + name + "_dataset.json");
            writer.write(json);
            writer.close();
        } catch (Exception e){
            System.out.println("Error while saving dataset.");
        }
    }

    private static int diff(Game game, int[][] a, int b[][], int length) {
        int di = 0;
        Color q = game.getCurrentPlayer();
        var ids = game.getPlacableBuildings(q);
        for(int i = 0; i < length; ++i){
            for(int j = 0; j < length; ++j) {
                di += Math.abs((a[i][j] - b[j][i]));
            }
        }
        return di;
    }

    public static Optional<Placement> GetBest(Game game, Board currentBoard){
        int length = _turns.size();
        Placement best = null;

        var boardMatrix = GenerateBoardMatrix(game, currentBoard);
        var avg_di = Arrays.stream(_turns.stream()
                .map(c -> diff(game, c.second, boardMatrix, boardMatrix.length))
                .toArray())
                .mapToInt(c-> (int)c)
                .average();

        if(avg_di.isEmpty())
            avg_di = OptionalDouble.of(1);

        for(int i = 0; i < length; ++i){
            var a = _turns.get(i);
            if(game.takeTurn(a.first, true)){
                float di = diff(game, a.second, boardMatrix, boardMatrix.length);
                if(di >= avg_di.getAsDouble()){
                    best = a.first;
                }
                game.undoLastTurn();
            }
        }

        if(best == null){
            List<Placement> possibleTurns = new ArrayList<Placement>();
            for(Building building : game.getPlacableBuildings()){
                for(Direction direction : building.getTurnable().getPossibleDirections()){
                    for(int x = 0; x < 10; ++x){
                        for(int y = 0; y < 10; ++y){
                            var placement = new Placement(new Position(x,y), direction, building);
                            if(game.takeTurn(placement)){
                                possibleTurns.add(placement);
                                game.undoLastTurn();
                            }
                        }
                    }
                }
            }

            if(!possibleTurns.isEmpty()){
                var turn = possibleTurns.get(new Random().nextInt(possibleTurns.size()));
                best = turn;
            }
        }
        Network.Add(game, best, game.getBoard());

        return Optional.of(best);
    }

    public static void printRow(int[] row) {
        for (int i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }

    public static int[][] GenerateBoardMatrix(Game game, Board board){
        int matrix[][] = new int[10][10];
        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++){
                matrix[x][y] = 0;
            }
        }

        for(Placement p: game.getBoard().getPlacedBuildings()){
            for(Position pos: p.form()){
                int x = p.x() + pos.x();
                int y = p.y() + pos.y();
                matrix[y][x] = p.building().getId();
            }
        }
        System.out.println();
        for(int[] row : matrix){
            printRow(row);
        }
        return matrix;
    }
}