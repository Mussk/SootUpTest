import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

import sootup.core.views.View;

import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;

import sootup.java.core.views.JavaView;



import java.io.IOException;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;




public class MySootUpAnalyzer {

    public static void main(String[] args) throws
            IOException,ClassHierarchyException, CallGraphBuilderCancelException, WalaException, CancelException {
        try {

            String pathToTests = Paths.get("src/main/resources/TestCases").toAbsolutePath().toString();
            MySootUpAnalyzer mySootUpAnalyzer = new MySootUpAnalyzer();

            //mySootUpAnalyzer.PrintStatementsList(pathToTests);

            mySootUpAnalyzer.doCallGraphAnalysis(pathToTests);

        }catch (Exception exception) {exception.printStackTrace();}
    }

    /**
     *  Prints statements of a given method in class.
     * If it finds method invoked by user (not by library), prints statements of that method.
     * Works with the depth of 1, for now
     * TODO: add recursive calls?
     * **/
    public void PrintStatementsList(String javaClassPath) throws Exception {

        System.out.println("Root folder: " + javaClassPath);
        AnalysisInputLocation inputLocation = PathBasedAnalysisInputLocation.create(Paths.get(javaClassPath), null);

        View view = new JavaView(inputLocation);

        TestSuiteReader testSuiteReader = new TestSuiteReader();
        List<String> testSuitePackages = testSuiteReader.GetTestSuitePaths(javaClassPath);

        for (String packagePath : testSuitePackages) {
            List<String> classNames = testSuiteReader.GetTestClasses(packagePath);

            for (String className : classNames) {

                 System.out.println("\n==================== " + className + " ====================\n");

                ClassType classType =
                        view.getIdentifierFactory().
                                getClassType(Paths.get(packagePath).getFileName().toString() + "." + className);

                MethodSignature methodSignature =
                        view.getIdentifierFactory()
                                .getMethodSignature(classType,
                                        "main", "void", Collections.singletonList("java.lang.String[]"));

                SootClass sootClass = view.getClass(classType).get();

                SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();

                List<Stmt> stmts = sootMethod.getBody().getStmts();

                for (Stmt statement : stmts)
                {
                    System.out.println(statement);

                    if(statement.containsInvokeExpr() &&
                            statement.getInvokeExpr().toString().startsWith("virtualinvoke") &&
                            statement.getInvokeExpr().getMethodSignature().getDeclClassType().toString().contains("testSourceCode"))
                    {

                        System.out.println("///////////////////// it is invokeExpr,invoked by user, print its expressions:\n");

                        MethodSignature invokedSignature = view.getIdentifierFactory()
                                .parseMethodSignature(statement.getInvokeExpr().getMethodSignature().toString());
                        SootMethod optionalInvokedMethod = view.getMethod(invokedSignature).get();

                        List<Stmt> nestedStatements = optionalInvokedMethod.getBody().getStmts();

                        nestedStatements.forEach(System.out::println);

                        System.out.println();

                    }
                }


            }
        }
    }

    public void doCallGraphAnalysis(String javaClassPath) throws Exception {

        List<AnalysisInputLocation> inputLocations = new ArrayList<>();
        inputLocations.add(
                new JavaClassPathAnalysisInputLocation(javaClassPath));
        System.out.println(System.getProperty("java.home"));
        inputLocations.add(
                new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "\\lib\\jrt-fs.jar"));

        JavaView view = new JavaView(inputLocations);

        TestSuiteReader testSuiteReader = new TestSuiteReader();
        List<String> testSuitePackages = testSuiteReader.GetTestSuitePaths(javaClassPath);


        for (String packagePath : testSuitePackages) {
            List<String> classNames = testSuiteReader.GetTestClasses(packagePath);

            for (String className : classNames) {

                System.out.println("\n==================== " + className + " ====================\n");
                // Get a MethodSignature
                ClassType classTypeA = view.
                        getIdentifierFactory().
                        getClassType(Paths.get(packagePath).getFileName().toString() + "." + className);

                MethodSignature entryMethodSignature =
                        view.getIdentifierFactory()
                                .getMethodSignature(
                                        classTypeA,
                                        "main",
                                        "void",
                                        Collections.singletonList("java.lang.String[]"));

             /*   CallGraphAlgorithm cha =
                        new ClassHierarchyAnalysisAlgorithm(view);

                CallGraph cg =
                        cha.initialize(Collections.singletonList(entryMethodSignature));

                System.out.println(cg);*/

                CallGraphAlgorithm rta =
                        new RapidTypeAnalysisAlgorithm(view);

                CallGraph cg =
                        rta.initialize(Collections.singletonList(entryMethodSignature));

                System.out.println(cg);

                cg.callsFrom(entryMethodSignature).forEach(System.out::println);

               /* // Create type hierarchy and CHA
                CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

                // Create CG by initializing CHA with entry method(s)
                CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

                cg.callsFrom(entryMethodSignature).forEach(System.out::println);*/

            }
        }
    }

}