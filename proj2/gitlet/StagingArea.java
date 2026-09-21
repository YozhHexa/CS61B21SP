package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StagingArea implements Serializable {
    /** represent the file to be added. */
    private final Map<String, String> additions;
    /** represent the file to be removed. */
    private final Set<String> removals;

    public StagingArea() {
        additions = new HashMap<>();
        removals = new HashSet<>();
    }

    public void stageAddition(String fileName, String sha1Value) {
        additions.put(fileName, sha1Value);
    }

    //TODO this name is not good
    public String getAdditionFileSha1value(String fileName) {
        return additions.get(fileName);
    }

    public void unstageAddition(String fileName) {
        additions.remove(fileName);
    }

    public boolean unstageAddition(String fileName, String sha1Value) {
        return additions.remove(fileName, sha1Value);
    }

    public boolean additionsContainsKey(String fileName) {
        return additions.containsKey(fileName);
    }

    public boolean additionsContainsValue(String sha1Value) {
        return additions.containsValue(sha1Value);
    }

    public void stageRemoval(String fileName) {
        removals.add(fileName);
    }
    public boolean removalsContainsKey(String fileName) {
        return removals.contains(fileName);
    }

    public void unstageRemoval(String fileName) {
        removals.remove(fileName);
    }

    public boolean isBlank() {
        if (additions.isEmpty() && removals.isEmpty()) {
            return true;
        }
        return false;
    }

    /** return the copy of additions map. */
    public Map<String, String> getAdditions() {
        Map<String, String> m = new HashMap<>(additions);
        return m;
    }

    /** return the copy of removal map. */
    public Set<String> getRemovals() {
        Set<String> s = new HashSet<>(removals);
        return s;
    }
}
