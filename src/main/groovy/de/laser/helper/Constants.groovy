package de.laser.helper

import groovy.transform.CompileStatic

@CompileStatic
class Constants {

    public static final String HTTP_CREATED                = "CREATED" // 200
    public static final String HTTP_BAD_REQUEST            = "BAD_REQUEST" // 400
    public static final String HTTP_FORBIDDEN              = "FORBIDDEN" // 403
    public static final String HTTP_NOT_ACCEPTABLE         = "NOT_ACCEPTABLE" // 406
    public static final String HTTP_CONFLICT               = "CONFLICT" // 409
    public static final String HTTP_PRECONDITION_FAILED    = "PRECONDITION_FAILED" // 412
    public static final String HTTP_INTERNAL_SERVER_ERROR  = "INTERNAL_SERVER_ERROR" // 500
    public static final String HTTP_NOT_IMPLEMENTED        = "NOT_IMPLEMENTED" // 501

    public static final String MIME_ALL                    = "*/*"
    public static final String MIME_TEXT_JSON              = "text/json"
    public static final String MIME_TEXT_PLAIN             = "text/plain"
    public static final String MIME_TEXT_TSV               = "text/tab-separated-values"
    public static final String MIME_TEXT_XML               = "text/xml"
    public static final String MIME_APPLICATION_JSON       = "application/json"
    public static final String MIME_APPLICATION_XML        = "application/xml"

    public static final String UTF8                        = "UTF-8"

    public static final String VALID_REQUEST               = "VALID_REQUEST"
    public static final String OBJECT_NOT_FOUND            = "OBJECT_NOT_FOUND"           // -> new for ApiBox
    public static final String OBJECT_STATUS_DELETED       = "OBJECT_STATUS_DELETED"      // -> new for ApiBox

}
