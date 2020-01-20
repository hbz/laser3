package de.laser.helper



public class FactoryResult {
    Object result = null
    List existingDuplicates = []
    List<String> status = []

    public static final String STATUS_OK = 'STATUS_OK'
    public static final String STATUS_ERR = 'STATUS_ERR'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ = 'STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ = 'STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_SYSTEM = 'STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_SYSTEM'

//    public FactoryResult(def result, List existingDuplicates, String status){
//        this.result = result
//        this.existingDuplicates = existingDuplicates
//        this.status += status
//    }
//    public FactoryResult(def result){
//        new FactoryResult(result, null, STATUS_OK)
//    }

}
