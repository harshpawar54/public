AccessPolicy policy = policySvc.getAccessPolicyDetails(policyKey);
System.out.println("RowVer class: " + policy.getRowVer().getClass());

Hashtable env = new Hashtable();
env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, "t3://localhost:14000");
OIMClient client = new OIMClient(env);
client.login("xelsysadm", "password".toCharArray());

AccessPolicyService policySvc = client.getService(AccessPolicyService.class);

AccessPolicy policy = policySvc.getAccessPolicyDetails(policyKey);

// Convert rowVer to byte[] if it came as a String
Object rowVer = policy.getRowVer();
if (rowVer instanceof String) {
    String s = (String) rowVer;
    policy.setRowVer(s.getBytes());  // Convert string to bytes
}

policy.setDescription("Updated from Platform service");
policySvc.updateAccessPolicy(policy);


Bug 32599369 – ACCESSPOLICYSERVICE UPDATEACCESSPOLICY THROWS CLASSCASTEXCEPTION (STRING→[B)

private static void safeUpdatePolicy(AccessPolicyService svc, AccessPolicy policy) throws AccessPolicyServiceException {
    Object rv = policy.getRowVer();
    if (rv instanceof String) {
        policy.setRowVer(((String) rv).getBytes());
    }
    svc.updateAccessPolicy(policy);
}