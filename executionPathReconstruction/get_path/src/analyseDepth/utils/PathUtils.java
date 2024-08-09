
package analyseDepth.utils;
import java.io.*;
import java.util.Collections;

import analyseDepth.customClass.ValueNode;
import analyseDepth.path.BlocksAlongHops;
import analyseDepth.path.OneHop;
import analyseDepth.path.Path;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.util.*;


public class PathUtils {

    public static ArrayList<ArrayList<Integer>> getEdge(ExceptionalBlockGraph bg){
        ArrayList<ArrayList<Integer>> edges = new ArrayList<>();
        for(Block block:bg.getBlocks()){
            List<Block> successors = block.getSuccs();
            for(Block successor:successors){
                ArrayList<Integer> edge = new ArrayList<>();
                edge.add(block.getIndexInMethod());
                edge.add(successor.getIndexInMethod());
                edges.add(edge);
            }
        }
        return edges;
    }

    public static int[][] getAdjMatrix(int num, ArrayList<ArrayList<Integer>> edges){
        int[][] AdjMatrix = new int[num][num];
        for(ArrayList<Integer> edge:edges){
            AdjMatrix[edge.get(0)][edge.get(1)] = 1;
        }
        return AdjMatrix;
    }

    public static ArrayList<Integer> getSuccessor(int[] list){
        ArrayList<Integer> successors = new ArrayList<>();
        for(int i=0;i<list.length;i++){
            if(list[i]==1){
            successors.add(i);
            }
        }
        return successors;
    }

    public static ArrayList<ArrayList<Integer>> traverseBlockAllPath(ExceptionalBlockGraph bg){
        ArrayList<Integer> unitPath = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allPath = new ArrayList<>();
        int blockNum = bg.size();
        int[] visited = new int[blockNum];
        ArrayList<ArrayList<Integer>> edges = getEdge(bg);
        int[][] AdjMatrix = getAdjMatrix(bg.size(), edges);
        List<Block> heads = bg.getHeads();

        for(Block block:heads){
            traverseBlock(block.getIndexInMethod(), unitPath, allPath, visited, AdjMatrix);
        }

        return allPath;
    }


    public static ArrayList<Path> traverseBlockFiltered(ExceptionalBlockGraph bg, ArrayList<Integer> blockIndex, String callFile, Boolean pathFlag) throws Exception {
        HashSet<Path> paths = new HashSet<>() ;
        ArrayList<Integer> unitPath = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allPath = new ArrayList<>();
        int blockNum = bg.size();

        int[] visited = new int[blockNum];
        ArrayList<ArrayList<Integer>> edges = getEdge(bg);
        int[][] AdjMatrix = getAdjMatrix(bg.size(), edges);
        List<Block> heads;
        if(blockNum > 1){
            return getShortPathExceedMaxBlock(callFile, AdjMatrix, bg, blockIndex, pathFlag);
        }

        heads = bg.getHeads();
        for(Block block:heads){
            traverseBlock(block.getIndexInMethod(), unitPath, allPath, visited, AdjMatrix);
        }
        ArrayList<ArrayList<Integer>> cuttedPaths;

        for(ArrayList indexPath:allPath){
            cuttedPaths = cutPath(blockIndex, indexPath);
            for(ArrayList cuttedPath: cuttedPaths){
                Path path = new Path();
                ArrayList<Block> blocks = getBlocks(bg, cuttedPath);
                path.setPath(blocks);
                paths.add(path);
            }
        }
        return new ArrayList<>(paths);
    }


    private static ArrayList<ArrayList<Integer>> cutPath(ArrayList<Integer> blockIndex, ArrayList<Integer> pathIndex){
        ArrayList<ArrayList<Integer>> cuttedPath = new ArrayList<>();
        for(int i = 0; i < pathIndex.size(); i++){
            if(blockIndex.contains(pathIndex.get(i)))              //cut off the path when meet with the reference of next method.
            {
                List<Integer> cttmp = pathIndex.subList(0,i+1);
                ArrayList<Integer> cttmpArr = new ArrayList<>();  // java.util.ArrayList$SubList cannot be cast to java.util.ArrayList
                for(int index: cttmp){
                    cttmpArr.add(index);
                }
                cuttedPath.add(cttmpArr);
            }
        }
        return cuttedPath;
    }


