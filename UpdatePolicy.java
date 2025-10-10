package com.myorg.oim.scheduler;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;
import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.rolemgmt.vo.Role;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.AccountData;
import oracle.iam.provisioning.vo.AccountData.ChildTableRecord;

/**
 * Bulk Role-Policy-Application updater
 * 1. Creates/updates roles
 * 2. Attaches policies to roles
 * 3. Updates parent + child form data for applications in those policies
 */
public class BulkRolePolicyAppUpdater extends TaskSupport {

    private static final Logger LOGGER = Logger.getLogger("BulkRolePolicyAppUpdater");

    private ProvisioningService provService;
    private RoleManager roleManager;
    private String inputFile;
    private int batchSize;
    private boolean disableWorkflow;

    @Override
    public void execute(HashMap<String, Object> params) {
        LOGGER.info("==== Starting BulkRolePolicyAppUpdater ====");
        try {
            initParams(params);
            provService = Platform.getService(ProvisioningService.class);
            roleManager = Platform.getService(RoleManager.class);

            List<RoleRecord> allRoles = loadInputFile(inputFile);
            LOGGER.info("Roles to process: " + allRoles.size());

            for (int i = 0; i < allRoles.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allRoles.size());
                List<RoleRecord> batch = allRoles.subList(i, end);
                processBatch(batch);
            }

            LOGGER.info("==== Completed BulkRolePolicyAppUpdater successfully ====");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error in BulkRolePolicyAppUpdater", e);
        }
    }

    private void initParams(HashMap<String, Object> params) {
        inputFile = (String) params.get("InputFilePath");
        batchSize = Integer.parseInt(params.getOrDefault("BatchSize", "50").toString());
        disableWorkflow = Boolean.parseBoolean(params.getOrDefault("DisableWorkflow", "true").toString());
    }

    private void processBatch(List<RoleRecord> batch) {
        HashMap<String, Object> ctx = new HashMap<>();
        if (disableWorkflow) {
            ctx.put("disableAuditing", Boolean.TRUE);
            ctx.put("disableEventHandlers", Boolean.TRUE);
            ctx.put("disableRules", Boolean.TRUE);
        }

        for (RoleRecord rr : batch) {
            try {
                // 1️⃣ Ensure Role exists or create
                Role role = getOrCreateRole(rr.getRoleName(), rr.getRoleDescription());

                // 2️⃣ For each policy, update app and attach to role
                for (PolicyRecord pol : rr.getPolicies()) {
                    updateApplicationForm(pol, ctx);
                    attachPolicyToRole(role, pol);
                }

                LOGGER.info("Role processed successfully: " + rr.getRoleName());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error processing role: " + rr.getRoleName(), e);
            }
        }
    }

    private Role getOrCreateRole(String roleName, String desc) throws Exception {
        try {
            Role role = roleManager.getDetails(roleName, null, true);
            LOGGER.info("Role exists: " + roleName);
            return role;
        } catch (Exception e) {
            LOGGER.info("Creating new role: " + roleName);
            Role newRole = new Role(roleName);
            newRole.setDescription(desc);
            return roleManager.create(newRole);
        }
    }

    private void attachPolicyToRole(Role role, PolicyRecord policy) {
        // ⚠️ Pseudocode: Adjust based on your OIM Policy linkage model.
        // Typically you will use a custom DB table or metadata relationship.
        LOGGER.info("Attaching policy " + policy.getPolicyName() + " to role " + role.getName());
        // Example: roleManager.addEntitlement(role.getEntityId(), policy.getPolicyName());
    }

    private void updateApplicationForm(PolicyRecord policy, HashMap<String, Object> ctx) {
        try {
            Account acc = provService.getAccountDetails(policy.getAppInstanceKey());
            AccountData accData = acc.getAccountData();

            // Update parent
            for (Map.Entry<String, String> e : policy.getParentFields().entrySet()) {
                accData.getData().put(e.getKey(), e.getValue());
            }

            // Update child rows
            if (policy.getChildFormName() != null && policy.getChildRows() != null) {
                List<ChildTableRecord> childRows = accData.getChildData().get(policy.getChildFormName());
                if (childRows != null) {
                    for (ChildTableRecord existingChild : childRows) {
                        for (Map<String, String> updateRow : policy.getChildRows()) {
                            for (Map.Entry<String, String> f : updateRow.entrySet()) {
                                existingChild.getChildData().put(f.getKey(), f.getValue());
                            }
                        }
                    }
                }
            }

            provService.modifyAccount(acc, ctx);
            LOGGER.info("Application updated for policy " + policy.getPolicyName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed updating app for policy " + policy.getPolicyName(), e);
        }
    }

    private List<RoleRecord> loadInputFile(String path) {
        List<RoleRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", -1);
                RoleRecord rr = new RoleRecord();
                rr.setRoleName(arr[0]);
                rr.setRoleDescription(arr[1]);

                PolicyRecord policy = new PolicyRecord();
                policy.setPolicyName(arr[2]);
                policy.setAppInstanceKey(Long.parseLong(arr[3]));
                policy.setParentFields(parseKeyValuePairs(arr[4]));
                policy.setChildFormName(arr[5]);
                policy.setChildRows(parseChildRows(arr[6]));

                rr.addPolicy(policy);
                list.add(rr);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading input CSV", e);
        }
        return list;
    }

    private Map<String, String> parseKeyValuePairs(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null || text.trim().isEmpty()) return map;
        for (String kv : text.split(";")) {
            String[] pair = kv.split("=", 2);
            if (pair.length == 2) map.put(pair[0].trim(), pair[1].trim());
        }
        return map;
    }

    private List<Map<String, String>> parseChildRows(String text) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return rows;
        for (String segment : text.split("\\|")) {
            Map<String, String> child = parseKeyValuePairs(segment.replace("[", "").replace("]", ""));
            rows.add(child);
        }
        return rows;
    }

    @Override
    public HashMap getAttributes() { return new HashMap(); }

    @Override
    public void setAttributes() {}
}

