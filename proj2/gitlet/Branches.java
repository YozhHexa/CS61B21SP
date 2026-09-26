package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class Branches implements Serializable {
    private HashMap<String, String> branchHeads;

    // return branches as a set in a lexicographic order
    public Set<String> keySet() {
        return new TreeSet<>(branchHeads.keySet());
    }

    //
    public void put(String branch, String headID) {
        branchHeads.put(branch, headID);
    }

    //
    public String get(String branch) {
        return branchHeads.get(branch);
    }
}
