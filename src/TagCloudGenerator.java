import java.util.Comparator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * A program that asks for an input text file and output a html file listing all
 * words and the time they appeared.
 *
 * @author Simon Lei & ZhengYi Hu
 *
 */
public final class TagCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Generate the header of the output html file, including title, heading and
     * headings of the table.
     *
     * @param toFile
     *            The output stream
     * @param fileName
     *            The name of input file
     * @param n
     *            Number of words in tag cloud
     */
    private static void generateHeader(SimpleWriter toFile, String fileName,
            int n) {

        //Title and heading
        toFile.println("<html>");
        toFile.println("<head>");
        toFile.print("<title>");
        toFile.print("Top " + n + " words in " + fileName);
        toFile.println("</title>");
        toFile.print("<link href=\"");
        toFile.print(
                "http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/");
        toFile.print(
                "projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\"");
        toFile.println("type=\"text/css\">");
        toFile.println(
                "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        toFile.println("</head>");
        toFile.println("<body>");
        toFile.println("<h2>Top " + n + " words in " + fileName + "</h2>");
        toFile.println("<hr>");
        toFile.println("<div class=\"cdiv\">");
        toFile.println("<p class=\"cbox\">");

    }

    /**
     * Generates the Set of separators.
     *
     * @param str
     *            the string of separator characters
     * @return A Set that contains separator characters
     */
    private static Set<Character> generateSeparatorSet(String str) {

        Set<Character> separators = new Set1L<>();
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            char toAdd = str.charAt(i);
            if (!separators.contains(toAdd)) {
                separators.add(toAdd);
            }
        }

        return separators;
    }

    /**
     * Returns the first word or separator that appears in a string following a
     * starting position that's given.
     *
     * @param text
     *            the string given
     * @param position
     *            the starting point
     * @param separators
     *            the Set of separators
     * @return the first word found after the starting position
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {

        char c = text.charAt(position);
        StringBuilder bdr = new StringBuilder();
        int i = position;
        if (separators.contains(c)) {
            while (separators.contains(c) && i < text.length()) {
                bdr.append(c);
                i++;
                if (i < text.length()) {
                    c = text.charAt(i);
                }
            }
        } else {
            while (!separators.contains(c) && i < text.length()) {
                bdr.append(c);
                i++;
                if (i < text.length()) {
                    c = text.charAt(i);
                }
            }
        }

        return bdr.toString();
    }

    /**
     * Build a map, scan through the input text, add the word into map if it is
     * its first appearance, update the number if the word existed before.
     *
     * @param inFile
     *            The input stream
     * @param separators
     *            the Set of separator characters
     * @param wordAndCount
     *            the Map where words and corresponding counts will be added
     * @updates wordAndCount
     */
    private static void mapBuilder(SimpleReader inFile,
            Set<Character> separators, Map<String, Integer> wordAndCount) {

        while (!inFile.atEOS()) {
            String line = inFile.nextLine();
            line = line.toLowerCase();
            int lineLen = line.length();
            int pos = 0;
            while (pos < lineLen) {
                String word = nextWordOrSeparator(line, pos, separators);
                if (!wordAndCount.hasKey(word)
                        && !separators.contains(word.charAt(0))) {
                    wordAndCount.add(word, 1);
                } else if (wordAndCount.hasKey(word)) {
                    int valueToInc = wordAndCount.value(word);
                    valueToInc++;
                    wordAndCount.replaceValue(word, valueToInc);
                }
                pos = pos + word.length();
            }
        }
    }

    /**
     * Generate the html code of the table that contains word and count.
     *
     * @param wordCountMap
     *            The map with word as keys and count as values
     * @param toFile
     *            The output stream
     * @param sm
     *            The SortingMachine with alphabetically sorted pairs
     * @param numWords
     *            The number of words in the tag cloud
     */
    private static void generateCloud(
            Map<Pair<String, Integer>, Integer> wordCountMap,
            SimpleWriter toFile, SortingMachine<Pair<String, Integer>> sm,
            int numWords) {

        sm.changeToExtractionMode();
        while (sm.size() > 0) {
            Map.Pair<String, Integer> p = sm.removeFirst();
            toFile.print("<span style=\"cursor:default\" class=\"");
            toFile.print("f" + wordCountMap.value(p));
            toFile.print("\" title=\"count: " + p.value() + "\">");
            toFile.println(p.key() + "</span>");
        }

        //End the table and the html file
        toFile.println("</p>");
        toFile.println("</div>");
        toFile.println("</body>");
        toFile.println("</html>");

    }

    /**
     * Compare pairs based on key in alphabetical order.
     */
    public static final class StringLT
            implements Comparator<Pair<String, Integer>> {
        @Override
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            return o1.key().compareToIgnoreCase(o2.key());
        }
    }

    /**
     * Compare pairs according to value.
     */
    public static final class IntegerLT
            implements Comparator<Pair<String, Integer>> {
        @Override
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            return o2.value().compareTo(o1.value());
        }
    }

    /**
     * Sort the Queue containing all words so it is in alphabetical order.
     *
     * @param m
     *            The Map to be sorted
     * @param num
     *            Number of words in tag cloud
     * @return the SortingMachine containing words in alphabetical order
     */
    public static SortingMachine<Map.Pair<String, Integer>> alphabeticalSortMostFrequent(
            Map<String, Integer> m, int num) {
        Comparator<Map.Pair<String, Integer>> intOrder = new IntegerLT();
        SortingMachine<Map.Pair<String, Integer>> intSm = new SortingMachine1L<Map.Pair<String, Integer>>(
                intOrder);
        while (m.size() > 0) {
            Pair<String, Integer> p = m.removeAny();
            intSm.add(p);
        }

        intSm.changeToExtractionMode();
        Comparator<Map.Pair<String, Integer>> strOrder = new StringLT();
        SortingMachine<Map.Pair<String, Integer>> strSm = new SortingMachine1L<Map.Pair<String, Integer>>(
                strOrder);
        for (int i = 0; i < num && i < intSm.size(); i++) {
            strSm.add(intSm.removeFirst());
        }

        return strSm;
    }

    /**
     * Generate a sorted map with a pair containing word and count as key, and
     * font size as value.
     *
     * @param wordCount
     *            The map containing words and how many times they occurred
     * @return the map that specifies the font sizes of each word.
     */
    public static Map<Pair<String, Integer>, Integer> getFont(
            SortingMachine<Map.Pair<String, Integer>> wordCount) {
        Map<Pair<String, Integer>, Integer> fontMap = new Map1L<Pair<String, Integer>, Integer>();
        Comparator<Map.Pair<String, Integer>> intC = new IntegerLT();
        SortingMachine<Map.Pair<String, Integer>> intPSm = new SortingMachine1L<Map.Pair<String, Integer>>(
                intC);

        final int fMax = 48;
        final int fMin = 11;
        int tMax = 0;
        int tMin = Integer.MAX_VALUE;

        //Get max count and min count.
        for (Map.Pair<String, Integer> p : wordCount) {
            if (p.value() > tMax) {
                tMax = p.value();
            } else if (p.value() < tMin) {
                tMin = p.value();
            }
            intPSm.add(p);
        }

        //Make the map
        intPSm.changeToExtractionMode();
        while (intPSm.size() > 0) {
            Map.Pair<String, Integer> p = intPSm.removeFirst();
            int count = p.value();
            int fontSize = 0;
            if (count > tMin) {
                int nom = (fMax - fMin) * (count - tMin);
                int denom = (tMax - tMin);
                fontSize = nom / denom + fMin;
            }
            fontMap.add(p, fontSize);
        }

        return fontMap;
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        /*
         * Ask names of input and output files
         */
        out.println("What is the name of input file?");
        String inFileName = in.nextLine();
        out.println("What name do you want for output file?");
        String outFileName = in.nextLine();
        out.println(
                "What is the number of words to be included in the generated tag cloud?");
        int numOfWords = in.nextInteger();

        /*
         * Open new input and output streams
         */
        SimpleReader inFile = new SimpleReader1L(inFileName);
        SimpleWriter toFile = new SimpleWriter1L(outFileName);

        /*
         * Generate a set containing all separators
         */
        String sep = " ,.?!-:;[]{}/'\"()<>@#$%^&*_\t";
        Set<Character> separators = generateSeparatorSet(sep);

        /*
         * Read the file, get words and counts into a Map, get words into a
         * Queue then a SortingMachine
         */

        Map<String, Integer> wordAndCount = new Map1L<String, Integer>();
        mapBuilder(inFile, separators, wordAndCount);
        SortingMachine<Map.Pair<String, Integer>> sm = alphabeticalSortMostFrequent(
                wordAndCount, numOfWords);
        Map<Pair<String, Integer>, Integer> fontMap = getFont(sm);

        /*
         * Output html code
         */
        generateHeader(toFile, inFileName, numOfWords);
        generateCloud(fontMap, toFile, sm, numOfWords);

        /*
         * Close input and output streams
         */
        in.close();
        out.close();
        inFile.close();
        toFile.close();
    }

}
