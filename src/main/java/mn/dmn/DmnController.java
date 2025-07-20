package mn.dmn;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/dmn")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DmnController {

    @Inject
    DmnService dmnService;

    @POST
    @Path("/evaluate")
    public Response evaluateDecision(DmnRequest request) {
        try {
            // Validate request
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new DmnResponse("Request body is required"))
                        .build();
            }

            if (request.getDmnFile() == null || request.getDmnFile().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new DmnResponse("DMN file path is required"))
                        .build();
            }

            // Evaluate decision
            DmnResponse response = dmnService.evaluateDecision(request);

            if (response.isSuccess()) {
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response)
                        .build();
            }

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new DmnResponse("Unexpected error: " + e.getMessage()))
                    .build();
        }

    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\": \"UP\", \"service\": \"DMN Evaluator\"}").build();
    }
}
