package application.baseball;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Judgement {
  private int getTotalCount(List<Integer> computer, List<Integer> inputNumber) {

    List<Integer> test = inputNumber.stream()
        .filter(num -> computer.contains(num)).collect(Collectors.toList());

    System.out.println(test);

    int result = (int) inputNumber.stream()
        .filter(computer::contains)
        .count();

    return result;
  }

  public int getStrikeCount(List<Integer> computer, List<Integer> inputNumber) {
    int count = 0;
    for (int i = 0; i < inputNumber.size(); i++) {
      if (Objects.equals(computer.get(i), inputNumber.get(i))) {
        count += 1;
      }
    }
    return count;
  }

  public int getBallCount(List<Integer> computer, List<Integer> inputNumber) {
    return getTotalCount(computer, inputNumber) - getStrikeCount(computer, inputNumber);
  }
}