package analyseDepth.path;

import analyseDepth.customClass.ValueNode;
import soot.toolkits.graph.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class BlocksAlongHops {
    private String startMethod;
    private String endMethod;
    private int blockNum;
    private String endMethodModifier;
    private ArrayList<Block> pathBlock;
    private HashMap<Block, ArrayList<ValueNode>> localMap;
    private ArrayList<ArrayList<Block>> hopBlocks;

    public BlocksAlongHops(){
        this.startMethod = "";
        this.endMethod = "";
        this.endMethodModifier = "";
        this.blockNum = 0;
        this.pathBlock = new ArrayList<Block>();
        this.hopBlocks = new ArrayList<ArrayList<Block>>();
        this.localMap = new  HashMap<Block, ArrayList<ValueNode>>();
    }

    public int getBlockNum() {
        return this.blockNum;
    }

    public String getEndMethod() {
        return this.endMethod;
    }

    public String getStartMethod() {
        return this.startMethod;
    }

    public String getEndMethodModifier(){return this.endMethodModifier;}

    public ArrayList<Block> getPathBlock(){return this.pathBlock;}

    public HashMap<Block, ArrayList<ValueNode>> getLocalMap(){return this.localMap;}

    public ArrayList<ArrayList<Block>> getHopBlocks(){return this.hopBlocks;}

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public void setEndMethod(String endMethod) {
        this.endMethod = endMethod;
    }

    public void setStartMethod(String startMethod) {
        this.startMethod = startMethod;
    }

    public void setEndMethodModifier(String endMethodModifier) {
        this.endMethodModifier = endMethodModifier;
    }

    public void setPathBlock(ArrayList<Block> pathBlock) { this.pathBlock = pathBlock; }

    public void setLocalMap(HashMap<Block, ArrayList<ValueNode>> localMap){this.localMap=localMap;}

    public void setHopBlocks(ArrayList<ArrayList<Block>> hopBlocks){this.hopBlocks=hopBlocks;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlocksAlongHops that = (BlocksAlongHops) o;
        return startMethod.equals(that.startMethod) && endMethod.equals(that.endMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startMethod, endMethod);
    }
}
