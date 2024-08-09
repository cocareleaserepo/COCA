package analyseDepth;

import analyseDepth.path.OneHop;
import analyseDepth.getClientcs.GetCSBranchInfo;
import analyseDepth.getPath.GetMultHop;
import analyseDepth.utils.CallUtils;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import soot.toolkits.scalar.ConstantInitializerToTagTransformer;

import java.util.ArrayList;

import static soot.jbco.util.BodyBuilder.retrieveAllBodies;



public class WrapRunPack {
    public static ArrayList<ArrayList<OneHop>> getMultiHop(String callStackFile) throws Exception {
        // Create tags from all values we only have in code assignments now
        try{
            prepareWork();
        }
        catch (java.util.ConcurrentModificationException e){
            PackManager.v().runPacks();
        }

        ArrayList<String[]> callStack = CallUtils.getCallStack(callStackFile);
        ArrayList<ArrayList<OneHop>> allCallPath = new ArrayList<>();
        for(String [] callPath: callStack){
                ArrayList<OneHop> multHop = GetMultHop.getMultHop(callPath, callStackFile);
                allCallPath.add(multHop);
        }
        return allCallPath;
    }

    public static void prepareWork(){
        retrieveAllBodies();
        for (SootClass sc : Scene.v().getApplicationClasses()) {
            if (Options.v().validate()) {
                sc.validate();
            }
            if (!sc.isPhantom()) {
                ConstantInitializerToTagTransformer.v().transformClass(sc, true);
            }
        }

    }




}
