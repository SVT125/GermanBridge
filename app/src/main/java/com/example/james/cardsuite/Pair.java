package com.example.james.cardsuite;

public class Pair<X, Y> {
    public X move;
    public Y values;
    public Pair() {}

    public Pair(X x, Y y) {
        this.move = x;
        this.values = y;
    }
}
