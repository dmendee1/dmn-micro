package mn.dmn;

import jakarta.json.bind.annotation.JsonbProperty;

import java.util.Map;

public class DmnRequest {

    @JsonbProperty("dmnFile")
    private String dmnFile;

    @JsonbProperty("decisionName")
    private String decisionName;

    @JsonbProperty("inputData")
    private Map<String, Object> inputData;

    public DmnRequest() {}

    public DmnRequest(String dmnFile, String decisionName, Map<String, Object> inputData) {
        this.dmnFile = dmnFile;
        this.decisionName = decisionName;
        this.inputData = inputData;
    }

    public String getDmnFile() {
        return dmnFile;
    }

    public void setDmnFile(String dmnFile) {
        this.dmnFile = dmnFile;
    }

    public String getDecisionName() {
        return decisionName;
    }

    public void setDecisionName(String decisionName) {
        this.decisionName = decisionName;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }
}