package analyseDepth.analyzer;

import analyseDepth.path.BlocksAlongHops;
import analyseDepth.path.OneHop;
import analyseDepth.path.Path;
import analyseDepth.utils.CallUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static analyseDepth.utils.PathUtils.getShortestPathMtd2Mtd;

public class BlockNumDepthAnalyzer {
    public BlockNumDepthAnalyzer() {

    }

    public float getCVEDepth(ArrayList<ArrayList<OneHop>> allCallPath) {
        ArrayList<BlocksAlongHops> allCallStackBlocksSelected = getShortestPathMtd2Mtd(allCallPath);

        float all_length = 0.0F;
        for(BlocksAlongHops blocksAlongHops:allCallStackBlocksSelected){
            all_length = all_length + blocksAlongHops.getBlockNum();
        }
        float average;
        if(allCallStackBlocksSelected.size() != 0){
            average = all_length/allCallStackBlocksSelected.size();
        }
        else{
            average = 0;
        }
        return average;
    }

    public void getVulMethodDepth(ArrayList<ArrayList<OneHop>> allCallPath) {
        ArrayList<BlocksAlongHops> allCallStackBlocks = new ArrayList<>();
        for(ArrayList<OneHop> callStack:allCallPath){
            BlocksAlongHops blocksAlongHops = new BlocksAlongHops();
            blocksAlongHops.setStartMethod(callStack.get(0).getStartMtd().getSignature());
            blocksAlongHops.setEndMethod(callStack.get(callStack.size()-1).getEndMtd().getSignature());
            blocksAlongHops.setEndMethodModifier(CallUtils.getModifier(callStack.get(callStack.size()-1).getEndMtd()));
            int blockNum = 0;
            for(OneHop oneHop:callStack){
                int shortestPathLength = 999999999;
                for(Path path:oneHop.getPaths()){
                    if(path.getPath().size() < shortestPathLength){
                        shortestPathLength = path.getPath().size();
                    }
                }
                blockNum = blockNum + shortestPathLength;
            }
            blocksAlongHops.setBlockNum(blockNum);
            allCallStackBlocks.add(blocksAlongHops);
        }
        HashSet<BlocksAlongHops> allCallStackBlocksSelected = new HashSet<>();
        for(BlocksAlongHops blocksAlongHops1:allCallStackBlocks){
            BlocksAlongHops blocksAlongHopstmp = blocksAlongHops1;
            for(BlocksAlongHops blocksAlongHops2:allCallStackBlocks){
                if(blocksAlongHopstmp.equals(blocksAlongHops2)){
                    if(blocksAlongHops2.getBlockNum() < blocksAlongHopstmp.getBlockNum()){
                        blocksAlongHopstmp = blocksAlongHops2;
                    }
                }
            }
            allCallStackBlocksSelected.add(blocksAlongHopstmp);
        }

        HashMap<String, ArrayList<Integer>> vulMethodDepthLst = new HashMap<>();
        for(BlocksAlongHops blocksAlongHops:allCallStackBlocksSelected){
            String endMethodName = blocksAlongHops.getEndMethod();
            String endMethodModifier = blocksAlongHops.getEndMethodModifier();
            String endMethod = endMethodName + "@" + endMethodModifier;
            if(vulMethodDepthLst.containsKey(endMethod)){
                ArrayList<Integer> blockNumTmp = vulMethodDepthLst.get(endMethod);
                blockNumTmp.add(blocksAlongHops.getBlockNum());
            }
            else{
                ArrayList<Integer> blockNumTmp = new ArrayList<>();
                blockNumTmp.add(blocksAlongHops.getBlockNum());
                vulMethodDepthLst.put(endMethod,blockNumTmp);
            }
        }

        HashMap<String, Float> vulMethodDepth = new HashMap<>();
        for(String key: vulMethodDepthLst.keySet()){
            ArrayList<Integer> blockNumTmp = vulMethodDepthLst.get(key);
            float vulMtdAverage;
            float vulMtdSum = 0.0F;
            for(int num:blockNumTmp){
                vulMtdSum = vulMtdSum + num;
            }
            vulMtdAverage = vulMtdSum/blockNumTmp.size();
            vulMethodDepth.put(key, vulMtdAverage);
        }

        for(String key: vulMethodDepth.keySet()){
            System.out.println(key + ":" + vulMethodDepth.get(key));
        }
    }
}
