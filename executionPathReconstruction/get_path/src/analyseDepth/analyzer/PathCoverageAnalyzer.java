package analyseDepth.analyzer;

import analyseDepth.getPath.GetMultHop;
import analyseDepth.path.OneHop;
import analyseDepth.path.Path;
import analyseDepth.utils.CallUtils;
import analyseDepth.utils.PathUtils;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.util.ArrayList;



import static analyseDepth.WrapRunPack.prepareWork;

public class PathCoverageAnalyzer {
    public void getPathCoverageCal(String callStackFile) throws Exception {
        try{
            prepareWork();
        }
        catch (java.util.ConcurrentModificationException e){
            PackManager.v().runPacks();
        }
        ArrayList<String[]> callStack = CallUtils.getCallStack(callStackFile);
        ArrayList<Float> pathcoverageCalLst = new ArrayList<>();
        for(String [] callPath: callStack){
            Float pathcoverageCal = getPathCoverage(callPath, callStackFile);
            pathcoverageCalLst.add(pathcoverageCal);
        }
       for(float pathcoverageCal:pathcoverageCalLst){
           System.out.println(pathcoverageCal);
       }
    }



    public Float getPathCoverage(String[] oneCallStack, String callFile) throws Exception {
        SootMethod origin = CallUtils.getStartMtd(oneCallStack[0], Scene.v());

        Float pathcoverageCal = 1.0F;
        if(origin == null){
            return  -1.0F;
        }

        for(int i = 1; i < oneCallStack.length && origin != null; i++){
            ArrayList<ArrayList<Integer>> allPathIndex= PathUtils.getAllCFGPath(callFile, origin);
            int allPathNum = allPathIndex.size();

            CallUtils.NextMethod nextMethod = new CallUtils.NextMethod();
            CallUtils.findNextMethod(origin, nextMethod, oneCallStack[i]);
            ArrayList<Integer> blockIndexLst = nextMethod.getRefBlockIndex();

            int functionPathNum = 0;
            for(ArrayList<Integer> path:allPathIndex){
                for(int blockIndex:blockIndexLst){
                    if(path.contains(blockIndex)){
                        functionPathNum = functionPathNum + 1;
                        break;
                    }
                }
            }

            origin = nextMethod.getSm();
            // TODO Have a test here

            pathcoverageCal = pathcoverageCal * (((float) functionPathNum)/((float) allPathNum));

        }
        return pathcoverageCal;
    }


}
