package de.laser.helper

import javax.servlet.http.HttpServletRequest

class AjaxUtils {

    static boolean isAjaxCall(HttpServletRequest request) {
        request.getRequestURI().startsWith('/ajax')
    }
}
