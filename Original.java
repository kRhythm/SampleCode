package com.github.javaparser.symbolsolver;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.github.javaparser.symbolsolver.javaparser.Navigator.requireParentNode;

/**
 * It prints information extracted from a source file. It is mainly intended as an example usage of JavaSymbolSolver.
 *
 * @author Federico Tomassetti
 */
public class SourceFileInfoExtractor {

    private TypeSolver typeSolver;

    private int ok = 0;
    private int ko = 0;
    private int unsupported = 0;
    private boolean printFileName = true;
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean verbose = false;

    public void setPrintFileName(boolean printFileName) {
        this.printFileName = printFileName;
    }

    public void clear() {
        ok = 0;
        ko = 0;
        unsupported = 0;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public int getOk() {
        return ok;

    }

    public int getUnsupported() {
        return unsupported;
    }

    public int getKo() {
        return ko;
    }

    

    private void solve(Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            solveTypeDecl((ClassOrInterfaceDeclaration) node);
        } else if (node instanceof Expression) {
            if ((requireParentNode(node) instanceof ImportDeclaration) || (requireParentNode(node) instanceof Expression)
                    || (requireParentNode(node) instanceof MethodDeclaration)
                    || (requireParentNode(node) instanceof PackageDeclaration)) {
                // skip
            } else if ((requireParentNode(node) instanceof Statement) || (requireParentNode(node) instanceof VariableDeclarator)) {
                try {
                    ResolvedType ref = JavaParserFacade.get(typeSolver).getType(node);
                    out.println("  Line " + node.getRange().get().begin.line + ") " + node + " ==> " + ref.describe());
                    ok++;
                } catch (UnsupportedOperationException upe) {
                    unsupported++;
                    err.println(upe.getMessage());
                    throw upe;
                } catch (RuntimeException re) {
                    ko++;
                    err.println(re.getMessage());
                    throw re;
                }
            }
        }
    }

    private void solveMethodCalls(Node node) {
        if (node instanceof MethodCallExpr) {
            out.println("  Line " + node.getBegin().get().line + ") " + node + " ==> " + toString((MethodCallExpr) node));
        }
        for (Node child : node.getChildNodes()) {
            solveMethodCalls(child);
        }
    }

   

    private List<Node> collectAllNodes(Node node) {
        List<Node> nodes = new LinkedList<>();
        collectAllNodes(node, nodes);
        nodes.sort((n1, n2) -> n1.getBegin().get().compareTo(n2.getBegin().get()));
        return nodes;
    }

    private void collectAllNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        node.getChildNodes().forEach(c -> collectAllNodes(c, nodes));
    }

    public void solve(Path path) throws IOException {
        File file = path.toFile();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                solve(f.toPath());
            }
        } else {
            if (file.getName().endsWith(".java")) {
                if (printFileName) {
                    out.println("- parsing " + file.getAbsolutePath());
                }
                CompilationUnit cu = JavaParser.parse(file);
                List<Node> nodes = collectAllNodes(cu);
                nodes.forEach(n -> solve(n));
            }
        }
    }

    public void solveMethodCalls(Path path) throws IOException {
        File file = path.toFile();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                solveMethodCalls(f.toPath());
            }
        } else {
            if (file.getName().endsWith(".java")) {
                if (printFileName) {
                    out.println("- parsing " + file.getAbsolutePath());
                }
                CompilationUnit cu = JavaParser.parse(file);
                solveMethodCalls(cu);
            }
        }
    }

    public void setTypeSolver(TypeSolver typeSolver) {
        this.typeSolver = typeSolver;
    }

}
