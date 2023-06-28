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
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.util.Optional;

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.core.resolution.SymbolResolutionCapability;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * Hello world!
 *
 */

public class App {

  private static final String SRC_PATH = "src/main/resources/java-baseball/src/main/java";

  private static final TypeSolver reflectionSolver = new ReflectionTypeSolver(false);

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

            cu.findAll(VariableDeclarationExpr.class).forEach(node -> {

              System.out.println("********");
              System.out.println(" * Variable Declaration Expr Node::: " + node);

              printVaraiableReference(node);
            });

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
        System.out.println("isArray::::::" + node.getVariable(0).getType().resolve().isArray()); // ReferenceType{~}
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

        } else { // 어레이가 아닌경우
          // check whether is reflection type solver!!!

          boolean checkIsInternal = isJDKPackage(
              node.getVariable(0).getType().resolve().asReferenceType().getQualifiedName());

          System.out.println("check Is Internal :::: " + checkIsInternal);

          System.out.println(node.getVariable(0).getType().resolve().asReferenceType().getQualifiedName());
          System.out.println(node.getVariable(0).getType().resolve().asReferenceType().describe());
          // 레퍼런스 타입이 Type<Parameter>를 가지고 있는지 확인
          if (!node.getVariable(0).getType().resolve().asReferenceType().typeParametersValues().isEmpty()) {

            System.out.println(
                node.getVariable(0).getType().resolve().asReferenceType().typeParametersValues().get(0)
                    .asReferenceType()
                    .isJavaLangObject());
          }

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

  public static boolean isJDKPackage(String packageName) {
    try {
      reflectionSolver.solveType(packageName);
      return true;
    } catch (UnsolvedSymbolException e) {
      return false;
    }
  }

  public static boolean printVaraiableReference(VariableDeclarationExpr node) throws UnsolvedSymbolException {
    try {
      System.out.println(node.getVariables());
      Type type = node.getVariable(0).getType();

      System.out.println("type 추출:: " + type);
      if (type.isReferenceType()) {
        System.out.println("type이 reference 타입입니다!!!");
        if (type.resolve().isArray()) {
          System.out.println("type이 Array Type입니다.");
          // System.out.println(
          // "resolve 전의 배열 타입에서 가져온 컴포넌트 타입 체크:::" +
          // type.asArrayType().getComponentType().getClass().getTypeName());
          // System.out.println("resolve 후에 배열 타입에서 가져온 컴포넌트 타입 체크::::"
          // + type.resolve().asArrayType().getComponentType().getClass().getTypeName());
          String embeddedTypeString = type.resolve().asArrayType().getComponentType().describe();
          if (!isJDKPackage(embeddedTypeString)) {
            System.out.println("배열에 내장된 값이 우리가 만든 레퍼런스 타입입니다!!!!");
            System.out.println("hashcode 출력:::" + type.resolve().asArrayType().getComponentType().hashCode());
          }
        } else { // 배열이 아닌 경우!
          System.out.println(type.resolve().asReferenceType().getQualifiedName()); // 타입체크?
          System.out.println(type.resolve().asReferenceType().describe());
          if (!isJDKPackage(type.resolve().asReferenceType().getQualifiedName())) {
            System.out.println("우리가 만든 레퍼런스 타입입니다!!!");
            System.out.println("hashcode 출력:::" + type.resolve().asReferenceType().hashCode());

          }
        }

      }

      return true;
    } catch (UnsolvedSymbolException e) {
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
