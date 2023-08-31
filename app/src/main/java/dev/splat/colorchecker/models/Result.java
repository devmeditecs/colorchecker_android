package dev.splat.colorchecker.models;

import java.util.Vector;

public class Result {
    private Integer CClass;
    private Vector<Integer> coords;

    public Result(Integer CClass, Vector<Integer> coords) {
        this.CClass = CClass;
        this.coords = coords;
    }

    public Integer getCClass() {
        return CClass;
    }

    public void setCClass(Integer CClass) {
        this.CClass = CClass;
    }

    public Vector<Integer> getCoords() {
        return coords;
    }

    public void setCoords(Vector<Integer> coords) {
        this.coords = coords;
    }
}
