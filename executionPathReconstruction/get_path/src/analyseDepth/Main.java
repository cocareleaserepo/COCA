package analyseDepth;

import java.util.*;

import analyseDepth.analyzer.PathCoverageAnalyzer;
import analyseDepth.getClientcs.GetCSBranchInfo;
import analyseDepth.path.OneHop;
import analyseDepth.analyzer.BlockNumDepthAnalyzer;
import analyseDepth.analyzer.ConstraintAnalyzer;
import analyseDepth.customClass.ConditionResult;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import soot.*;
import soot.options.Options;
import analyseDepth.utils.VFUtils;


public class Main {
    public static void main(String[] args) throws Exception {
        org.apache.commons.cli.Options options = OptionsCfg.buildOptions();

        BasicParser basicParser = new BasicParser();
        CommandLine commandLine = basicParser.parse(options, args);

        if (!commandLine.hasOption("jarName") && !commandLine.hasOption("callStackFile")) {
            OptionsCfg.printUsage(options);
            return;
        }

        String classesDir = commandLine.getOptionValue("jarName");
        String callStackFile = commandLine.getOptionValue("callStackFile");
        //set classpath

        Options.v().set_soot_classpath(Scene.v().defaultClassPath() + ":./");
        //set phase option
        //add an intra-procedural analysis phase to Soot
        Options.v().set_process_dir(Arrays.asList(classesDir));
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_output_format(1);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_shimple(false);
        Options.v().set_whole_program(false);
        Scene.v().loadNecessaryClasses();
        Scene.v().loadBasicClasses();


        ArrayList<ArrayList<OneHop>> allCallPath;
        allCallPath = WrapRunPack.getMultiHop(callStackFile);
        if(commandLine.hasOption("CVEBlockDepth") || commandLine.hasOption("MethodBlockDepth")){

            if (commandLine.hasOption("CVEBlockDepth")) {
                BlockNumDepthAnalyzer blockNumDepthAnalyzer = new BlockNumDepthAnalyzer();
                float CVEDepth = blockNumDepthAnalyzer.getCVEDepth(allCallPath);
                System.out.println(CVEDepth);
            }

            if (commandLine.hasOption("MethodBlockDepth")) {
                BlockNumDepthAnalyzer blockNumDepthAnalyzer = new BlockNumDepthAnalyzer();
                blockNumDepthAnalyzer.getVulMethodDepth(allCallPath);
            }
        }

        if(commandLine.hasOption("ConstraintLength") || commandLine.hasOption("ConstraintVariable") || commandLine.hasOption("ConstraintOperand") || commandLine.hasOption("ConstraintVariableReturnType") || commandLine.hasOption("ConstraintTreeExp")){

            ConstraintAnalyzer constraintAnalyzer = new ConstraintAnalyzer();
            ArrayList<ArrayList<ConditionResult>> allConditionResultAlongPath = constraintAnalyzer.getConstraintAlongPath(allCallPath);
            if(commandLine.hasOption("ConstraintLength")){
                constraintAnalyzer.printPathConditionLength(allConditionResultAlongPath);
            }
            else if(commandLine.hasOption("ConstraintVariable")){
                constraintAnalyzer.printPathConditionVariableType(allConditionResultAlongPath);
            }
            else if(commandLine.hasOption("ConstraintOperand")){
                constraintAnalyzer.printPathConditionOperand(allConditionResultAlongPath);
            }
            else if(commandLine.hasOption("ConstraintVariableReturnType")){
                constraintAnalyzer.printPathConditionVariableReturnType(allConditionResultAlongPath);
            }
            else if(commandLine.hasOption("ConstraintTreeExp")){
                constraintAnalyzer.printConditionTreeExp(allConditionResultAlongPath);
            }
        }

        if(commandLine.hasOption("FunctionParaAna")){
            VFUtils.getFunctionArgTree(allCallPath);
        }

        if(commandLine.hasOption("CallSiteInfo")){
            ArrayList<ArrayList<Boolean>> result = GetCSBranchInfo.getClientCallsiteInfo(callStackFile);
            GetCSBranchInfo.printFlag(result);
        }

        if(commandLine.hasOption("PathCoverage")){
            PathCoverageAnalyzer pathCoverageAnalyzer = new PathCoverageAnalyzer();
            pathCoverageAnalyzer.getPathCoverageCal(callStackFile);
        }

    }
}