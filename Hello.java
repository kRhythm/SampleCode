
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
                    }
                    CompilationUnit cu = parse(file);
                    List<Node> nodes = collectAllNodes(cu);
                    nodes.forEach(n -> solve(n));
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private List<Node> collectAllNodes(Node node) {
        List<Node> nodes = new ArrayList<>();
        node.walk(nodes::add);
        nodes.sort(comparing(n -> n.getBegin().get()));
        return nodes;
    }
    
     public void solveMethodCalls(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    if (printFileName) {
                        out.println("- parsing " + file.toAbsolutePath());
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
