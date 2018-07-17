package utils.ObjectParse.monkeyLearn;

import java.util.*;

/**
 * @author Pedro Feiteira, n48119, NB24217
 * This class is used as GSON conversion
 * watch -> https://app.monkeylearn.com/main/classifiers/cl_4yVFrrYo/tab/run/
 */
public class DESC {
    private String base;
    private Map<String, List<String>> scenarios;
    private boolean hasScenario;

    public DESC(String base, Map<String, List<String>> scenarios){
        this.base = base;
        this.scenarios = scenarios;
        hasScenario = scenarios != null ? true : false;
    }

    public String getBase() {
        return base;
    }

    public Map<String, List<String>> getScenarios() {
        return scenarios;
    }

    public boolean isHasScenario() {
        return hasScenario;
    }
}
