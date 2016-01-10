import java.util.Arrays;

/**
 *
 * Created by Vipul Somani (vasomani@usc.edu) on 11/9/2015.
 */
public class AtomicTerm {

    public static boolean isArgumentConstant(String arg){
        return Character.isUpperCase(arg.charAt(0));
    }

    private String name;
    private int numOfArguments;
    private String[] argumentArray;

    public AtomicTerm(AtomicTerm term){
        this.name = term.name;
        this.numOfArguments = term.numOfArguments;
        this.argumentArray = term.argumentArray.clone();
    }

    public AtomicTerm(String name, String args){
        this.name = name;
        argumentArray = args.split(",");
        this.numOfArguments = argumentArray.length;
    }

    public String getName() {
        return name;
    }

    public int getNumOfArguments() {
        return numOfArguments;
    }

    public String[] getArgumentArray() {
        return argumentArray;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + numOfArguments;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof AtomicTerm)){
            return false;
        }
        AtomicTerm term = (AtomicTerm)obj;
        boolean primaryEquality = getName().equals(term.getName()) &&
                getNumOfArguments() == term.getNumOfArguments();
        if(!primaryEquality){
            return false;
        }
        for(int i =0; i < term.getNumOfArguments(); i++){
            if(isArgumentConstant(term.getArgumentArray()[i]) || isArgumentConstant(this.getArgumentArray()[i])){
                if(!term.getArgumentArray()[i].equals(this.getArgumentArray()[i])){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString(){
        return name + Arrays.toString(argumentArray);
    }
}
