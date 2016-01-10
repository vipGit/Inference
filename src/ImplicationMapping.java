import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Implication mapping
 * Created by Vipul Somani (vasomani@usc.edu) on 11/17/2015.
 */
public class ImplicationMapping {
    public AtomicTerm consequent;
    public ArrayList<AtomicTerm> antecedents;
    public HashMap<String, String> variableMappings;

    public void  addAntecedent(AtomicTerm antecedent){
        if(antecedents == null){
            antecedents = new ArrayList<>();
        }
        this.antecedents.add(antecedent);
    }

    public void createSubstitutions(){
        variableMappings = new HashMap<>();
        for(String var : consequent.getArgumentArray()){
            if(!AtomicTerm.isArgumentConstant(var)){
                variableMappings.put(var, null);
            }
        }
        for(AtomicTerm antecedent : antecedents){
            for(String var : antecedent.getArgumentArray()){
                if(!AtomicTerm.isArgumentConstant(var)){
                    if(!variableMappings.containsKey(var)) {
                        variableMappings.put(var, null);
                    }
                }
            }
        }
    }

    public void cloneMapping(HashMap<String, String> hashMap){
        variableMappings = new HashMap<>();
        for(String var : hashMap.keySet()){
            variableMappings.put(var, hashMap.get(var));
        }
    }

    @Override
    public boolean equals(Object o){
        if(o == null || !(o instanceof ImplicationMapping)){
            return false;
        }
        ImplicationMapping mapping = (ImplicationMapping)o;
        if(!mapping.consequent.equals(this.consequent) || !Arrays.equals(mapping.consequent.getArgumentArray(), this.consequent.getArgumentArray())){
            return false;
        }
        if(mapping.antecedents.size() != this.antecedents.size()){
            return false;
        }
        for(AtomicTerm a1 : mapping.antecedents){
            if(!this.antecedents.contains(a1)){
                return false;
            }
            for(AtomicTerm a2 : this.antecedents){
                if(a1.equals(a2)){
                    if(!Arrays.equals(a1.getArgumentArray(), a2.getArgumentArray())){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    @Override
    public String toString(){
        return antecedents + "=>" + consequent;
    }
}