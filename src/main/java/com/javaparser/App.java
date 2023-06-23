package com.javaparser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

/**
 * Hello world!
 *
 */

public class App {

  private static final String SRC_PATH = "src/main/resources/java-baseball/src/main/java";

  private static CompilationUnit tempGameStatusCu = null;
  private static Node tempGameStatusNodeFromToAst = null;

  private static DataKey<List<String>> childList = new DataKey<List<String>>() {

  };

  private static List<CompilationUnit> cuList = new ArrayList<CompilationUnit>();

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
            cuList.add(cu);

            System.out.println("cu:::" + cu.getStorage().get().getFileName());
            String fileName = cu.getStorage().get().getFileName();

            if (fileName.equals("GameStatus.java")) {
              tempGameStatusCu = cu;
            }

            if (!fileName.equals("Game.java")) {
              continue;
            }

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

      System.out.println("\n check MethodDeclaration and its MethodCallExpr -------------------------\n");

      for (ParseResult<CompilationUnit> parseResult : parseResults) {
        try {
          Optional<CompilationUnit> optionalCompilationUnit = parseResult.getResult();

          if (optionalCompilationUnit.isPresent()) {
            CompilationUnit cu = optionalCompilationUnit.get();
            System.out.println("cu:::" + cu.getStorage().get().getFileName());
            String fileName = cu.getStorage().get().getFileName();

            if (!fileName.equals("GameStatus.java")) {
              continue;
            }

            System.out.println(cu);
            System.out.println(tempGameStatusCu);
            System.out.println(cu == tempGameStatusCu);
            System.out.println(tempGameStatusNodeFromToAst);
            CompilationUnit cuFromNode = tempGameStatusNodeFromToAst.findCompilationUnit().get();
            System.out.println(cuFromNode);

            cu.findAll(MethodDeclaration.class).forEach(mdNode -> {
              System.out.println(mdNode.getDeclarationAsString());
              if (mdNode.containsData(childList)) {
                System.out.println("=============");
                System.out.println("MethodDeclaration.resolve().getQualifiedSignature::: " +
                    mdNode.resolve().getQualifiedSignature());
                System.out.println("its MethodCallExpr List:::: ");
                mdNode.getData(childList).stream().forEach(System.out::println);
                System.out.println("=============\n");
              }

            });

          }

        } catch (Exception e) {
          throw e;
        }
      }

    }
  }

  public static boolean printMethodReference(MethodCallExpr node)
      throws UnsolvedSymbolException {
    try {

      System.out.println("packageName::: " + node.resolve().getPackageName());
      System.out.println("className:::" + node.resolve().getClassName());
      System.out.println("className.getQualifiedSignature()::: " + node.resolve().getQualifiedSignature());

      // resolve to AST
      Optional<MethodDeclaration> md = node.resolve().toAst(MethodDeclaration.class);

      if (md.isPresent()) {
        MethodDeclaration refDeclaration = md.get();
        System.out.println(refDeclaration.getSignature());
        System.out.println(refDeclaration.getMetaModel().getTypeName());

        if (refDeclaration.getSignature().toString().equals("getCode()")) {
          Optional<Node> parentNodeOptional = null;
          Node parentNode = refDeclaration;

          // search the cu of resolved MethodDeclaration.
          do {
            parentNodeOptional = parentNode.getParentNode();
            parentNode = parentNodeOptional.get();

          } while (parentNode.getParentNode().isPresent());

          System.out.println(parentNode.getMetaModel().getTypeName());
          tempGameStatusNodeFromToAst = parentNode;
          System.out.println(parentNode);

        }
        if (!md.get().containsData(childList)) {
          List<String> meNameList = new ArrayList<String>();
          System.out.println("DataKey is absent!");
          meNameList.add(node.toString());
          md.get().setData(childList, meNameList);
          System.out.println("check DataKey:::: " + md.get().getData(childList));
        } else {
          System.out.println("DataKey is present:::" + md.get().getDataKeys());
          List<String> tempMeNameList = md.get().getData(childList);
          tempMeNameList.add(node.toString());
          md.get().setData(childList, tempMeNameList);
          System.out.println("check DataKey:::: " + md.get().getData(childList));
        }

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
