
package com.github.javaparser;


import com.github.valfirst.jbehave.junit.monitoring.JUnitReportingRunner;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

abstract class BasicJBehaveTest extends JUnitStories {

    private final String storiesPath;

    BasicJBehaveTest(String storiesPath) {
        this.storiesPath = storiesPath;
        JUnitReportingRunner.recommendedControls(configuredEmbedder());
    }

    public void solve(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    if (printFileName) {
                        out.println("- parsing " + file.toAbsolutePath());
                        String S = toString(node);
                        for(int i=0;i<5;i++){
                            if(O==S)
                                count++;
                        }
                        
                    }
                    
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private String toString(MethodCallExpr node) {
        try {
            return toString(JavaParserFacade.get(typeSolver).solve(node));
        } catch (Exception e) {
            if (verbose) {
                System.err.println("Error resolving call at L" + lineNr(node) + ": " + node);
                e.printStackTrace();
            }
            return "ERROR";
        }
    }

    private String toString(SymbolReference<ResolvedMethodDeclaration> methodDeclarationSymbolReference) {
        if (methodDeclarationSymbolReference.isSolved()) {
            return methodDeclarationSymbolReference.getCorrespondingDeclaration().getQualifiedSignature();
        } else {
            return "UNSOLVED";
        }
    }
    
    private List<Node> collectAllNodes(Node node) {
        List<Node> nodes = new ArrayList<>();
        
        return nodes;
    }
    
     public void solveMethodCalls(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    if (printFileName) {
                        out.println("- parsing " + file.toAbsolutePath());
                        System.out.println("Hello there");
                    }
                    CompilationUnit cu = parse(file);
                    solveMethodCalls(cu);
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public final List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), storiesPath, "");
    }

}
