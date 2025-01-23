private void roleUser(String usrkey,String roleName,String operation ,OIMService osevice,RoleManager rm) throws Exception {
                     
                    String methodName = " ::roleUser:: ";  
                    try {
                    RequestData requestData = new RequestData();
                       Beneficiary beneficiary = new Beneficiary();
                 //      String userKey = "12"; //User with key 12
                       beneficiary.setBeneficiaryKey(usrkey);
                       beneficiary.setBeneficiaryType(Beneficiary.USER_BENEFICIARY);
 
                       RequestBeneficiaryEntity requestEntity = new RequestBeneficiaryEntity();
                       requestEntity.setRequestEntityType(OIMType.Role);
                       requestEntity.setEntitySubType("Default");
                      String rolekey =getRolekey(roleName,rm);
                       requestEntity.setEntityKey(rolekey); //Role with key 10
                       if(operation.equalsIgnoreCase("add")) {
                        System.out.println(CLASS_NAME + methodName+"add : "+operation);
                           requestEntity.setOperation(RequestConstants.MODEL_ASSIGN_ROLES_OPERATION);   
                       }else if(operation.equalsIgnoreCase("remove")){
                            System.out.println(CLASS_NAME + methodName+"remove : "+operation);
                           requestEntity.setOperation(RequestConstants.MODEL_REMOVE_ROLES_OPERATION);
                       }
                       else if(operation.equalsIgnoreCase("modifydate")){
                           System.out.println(CLASS_NAME + methodName+"modifydate : "+operation);
                           requestEntity.setOperation(RequestConstants.MODEL_ROLE_GRANT_OPERATION);
                     
                           Date now = new Date();
                           Date addedDate2 = addDays(now, 14);
//                         extraAttributes.put("endDate",addedDate2);
                             List<RequestBeneficiaryEntityAttribute> attrs = new ArrayList<RequestBeneficiaryEntityAttribute>();
                            RequestBeneficiaryEntityAttribute attr = new RequestBeneficiaryEntityAttribute("endDate", addedDate2, RequestBeneficiaryEntityAttribute.TYPE.Date); 
                            attrs.add(attr);
                           requestEntity.setEntityData(attrs);
                       }
 
                       List<RequestBeneficiaryEntity> entities = new ArrayList<RequestBeneficiaryEntity>();
                       entities.add(requestEntity);
 
                       beneficiary.setTargetEntities(entities);
 
                       List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();
                       beneficiaries.add(beneficiary);
                       requestData.setBeneficiaries(beneficiaries);
                     
                       oracle.iam.vo.OperationResult result = osevice.doOperation(requestData, OIMService.Intent.REQUEST);
                       if( result.getRequestID() != null ) {
                           //Operation resulted in to request creation.
                           System.out.println("Request submitted with ID: " + result.getRequestID());
                       } else {
                           System.out.println("Role is assigned to user successfully");
                       }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }