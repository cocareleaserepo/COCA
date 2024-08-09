package analyseDepth.customClass;

import soot.Value;

import java.util.ArrayList;



public class ConditionResult {
    private Value condition;
    private ArrayList<ValueNode> value;

    private String constraintTreeStr;
    public Value getCondition() {
        return condition;
    }

    public ArrayList<ValueNode> getValue() {
        return value;
    }

    public void setCondition(Value condition) {
        this.condition = condition;
    }

    public void setValue(ArrayList<ValueNode> value) {
        this.value = value;
    }

    public void setConstraintTreeStr(String constraintTreeStr) {
        this.constraintTreeStr = constraintTreeStr;
    }

    public String getConstraintTreeStr() {
        return constraintTreeStr;
    }
}
