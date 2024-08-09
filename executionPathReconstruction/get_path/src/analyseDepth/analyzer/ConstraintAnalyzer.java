package analyseDepth.analyzer;

import analyseDepth.path.BlocksAlongHops;
import analyseDepth.path.OneHop;
import analyseDepth.customClass.ConditionResult;
import analyseDepth.utils.ConstraintUtils;
import soot.Unit;
import soot.Value;

import soot.jimple.*;
import analyseDepth.customClass.ValueNode;
import soot.jimple.internal.*;
import soot.toolkits.graph.Block;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static analyseDepth.utils.ConstraintUtils.getStringCondition;
import static analyseDepth.utils.ConstraintUtils.neOperand;
import static analyseDepth.utils.PathUtils.getShortestPathMtd2Mtd;



public class ConstraintAnalyzer {
    public ArrayList<ArrayList<ConditionResult>> getConstraintAlongPath(ArrayList<ArrayList<OneHop>> allCallPath) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ArrayList<BlocksAlongHops> allCallStackBlocksSelected = getShortestPathMtd2Mtd(allCallPath);
        ArrayList<ArrayList<ConditionResult>> allPathCondition = new ArrayList<>();

        for (BlocksAlongHops blocksAlongHops : allCallStackBlocksSelected) {
            ArrayList<ConditionResult> pathCondition = new ArrayList<>();
            int blockIndex = 0;

            for (Block block : blocksAlongHops.getPathBlock()) {
                //Grantee not the last block

                if (blockIndex < blocksAlongHops.getPathBlock().size() - 1) {

                    HashMap<Block, ArrayList<ValueNode>> localMap = blocksAlongHops.getLocalMap();
                    ArrayList<ValueNode> conditionTrees = localMap.get(block);
                    if (conditionTrees.size() != 0 && conditionTrees.get(0).getFlagNode()) {                                                    //Check whether this block is the last block of the shortest path, if it is, its constraint(goto condition) dose not matter.
                        continue;
                    }

                    Unit lastUnit = block.getTail();
                    String nextBlockFirstUnit = blocksAlongHops.getPathBlock().get(blockIndex + 1).getHead().toString();

                    //If the last unit of basic block is if statement
                    if (JIfStmt.class.isInstance((Stmt) lastUnit)) {
                        String targetUnit = ((JIfStmt) lastUnit).getTarget().toString();

                        if (nextBlockFirstUnit.equals(targetUnit)) {
                            ConditionExpr v = (ConditionExpr) ((JIfStmt) lastUnit).getCondition();

                            //new condition result
                            ConditionResult conditionResulttmp = new ConditionResult();
                            conditionResulttmp.setCondition(v);
                            conditionResulttmp.setValue(conditionTrees);

                            //Map condition variable to value
                            String conditionString = getStringCondition(v, conditionTrees);
                            conditionResulttmp.setConstraintTreeStr(conditionString);

                            pathCondition.add(conditionResulttmp);

                        } else {
                            ConditionExpr v = (ConditionExpr) ((JIfStmt) lastUnit).getCondition();
                            ConditionExpr vNe = neOperand(v);

                            //new condition result
                            ConditionResult conditionResulttmp = new ConditionResult();
                            conditionResulttmp.setCondition(vNe);
                            conditionResulttmp.setValue(conditionTrees);

                            //Map condition variable to value
                            String conditionString = getStringCondition(vNe, conditionTrees);
                            conditionResulttmp.setConstraintTreeStr(conditionString);

                            pathCondition.add(conditionResulttmp);

                        }
                    }

                    //If the last unit of basic block is switch case statement, in table switch format
                    else if (JTableSwitchStmt.class.isInstance((Stmt) lastUnit)) {
                        JTableSwitchStmt table = (JTableSwitchStmt) lastUnit;
                        Value key = table.getKey();
                        int low = table.getLowIndex();
                        int high = table.getHighIndex();
                        for (int i = low; i < high; i++) {
                            if (table.getTarget(i - low).toString().equals(nextBlockFirstUnit)) {
                                ConditionExpr v = new JEqExpr(key, IntConstant.v(i));


                                //new condition result
                                ConditionResult conditionResulttmp = new ConditionResult();
                                conditionResulttmp.setCondition(v);
                                conditionResulttmp.setValue(conditionTrees);


                                //Map condition variable to value
                                String conditionString = getStringCondition(v, conditionTrees);
                                conditionResulttmp.setConstraintTreeStr(conditionString);


                                pathCondition.add(conditionResulttmp);

                            }
                        }
                        if (table.getDefaultTarget().toString().equals(nextBlockFirstUnit)) {
                            // TODO: Use a range to cover the default target of the switch table
                        }

                    }

                    //If the last unit of basic block is switch case statement, in look up switch format
                    else if (JLookupSwitchStmt.class.isInstance((Stmt) lastUnit)) {
                        JLookupSwitchStmt directory = (JLookupSwitchStmt) lastUnit;
                        Value key = directory.getKey();
                        List<IntConstant> caseList = directory.getLookupValues();
                        for (int i = 0; i < caseList.toArray().length; i++) {
                            if (directory.getTarget(i).toString().equals(nextBlockFirstUnit)) {
                                ConditionExpr v = new JEqExpr(key, IntConstant.v(i));


                                //new condition result
                                ConditionResult conditionResulttmp = new ConditionResult();
                                conditionResulttmp.setCondition(v);
                                conditionResulttmp.setValue(conditionTrees);

                                //Map condition variable to value
                                String conditionString = getStringCondition(v, conditionTrees);
                                conditionResulttmp.setConstraintTreeStr(conditionString);

                                pathCondition.add(conditionResulttmp);
                            }
                        }
                        if (directory.getDefaultTarget().toString().equals(nextBlockFirstUnit)) {
                            // TODO: Use a range to cover the default target of the switch table
                        }
                    }

                }
                blockIndex = blockIndex + 1;

            }

            allPathCondition.add(pathCondition);
        }
        return allPathCondition;
    }

    public void printPathConditionLength(ArrayList<ArrayList<ConditionResult>> allPathCondition) {
        for (ArrayList<ConditionResult> pathConditionResult : allPathCondition) {
            System.out.println(pathConditionResult.size());
        }
    }

    public void printPathConditionOperand(ArrayList<ArrayList<ConditionResult>> allPathCondition) {
        for (ArrayList<ConditionResult> pathConditionResult : allPathCondition) {
            int index = 0;
            if (pathConditionResult.size() == 0) {
                System.out.println("");
            }
            for (ConditionResult conditionVariableType : pathConditionResult) {
                if (index == pathConditionResult.size() - 1) {
                    System.out.println(conditionVariableType.getCondition().getClass().toString());
                } else {
                    System.out.print(conditionVariableType.getCondition().getClass().toString() + ";");
                }
                index = index + 1;
            }
        }
    }

    public void printPathConditionVariableType(ArrayList<ArrayList<ConditionResult>> allPathCondition) {
        for (ArrayList<ConditionResult> pathConditionResult : allPathCondition) {
            int index = 0;
            ArrayList<String> conditionVariableTypeAlongPath = getConditionVariableType(pathConditionResult);

            if (conditionVariableTypeAlongPath.size() == 0) {
                System.out.println("");
            }

            for (String conditionVariableType : conditionVariableTypeAlongPath) {
                if (index == conditionVariableTypeAlongPath.size() - 1) {
                    System.out.println(conditionVariableType);
                } else {
                    System.out.print(conditionVariableType + ";");
                }
                index = index + 1;
            }
        }
    }

    public void printPathConditionVariableReturnType(ArrayList<ArrayList<ConditionResult>> allPathCondition) {
        for (ArrayList<ConditionResult> pathConditionResult : allPathCondition) {
            int index = 0;
            ArrayList<String> conditionVariableTypeAlongPath = getConditionVariableReturnType(pathConditionResult);
            if (conditionVariableTypeAlongPath.size() == 0) {
                System.out.println();
            }
            for (String conditionVariableType : conditionVariableTypeAlongPath) {
                if (index == conditionVariableTypeAlongPath.size() - 1) {
                    System.out.println(conditionVariableType);
                } else {
                    System.out.print(conditionVariableType + ";");
                }
                index = index + 1;
            }
        }
    }

    public void printConditionTreeExp(ArrayList<ArrayList<ConditionResult>> allPathCondition) {
        for (ArrayList<ConditionResult> conditionStringLst : allPathCondition) {
            for (ConditionResult conditionString : conditionStringLst) {
                System.out.println(conditionString.getConstraintTreeStr());
            }
            System.out.println("##############");
        }
    }


    public ArrayList<String> getConditionVariableType(ArrayList<ConditionResult> pathConditionResult) {
        ArrayList<String> conditionVariableType = new ArrayList<>();
        for (ConditionResult conditionResult : pathConditionResult) {
            ArrayList<String> tmp = new ArrayList<>();
            for (ValueNode valueNode : conditionResult.getValue()) {
                if(valueNode.getValue() instanceof AbstractBinopExpr){
                    tmp = processExpr(valueNode);
                }
                else{
                    tmp.add(valueNode.getValue().getClass().toString());
                }
            }
            if(tmp.size() ==0){
                conditionVariableType.addAll(ConstraintUtils.getStringFromCondition((ConditionExpr) conditionResult.getCondition()));
            }
            else{
                conditionVariableType.addAll(tmp);
            }
        }
        return conditionVariableType;
    }


    public ArrayList<String> processExpr(ValueNode value){
        ArrayList<String> opType = new ArrayList<>();
        for(ValueNode vChild:value.getChildren()){
            if(vChild.getValue() instanceof JimpleLocal || vChild.getValue() instanceof AbstractBinopExpr){
                continue;
            }
            else {
                opType.add(vChild.getValue().getClass().toString());
            }
        }
        return opType;
    }


    public ArrayList<String> getConditionVariableReturnType(ArrayList<ConditionResult> pathConditionResult) {
        ArrayList<String> conditionVariableType = new ArrayList<>();
        for (ConditionResult conditionResult : pathConditionResult) {
            ArrayList<String> tmp = new ArrayList<>();
            for(ValueNode value:conditionResult.getValue()){
                tmp.addAll(getType(value.getValue()));
            }
            if(tmp.size() == 0){
                conditionVariableType.addAll(getType((ConditionExpr) conditionResult.getCondition()));
            }
            else{
                conditionVariableType.addAll(tmp);
            }
        }
        return conditionVariableType;
    }

    public ArrayList<String> getType(Value value) {
        if (value instanceof JSpecialInvokeExpr || value instanceof JInterfaceInvokeExpr || value instanceof JVirtualInvokeExpr || value instanceof JStaticInvokeExpr) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(((AbstractInvokeExpr) value).getMethodRef().getReturnType().toString());
            return tmp;
        }

        if (value instanceof JLengthExpr) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(value.getType().toString());
            return tmp;
        }

        if (value instanceof JInstanceOfExpr) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add("Object");
            return tmp;
        }
        if (value instanceof JCastExpr) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(((JCastExpr) value).getCastType().toString());
            return tmp;
        }
        if (value instanceof JInstanceFieldRef || value instanceof StaticFieldRef || value instanceof ThisRef || value instanceof JArrayRef || value instanceof ParameterRef) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(value.getType().toString());
            return tmp;
        }
        if (value instanceof Constant) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(value.getType().toString());
            return tmp;
        }
        if (value instanceof AbstractBinopExpr) {
            ArrayList<String> tmp = getBinopType((AbstractBinopExpr) value);
            return tmp;
        }
        else {
            ArrayList<String> tmp = new ArrayList<>();
            System.err.println("Check "+value.getClass().toString());
            return tmp;
        }
    }

    public ArrayList<String> getType(ConditionExpr condition) {
        ArrayList<String> a = new ArrayList<>();
        Value op1 = condition.getOp1();
        a.add(op1.getType().toString());
        Value op2 = condition.getOp2();
        a.add(op2.getType().toString());
        return a;
    }

    public ArrayList<String> getBinopType(AbstractBinopExpr value) {
        Value op1 = value.getOp1();
        ArrayList<String> tmp1 = new ArrayList<>();
        ArrayList<String> tmp2 = new ArrayList<>();
        if(op1 instanceof JimpleLocal){
            tmp1.add(op1.getType().toString());
        }
        else{
            tmp1 = getType(op1);
        }
        Value op2 = value.getOp2();
        if(op2 instanceof JimpleLocal){
            tmp2.add(op2.getType().toString());
        }
        else{
            tmp2 = getType(op2);
        }
        tmp1.addAll(tmp2);
        return tmp1;
    }

}