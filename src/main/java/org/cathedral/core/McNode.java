package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Board;

import java.io.Serializable;
import java.util.List;

public class McNode implements Serializable {
    private Board state;
    private McNode parent;
    private List<McNode> children;

    public McNode(Board board, McNode parent, List<McNode> children){
        this.state = board;
        this.parent = parent;
        this.children = children;
    }
}
