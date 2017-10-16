package de.laser.domain

abstract class Constants {

    static final HTTP_CREATED                = "CREATED" // 200
    static final HTTP_BAD_REQUEST            = "BAD_REQUEST" // 400
    static final HTTP_FORBIDDEN              = "FORBIDDEN" // 403
    static final HTTP_NOT_ACCEPTABLE         = "NOT_ACCEPTABLE" // 406
    static final HTTP_CONFLICT               = "CONFLICT" // 409
    static final HTTP_PRECONDITION_FAILED    = "PRECONDITION_FAILED" // 412
    static final HTTP_INTERNAL_SERVER_ERROR  = "INTERNAL_SERVER_ERROR" // 500
    static final HTTP_NOT_IMPLEMENTED        = "NOT_IMPLEMENTED" // 501

    static final MIME_ALL                    = "*/*"
    static final MIME_TEXT_JSON              = "text/json"
    static final MIME_TEXT_PLAIN             = "text/plain"
    static final MIME_TEXT_XML               = "text/xml"
    static final MIME_APPLICATION_JSON       = "application/json"
    static final MIME_APPLICATION_XML        = "application/xml"

    static final UTF8                        = "UTF-8"
}
