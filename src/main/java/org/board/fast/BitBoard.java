package org.board.fast;

import de.fhkiel.ki.cathedral.game.*;

import java.util.*;

public class BitBoard {
    private BitSet bits = new BitSet(100);
    private int turn = 0;
    private static final BitSet cathedral_d0 = BitSet.valueOf(new byte[]{1,10,11,12,21,31});

    public void set(List<Placement> placements) {
        placements.forEach(this::set);
    }

    public List<Placement> getPossiblePlacements(){
        List<Placement> result = new ArrayList<>();
        List<BitSet> masks = new ArrayList<>();
        masks.add(cathedral_d0);
        for(var mask : masks){
            if(!bits.intersects(mask)){
                result.add(new Placement(1,1, Direction._0, Building.Blue_Cathedral));
            }
        }
        return result;
    }

    public void setTurn(int turn){
        this.turn = turn;
    }

    public void set(Placement placement) {
        if(placement == null)
            return;
        int x = placement.x();
        int y = placement.y();

        BitSet tmpMask = new BitSet(100);
        for (var point : placement.form()) {
            int dy = (point.x() + x);
            int dx = (point.y() + y) * 10 + dy;
            tmpMask.set(dx);
        }

        if(!bits.intersects(tmpMask)){
            bits.xor(tmpMask);
        }
    }

    public BitSet getMask(){
        return this.bits;
    }

    public List<Placement> generateMoves(Color color) {
        return null;
    }
}
