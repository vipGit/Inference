import java.util.*;

/**
 * Knowledge Base containing facts and clauses
 * Created by Vipul Somani (vasomani@usc.edu) on 11/9/2015.
 */
public class KnowledgeBase {
    private static HashMap<String, ArrayList<AtomicTerm>> facts = new HashMap<>();
    private static HashMap<String, ArrayList<ImplicationMapping>> clauses = new HashMap<>();

    public static HashMap<String, ArrayList<AtomicTerm>> getFacts() {
        return facts;
    }

    public static HashMap<String, ArrayList<ImplicationMapping>> getClauses() {
        return clauses;
    }

    public static void addFact(String name, AtomicTerm fact) {
        if (facts.containsKey(name)) {
            if (!facts.get(name).contains(fact)) {
                facts.get(name).add(fact);
            }
        } else {
            ArrayList<AtomicTerm> terms = new ArrayList<>();
            terms.add(fact);
            facts.put(name, terms);
        }
    }

    public static void addClause(AtomicTerm consequent, ImplicationMapping mapping) {
        if (clauses.containsKey(consequent.getName())) {
            ArrayList<ImplicationMapping> sentences = clauses.get(consequent.getName());
            boolean found = false;
            for(ImplicationMapping sentence: sentences){
                if(sentence.equals(mapping)){
                    found = true;
                    break;
                }
            }
            if(!found) {
                sentences.add(mapping);
            }
        } else {
            ArrayList<ImplicationMapping> sentences = new ArrayList<>();
            sentences.add(mapping);
            clauses.put(consequent.getName(), sentences);
        }
    }

    public static void printKB() {
        System.out.println(facts);
        System.out.println(clauses);
    }

}
