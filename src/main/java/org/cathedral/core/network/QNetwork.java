package org.cathedral.core.network;

import org.cathedral.core.evaluators.Evaluator;

public class QNetwork {
    private final double alpha = 0.1;
    private final double gamma = 0.9;
    private final Evaluator evaluator;
    private double[][] Q = new double[10][10];

    public QNetwork(Evaluator evaluator){
        this.evaluator = evaluator;
    }
}
