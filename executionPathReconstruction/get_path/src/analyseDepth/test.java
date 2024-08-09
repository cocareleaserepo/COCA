package analyseDepth;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.CompleteBlockGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;

import java.util.Arrays;

import static analyseDepth.WrapRunPack.prepareWork;


public class test {
    public static void main(String[] args) {
        G.reset();
        Options.v().set_soot_classpath(Scene.v().defaultClassPath() + ":./");
        //set phase option
        //add an intra-procedural analysis phase to Soot
        Options.v().set_process_dir(Arrays.asList("test_CHA.jar"));
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_output_format(1);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_shimple(false);
        Options.v().set_whole_program(false);
        Scene.v().loadNecessaryClasses();

        prepareWork();
        SootClass sc = Scene.v().getSootClass("hah.hh.B");
        SootMethod sm = sc.getMethodByName("main");
        CompleteBlockGraph bg = new CompleteBlockGraph(sm.getActiveBody());
        int a = 1;
    }

}
