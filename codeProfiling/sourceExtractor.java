import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Stream;

public class sourceExtractor {
    private static final Logger LOGGER = Logger.getLogger(sourceExtractor.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("project_extractor.log");
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.severe("Failed to set up file logging: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            LOGGER.severe("Usage: java ProjectMethodSourceExtractor <project_root_path> <fully_qualified_method_signature>");
            System.exit(1);
        }

        String projectRootPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        try {
            String sourceCode = extractMethodSourceFromProject(projectRootPath, fullyQualifiedMethodSignature);
            if (sourceCode != null) {
                System.out.println("Extracted source code:");
                System.out.println(sourceCode);
            } else {
                LOGGER.warning("Method not found in the project.");
            }
        } catch (IOException e) {
            LOGGER.severe("Error processing project: " + e.getMessage());
        }
    }

    public static String extractMethodSourceFromProject(String projectRootPath, String fullyQualifiedMethodSignature) throws IOException {
        String[] parts = fullyQualifiedMethodSignature.split("\\.");
        String methodSignature = parts[parts.length - 1];
        String packageName = String.join(".", java.util.Arrays.copyOfRange(parts, 0, parts.length - 1));

        Path projectRoot = Paths.get(projectRootPath);
        String sourceFilePath = findSourceFile(projectRoot, packageName);

        if (sourceFilePath == null) {
            LOGGER.warning("Source file not found for package: " + packageName);
            return null;
        }

        return extractMethodSource(sourceFilePath, methodSignature);
    }

    private static String findSourceFile(Path projectRoot, String packageName) throws IOException {
        String packagePath = packageName.replace('.', File.separatorChar);
        Path sourceRoot = findSourceRoot(projectRoot);

        if (sourceRoot == null) {
            LOGGER.warning("Source root not found in the project.");
            return null;
        }

        Path fullPath = sourceRoot.resolve(packagePath);

        try (Stream<Path> paths = Files.walk(fullPath)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .findFirst()
                .map(Path::toString)
                .orElse(null);
        }
    }

    private static Path findSourceRoot(Path projectRoot) throws IOException {
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            return paths
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().equals("src") ||
                             p.getFileName().toString().equals("java"))
                .findFirst()
                .orElse(null);
        }
    }

    public static String extractMethodSource(String sourceFilePath, String methodSignature) throws IOException {
        String source = new String(Files.readAllBytes(Paths.get(sourceFilePath)));
        ASTParser parser = ASTParser.newParser(AST.JLS14);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodVisitor visitor = new MethodVisitor(methodSignature);
        cu.accept(visitor);

        if (visitor.getFoundMethod() != null) {
            return getMethodSource(source, visitor.getFoundMethod());
        }

        return null;
    }

    private static String getMethodSource(String source, MethodDeclaration method) {
        Document document = new Document(source);
        ASTRewrite rewrite = ASTRewrite.create(method.getAST());
        TextEdit edits = rewrite.rewriteAST(document, null);

        try {
            edits.apply(document);
            int start = method.getStartPosition();
            int length = method.getLength();
            return document.get(start, length);
        } catch (Exception e) {
            LOGGER.severe("Error extracting method source: " + e.getMessage());
            return null;
        }
    }

    private static class MethodVisitor extends ASTVisitor {
        private final String targetSignature;
        private MethodDeclaration foundMethod;

        public MethodVisitor(String targetSignature) {
            this.targetSignature = targetSignature;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            String signature = getMethodSignature(node);
            if (signature.equals(targetSignature)) {
                foundMethod = node;
                return false;
            }
            return true;
        }

        private String getMethodSignature(MethodDeclaration node) {
            StringBuilder sb = new StringBuilder();
            sb.append(node.getName().getIdentifier()).append("(");
            for (Object param : node.parameters()) {
                VariableDeclaration vd = (VariableDeclaration) param;
                sb.append(vd.getType().toString()).append(",");
            }
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }
            sb.append(")");
            return sb.toString();
        }

        public MethodDeclaration getFoundMethod() {
            return foundMethod;
        }
    }
}