package de.laser.http

import javax.servlet.http.HttpServletRequest

/**
 * Helper class to determine the request type
 */
class AjaxHelper {

    /**
     * Checks whether the current request is an XmlHttpRequest or not
     * @param request the current request
     * @return true if the request is an XHR, false otherwise
     */
    static boolean isXHR(HttpServletRequest request) {
        request.xhr
    }

    /**
     * Currently unused
     * Checks whether the current request is addressing the AjaxController or not
     * @param request the current request
     * @return true if the AjaxController has been addresses, false otherwise
     * @deprecated unnecessary since the listeners are all in the respective controllers
     */
    @Deprecated
    static boolean isAjax(HttpServletRequest request) {
        request.getRequestURI().startsWith('/ajax')
    }
}
