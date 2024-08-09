package analyseDepth.path;

import soot.toolkits.graph.Block;

import java.util.ArrayList;
import java.util.Objects;


public class Path {
    private ArrayList<Block> path;

    public ArrayList<Block> getPath() {
        return path;
    }

    public void setPath(ArrayList<Block> path) {
        this.path = path;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path1 = (Path) o;
        return Objects.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
