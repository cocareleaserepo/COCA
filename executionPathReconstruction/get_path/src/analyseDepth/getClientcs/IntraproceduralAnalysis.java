package analyseDepth.getClientcs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashSet;
import java.util.Set;

import static analyseDepth.utils.CallUtils.getRefSignature;

/**
 * Class implementing dataflow analysis
 */
public class IntraproceduralAnalysis extends ForwardFlowAnalysis<Unit, Set<FlowAbstraction>> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final SootMethod method;
	public String mtdSig;
	public Boolean returnFlag = false;
	public Boolean parameterFlag = false;

	public IntraproceduralAnalysis(Body b, String mtdSig) {
		super(new ExceptionalUnitGraph(b));
		this.method = b.getMethod();
		this.mtdSig = mtdSig;
	}


	@Override
	protected void flowThrough(Set<FlowAbstraction> taintsIn, Unit d, Set<FlowAbstraction> taintsOut) {
		Stmt s = (Stmt) d;

		if (s instanceof JAssignStmt) {
			JAssignStmt as = (JAssignStmt) s;
			Value rightOp = as.getRightOp();
			Value leftOp = as.getLeftOp();

			if(s.containsInvokeExpr()){
				String refSmSig;
				Boolean dangerFunctionFlag = false;
				try{
					SootMethod sm = s.getInvokeExpr().getMethod();
					refSmSig = getRefSignature(sm);
				}
				catch (Exception e){
					refSmSig = null;
				}


				if(refSmSig != null) {
					if (refSmSig.equals(this.mtdSig)) {
						taintsOut.add(new FlowAbstraction(d, (Local) leftOp));
						dangerFunctionFlag = true;
					}
				}

				if(!dangerFunctionFlag){
					InvokeExpr inv = s.getInvokeExpr();
					for (FlowAbstraction in : taintsIn) {
						if (inv.getArgs().contains(in.getLocal())) {
							this.parameterFlag = true;
						}
					}
				}
			}

			if (rightOp instanceof JimpleLocal) {
				for (FlowAbstraction abs : taintsIn) {
					if (rightOp == abs.getLocal()) {
						if (leftOp instanceof JInstanceFieldRef) {
							// Base Class as Tainted
							taintsOut.add(new FlowAbstraction(d, (Local) ((JInstanceFieldRef) leftOp).getBase()));
						} else if(leftOp instanceof JimpleLocal)
							taintsOut.add(new FlowAbstraction(d, (Local) leftOp));
					}
				}
			}

			if (rightOp instanceof AbstractJimpleFloatBinopExpr){
				Value op1 = ((AbstractJimpleFloatBinopExpr) rightOp).getOp1();
				Value op2 = ((AbstractJimpleFloatBinopExpr) rightOp).getOp2();

				for (FlowAbstraction abs : taintsIn) {
					if (op1 == abs.getLocal() || op2 == abs.getLocal()) {
						if (leftOp instanceof JInstanceFieldRef) {
							// Base Class as Tainted
							taintsOut.add(new FlowAbstraction(d, (Local) ((JInstanceFieldRef) leftOp).getBase()));
						} else
							taintsOut.add(new FlowAbstraction(d, (Local) leftOp));
					}
				}
			}


		}
		else if (s instanceof JInvokeStmt) {
			InvokeExpr inv = s.getInvokeExpr();
			for (FlowAbstraction in : taintsIn) {
				if (inv.getArgs().contains(in.getLocal())) {
					this.parameterFlag = true;
				}
			}

		}
		else if (s instanceof ReturnStmt) {
			for (FlowAbstraction in : taintsIn) {
				if (((ReturnStmt) s).getOp() == in.getLocal()) {
					this.returnFlag = true;
				}
			}
		}
		taintsOut.addAll(taintsIn);
	}

	@Override
	protected Set<FlowAbstraction> newInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected Set<FlowAbstraction> entryInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected void merge(Set<FlowAbstraction> in1, Set<FlowAbstraction> in2, Set<FlowAbstraction> out) {
		out.addAll(in1);
		out.addAll(in2);
	}

	@Override
	protected void copy(Set<FlowAbstraction> source, Set<FlowAbstraction> dest) {
		dest.clear();
		dest.addAll(source);
	}

	public void doAnalyis() {
		super.doAnalysis();
	}

}
