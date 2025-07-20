package mn.dmn;
import jakarta.enterprise.context.RequestScoped;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.*;
import org.kie.dmn.api.core.ast.DecisionNode;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RequestScoped
public class DmnService {

    public DmnResponse evaluateDecision(DmnRequest request) {
        try {
            // Create DMN runtime
            KieServices kieServices = KieServices.Factory.get();
            KieContainer kieContainer = kieServices.newKieClasspathContainer();

            org.kie.api.KieBase kieBase;
            try {
                kieBase = kieContainer.getKieBase();
            } catch (Exception e) {
                System.out.println("No default KieBase found, trying to get first available...");
                // If no default KieBase, get the first available one
                if (!kieContainer.getKieBaseNames().isEmpty()) {
                    String firstKieBaseName = kieContainer.getKieBaseNames().iterator().next();
                    kieBase = kieContainer.getKieBase(firstKieBaseName);
                    System.out.println("Using KieBase: " + firstKieBaseName);
                } else {
                    return new DmnResponse("No KieBase available. Please ensure module.xml is properly configured in src/main/resources/META-INF/");
                }
            }

            DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieBase).get(DMNRuntime.class);

            // If no runtime found, try alternative approach
            if (dmnRuntime == null) {
//                dmnRuntime = DMNRuntimeImpl.createResultImpl();
                System.out.println("dmnRuntime is null");
            }

            // Load DMN model
            DMNModel dmnModel = loadDMNModel(dmnRuntime, request.getDmnFile());

            if (dmnModel == null) {
                return new DmnResponse("Failed to load DMN model from file: " + request.getDmnFile());
            }

            // Create DMN context with input data
            assert dmnRuntime != null;
            DMNContext dmnContext = dmnRuntime.newContext();
            if (request.getInputData() != null) {
                for (Map.Entry<String, Object> entry : request.getInputData().entrySet()) {
                    dmnContext.set(entry.getKey(), entry.getValue());
                }
            }

            // Evaluate decision
            DMNResult dmnResult;
            if (request.getDecisionName() != null && !request.getDecisionName().trim().isEmpty()) {
                // Evaluate specific decision
                dmnResult = dmnRuntime.evaluateByName(dmnModel, dmnContext, request.getDecisionName());
            } else {
                // Evaluate all decisions
                dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
            }

            // Check for errors
            if (dmnResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder("DMN evaluation errors: ");
                dmnResult.getMessages().forEach(msg -> errorMessage.append(msg.getText()).append("; "));
                return new DmnResponse(errorMessage.toString());
            }

            // Extract result and reason
            return processResult(dmnResult, request.getDecisionName(), dmnModel);

        } catch (Exception e) {
            return new DmnResponse("Error evaluating DMN: " + e.getMessage());
        }
    }

    private DMNModel loadDMNModel(DMNRuntime dmnRuntime, String dmnFilePath) {
        try {
            InputStream dmnStream = getClass().getClassLoader().getResourceAsStream(dmnFilePath);

            // If not found in classpath, try to load from file system
            Path path = Paths.get(dmnFilePath);
            if (Files.exists(path)) {
                dmnStream = Files.newInputStream(path);
            } else {
                // Try relative to src/main/resources
                path = Paths.get("src/main/resources/" + dmnFilePath);
                if (Files.exists(path)) {
                    dmnStream = Files.newInputStream(path);
                }
            }

            if (dmnStream == null) {
                throw new RuntimeException("DMN file not found: " + dmnFilePath);
            }

            // Use KieServices to compile and load the DMN model
            KieServices kieServices = KieServices.Factory.get();
            org.kie.api.io.Resource dmnResource = kieServices.getResources().newInputStreamResource(dmnStream);
            dmnResource.setSourcePath(dmnFilePath);

            // Create a KieFileSystem and add the DMN resource
            org.kie.api.builder.KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write(dmnResource);

            // Create a KieBuilder from the KieFileSystem
            org.kie.api.builder.KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            // Check for build errors
            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                throw new RuntimeException("DMN compilation errors: " + kieBuilder.getResults().getMessages());
            }

            // Get the first available model (since we just loaded one)
            List<DMNModel> models = dmnRuntime.getModels();
            if (models == null || models.isEmpty()) {
                throw new RuntimeException("No DMN models found in file: " + dmnFilePath);
            }

            // Return the first model
            return models.getFirst();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load DMN model: " + e.getMessage(), e);
        }
    }

    private DmnResponse processResult(DMNResult dmnResult, String decisionName, DMNModel dmnModel) {
        try {
            Object result;
            String reason = "Decision evaluated successfully";

            if (decisionName != null && !decisionName.trim().isEmpty()) {
                // Get specific decision result
                DMNDecisionResult decisionResult = dmnResult.getDecisionResultByName(decisionName);
                if (decisionResult == null) {
                    return new DmnResponse("Decision not found: " + decisionName);
                }
                result = decisionResult.getResult();

                // Find the decision node to get additional context
                DecisionNode decisionNode = dmnModel.getDecisionByName(decisionName);
                if (decisionNode != null) {
                    reason = "Decision '" + decisionName + "' evaluated";
                }
            } else {
                // Get first decision result if no specific decision name provided
                if (!dmnResult.getDecisionResults().isEmpty()) {
                    DMNDecisionResult firstResult = dmnResult.getDecisionResults().getFirst();
                    result = firstResult.getResult();
                    reason = "Decision '" + firstResult.getDecisionName() + "' evaluated";
                } else {
                    return new DmnResponse("No decision results found");
                }
            }

            // Convert result to boolean
            boolean booleanResult = convertToBoolean(result);

            // Enhance reason with result details
            if (result != null) {
                reason += " - Result: " + result;
            }

            return new DmnResponse(booleanResult, reason);

        } catch (Exception e) {
            return new DmnResponse("Error processing result: " + e.getMessage());
        }
    }

    private boolean convertToBoolean(Object result) {
        switch (result) {
            case null -> {
                return false;
            }
            case Boolean b -> {
                return b;
            }
            case String s -> {
                String strResult = s.toLowerCase().trim();
                return "true".equals(strResult) || "yes".equals(strResult) || "1".equals(strResult);
            }
            case Number number -> {
                return number.doubleValue() > 0;
            }
            default -> {
            }
        }

        // For other types, consider non-null as true
        return true;
    }
}