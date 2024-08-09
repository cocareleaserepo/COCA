package analyseDepth.utils;


import analyseDepth.customClass.ValueNode;
import soot.RefType;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class ConstraintUtils {

    public static ConditionExpr neOperand(ConditionExpr condition) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Value left = condition.getOp1();
        Value right = condition.getOp2();
        ConditionExpr value = null;
        String conditionClzName = condition.getClass().toString();
        if(conditionClzName.equals("class soot.jimple.internal.JEqExpr")){
            value = new JNeExpr(left, right);
        }
        else if(conditionClzName.equals("class soot.jimple.internal.JGeExpr")){
            value = new JLtExpr(left, right);
        }

        else if(conditionClzName.equals("class soot.jimple.internal.JGtExpr")){
            value = new JLeExpr(left, right);
        }
        else if(conditionClzName.equals("class soot.jimple.internal.JLeExpr")){
            value = new JGtExpr(left, right);
        }
        else if(conditionClzName.equals("class soot.jimple.internal.JLtExpr")){
            value = new JGeExpr(left, right);
        }
        else if(conditionClzName.equals("class soot.jimple.internal.JNeExpr")){
            value = new JEqExpr(left, right);
        }
        return value;

    }

    public static String getStringCondition(Value v, ArrayList<ValueNode> conditionTrees){
        String conditionString = v.toString();
        String conditionSub = null;
        List<ValueBox> conditionBox = v.getUseBoxes();
        for(ValueBox valueBoxCondition:conditionBox){
            if(JimpleLocal.class.isInstance((Value) valueBoxCondition.getValue())){
                for(ValueNode conditionTree:conditionTrees){
                    if(conditionTree.getLocalID()==valueBoxCondition.getValue()){
                        String conditionLocalName = valueBoxCondition.getValue().toString();
                        String conditionValue = buildConditionStringSimple(conditionTree);
                        //String conditionValue = "";                                                 // Turn off.
                        conditionSub = conditionString.replace(conditionLocalName, conditionValue);
                    }
                }
            }
        }
        return conditionSub;
    }

    public static String buildConditionStringFull(ValueNode conditionNode){
        if(conditionNode.getChildren().size() == 0){
            return conditionNode.getValue().toString();
        }
        String originalExp = conditionNode.getValue().toString();
        String newExp = "";
        for(ValueNode conditonChild:conditionNode.getChildren()){
            String childName = conditonChild.getLocalID().toString();
            String childTrueValue = buildConditionStringFull(conditonChild);
            newExp =originalExp.replace(childName, childTrueValue);
        }
        return newExp;

    }

    public static String buildConditionStringSimple(ValueNode conditionNode){
        if(conditionNode.getChildren().size() == 0){
            return constructConditionSimpleString(conditionNode);
        }
        String newExp = constructConditionSimpleString(conditionNode);
        for(ValueNode conditonChild:conditionNode.getChildren()){
            String childName = conditonChild.getLocalID().toString();
            String childTrueValue = buildConditionStringSimple(conditonChild);
            newExp = newExp.replace(childName, childTrueValue);
        }
        return newExp;

    }



    public static String constructConditionSimpleString(ValueNode vnode){
        // Build the string from condition with easy to read condition and easy to build a tree
        String res = "";
        Value v = vnode.getValue();
        if(ParameterRef.class.isInstance(v)){
            res = res + "PARAMETER_" +((ParameterRef) v).getIndex();
        }
        else if(ThisRef.class.isInstance(v)){
            res = res + "THIS_";
        }
        else if(JCastExpr.class.isInstance(v)){
            res = res + ((JCastExpr) v).getOp().toString();
        }
        else if(JInstanceFieldRef.class.isInstance(v)){
            res = res + ((JInstanceFieldRef) v).getBaseBox().getValue().toString();
            res = res + ".";
            res = res + ((JInstanceFieldRef) v).getFieldRef().name();
        }
        else if(StaticFieldRef.class.isInstance(v)){
            res = res + "STATIC_";
            res = res + ((StaticFieldRef) v).getFieldRef().declaringClass().getShortName();
            res = res + ".";
            res = res + ((StaticFieldRef) v).getFieldRef().name();
        }
        else if(AbstractInstanceInvokeExpr.class.isInstance(v)) {
            res = res + ((AbstractInstanceInvokeExpr) v).getBaseBox().getValue().toString();
            res = res + ".";
            res = res + ((AbstractInstanceInvokeExpr) v).getMethodRef().getName();
            res = res + "(";
            for (int index = 0; index < ((AbstractInstanceInvokeExpr) v).getArgCount(); index++) {
                if (index == ((AbstractInstanceInvokeExpr) v).getArgCount() - 1) {
                    res = res + ((AbstractInstanceInvokeExpr) v).getArg(index).toString();
                } else {
                    res = res + ((AbstractInstanceInvokeExpr) v).getArg(index).toString() + ",";
                }
            }
            res = res + ")";
        }
        else if(AbstractStaticInvokeExpr.class.isInstance(v)){
            res = res + "STATIC_" + ((AbstractStaticInvokeExpr) v).getMethodRef().getDeclaringClass().getShortName();
            res = res + ".";
            res = res + ((AbstractStaticInvokeExpr) v).getMethodRef().getName();
            res = res + "(";
            for (int index = 0; index < ((AbstractStaticInvokeExpr) v).getArgCount(); index++) {
                if (index == ((AbstractStaticInvokeExpr) v).getArgCount() - 1) {
                    res = res + ((AbstractStaticInvokeExpr) v).getArg(index).toString();
                } else {
                    res = res + ((AbstractStaticInvokeExpr) v).getArg(index).toString() + ",";
                }
            }
            res = res + ")";

        }

        else if(JNewExpr.class.isInstance(v)){
            res = res + "NEW_" + ((RefType) v.getType()).getSootClass().getShortName() + "(";
            int index = 0;
            for(ValueNode vchild:vnode.getChildren()){
                if (index < vnode.getChildren().size() - 1){
                    res = res + vchild.getLocalID().toString() + ",";
                }
                else{
                    res = res + vchild.getLocalID().toString();
                }
                index = index + 1;
            }
            res = res + ")";
        }

        else if(Constant.class.isInstance(v)) {

            if(StringConstant.class.isInstance(v)){
                if ((((StringConstant) v).value).length() == 0){
                    res = res + "EMPTY_STRING";
                }
            }

            else if (ClassConstant.class.isInstance(v)) {
                String clsFullName = ((ClassConstant) v).getValue().substring(0, ((ClassConstant) v).getValue().length() - 1);
                String s = clsFullName.split("/")[clsFullName.split("/").length - 1];
                res = res + s;
            }

        }


        else{
            res = res + v.toString();
        }
        return res;
    }


    public static ArrayList<String> getStringFromCondition(ConditionExpr v){
        ArrayList<String> valueNodes = new ArrayList<>();
        Value op1 = v.getOp1();
        Value op2 = v.getOp2();
        if(!(op1 instanceof JimpleLocal)){
            valueNodes.add(op1.getClass().toString());
        }
        if(!(op2 instanceof JimpleLocal)){
            valueNodes.add(op2.getClass().toString());
        }
        return valueNodes;
    }

}
