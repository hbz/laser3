package com.k_int.kbplus.ajax

import com.k_int.kbplus.OrgRole

abstract class AjaxHandler {

    /**
     * delegates requesting ajax call to private functions in extending classes
     * 
     * @return
     */
    def ajax() {
    }
    
    def private ajaxList() {
    }
    def private ajaxAdd() {
    }
    def private ajaxDelete() {
    }
}
