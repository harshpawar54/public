package com.myorg.oim.scheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.iam.platform.Platform;
import oracle.iam.scheduler.vo.TaskSupport;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.AccountData;
import oracle.iam.provisioning.vo.AccountData.ChildTableRecord;

/**
 * Bulk scheduled task to update multiple parent and child fields for
 * Application Instances (used in policy updates).
 */
public class BulkPolicyAppFormUpdaterV2 extends TaskSupport {

    private static final Logger LOGGER = Logger.getLogger("BulkPolicyAppFormUpdaterV2");

    private ProvisioningService provService;
    private String inputFile;
    private int batchSize;
    private boolean disableWorkflow;

    @Override
    public void execute(HashMap<String, Object> params) {
        LOGGER.info("==== Starting BulkPolicyAppFormUpdaterV2 ====");
        try {
            initParams(params);
            provService = Platform.getService(ProvisioningService.class);

            List<AppRecord> allRecords = loadInputFile(inputFile);
            LOGGER.info("Records to process: " + allRecords.size());

            for (int i = 0; i < allRecords.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allRecords.size());
                List<AppRecord> batch = allRecords.subList(i, end);
                processBatch(batch);
            }

            LOGGER.info("==== Completed BulkPolicyAppFormUpdaterV2 successfully ====");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error in BulkPolicyAppFormUpdaterV2", e);
        }
    }

    private void initParams(HashMap<String, Object> params) {
        inputFile = (String) params.get("InputFilePath");
        batchSize = Integer.parseInt(params.getOrDefault("BatchSize", "100").toString());
        disableWorkflow = Boolean.parseBoolean(params.getOrDefault("DisableWorkflow", "true").toString());
        LOGGER.info("Params -> InputFilePath=" + inputFile + " BatchSize=" + batchSize + " DisableWorkflow=" + disableWorkflow);
    }

    private void processBatch(List<AppRecord> batch) {
        HashMap<String, Object> ctx = new HashMap<>();
        if (disableWorkflow) {
            ctx.put("disableAuditing", Boolean.TRUE);
            ctx.put("disableEventHandlers", Boolean.TRUE);
            ctx.put("disableRules", Boolean.TRUE);
        }

        for (AppRecord rec : batch) {
            try {
                Account acc = provService.getAccountDetails(rec.getAppInstanceKey());
                AccountData accData = acc.getAccountData();

                // --- Update Parent Fields ---
                if (rec.getParentFields() != null && !rec.getParentFields().isEmpty()) {
                    for (Map.Entry<String, String> e : rec.getParentFields().entrySet()) {
                        accData.getData().put(e.getKey(), e.getValue());
                        LOGGER.info("Parent field updated: " + e.getKey() + "=" + e.getValue());
                    }
                }

                // --- Update Child Form Fields ---
                if (rec.getChildFormName() != null && rec.getChildRows() != null) {
                    List<ChildTableRecord> childRows = accData.getChildData().get(rec.getChildFormName());
                    if (childRows != null) {
                        for (ChildTableRecord existingChild : childRows) {
                            for (Map<String, String> updateRow : rec.getChildRows()) {
                                // Optional: match based on key if "FILTER_FIELD" present
                                String filterField = updateRow.get("FILTER_FIELD");
                                String filterValue = updateRow.get("FILTER_VALUE");
                                if (filterField == null || filterValue == null ||
                                        filterValue.equalsIgnoreCase(existingChild.getChildData().get(filterField))) {
                                    for (Map.Entry<String, String> f : updateRow.entrySet()) {
                                        if (!f.getKey().startsWith("FILTER_")) {
                                            existingChild.getChildData().put(f.getKey(), f.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- Commit Once ---
                provService.modifyAccount(acc, ctx);
                LOGGER.info("Policy updated successfully: " + rec.getPolicyName());

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed updating policy " + rec.getPolicyName(), e);
            }
        }
    }

    /**
     * Reads input CSV. Each row:
     * PolicyName,AppInstanceKey,ParentKeyValuePairs,ChildFormName,ChildRows
     * ParentKeyValuePairs => key1=value1;key2=value2
     * ChildRows => [key1=value1;key2=value2]|[key1=value1;FILTER_FIELD=UD_CHILD_NAME;FILTER_VALUE=Role1]
     */
    private List<AppRecord> loadInputFile(String path) {
        List<AppRecord> list = new ArrayList<>();
        if (path == null || path.trim().isEmpty()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", -1);
                AppRecord rec = new AppRecord();
                rec.setPolicyName(arr[0]);
                rec.setAppInstanceKey(Long.parseLong(arr[1]));
                rec.setParentFields(parseKeyValuePairs(arr[2]));
                rec.setChildFormName(arr[3]);
                rec.setChildRows(parseChildRows(arr[4]));
                list.add(rec);
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
        for (String segment : text.split("\\|")) { // each child row inside []
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

/** Simple POJO for each record */
class AppRecord {
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