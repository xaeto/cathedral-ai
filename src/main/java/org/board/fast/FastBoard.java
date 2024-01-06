package org.board.fast;

import de.fhkiel.ki.cathedral.game.*;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.*;

public class FastBoard {
    private record FastBoardRecord(MutableInt[][] field, Color current){};
    private Stack<FastBoardRecord> state = new Stack<>();
    public static MutableInt[][] convert(Color [][] field){
        MutableInt[][] res = new MutableInt[10][10];
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                if(field[y][x] == Color.White_Owned || field[y][x] == Color.Black_Owned){
                    res[y][x] = new MutableInt(field[y][x] == Color.White_Owned ? -1 : -2);
                } else {
                    res[y][x] = new MutableInt(field[y][x].getId());
                }
            }
        }
        return res;
    }

    public static FastBoard From(Board board, Color color){
        var field = convert(board.getField());
        return new FastBoard(field, color);
    }

    private HashSet<Integer> getBuildingIds(MutableInt[][] state){
        var set = new HashSet<Integer>();
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                int val = state[y][x].intValue();
                if(val > 0)
                    set.add(val);
            }
        }
        return set;
    }

    private List<Building> getBuildings(HashSet<Integer> ids){
        if(ids.isEmpty()){
            var res = new ArrayList<Building>();
            res.add(Building.Blue_Cathedral);
            return res;
        }
        var placeableBuildings = new ArrayList<Building>();
        var buildings = Arrays.stream(Building.values()).toList();
        for(var id : ids){
            buildings.removeIf(c-> c.getId() == id);
        }

        return placeableBuildings;
    }

    private boolean takeTurn(MutableInt[][] field, Placement placement){
        boolean valid = true;
        var cp = field.clone();
        int y = placement.y();
        int x = placement.x();

        for(var pos : placement.form()){
            int dx = pos.x() + x;
            if(dx < 0 || dx > 9)
                continue;
            int dy = pos.y() + y;
            if(dy < 0 || dy > 9)
                continue;

            MutableInt tile = field[dy][dx];
            int id = tile.intValue();
            if(id == Color.White.getId() || id == Color.Black.getId() || id == -1 || id == -2){
                valid = false;
                break;
            } else {
                field[dy][dx].setValue(placement.building().getId());
            }
        }

        if(!valid){
            for(int i = 0; i < 10; ++i){
                for(int k = 0; k < 10; ++k){
                    field[i][k].setValue(cp[i][k].intValue());
                }
            }
        }

        return valid;
    }

    public List<Placement> getPossiblePlacements(MutableInt[][] field, List<Building> buildings){
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            for(var direction : building.getTurnable().getPossibleDirections()){
                for(int y = 0; y < 10; ++y){
                    for(int x = 0; x < 10; ++x){
                        var p = new Placement(x, y, direction, building);
                        if(takeTurn(field, p)){
                            placements.add(p);
                        }
                    }
                }
            }
        }
        return placements;
    }

    public List<Placement> getPossiblePlacements(){
        var state = this.state.peek();
        var buildingIds = getBuildingIds(state.field);
        var buildings = getBuildings(buildingIds);
        var placements = new ArrayList<Placement>();

        for(var building : buildings){
            for(var direction : building.getTurnable().getPossibleDirections()){
                for(int y = 0; y < 10; ++y){
                    for(int x = 0; x < 10; ++x){
                        var p = new Placement(x, y, direction, building);
                        if(takeTurn(state.field.clone(), p)){
                            placements.add(p);
                        }
                    }
                }
            }
        }

        return placements;
    }

    private FastBoard(MutableInt[][] field, Color color){
        this.state.push(new FastBoardRecord(field, color));
    }
}
