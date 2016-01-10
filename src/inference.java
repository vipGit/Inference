import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main Class.Input parsing and Output file writing
 */
public class inference {
    private static final String OUTPUT_FILENAME = "output.txt";

    private static PrintWriter mOutputWriter;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        writeToFile(null);
        BufferedReader inputReader;
        try {
            inputReader = new BufferedReader(new FileReader(args[1]));
        } catch (FileNotFoundException e) {
            System.out.println("Cannot read input file");
            return;
        }
        try {
            int numberOfQueries = Integer.parseInt(inputReader.readLine().trim());
            ArrayList<AtomicTerm> queries = new ArrayList<>(numberOfQueries);
            for (int i = 1; i <= numberOfQueries; i++ ) {
                String query = inputReader.readLine().trim();
                queries.add(createTerm(query));
            }
            int numberOfKBSentences = Integer.parseInt(inputReader.readLine().trim());
            for (int i = 1; i <= numberOfKBSentences; i++ ) {
                String sentence = inputReader.readLine().trim();
                String[] terms = sentence.split("=>");
                if(terms.length == 1) {
                    //Fact
                    AtomicTerm fact = createTerm(terms[0]);
                    KnowledgeBase.addFact(fact.getName(), fact);
                }
                if(terms.length == 2){
                    // Sentence of form a1^..ai^...an => q
                    AtomicTerm consequent = createTerm(terms[1].trim());
                    String[] conjuncts = terms[0].trim().split("\\^");
                    ImplicationMapping mapping = new ImplicationMapping();
                    mapping.consequent = consequent;
                    for(String c: conjuncts) {
                        mapping.addAntecedent(createTerm(c.trim()));
                    }
                    KnowledgeBase.addClause(consequent, mapping);
                }
            }
            for(AtomicTerm query : queries){
                InferenceEngine.init();
                System.out.println();
                boolean result;
                try {
//                    result = true;
                    result = InferenceEngine.isQueryEntailedByKB(query);
                }catch (Exception e){
                    System.out.println("Exception for " + query);
                    result = false;
                }
                System.out.print(result);
                writeToFile(String.valueOf(result).toUpperCase());
                if(result){
                    KnowledgeBase.addFact(query.getName(), query);
                }
            }
        } catch (IOException e) {
            System.out.println("IO Exception while reading/writing");
        } finally {
            try {
                inputReader.close();
            } catch (IOException e) {
                System.out.println("IO Exception while closing buffer reader");
            }
        }
        System.out.println();
        KnowledgeBase.printKB();
        System.out.println((System.currentTimeMillis() - start) + " milliseconds");
    }

    private static AtomicTerm createTerm(String termString){
        int indexOpen = termString.indexOf('(');
        int indexClose = termString.indexOf(')');
        String arguments = termString.substring(indexOpen + 1, indexClose);
        String name = termString.substring(0, indexOpen);
        return new AtomicTerm(name, arguments);
    }

    private static AtomicTerm isEqual(AtomicTerm old, AtomicTerm current){
        if(old.equals(current) && Arrays.equals(old.getArgumentArray(), current.getArgumentArray())){
            return old;
        }
        return current;
    }

    private static void writeToFile(String output){
        try {
            if(output == null) {
                mOutputWriter = new PrintWriter(new FileWriter(OUTPUT_FILENAME, false));
            }else{
                mOutputWriter = new PrintWriter(new FileWriter(OUTPUT_FILENAME, true));
            }
        } catch (IOException e) {
            System.out.print("Cannot create output file");
        }
        if(output != null) {
            mOutputWriter.println(output);
        }
        mOutputWriter.close();
    }
}
