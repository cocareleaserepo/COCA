package analyseDepth.analyzer;

import analyseDepth.path.OneHop;
import analyseDepth.path.Path;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.Block;


import java.util.ArrayList;
import java.util.Iterator;


public class ClientContextAnalyzer {
    public boolean haveBranch(ArrayList<ArrayList<OneHop>> allCallPath){
        OneHop firstHop = allCallPath.get(0).get(0);
        ArrayList<Path> paths = firstHop.getPaths();

        for(Path path:paths){
            int flag = 1;
            for(Block block:path.getPath()){
                for (Iterator<Unit> it = block.iterator(); it.hasNext(); ) {
                    Unit unit = it.next();
                    if(JIfStmt.class.isInstance((Stmt) unit)){
                        flag = 0;
                    }

                }
            }
            if(flag == 1){
                return false;
            }
        }
        return true;
    }

}
