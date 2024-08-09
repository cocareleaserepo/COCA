package analyseDepth.utils;

import analyseDepth.customClass.ValueNode;
import analyseDepth.path.BlocksAlongHops;
import analyseDepth.path.OneHop;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static analyseDepth.utils.CallUtils.getRefSignature;
import static analyseDepth.utils.ConstraintUtils.buildConditionStringSimple;
import static analyseDepth.utils.PathUtils.*;

public class VFUtils {

    public static List<Value> getFunctionArgValue(Block lstBlk, String VFSignature){
        List<Value> argValues = null;
        for(Unit unit:lstBlk) {
            if (JInvokeStmt.class.isInstance((Stmt) unit) || JAssignStmt.class.isInstance((Stmt) unit)) {
                SootMethod refMtd = null;
                try {
                    refMtd = ((Stmt) unit).getInvokeExpr().getMethod();
                }
                catch (RuntimeException e){
                    continue;
                }

                if (refMtd.getSignature().equals(VFSignature)) {
                    InvokeExpr vfInvokeExpr  = ((Stmt) unit).getInvokeExpr();
                    argValues = vfInvokeExpr.getArgs();

                }
            }
        }
        return argValues;
    }

    public static void getFunctionArgTree(ArrayList<ArrayList<OneHop>> allCallPath){
        ArrayList<BlocksAlongHops> allCallStackBlocksSelected = getShortestPathMtd2Mtd(allCallPath);
        for(BlocksAlongHops pathBlock: allCallStackBlocksSelected){

        ArrayList<Block> shortestPath = pathBlock.getPathBlock();
        String VFSignature = allCallStackBlocksSelected.get(allCallStackBlocksSelected.size() - 1).getEndMethod();

        ArrayList<Unit> units = new ArrayList<>();
        ArrayList<ValueNode> argTreelst = new ArrayList<>();

        for(Block block:shortestPath){
            for(Unit unit:block){
                units.add(unit);
            }
        }

        List<Value> argValues = getFunctionArgValue(shortestPath.get(shortestPath.size() - 1), VFSignature);
        ArrayList<Unit> allUnirReversed = new ArrayList<>(units);
        Collections.reverse(allUnirReversed);
        for(Value v:argValues){
            warpGetFuncArgSub(v, argTreelst, allUnirReversed);
        }

        for(ValueNode argTree:argTreelst){
            printFATree(argTree);
            System.out.println("##############");
            }
        }

    }

    public static void printFATree(ValueNode argTree){

        String conditionValue = buildConditionStringSimple(argTree);
        //String conditionValue = "";                                                 // Turn off.
        System.out.println(conditionValue);
    }

}
