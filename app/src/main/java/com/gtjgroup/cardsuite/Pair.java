package com.gtjgroup.cardsuite;

public class Pair<X, Y> {
    public X move;
    public Y values;
    public Pair() {}

    public Pair(X x, Y y) {
        this.move = x;
        this.values = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!move.equals(pair.move)) return false;
        return values.equals(pair.values);

    }

    @Override
    public int hashCode() {
        int result = move.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }
}
