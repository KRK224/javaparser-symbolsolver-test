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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.util.Optional;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.core.resolution.SymbolResolutionCapability;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

/**
 * Hello world!
 *
 */

public class App {

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

            cu.findAll(FieldDeclaration.class).forEach(node -> {

              System.out.println("********");
              System.out.println(" * FieldDeclaration::: " + node);

              System.out.println("all List::: " + node.getVariables());

              printReferenceType(node);

              System.out.println("********\n");
            });

          }
        } catch (UnsolvedSymbolException e) {
          throw e;
        }

      }

    }
  }

  public static boolean printReferenceType(FieldDeclaration node) {
    try {

      System.out.println("variableDeclaration 자체에서 resolve 결과::: 내 AST를 의미");
      System.out.println(node.getVariable(0));
      System.out.println(node.getVariable(0).resolve());
      System.out.println(node.getVariable(0).resolve().toAst());

      if (!node.getVariable(0).getType().isPrimitiveType()) {
        System.out.println("현재 타입이 Primitive가 아닌 경우! --------------------------------------------------------------");
        System.out.println(
            "resolve하기 전에 ReferenceType인지 체크 :::: isReferenceType? = "
                + node.getVariable(0).getType().isReferenceType());
        System.out.println(node.getVariable(0).getType());
        System.out.println(node.getVariable(0).getType().resolve()); // ReferenceType{~}
        System.out.println(node.getVariable(0).getType().resolve().isArray()); // ReferenceType{~}
        if (node.getVariable(0).getType().resolve().isArray()) { // Array type인 경우
          System.out.println("Array 입니다!!!!!");
          // 배열인 경우, asArrayType으로 받아와서 ComponentType 가져오고 그 타입이 ReferenctType이면 받아온다!
          System.out.println(
              node.getVariable(0).getType().resolve().asArrayType().describe());
          System.out.println(
              node.getVariable(0).getType().resolve().arrayLevel());
          if (node.getVariable(0).getType().asArrayType().getComponentType().isReferenceType()) {
            System.out.println(
                node.getVariable(0).getType().resolve().asArrayType().getComponentType().asReferenceType()
                    .getQualifiedName());
          }

        } else {
          System.out.println(node.getVariable(0).getType().resolve().asReferenceType().getQualifiedName());
          System.out.println(node.getVariable(0).getType().resolve().hashCode());
        }

        System.out
            .println("-----------------------------------------------------------------------------------------------");
      }

      return true;

    } catch (Exception e) {
      System.out.println("=================");
      System.out.printf("!!!!!!!! Got an Error to find reference for \'%s\' \n", node.getVariables());
      System.out.println(e.getMessage());
      System.out.println("=================");
      return false;
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