    public static void traverseBlock(int origin, ArrayList<Integer> unitPath, ArrayList<ArrayList<Integer>> allPath, int[] visited, int[][] AdjMatrix){
//        System.out.println(origin);
//        if(origin==86){
//            int a = 1;
//        }
        unitPath.add(origin);
        if(PathUtils.getSuccessor(AdjMatrix[origin]).size() == 0){
            allPath.add((ArrayList<Integer>) unitPath.clone());
            unitPath.remove(unitPath.size()-1);
            return;
        }
        for(int successor: PathUtils.getSuccessor(AdjMatrix[origin])){
            //Process the block self-loop, like 86->86
            if(successor == origin)
            {
                if(PathUtils.getSuccessor(AdjMatrix[origin]).size() == 1)
                {
                    allPath.add((ArrayList<Integer>) unitPath.clone());
                }
                else{
                    continue;
                }
            }

            if(visited[origin] == 2){                                                   //Unfold the loop once eg：1->2->3->1:123145
                //Avoid unlimited loop                                                  //                          ->4->5
                if(PathUtils.getSuccessor(AdjMatrix[origin]).size() == 1){
                    allPath.add((ArrayList<Integer>) unitPath.clone());
                }
                //Bypass the successor that has been visited.
                else if(PathUtils.getSuccessor(AdjMatrix[origin]).size() > 1 && visited[successor] != 0){
                    continue;
                }
                //Dive into the successor that has not been visited.
                else {
                    visited[successor] = visited[successor] + 1;
                    traverseBlock(successor, unitPath, allPath, visited, AdjMatrix);
                    visited[successor] = visited[successor] - 1;
                }
            }

            else {
                visited[successor] = visited[successor] + 1;
                traverseBlock(successor, unitPath, allPath, visited, AdjMatrix);
                visited[successor] = visited[successor] - 1;
            }
        }
        unitPath.remove(unitPath.size()-1);
        return;
    }


    public static ArrayList<Block> getBlocks(ExceptionalBlockGraph bg, ArrayList<Integer> pathIndex){
        ArrayList<Block> blocks = new ArrayList<>();
        List<Block> allBlocks = bg.getBlocks();
        for(int index:pathIndex){
                blocks.add(allBlocks.get(index));
        }
        return blocks;
    }

    public static ArrayList<BlocksAlongHops> getShortestPathMtd2Mtd(ArrayList<ArrayList<OneHop>> allCallPath){
        ArrayList<BlocksAlongHops> allCallStackBlocks = new ArrayList<>();
        for(ArrayList<OneHop> callStack:allCallPath){
            HashMap<Block, ArrayList<ValueNode>> localMap = new HashMap<>();
            BlocksAlongHops blocksAlongHops = new BlocksAlongHops();
            blocksAlongHops.setStartMethod(callStack.get(0).getStartMtd().getSignature());
            blocksAlongHops.setEndMethod(callStack.get(callStack.size() - 1).getEndMtd().getSignature());

            int blockNum = 0;
            ArrayList<Block> shortestPath = new ArrayList<>();
            for(OneHop oneHop:callStack){
                int shortestPathLength = 999999999;
                ArrayList<Block> shortestPathTmp = new ArrayList<>();
                for(Path path:oneHop.getPaths()){
                    if(path.getPath().size() < shortestPathLength){
                        shortestPathLength = path.getPath().size();
                        shortestPathTmp = path.getPath();
                    }
                }
                blockNum = blockNum + shortestPathLength;
                shortestPath.addAll(shortestPathTmp);
                localMap.putAll(getBlockValueNodeMap(shortestPathTmp));
            }
            blocksAlongHops.setBlockNum(shortestPath.size());
            blocksAlongHops.setPathBlock(shortestPath);
            blocksAlongHops.setLocalMap(localMap);
            allCallStackBlocks.add(blocksAlongHops);
        }
        return allCallStackBlocks;
//        HashSet<BlocksAlongHops> allCallStackBlocksSelected = new HashSet<>();
//        for(BlocksAlongHops blocksAlongHops1:allCallStackBlocks){
//            BlocksAlongHops blocksAlongHopstmp = blocksAlongHops1;
//            for(BlocksAlongHops blocksAlongHops2:allCallStackBlocks){
//                if(blocksAlongHopstmp.equals(blocksAlongHops2)){
//                    if(blocksAlongHops2.getBlockNum() < blocksAlongHopstmp.getBlockNum()){
//                        blocksAlongHopstmp = blocksAlongHops2;
//                    }
//                }
//            }
//            allCallStackBlocksSelected.add(blocksAlongHopstmp);
//        }
//        ArrayList<BlocksAlongHops> allCallStackBlocksSelectedList = new ArrayList<>(allCallStackBlocksSelected);
//        return allCallStackBlocksSelectedList;
    }


