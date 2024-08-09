package analyseDepth.path;

import soot.SootMethod;

import java.util.ArrayList;


public class OneHop {
    private int pathNum;
    private ArrayList<Path> paths;
    private SootMethod startMtd;
    private SootMethod endMtd;

    public OneHop(){
        this.startMtd = null;
        this.endMtd = null;
    }

    public void setStartMtd(SootMethod startMtd) {
        this.startMtd = startMtd;
    }

    public void setEndMtd(SootMethod endMtd) {
        this.endMtd = endMtd;
    }

    public void setPaths(ArrayList<Path> paths) {
        this.paths = paths;
    }

    public void setPathNum(int pathNum) {
        this.pathNum = pathNum;
    }

    public SootMethod getEndMtd() {
        return endMtd;
    }

    public SootMethod getStartMtd() {
        return startMtd;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }

    public int getPathNum() {
        return pathNum;
    }
}
