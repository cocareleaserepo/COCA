package analyseDepth.utils;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;

public class CallUtils {
    public static ArrayList<String[]> getCallStack(String fileName){
        ArrayList<String[]> callStack = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader reader = null;
        try{
            String tempString = null;
            reader = new BufferedReader(new FileReader(file));
            while((tempString = reader.readLine()) != null){
                callStack.add(tempString.split(";"));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return callStack;
    }

    public static SootMethod getStartMtd(String mtdSig, Scene scene){
        ArrayList<String> mtdInfo = splitClsNameMtdNameParaName(mtdSig);
        SootClass st = scene.getSootClass(mtdInfo.get(0));
        String mtdName = mtdInfo.get(1);
        ArrayList<String> para = new ArrayList<>();
        if(mtdInfo.size() > 2) {
            if(mtdInfo.size() == 3){
                para.add(mtdInfo.get(2));   //Handle the case when there is only one parameter because sublist(2, -1) will come across with IllegalArgumentException.
            }
            else {
                    List<String> paraTmpLst = mtdInfo.subList(2, mtdInfo.size());
                    for(String parameter:paraTmpLst) {
                        para.add(parameter);
                    }
            }
        }

        SootMethod mtd_null = null;
        List<SootMethod> sootMtds = st.getMethods();
        for(SootMethod mtd:sootMtds){
            if(mtd.getName().equals(mtdName) && mtd.getParameterCount() == para.size()){
                List<Type> paraType = mtd.getParameterTypes();
                if(paraType.size() == 0){
                    return mtd;
                }
                int flag = 1;
                for(int i = 0; i < paraType.size(); i++){
                    if(!paraType.get(i).toString().equals(para.get(i))){
                        flag = 0;
                        break;
                    }
                }
                if(flag == 1){
                    return mtd;
                }

            }
        }
        return mtd_null;
    }

    private static ArrayList<String> splitClsNameMtdNameParaName(String mtdSig){
        ArrayList<String> mtdInfo = new ArrayList<>();
        String clsName = mtdSig.split(":")[0];
        String mtdName = mtdSig.split(":")[1].split("\\(")[0];
        mtdInfo.add(clsName);
        mtdInfo.add(mtdName);
        String[] para = new String[0];
        String paras = mtdSig.substring(mtdSig.indexOf("(")+1, mtdSig.indexOf(")"));
        if(paras.length() != 0){
            para = paras.split(",");
        }
        mtdInfo.addAll(Arrays.asList(para));
        return mtdInfo;
    }

    public static void findNextMethod(SootMethod origin,  NextMethod nmd, String signature){

        Body body = null;
        try {
            body = origin.getActiveBody();
        }
        catch (Throwable e){
            nmd.setSm(null);
        }
        ExceptionalBlockGraph bg = new ExceptionalBlockGraph(body);
        List<Block> blocks = bg.getBlocks();
        ArrayList<Integer> refBlocks = new ArrayList<>();
        for(int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
            for (Unit unit : blocks.get(blockIndex)) {
                if (JInvokeStmt.class.isInstance((Stmt) unit) || JAssignStmt.class.isInstance((Stmt) unit)) {
                    SootMethod refMtd = null;
                    try {
                        refMtd = ((Stmt) unit).getInvokeExpr().getMethod();
                    }
                    catch (RuntimeException e){
                        continue;
                    }

                    String refSignature = getRefSignature(refMtd);
                    if (refSignature.equals(signature)) {
                        if(JInvokeStmt.class.isInstance((Stmt) unit)){
                            nmd.setAssiginOrInvoke(false);
                        }
                        if(JAssignStmt.class.isInstance((Stmt) unit)) {
                            nmd.setAssiginOrInvoke(true);
                        }
                        nmd.setSm(refMtd);
                        refBlocks.add(blockIndex);
                    }
                }
            }
        }
        nmd.setRefBlockIndex(refBlocks);
    }

    public static String getRefSignature(SootMethod refMtd){
        StringBuilder refSignature = new StringBuilder();
        String ClsName = refMtd.getDeclaringClass().toString();
        refSignature.append(ClsName);
        refSignature.append(":");
        refSignature.append(refMtd.getName());
        refSignature.append("(");
        List<Type> refPara = refMtd.getParameterTypes();
        if (refPara != null) {
            for(int i = 0; i < refPara.size(); ++i) {
                refSignature.append(((Type)refPara.get(i)).toString());
                if (i < refPara.size() - 1) {
                    refSignature.append(",");
                }
            }
        }

        refSignature.append(")");
        return refSignature.toString();
    }

    public static String getModifier(SootMethod sm){
        String modifier = "";
        if(sm.isPrivate()){
            modifier = "private";
        }
        else if(sm.isPublic()){
            modifier = "public";
        }
        else if(sm.isProtected()){
            modifier = "protected";
        }
        return modifier;
    }


    public static class NextMethod{
        private SootMethod sm;
        private Boolean AssiginOrInvoke;
        private ArrayList<Integer> refBlockIndex;

        public SootMethod getSm(){
            return this.sm;
        }
        public void setSm(SootMethod sm) {
            this.sm = sm;
        }
        public void setRefBlockIndex(ArrayList<Integer> refBlockIndex) {
            this.refBlockIndex = refBlockIndex;
        }
        public void setAssiginOrInvoke(Boolean flag){this.AssiginOrInvoke=flag;}


        public Boolean getAssiginOrInvoke() {
            return AssiginOrInvoke;
        }

        public ArrayList<Integer> getRefBlockIndex() {
            return refBlockIndex;
        }
    }

}