    public static HashMap<Block, ArrayList<ValueNode>> getBlockValueNodeMap(ArrayList<Block> shortestPath){
        ArrayList<Unit> units = new ArrayList<>();
        HashMap<Block, ArrayList<ValueNode>> blockConstraintLocalMap = new HashMap<>();

        //Check whether this block is the last block of the shortest path, if it is, its constraint(goto condition) dose not matter.
        ArrayList<ValueNode> lastBlockMapValueLst = new ArrayList<>();
        ValueNode lastBlockMapValue = new ValueNode();
        lastBlockMapValue.setFlagNode(true);
        List<Block> shortestPathExceptLastBlock = null;
        blockConstraintLocalMap.put(shortestPath.get(shortestPath.size()-1), lastBlockMapValueLst);
        shortestPathExceptLastBlock = shortestPath.subList(0, shortestPath.size()-1);
        //Check whether this block is the last block of the shortest path, if it is, its constraint(goto condition) dose not matter.

        for(Block block:shortestPathExceptLastBlock){
            for(Unit unit:block){
                units.add(unit);
            }

            ArrayList<ValueNode> valueNodesTmp = new ArrayList<>();
            ArrayList<Unit> unitTmp = new ArrayList<>(units);
            Collections.reverse(unitTmp);

            Unit lastUnit = unitTmp.get(0);
            if(JIfStmt.class.isInstance((Stmt) lastUnit)){
                Value v = ((JIfStmt) lastUnit).getCondition();
                warpGetConditionSub(v, valueNodesTmp, unitTmp);
            }

            //If the last unit of basic block is switch case statement, in table switch format
            else if(JTableSwitchStmt.class.isInstance((Stmt) lastUnit)){
                JTableSwitchStmt table = (JTableSwitchStmt) lastUnit;
                Value key = table.getKey();
                warpGetConditionSub(key, valueNodesTmp, unitTmp);
            }

            //If the last unit of basic block is switch case statement, in look up switch format
            else if(JLookupSwitchStmt.class.isInstance((Stmt) lastUnit)){
                JLookupSwitchStmt table = (JLookupSwitchStmt) lastUnit;
                Value key = table.getKey();
                warpGetConditionSub(key, valueNodesTmp, unitTmp);
            }
            blockConstraintLocalMap.put(block, valueNodesTmp);
        }



        return blockConstraintLocalMap;
    }

    public static void warpGetConditionSub(Value v, ArrayList<ValueNode> valueNodesTmp, ArrayList<Unit> unitTmp){
        List<ValueBox> usedValue = v.getUseBoxes();
        for(ValueBox valueBox:usedValue){
            if(JimpleLocal.class.isInstance((Value) valueBox.getValue())){
                ValueNode valueNodeTmp = new ValueNode();
                valueNodeTmp.setLocalID(valueBox.getValue());
                getConditionSub(unitTmp, 1, valueNodeTmp);
                valueNodesTmp.add(valueNodeTmp);
            }
        }
    }

    public static void warpGetFuncArgSub(Value v, ArrayList<ValueNode> valueNodesTmp, ArrayList<Unit> unitTmp){
        if(JimpleLocal.class.isInstance(v)){
                ValueNode valueNodeTmp = new ValueNode();
                valueNodeTmp.setLocalID(v);
                getConditionSub(unitTmp, 1, valueNodeTmp);
                valueNodesTmp.add(valueNodeTmp);
            }
    }


