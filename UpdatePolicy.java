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

public class BulkPolicyAppFormUpdater extends TaskSupport {

    private static final Logger LOGGER = Logger.getLogger("BulkPolicyAppFormUpdater");

    private ProvisioningService provService;
    private boolean disableWorkflow;
    private int batchSize;
    private String inputFile;

    @Override
    public void execute(HashMap<String, Object> params) {
        LOGGER.info("==== Starting BulkPolicyAppFormUpdater Scheduled Task ====");

        try {
            initParams(params);
            provService = Platform.getService(ProvisioningService.class);

            List<AppRecord> allRecords = loadInputFile(inputFile);
            LOGGER.info("Total records to process: " + allRecords.size());

            // Split into batches
            for (int i = 0; i < allRecords.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allRecords.size());
                List<AppRecord> batch = allRecords.subList(i, end);
                processBatch(batch);
            }

            LOGGER.info("==== Completed BulkPolicyAppFormUpdater successfully ====");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during BulkPolicyAppFormUpdater execution: ", e);
        }
    }

    private void initParams(HashMap<String, Object> params) {
        inputFile = (String) params.get("InputFilePath");
        batchSize = Integer.parseInt(params.getOrDefault("BatchSize", "100").toString());
        disableWorkflow = Boolean.parseBoolean(params.getOrDefault("DisableWorkflow", "true").toString());

        LOGGER.info("Parameters -> InputFilePath=" + inputFile + ", BatchSize=" + batchSize + ", DisableWorkflow=" + disableWorkflow);
    }

    private void processBatch(List<AppRecord> batch) {
        HashMap<String, Object> context = new HashMap<>();
        if (disableWorkflow) {
            context.put("disableAuditing", Boolean.TRUE);
            context.put("disableEventHandlers", Boolean.TRUE);
            context.put("disableRules", Boolean.TRUE);
        }

        for (AppRecord rec : batch) {
            try {
                Account account = provService.getAccountDetails(rec.getAppInstanceKey());
                AccountData accData = account.getAccountData();

                // Update parent form field
                if (rec.getParentFieldName() != null && rec.getParentFieldValue() != null) {
                    accData.getData().put(rec.getParentFieldName(), rec.getParentFieldValue());
                    LOGGER.info("Updated parent form field: " + rec.getParentFieldName());
                }

                // Update child form fields
                if (rec.getChildFormName() != null && rec.getChildFieldName() != null) {
                    List<ChildTableRecord> childRows = accData.getChildData().get(rec.getChildFormName());
                    if (childRows != null && !childRows.isEmpty()) {
                        for (ChildTableRecord child : childRows) {
                            if (rec.getChildFilterField() == null ||
                                rec.getChildFilterValue().equalsIgnoreCase(child.getChildData().get(rec.getChildFilterField()))) {
                                child.getChildData().put(rec.getChildFieldName(), rec.getChildFieldValue());
                                LOGGER.info("Updated child form: " + rec.getChildFormName() +
                                            " field: " + rec.getChildFieldName());
                            }
                        }
                    }
                }

                // Commit changes
                provService.modifyAccount(account, context);
                LOGGER.info("Updated Account for Policy: " + rec.getPolicyName());

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to update for Policy: " + rec.getPolicyName(), e);
            }
        }
    }

    private List<AppRecord> loadInputFile(String csvPath) {
        List<AppRecord> records = new ArrayList<>();
        if (csvPath == null || csvPath.trim().isEmpty()) {
            LOGGER.warning("No input file path provided.");
            return records;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            // Expected CSV header:
            // PolicyName,AppInstanceKey,ParentFieldName,ParentFieldValue,ChildFormName,ChildFilterField,ChildFilterValue,ChildFieldName,ChildFieldValue
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", -1);
                AppRecord rec = new AppRecord();
                rec.setPolicyName(arr[0]);
                rec.setAppInstanceKey(Long.parseLong(arr[1]));
                rec.setParentFieldName(arr[2]);
                rec.setParentFieldValue(arr[3]);
                rec.setChildFormName(arr[4]);
                rec.setChildFilterField(arr[5]);
                rec.setChildFilterValue(arr[6]);
                rec.setChildFieldName(arr[7]);
                rec.setChildFieldValue(arr[8]);
                records.add(rec);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading input file: " + csvPath, e);
        }

        return records;
    }

    @Override
    public HashMap getAttributes() {
        return new HashMap();
    }

    @Override
    public void setAttributes() {}
}

/**
 * POJO for each record in input CSV
 */
class AppRecord {
    private String policyName;
    private Long appInstanceKey;
    private String parentFieldName;
    private String parentFieldValue;
    private String childFormName;
    private String childFilterField;
    private String childFilterValue;
    private String childFieldName;
    private String childFieldValue;

    public String getPolicyName() { return policyName; }
    public void setPolicyName(String s) { policyName = s; }

    public Long getAppInstanceKey() { return appInstanceKey; }
    public void setAppInstanceKey(Long k) { appInstanceKey = k; }

    public String getParentFieldName() { return parentFieldName; }
    public void setParentFieldName(String s) { parentFieldName = s; }

    public String getParentFieldValue() { return parentFieldValue; }
    public void setParentFieldValue(String s) { parentFieldValue = s; }

    public String getChildFormName() { return childFormName; }
    public void setChildFormName(String s) { childFormName = s; }

    public String getChildFilterField() { return childFilterField; }
    public void setChildFilterField(String s) { childFilterField = s; }

    public String getChildFilterValue() { return childFilterValue; }
    public void setChildFilterValue(String s) { childFilterValue = s; }

    public String getChildFieldName() { return childFieldName; }
    public void setChildFieldName(String s) { childFieldName = s; }

    public String getChildFieldValue() { return childFieldValue; }
    public void setChildFieldValue(String s) { childFieldValue = s; }
}