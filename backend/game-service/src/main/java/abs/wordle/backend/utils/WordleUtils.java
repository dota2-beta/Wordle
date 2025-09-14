package abs.wordle.backend.utils;

import abs.wordle.backend.enums.LetterStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordleUtils {
    private WordleUtils() {}

    public static List<LetterStatus> determineLetterStatuses(String word, String guess) {
        List<LetterStatus> letterStatuses = new ArrayList<>();
        HashMap<Character, Integer> letterCounts = new HashMap<>();

        for(char c : word.toCharArray()) {
            letterCounts.put(c, letterCounts.getOrDefault(c, 0) + 1);
        }

        for(int i = 0; i < word.length(); i++) {
            if(word.charAt(i) == guess.charAt(i)) {
                letterStatuses.add(LetterStatus.CORRECT);
                letterCounts.put(word.charAt(i), letterCounts.get(word.charAt(i)) - 1);
            }
            else
                letterStatuses.add(null);
        }

        for(int i = 0; i < word.length(); i++) {
            if (letterStatuses.get(i) == null) {
                int guessChar = word.indexOf(guess.charAt(i));
                if(guessChar == -1)
                    letterStatuses.set(i, LetterStatus.INCORRECT);
                else if (letterCounts.get(guess.charAt(i)) > 0) {
                    letterStatuses.set(i, LetterStatus.MISPLACED);
                    letterCounts.put(guess.charAt(i), letterCounts.get(guess.charAt(i)) - 1);
                }
                else
                    letterStatuses.set(i, LetterStatus.INCORRECT);
            }
        }

        System.out.println(letterStatuses);
        return letterStatuses;
    }
}
