package analyseDepth.getPath;

import analyseDepth.path.OneHop;
import analyseDepth.path.Path;
import analyseDepth.utils.CallUtils;
import analyseDepth.utils.PathUtils;
import soot.Scene;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.util.ArrayList;


public class GetMultHop {
    public static ArrayList<OneHop> getMultHop(String[] oneCallStack, String callFile) throws Exception {
        ArrayList<OneHop> multHop = new ArrayList<>();
        SootMethod start = CallUtils.getStartMtd(oneCallStack[0], Scene.v());

        SootMethod origin = start;
        for(int i = 1; i < oneCallStack.length && origin != null; i++){

            OneHop oneHopTmp = new OneHop();
            CallUtils.NextMethod nextMethod = new CallUtils.NextMethod();
            CallUtils.findNextMethod(origin, nextMethod, oneCallStack[i]);
            ArrayList<Integer> pathIndex = nextMethod.getRefBlockIndex();

            ArrayList<Path> pathsOneCall = PathUtils.traverseBlockFiltered(new ExceptionalBlockGraph(origin.getActiveBody()), pathIndex, callFile, false);
            oneHopTmp.setPaths(pathsOneCall);
            oneHopTmp.setStartMtd(origin);
            oneHopTmp.setEndMtd(nextMethod.getSm());
            oneHopTmp.setPathNum(pathsOneCall.size());
            multHop.add(oneHopTmp);
            origin = nextMethod.getSm();

        }
        return multHop;
    }

}
