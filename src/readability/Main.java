package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final int[] ageMap = {0, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String text;
        if (args.length > 0) {
            text = fileReader(args[0]);
        } else {
            System.out.println("Enter file path: ");
            text = fileReader(scanner.nextLine());
        }
        if (text.isBlank()) return;

        int charsCount = text.replaceAll("[\\s\\h]+", "").length();
        int sentencesCount = text.split("[\\.!\\?]").length;
        String[] words = text.split("[\\s\\h]+");
        int wordsCount = words.length;

        int syllablesCount = 0;
        int polysyllableCount = 0;
        for (String word : words) {
            int[] wordProcessing = countSyllableAndPolysyllable(word);
            syllablesCount += wordProcessing[0];
            polysyllableCount += wordProcessing[1];
        }

        System.out.printf(
                "The text is:%n%s%nWords: %d%nSentences: %d%nCharacters: %d%nSyllables: %d%nPolysyllables: %d%n",
                text,
                wordsCount,
                sentencesCount,
                charsCount,
                syllablesCount,
                polysyllableCount
        );
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");

        String scoreToCalc = scanner.nextLine();
        switch (scoreToCalc.toLowerCase()) {
            case "ari":
                int ariAge = getAriScore(charsCount, wordsCount, sentencesCount);
                printAverageAge(ariAge);
                break;
            case "fk":
                int fkAge = getFleschKincaidScore(wordsCount, syllablesCount, sentencesCount);
                printAverageAge(fkAge);
                break;
            case "smog":
                int smogAge = getSmogIndex(polysyllableCount, sentencesCount);
                printAverageAge(smogAge);
                break;
            case "cl":
                int clAge = getColemanLiauIndex(charsCount, wordsCount, sentencesCount);
                printAverageAge(clAge);
                break;
            case "all":
                int ariAgeAll = getAriScore(charsCount, wordsCount, sentencesCount);
                int fkAgeAll = getFleschKincaidScore(wordsCount, syllablesCount, sentencesCount);
                int smogAgeAll = getSmogIndex(polysyllableCount, sentencesCount);
                int clAgeAll = getColemanLiauIndex(charsCount, wordsCount, sentencesCount);
                printAverageAge((double) (ariAgeAll + fkAgeAll + smogAgeAll + clAgeAll) / 4.0);
                break;
            default:
                System.out.println("Action invalid.");
        }

    }

    private static String fileReader(String file) {
        try {
            return new String(Files.readAllBytes(Path.of(file)));
        } catch (IOException exception) {
            System.out.println("Error: File Not Found! ");
            return "";
        }
    }


    private static int[] countSyllableAndPolysyllable(String word) {
        String[] vowels = word.split("[^aiueoyAIUEOY]+");
        int syllable = 0;
        for (String vowel : vowels) {
            if (!vowel.isBlank()) syllable++;
        }

        if (word.charAt(word.length() - 1) == 'e') {
            syllable--;
        }

        return new int[]{syllable == 0 ? 1 : syllable, syllable > 2 ? 1 : 0};
    }

    private static int getAriScore(int characters, int words, int sentences) {
        double score = 4.71 * characters / words + 0.5 * words / sentences - 21.43;
        System.out.printf("%nAutomated Readability Index: %s", getUnderstoodBy(score));
        return getAge(score);
    }

    private static int getFleschKincaidScore(int words, int syllables, int sentences) {
        double score = 0.39 * words / sentences + 11.8 * syllables / words - 15.59;
        System.out.printf("%nFlesch\\–Kincaid readability tests: %s", getUnderstoodBy(score));
        return getAge(score);
    }

    private static int getSmogIndex(int polysyllables, int sentences) {
        double score = 1.043 * Math.sqrt(polysyllables * 30 / (double) sentences) + 3.1291;
        System.out.printf("%nSimple Measure of Gobbledygook: %s", getUnderstoodBy(score));
        return getAge(score);
    }

    private static int getColemanLiauIndex(int characters, int words, int sentences) {
        double score = 0.0588 * characters / words * 100 - 0.296 * sentences / words * 100 - 15.8;
        System.out.printf("%nColeman–Liau index: %s", getUnderstoodBy(score));
        return getAge(score);
    }

    private static String getUnderstoodBy(double score) {
        return String.format("%4.2f (about %s year olds).", score, getAge(score));
    }

    private static int getAge(double score) {
        int validScore = score < 14 ? (int) Math.ceil(score) : 14;
        return ageMap[validScore];
    }

    private static void printAverageAge(double age) {
        System.out.printf("%n%nThis text should be understood in average by %4.2f year olds.", age);
    }
}
