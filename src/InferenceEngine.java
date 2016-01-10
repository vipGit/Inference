import java.util.*;

/**
 * Inference Engine
 * Created by Vipul Somani (vasomani@usc.edu) on 11/9/2015.
 */
public class InferenceEngine {

    private static HashMap<String, ArrayList<AtomicTerm>> processed = new HashMap<>();

    private static void addToProcessed(AtomicTerm term) {
        if (processed.containsKey(term.getName())) {
            processed.get(term.getName()).add(term);
        } else {
            ArrayList<AtomicTerm> list = new ArrayList<>();
            list.add(term);
            processed.put(term.getName(), list);
        }
    }

    private static boolean isProcessed(AtomicTerm term) {
        if (processed.containsKey(term.getName())) {
            for (AtomicTerm atomicTerm : processed.get(term.getName())) {
                if (atomicTerm.equals(term)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isQueryEntailedByKB(AtomicTerm query) {
        System.out.println("============================================");
        return backwardChainingOR(query) != null;
    }

    private static ArrayList<AtomicTerm> backwardChainingOR(AtomicTerm goal) {
        HashMap<String ,ArrayList<AtomicTerm>> localProcessedQueue;
        //Check if given straight as fact
        System.out.print(goal + "->");
        ArrayList<AtomicTerm> unified = new ArrayList<>();
        if (KnowledgeBase.getFacts().containsKey(goal.getName())) {
            ArrayList<AtomicTerm> facts = KnowledgeBase.getFacts().get(goal.getName());
            for (AtomicTerm left : facts) {
                if (canUnifyFacts(left, goal)) {
                    unified.add(left);
                }
            }
        }
        //Check if visited
        if (isProcessed(goal)) {
            return unified.size() != 0 ? unified : null;
        }
        localProcessedQueue = new HashMap<>(processed);
        //Add to visited
        addToProcessed(goal);
        //Check if it can be implied in a rule
        if (!KnowledgeBase.getClauses().containsKey(goal.getName())) {
            return unified.size() != 0 ? unified : null;
        }
        //For all possible rules generate the list of unified facts
        ArrayList<ImplicationMapping> sentences = KnowledgeBase.getClauses().get(goal.getName());
        for (ImplicationMapping rule : sentences) {
            AtomicTerm consequent = new AtomicTerm(rule.consequent);
            // Filter RHS
            if (!canUnifyConsequent(consequent, goal)) {
                continue;
            }
            //Clone
            ImplicationMapping mapping = new ImplicationMapping();
            mapping.consequent = consequent;
            for (AtomicTerm term : rule.antecedents) {
                mapping.addAntecedent(new AtomicTerm(term));
            }
            //Substitute
            if (!substituteRightToLeft(mapping, goal)) {
                continue;
            }
            ArrayList<AtomicTerm> andResult = backwardChainingAND(mapping);
            if (andResult != null) {
                unified.addAll(andResult);
            }
        }
        //return all answers generated from processing facts and clauses
        processed = localProcessedQueue;
        return unified.size() != 0 ? unified : null;
    }

    private static ArrayList<AtomicTerm> backwardChainingAND(ImplicationMapping rule) {
        ArrayList<QueueTerm> queue = new ArrayList<>();
        queue.add(new QueueTerm(rule, -1));
        ArrayList<AtomicTerm> andResult = new ArrayList<>();
        while (!queue.isEmpty()) {
            QueueTerm queueTerm = queue.remove(0);
            ImplicationMapping ruleToCheck = queueTerm.rule;
            int index = queueTerm.index;
            if (index + 1 < ruleToCheck.antecedents.size()) {
                ArrayList<AtomicTerm> conjuctResult = backwardChainingOR(ruleToCheck.antecedents.get(index + 1));
                if (conjuctResult == null) {
                    continue;
                }
                for (AtomicTerm fact : conjuctResult) {
                    ImplicationMapping mapping = new ImplicationMapping();
                    mapping.consequent = new AtomicTerm(ruleToCheck.consequent);
                    for (AtomicTerm term : ruleToCheck.antecedents) {
                        mapping.addAntecedent(new AtomicTerm(term));
                    }
                    mapping.cloneMapping(ruleToCheck.variableMappings);
                    if (!substituteLeftToRight(mapping, fact, index + 1)) {
                        continue;
                    }
                    queue.add(new QueueTerm(mapping, index + 1));
                }
            } else {
                // Add consequent to andResult
                andResult.add(ruleToCheck.consequent);
            }
        }
        return andResult.size() != 0 ? andResult : null;
    }

    private static boolean canUnifyConsequent(AtomicTerm consequent, AtomicTerm goal) {
        for (int i = 0; i < consequent.getNumOfArguments(); i++) {
            if (AtomicTerm.isArgumentConstant(consequent.getArgumentArray()[i]) && AtomicTerm.isArgumentConstant(goal.getArgumentArray()[i])) {
                if (!consequent.getArgumentArray()[i].equals(goal.getArgumentArray()[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean substituteRightToLeft(ImplicationMapping rule, AtomicTerm goal) {
        if(!mapVariablesRightToLeft(rule, goal)){
            return false;
        }
        rule.createSubstitutions();
        String[] varArray = rule.consequent.getArgumentArray();
        for (int i = 0; i < varArray.length; i++) {
            if (!AtomicTerm.isArgumentConstant(varArray[i]) && AtomicTerm.isArgumentConstant(goal.getArgumentArray()[i])) {
                String value = rule.variableMappings.get(varArray[i]);
                String newValue = goal.getArgumentArray()[i];
                if (value != null && !newValue.equals(value)) {
                    return false;
                }
                rule.variableMappings.put(varArray[i], newValue);
                varArray[i] = rule.variableMappings.get(varArray[i]);
            }
        }
        for (AtomicTerm predicate : rule.antecedents) {
            String[] argumentArray = predicate.getArgumentArray();
            for (int i = 0; i < argumentArray.length; i++) {
                if (!AtomicTerm.isArgumentConstant(argumentArray[i])) {
                    if (rule.variableMappings.get(argumentArray[i]) != null) {
                        argumentArray[i] = rule.variableMappings.get(argumentArray[i]);
                    }
                }
            }
        }
        return true;
    }

    private static boolean mapVariablesRightToLeft(ImplicationMapping rule, AtomicTerm goal) {
        HashMap<String, String> hashMap = new HashMap<>();
        String[] leftArray = rule.consequent.getArgumentArray();
        String[] rightArray = goal.getArgumentArray();
        for (int i = 0; i < rightArray.length; i++) {
            if (!AtomicTerm.isArgumentConstant(rightArray[i])) {
                if (!hashMap.containsKey(rightArray[i])) {
                    hashMap.put(rightArray[i], leftArray[i]);
                } else {
                    String replaceThis = leftArray[i];
                    String replaceWith = hashMap.get(rightArray[i]);
                    if (AtomicTerm.isArgumentConstant(replaceThis)) {
                        hashMap.put(rightArray[i], replaceThis);
                        continue;
                    }
                    for (int k = 0; k < leftArray.length; k++) {
                        if (leftArray[k].equals(replaceThis)) {
                            leftArray[k] = replaceWith;
                        }
                    }
                    for (AtomicTerm predicate : rule.antecedents) {
                        String[] predicateArray = predicate.getArgumentArray();
                        for (int j = 0; j < predicateArray.length; j++) {
                            if (predicateArray[j].equals(replaceThis)) {
                                predicateArray[j] = replaceWith;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean substituteLeftToRight(ImplicationMapping rule, AtomicTerm fact, int index) {
        if(!mapVariablesLeftToRight(rule, fact, index)){
            return false;
        }
        String[] varArray = rule.antecedents.get(index).getArgumentArray();
        for (int i = 0; i < varArray.length; i++) {
            if (!AtomicTerm.isArgumentConstant(varArray[i]) && AtomicTerm.isArgumentConstant(fact.getArgumentArray()[i])) {
                String value = rule.variableMappings.get(varArray[i]);
                String newValue = fact.getArgumentArray()[i];
                if (value != null && !newValue.equals(value)) {
                    return false;
                }
                rule.variableMappings.put(varArray[i], newValue);
            }
        }
        varArray = rule.consequent.getArgumentArray();
        for (int i = 0; i < varArray.length; i++) {
            if (!AtomicTerm.isArgumentConstant(varArray[i])) {
                if (rule.variableMappings.get(varArray[i]) != null) {
                    varArray[i] = rule.variableMappings.get(varArray[i]);
                }
            }
        }
        for (int j = index; j < rule.antecedents.size(); j++) {
            String[] argumentArray = rule.antecedents.get(j).getArgumentArray();
            for (int i = 0; i < argumentArray.length; i++) {
                if (!AtomicTerm.isArgumentConstant(argumentArray[i])) {
                    if (rule.variableMappings.get(argumentArray[i]) != null) {
                        argumentArray[i] = rule.variableMappings.get(argumentArray[i]);
                    }
                }
            }
        }
        return true;
    }

    private static boolean mapVariablesLeftToRight(ImplicationMapping rule, AtomicTerm fact, int index) {
        HashMap<String, String> hashMap = new HashMap<>();
        String[] leftArray = rule.antecedents.get(index).getArgumentArray();
        String[] rightArray = fact.getArgumentArray();
        for (int i = 0; i < rightArray.length; i++) {
            if (!AtomicTerm.isArgumentConstant(rightArray[i])) {
                if (!hashMap.containsKey(rightArray[i])) {
                    hashMap.put(rightArray[i], leftArray[i]);
                } else {
                    String replaceThis = leftArray[i];
                    String replaceWith = hashMap.get(rightArray[i]);
                    if (AtomicTerm.isArgumentConstant(replaceThis)) {
                        hashMap.put(rightArray[i], replaceThis);
                        continue;
                    }
                    for (int k = 0; k < leftArray.length; k++) {
                        if (leftArray[k].equals(replaceThis)) {
                            leftArray[k] = replaceWith;
                        }
                    }
                    for (int m = 0; m < rule.consequent.getArgumentArray().length; m++) {
                        if (rule.consequent.getArgumentArray()[m].equals(replaceThis)) {
                            rule.consequent.getArgumentArray()[m] = replaceWith;
                        }
                    }
                    for (int n = index+1; n < rule.antecedents.size(); n++) {
                        String[] predicateArray = rule.antecedents.get(n).getArgumentArray();
                        for (int j = 0; j < predicateArray.length; j++) {
                            if (predicateArray[j].equals(replaceThis)) {
                                predicateArray[j] = replaceWith;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean canUnifyFacts(AtomicTerm left, AtomicTerm right) {
        if (left == null || right == null) {
            //Error cases
            return false;
        }
//        if (!left.isAllConstants() && !right.isAllConstants()) {
//            //None of them are facts and no substitutions provided then return false
//            return false;
//        }
        // Only one of them is fact; It should be left. Try to unify.
        boolean match = false;
        //right has variables
        String[] args = right.getArgumentArray();
        for (int i = 0; i < args.length; i++) {
            match = false;
            if (AtomicTerm.isArgumentConstant(args[i])) {
                //Partial Constant
                if (args[i].equals(left.getArgumentArray()[i])) {
                    match = true;
                }
            } else {
                // No specific substitution required.
                match = true;
            }
            if (!match) {
                break;
            }
        }
        return match;
    }

    public static void init() {
        processed.clear();
    }

    static class QueueTerm {
        public ImplicationMapping rule;
        public int index;

        public QueueTerm(ImplicationMapping rule, int index) {
            this.rule = rule;
            this.index = index;
        }
    }

}
