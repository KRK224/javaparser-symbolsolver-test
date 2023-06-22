package com.javaparser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
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

  private static final String SRC_PATH = "src/main/resources/java-baseball/src/main/java";

  private static DataKey<List<String>> childList = new DataKey<List<String>>() {

  };

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

            cu.findAll(MethodCallExpr.class).forEach(node -> {

              System.out.println("********");
              System.out.println(" * Method Call::: " + node);

              printMethodReference(node);

              System.out.println("********\n");
            });

          }
        } catch (UnsolvedSymbolException e) {
          throw e;
        }

      }

      for (ParseResult<CompilationUnit> parseResult : parseResults) {
        try {
          Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();

          if (optionalCompilationUnit.isPresent()) {
            CompilationUnit cu = optionalCompilationUnit.get();

            System.out.println("cu:::" + cu.getStorage().get().getFileName());
            cu.findAll(MethodDeclaration.class).forEach(mdNode -> {
              System.out.println("MethodDeclaration 자신 출력::: " + mdNode.getSignature());
              if (!mdNode.getData(childList).isEmpty()) {
                System.out.println("\n=============");
                System.out.println("호출 리스트 출력:::: ");
                mdNode.getData(childList).stream().forEach(System.out::println);
                System.out.println("=============\n");
              }

            });
          }
        } catch (Exception e) {

        }
      }

    }
  }

  public static boolean printMethodReference(MethodCallExpr node)
      throws UnsolvedSymbolException {
    try {
      // node.resolve();
      System.out.println("packageName::: " + node.resolve().getPackageName());
      System.out.println("className:::" + node.resolve().getClassName());
      System.out.println("className.getQualifiedSignature()::: " + node.resolve().getQualifiedSignature());
      System.out.println("getQualifiedName()::: " + node.resolve().getQualifiedName());
      System.out.println("getSignature()::: " + node.resolve().getSignature());
      List<String> meNameList = new ArrayList<String>();
      System.out.println("node.toString " + node.toString());
      meNameList.add(node.toString());
      Optional<MethodDeclaration> md = node.resolve().toAst(MethodDeclaration.class);
      if (md.isPresent()) {

        System.out.println("setData 중...");
        md.get().setData(childList, meNameList);
        System.out.println("setData 완료");
        System.out.println("getData 테스트");
        System.out.println(md.get().getData(childList));
        System.out.println("직접 접근해서 가져오기 성공");
      }
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
