package analyseDepth.getClientcs;

import analyseDepth.path.OneHop;
import analyseDepth.utils.CallUtils;
import analyseDepth.utils.PathUtils;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static analyseDepth.WrapRunPack.prepareWork;



public class GetCSBranchInfo {
    public static ArrayList<ArrayList<Boolean>>  getClientCallsiteInfo(String callStackFile) throws Exception {

        try{
            prepareWork();
        }
        catch (java.util.ConcurrentModificationException e){
            PackManager.v().runPacks();
        }

        ArrayList<String[]> callStack = CallUtils.getCallStack(callStackFile);
        ArrayList<ArrayList<Boolean>> flagLst = new ArrayList<>();
        for(String [] callPath: callStack){
            ArrayList<Boolean> infoFlag = GetCSBranchInfo.getCSBranchInfo(callPath, callStackFile);
            flagLst.add(infoFlag);

        }
        return flagLst;
    }


    public static ArrayList<Boolean> getCSBranchInfo(String[] oneCallStack, String callStackFile) throws Exception {
        SootMethod origin = CallUtils.getStartMtd(oneCallStack[0], Scene.v());

        Boolean branchFlag = false;
        Boolean returnFlag = false;
        Boolean parameterFlag = false;
        try{
            origin.getActiveBody();
        }
        catch (Exception e){
            ArrayList<Boolean> returnValue = new ArrayList<>();
            returnValue.add(false);returnValue.add(false);returnValue.add(false);
            return returnValue;
        }

        ////////////Get whether the function is in a branch/////////
        CallUtils.NextMethod nextMethod = new CallUtils.NextMethod();
        CallUtils.findNextMethod(origin, nextMethod, oneCallStack[1]);
        ArrayList<Integer> pathIndex = nextMethod.getRefBlockIndex();
        ArrayList<ArrayList<Integer>> pathsOneCall = PathUtils.getAllCFGPath(callStackFile ,origin);
        int resPath = getResPath(pathIndex,pathsOneCall);
        if(resPath==0){
            ArrayList<Boolean> returnValue = new ArrayList<>();
            returnValue.add(false);returnValue.add(false);returnValue.add(false);
            return returnValue;
        }

        branchFlag = !(resPath == pathsOneCall.size());

        ////////////Get whether the return of function will be returned or be a parameter/////////

        if(nextMethod.getAssiginOrInvoke()){        //If the method is called in a assignment, we will do the analysis, otherwise we just set return Flag and parameter Flag to false
            IntraproceduralAnalysis ipa = new IntraproceduralAnalysis(origin.getActiveBody(), oneCallStack[1]);
            ipa.doAnalyis();
            returnFlag = ipa.returnFlag;
            parameterFlag = ipa.parameterFlag;
        }

        ArrayList<Boolean> returnValue = new ArrayList<>();
        returnValue.add(branchFlag);
        returnValue.add(returnFlag);
        returnValue.add(parameterFlag);
        return returnValue;
    }

    private static int getResPath(ArrayList<Integer> pathIndex, ArrayList<ArrayList<Integer>> pathsOneCall){
        int resPath = 0;
        for(ArrayList<Integer> path:pathsOneCall){
            path.retainAll(pathIndex);
            if(path.size() == 0){
                continue;
            }
            else{
                resPath = resPath + 1;
            }
        }
        return resPath;
    }

    public static void printFlag(ArrayList<ArrayList<Boolean>> FlagLst){
        for(ArrayList<Boolean> infoFlag:FlagLst){
            for(Boolean flag:infoFlag){
                if(flag){
                    System.out.print("1@");
                }else{
                    System.out.print("0@");
                }
            }
            System.out.print("\n");
        }
    }
}
