package analyseDepth;

import org.apache.commons.cli.*;

public class OptionsCfg {
    public static Options buildOptions() {
        Options options = new Options();
        options.addOption("j","jarName", true, "Jar file that to be analysed");
        options.addOption("c", "callStackFile", true, "File that contains call stack");
        options.addOption("h","help", false, "Print usage");
        options.addOption("bd", "CVEBlockDepth", false, "get block depth of target CVE");
        options.addOption("md", "MethodBlockDepth", false, "get block depth of method in target CVE");

        options.addOption("cs", "CallSiteInfo", false, "get information about the call site:Guarded@Returned@AsPara");
        options.addOption("cl", "ConstraintLength", false, "get the number of constraint along the path");
        options.addOption("cv", "ConstraintVariable", false, "get the type of variable involved in the constraint along the path");
        options.addOption("co", "ConstraintOperand", false, "get the operand of constraint along the path");
        options.addOption("cr", "ConstraintVariableReturnType", false, "get information about return type of the constraint variable");
        options.addOption("pc", "PathCoverage", false, "get path coverage of the path");
        options.addOption("fp", "FunctionParaAna", false, "get the vulnerable function parameter tree");
        options.addOption("ct", "ConstraintTreeExp", false, "get the path constraint expression tree");
        return options;
    }

    public static void printUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("OptionsUsage", options);
        System.out.println("Java -jar analyseDepth.jar -j JarFile -c CallStackFile <analysis pattern>");
    }
}
