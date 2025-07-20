package mn.dmn;

import jakarta.json.bind.annotation.JsonbProperty;

public class DmnResponse {

    @JsonbProperty("result")
    private boolean result;

    @JsonbProperty("reason")
    private String reason;

    @JsonbProperty("success")
    private boolean success;

    @JsonbProperty("error")
    private String error;

    public DmnResponse() {}

    public DmnResponse(boolean result, String reason) {
        this.result = result;
        this.reason = reason;
        this.success = true;
        this.error = null;
    }

    public DmnResponse(String error) {
        this.result = false;
        this.reason = null;
        this.success = false;
        this.error = error;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
