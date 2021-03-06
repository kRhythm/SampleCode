
/**
 * Resolves resolvable nodes from one or more source files, and reports the results.
 * It is mainly intended as an example usage of JavaSymbolSolver.
 *
 * @author Federico Tomassetti
 */
public class SourceFileInfoExtractor {

    private final TypeSolver typeSolver;

    private int successes = 0;
    private int failures = 0;
    private int unsupported = 0;
    private boolean printFileName = true;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private boolean verbose = false;

    

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void setErr(PrintStream err) {
        this.err = err;
    }

    public int getSuccesses() {
        return successes;
    }

    public int getUnsupported() {
        return unsupported;
    }

    public int getFailures() {
        return failures;
    }

    private void solveTypeDecl(ClassOrInterfaceDeclaration node) {
        ResolvedTypeDeclaration typeDeclaration = JavaParserFacade.get(typeSolver).getTypeDeclaration(node);
        if (typeDeclaration.isClass()) {
            out.println("\n[ Class " + typeDeclaration.getQualifiedName() + " ]");
            for (ResolvedReferenceType sc : typeDeclaration.asClass().getAllSuperClasses()) {
                out.println("  superclass: " + sc.getQualifiedName());
            }
            for (ResolvedReferenceType sc : typeDeclaration.asClass().getAllInterfaces()) {
                out.println("  interface: " + sc.getQualifiedName());
            }
        }
    }

    private void solve(Node node) {
        if (node instanceof ClassOrInterfaceDeclaration) {
            solveTypeDecl((ClassOrInterfaceDeclaration) node);
        } else if (node instanceof Expression) {
            Node parentNode = demandParentNode(node);
            if (parentNode instanceof ImportDeclaration ||
                    parentNode instanceof Expression ||
                    parentNode instanceof MethodDeclaration ||
                    parentNode instanceof PackageDeclaration) {
                // skip
                return;
            }
            if (parentNode instanceof Statement ||
                    parentNode instanceof VariableDeclarator ||
                    parentNode instanceof SwitchEntry) {
                try {
                    ResolvedType ref = JavaParserFacade.get(typeSolver).getType(node);
                    out.println("  Line " + lineNr(node) + ") " + node + " ==> " + ref.describe());
                    successes++;
                } catch (UnsupportedOperationException upe) {
                    unsupported++;
                    err.println(upe.getMessage());
                    throw upe;
                } catch (RuntimeException re) {
                    failures++;
                    err.println(re.getMessage());
                    throw re;
                }
            }
        }
    }

    private void solveMethodCalls(Node node) {
        if (node instanceof MethodCallExpr) {
            out.println("  Line " + lineNr(node) + ") " + node + " ==> " + toString((MethodCallExpr) node));
        }
        for (Node child : node.getChildNodes()) {
            solveMethodCalls(child);
        }
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
        node.walk(nodes::add);
        nodes.sort(comparing(n -> n.getBegin().get()));
        return nodes;
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

    private int lineNr(Node node) {
        return node.getRange().map(range -> range.begin.line).orElseThrow(IllegalStateException::new);
    }
}