    public static void processNewExpr(ArrayList<Unit> units, int unitIndex, ValueNode node){
        int indexTmp = 0;
        String constructorCls = ((AssignStmt) units.get(unitIndex)).getRightOpBox().getValue().getType().toString();
        Value rcvObj = ((AssignStmt) units.get(unitIndex)).getLeftOp();
        while(indexTmp < unitIndex){

            if((units.get(indexTmp) instanceof JInvokeStmt) && ((JInvokeStmt) units.get(indexTmp)).getInvokeExprBox().getValue() instanceof JSpecialInvokeExpr
                    && ((JSpecialInvokeExpr) ((JInvokeStmt) units.get(indexTmp)).getInvokeExprBox().getValue()).getBaseBox().getValue() == rcvObj
                    && ((JSpecialInvokeExpr) ((JInvokeStmt) units.get(indexTmp)).getInvokeExprBox().getValue()).getMethodRef().getDeclaringClass().toString().equals(constructorCls)
                    && ((JSpecialInvokeExpr) ((JInvokeStmt) units.get(indexTmp)).getInvokeExprBox().getValue()).getMethodRef().getName().equals("<init>")
            ){
                List<Value> paraLst = ((JSpecialInvokeExpr) ((JInvokeStmt) units.get(indexTmp)).getInvokeExprBox().getValue()).getArgs();
                for(Value para:paraLst){
                    if(JimpleLocal.class.isInstance(para)){
                        ValueNode valueNodetmp = new ValueNode();
                        valueNodetmp.setLocalID(para);
                        node.addChildren(valueNodetmp);
                        getConditionSub(units, indexTmp, valueNodetmp);
                    }
                    else if(para instanceof Constant){
                        ValueNode valueNodetmp = new ValueNode();
                        valueNodetmp.setLocalID(para);
                        node.addChildren(valueNodetmp);
                    }
                }
            }
            indexTmp += 1;
        }
    }


    public static void getConditionSub(ArrayList<Unit> units, int unitIndex, ValueNode node){
        while(unitIndex < units.size()){
            Unit unitAnalysis = units.get(unitIndex);
            if(unitAnalysis instanceof AssignStmt){
                if(((AssignStmt) unitAnalysis).getLeftOp() == node.getLocalID()) {

                    Value rightExp = ((AssignStmt) unitAnalysis).getRightOp();
                    node.setValue(rightExp);

                    // New expression is different
                    if (((AssignStmt) unitAnalysis).getRightOp().getUseBoxes().size() == 0 && ((AssignStmt) unitAnalysis).getRightOpBox().getValue() instanceof JNewExpr) {
                        processNewExpr(units, unitIndex, node);
                    }

                    else{
                        List<ValueBox> usedValue = rightExp.getUseBoxes();
                        for (ValueBox valueBox : usedValue) {
                            if (JimpleLocal.class.isInstance((Value) valueBox.getValue())) {
                                ValueNode valueNodetmp = new ValueNode();
                                valueNodetmp.setLocalID(valueBox.getValue());
                                node.addChildren(valueNodetmp);
                                if (valueBox.getValue() != node.getLocalID()) {                //avoid cases like r1= invoke(r1)
                                    getConditionSub(units, unitIndex, valueNodetmp);
                                }
                            }
                        }

                        // In case the rightExp is already a JimpleLocal
                        if(usedValue.size() == 0 && rightExp instanceof JimpleLocal){
                            ValueNode valueNodetmp = new ValueNode();
                            valueNodetmp.setLocalID(rightExp);
                            node.addChildren(valueNodetmp);
                            if (rightExp != node.getLocalID()) {                //avoid cases like r1= invoke(r1)
                                getConditionSub(units, unitIndex, valueNodetmp);
                            }
                        }
                    }
                }
            }
            else if(unitAnalysis instanceof IdentityStmt){
                if(((IdentityStmt) unitAnalysis).getLeftOp() == node.getLocalID()){
                    node.setValue(((IdentityStmt) unitAnalysis).getRightOp());
                }
            }
            unitIndex = unitIndex + 1;
        }
    }