/* ===== Helper POJOs ===== */
class RoleRecord {
    private String roleName;
    private String roleDescription;
    private List<PolicyRecord> policies = new ArrayList<>();

    public String getRoleName() { return roleName; }
    public void setRoleName(String n) { roleName = n; }
    public String getRoleDescription() { return roleDescription; }
    public void setRoleDescription(String d) { roleDescription = d; }
    public List<PolicyRecord> getPolicies() { return policies; }
    public void addPolicy(PolicyRecord p) { policies.add(p); }
}

class PolicyRecord {
    private String policyName;
    private Long appInstanceKey;
    private Map<String, String> parentFields;
    private String childFormName;
    private List<Map<String, String>> childRows;

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String s) { policyName = s; }
    public Long getAppInstanceKey() { return appInstanceKey; }
    public void setAppInstanceKey(Long k) { appInstanceKey = k; }
    public Map<String, String> getParentFields() { return parentFields; }
    public void setParentFields(Map<String, String> m) { parentFields = m; }
    public String getChildFormName() { return childFormName; }
    public void setChildFormName(String s) { childFormName = s; }
    public List<Map<String, String>> getChildRows() { return childRows; }
    public void setChildRows(List<Map<String, String>> c) { childRows = c; }
}


RoleName,RoleDescription,PolicyName,AppInstanceKey,ParentKeyValuePairs,ChildFormName,ChildRows
Role-Admin,Administrator role,Policy-001,12345,UD_APP_FIELD1=ON;UD_APP_FIELD2=Active,UD_CHILD,[UD_CHILD_NAME=RoleA;UD_CHILD_STATUS=Active]|[UD_CHILD_NAME=RoleB;UD_CHILD_STATUS=Inactive]
Role-Viewer,Viewer role,Policy-002,12346,UD_APP_FIELD1=READ_ONLY;UD_APP_FIELD3=Yes,UD_CHILD,[UD_CHILD_NAME=RoleC;UD_CHILD_STATUS=Active]