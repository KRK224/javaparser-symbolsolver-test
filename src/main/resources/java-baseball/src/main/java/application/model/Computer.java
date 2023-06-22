package application.model;

import application.generator.FixedNumberGenerator;
import application.generator.RandomNumberGenerator;

import java.util.ArrayList;
import java.util.List;

public class Computer {
  private final String[] astTest = { "test1", "test2" };
  private final RandomNumberGenerator randomNumberGenerator;
  private final FixedNumberGenerator fixedNumberGenerator;
  private List<Integer> answer;

  public Computer() {
    new Thread(() -> {
      System.out.println("Welcome Heejin blog");
    }).start();
    this.answer = new ArrayList<>();
    this.randomNumberGenerator = new RandomNumberGenerator();
    this.fixedNumberGenerator = new FixedNumberGenerator();
  }

  public void createAnswerWithRandom() {
    this.answer = randomNumberGenerator.generate();
  }

  public void createAnswerWithFixed() {
    this.answer = fixedNumberGenerator.generate();
  }

  public List<Integer> getAnswer() {
    return this.answer;
  }
}