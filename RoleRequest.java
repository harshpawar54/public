import oracle.iam.platform.OIMClient;
import oracle.iam.request.api.RequestService;
import oracle.iam.request.vo.Request;
import oracle.iam.request.vo.RequestData;
import oracle.iam.request.vo.RequestEntity;
import oracle.iam.request.vo.RequestTemplate;

import java.util.Hashtable;

public class AssignRoleRequest {
    public static void main(String[] args) {
        try {
            // Set up OIMClient properties
            Hashtable<String, String> env = new Hashtable<>();
            env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
            env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, "t3://<OIM_HOST>:<OIM_PORT>"); // Replace with your OIM URL

            OIMClient oimClient = new OIMClient(env);

            // Authenticate
            oimClient.login("<USERNAME>", "<PASSWORD>".toCharArray()); // Replace with OIM credentials

            // Get RequestService
            RequestService requestService = oimClient.getService(RequestService.class);

            // Create a RequestData object
            RequestData requestData = new RequestData();

            // Specify the request template for assigning a role
            RequestTemplate requestTemplate = requestService.getRequestTemplate("Assign Roles");

            // Set the user entity (target user)
            RequestEntity userEntity = new RequestEntity();
            userEntity.setEntityType(RequestConstants.USER_TYPE);
            userEntity.setEntitySubType(RequestConstants.USER_TYPE);
            userEntity.setEntityKey("<USER_KEY>"); // Replace with User Key
            requestData.addEntity(userEntity);

            // Set the role entity (role to assign)
            RequestEntity roleEntity = new RequestEntity();
            roleEntity.setEntityType(RequestConstants.ROLE_TYPE);
            roleEntity.setEntitySubType(RequestConstants.ROLE_TYPE);
            roleEntity.setEntityKey("<ROLE_KEY>"); // Replace with Role Key
            requestData.addEntity(roleEntity);

            // Create the request
            Request request = new Request();
            request.setRequestTemplate(requestTemplate);
            request.setRequestData(requestData);
            request.setBeneficiaryKey("<USER_KEY>"); // Replace with User Key

            // Submit the request
            String requestId = requestService.submitRequest(request);
            System.out.println("Role assignment request submitted successfully. Request ID: " + requestId);

            // Logout
            oimClient.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}