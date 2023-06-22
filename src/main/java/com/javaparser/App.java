package com.javaparser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

/**
 * Hello world!
 *
 */

public class App {

  // singleton
  public static class Test {
    private int i = 0;

    Test(int i) {
      this.i = i;
    }

    public int getI() {
      return this.i;
    }

  }

  private static final String SRC_PATH = "src/main/resources/java-baseball/src/main/java";

  public static void main(String[] args) throws Exception {

    JavaParserTypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(SRC_PATH));

    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
    ParserConfiguration myConfiguration = new ParserConfiguration();
    myConfiguration.setSymbolResolver(symbolSolver);
    // SymbolSolverCollectionStrategy symbolSolverCollectionStrategy = new
    // SymbolSolverCollectionStrategy(myConfiguration);
    SymbolSolverCollectionStrategy symbolSolverCollectionStrategy = new SymbolSolverCollectionStrategy();

    Path root = Paths.get(SRC_PATH);

    ProjectRoot projectRoot = symbolSolverCollectionStrategy.collect(root);

    List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();

    for (SourceRoot sr : sourceRootList) {
      System.out.println("sourceRoot iterator:" + sr);

      List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();
      System.out.println("compilationUnit List size: " + parseResults.size());

      for (ParseResult<CompilationUnit> parseResult : parseResults) {
        try {
          Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();

          if (optionalCompilationUnit.isPresent()) {
            CompilationUnit cu = optionalCompilationUnit.get();

            System.out.println("cu:::" + cu.getStorage().get().getFileName());
            // AtomicInteger ordinal = new AtomicInteger(0);

            cu.findAll(MethodCallExpr.class).forEach(node -> {

              System.out.println("********");
              System.out.println(" * Method Call::: " + node);
              // Test test1 = new Test(ordinal.getAndIncrement());

              // DataKey<Test> myTest = new DataKey<Test>() {

              // };

              // System.out.println("=============");
              // node.setData(myTest, test1);
              // System.out.println(node.getData(myTest).getI());
              // System.out.println("=============");

              // reflection type MethodCallExpr throwing UnsolvedSymbolException
              // System.out.println(node.resolve().getQualifiedSignature());

              printMethodReference(node);

              System.out.println("********\n");
            });

          }
        } catch (UnsolvedSymbolException e) {
          throw e;
        }

      }

    }
  }

  public static boolean printMethodReference(MethodCallExpr node) throws UnsolvedSymbolException {
    try {
      // node.resolve();
      System.out.println("packageName::: " + node.resolve().getPackageName());
      System.out.println("className:::" + node.resolve().getClassName());
      System.out.println("className.methodDeclaration::: " +
          node.resolve().getQualifiedSignature());
      return true;

    } catch (UnsolvedSymbolException e) {
      System.out.println("=================");
      System.out.printf("!!!!!!!! Got an Error to find reference for \'%s\' \n", node.getName());
      System.out.println(e.getMessage());
      System.out.println("=================");
      return false;
    }

  }
}
