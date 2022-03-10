package de.laser.helper

import javax.servlet.http.HttpServletRequest

class AjaxUtils {

    static boolean isXHR(HttpServletRequest request) {
        request.xhr
    }

    static boolean isAjax(HttpServletRequest request) {
        request.getRequestURI().startsWith('/ajax')
    }
}
