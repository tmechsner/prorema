package de.unibielefeld.techfak.tdpe.prorema.utils;

import lombok.Getter;

/**
 * A simple tuple class.
 * Created by Benedikt Volkmer on 4/28/16.
 *
 * @param <L> Class of left element
 * @param <R> Class of right element
 */
@Getter
public class Tuple<L, R> {

    private final L left;
    private final R right;

    public Tuple(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof Tuple)
               && this.left.equals(((Tuple) o).getLeft())
               && this.right.equals(((Tuple) o).getRight());
    }

}
