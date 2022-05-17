package de.laser.http

import javax.servlet.http.HttpServletRequest

class AjaxHelper {

    static boolean isXHR(HttpServletRequest request) {
        request.xhr
    }

    static boolean isAjax(HttpServletRequest request) {
        request.getRequestURI().startsWith('/ajax')
    }
}