    public static ArrayList<Path> getShortPathExceedMaxBlock(String callFile, int[][] AdjMatrix, ExceptionalBlockGraph bg, ArrayList<Integer> blockIndex, Boolean pathFlag) throws Exception {

        File adjFileexit = new File("./" + callFile + ".adjTmp");
        if(! adjFileexit.exists()){
            BufferedWriter out = new BufferedWriter(new FileWriter("./" + callFile + ".adjTmp"));
            for(int[] row:AdjMatrix){
                for(int col:row) {
                    out.write(""+col);
                    out.write(" ");
                }
                out.write("\n");
            }
            out.close();
        }

        List<Block> heads = bg.getHeads();
        ArrayList<Path> paths = new ArrayList<>();
        for(Block headBlock:heads) {
            int headIndex = headBlock.getIndexInMethod();

            for (int end : blockIndex) {

                if(headIndex == end){
                    ArrayList<Integer> BlockIndexInterger = new ArrayList<>();
                    BlockIndexInterger.add(end);
                    Path path = new Path();
                    ArrayList<Block> blocks = getBlocks(bg, BlockIndexInterger);
                    path.setPath(blocks);
                    paths.add(path);
                    continue;
                }


                //When the flag is true is get all the path in the CFG, when the flag is false it get the shortest.

                Process proc = null;
                if(pathFlag) {
                    //proc = Runtime.getRuntime().exec("/data/bugDetection/miniconda3/bin/python "+ "./use_networkx.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(headIndex) +" " +String.valueOf(end));
                    proc = Runtime.getRuntime().exec("python3 " + "./use_networkx.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(headIndex) + " " + String.valueOf(end));
                }
                else{
                    //proc = Runtime.getRuntime().exec("/data/bugDetection/miniconda3/bin/python "+ "./use_networkx_shortest.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(headIndex) +" " +String.valueOf(end));
                    proc = Runtime.getRuntime().exec("python3 " + "./use_networkx_shortest.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(headIndex) + " " + String.valueOf(end));
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String thisLine;
                while ((thisLine = in.readLine()) != null) {
                    if(thisLine.trim().equals("")) {
                        continue;
                    }
                    String[] BlockIndexString = thisLine.trim().split(" ");
                    ArrayList<Integer> BlockIndexInterger = new ArrayList<>();
                    for (String index : BlockIndexString) {
                        BlockIndexInterger.add(Integer.parseInt(index));
                    }
                    Path path = new Path();
                    ArrayList<Block> blocks = getBlocks(bg, BlockIndexInterger);
                    path.setPath(blocks);
                    paths.add(path);
                }
            }
        }

        File adjFileexitde = new File("./" + callFile + ".adjTmp");
        if(adjFileexitde.exists()){
            adjFileexitde.delete();
        }
        if(paths.size() == 0){
            throw new Exception("Python 获取的路径为空" + " Check file:" + callFile);
        }
        return paths;
    }


    public static ArrayList<ArrayList<Integer>> getAllCFGPath(String callFile, SootMethod sootMethod) throws Exception {
        ExceptionalBlockGraph bg = new ExceptionalBlockGraph(sootMethod.getActiveBody());
        ArrayList<ArrayList<Integer>> edges = getEdge(bg);
        int[][] AdjMatrix = getAdjMatrix(bg.size(), edges);

        BufferedWriter out = new BufferedWriter(new FileWriter("./" + callFile + ".adjTmp"));
        for(int[] row:AdjMatrix){
            for(int col:row) {
                out.write(""+col);
                out.write(" ");
            }
            out.write("\n");
        }
        out.close();

        List<Block> heads = bg.getHeads();
        List<Block> tails = bg.getTails();

        ArrayList<ArrayList<Integer>> paths = new ArrayList<>();

        for(Block block:heads){
            for(Block block1:tails){
                if(block.getIndexInMethod() == block1.getIndexInMethod()) {
                    ArrayList<Integer> BlockIndexInterger = new ArrayList<>();
                    BlockIndexInterger.add(block.getIndexInMethod());
                    paths.add(BlockIndexInterger);
                }
                Process proc = null;
                if(bg.size() <= 200) {
                    //Process proc = Runtime.getRuntime().exec("/data/bugDetection/miniconda3/bin/python "+ "./use_networkx.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(block.getIndexInMethod()) + " " +String.valueOf(block1.getIndexInMethod()));
                    proc = Runtime.getRuntime().exec("python3 " + "./use_networkx.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(block.getIndexInMethod()) + " " + String.valueOf(block1.getIndexInMethod()));
                }
                else{
                    //Process proc = Runtime.getRuntime().exec("/data/bugDetection/miniconda3/bin/python "+ "./use_networkx_shortest.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(block.getIndexInMethod()) + " " +String.valueOf(block1.getIndexInMethod()));
                    proc = Runtime.getRuntime().exec("python3 " + "./use_networkx_shortest.py " + "./" + callFile + ".adjTmp" + " " + String.valueOf(block.getIndexInMethod()) + " " + String.valueOf(block1.getIndexInMethod()));
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String thisLine;
                while ((thisLine = in.readLine()) != null) {
                    if(thisLine.trim().equals("")) {
                        continue;
                    }
                    String[] BlockIndexString = thisLine.trim().split(" ");
                    ArrayList<Integer> BlockIndexInterger = new ArrayList<>();
                    for (String index : BlockIndexString) {
                        BlockIndexInterger.add(Integer.parseInt(index));
                    }
                    paths.add(BlockIndexInterger);
                }
            }
        }

        File adjFileexitde = new File("./" + callFile + ".adjTmp");
        if(adjFileexitde.exists()){
            adjFileexitde.delete();
        }

        if(paths.size() == 0){
            throw new Exception("Python get empty size" + " Check file:" + callFile);
        }
        return paths;
    }

}
