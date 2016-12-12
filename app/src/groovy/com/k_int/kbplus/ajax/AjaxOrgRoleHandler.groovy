package com.k_int.kbplus.ajax

import com.k_int.kbplus.OrgRole

abstract class AjaxOrgRoleHandler {

    /**
     * delegates requesting ajax call to private functions in extending classes
     * 
     * @return
     */
    def ajax() {
    }
    
    def private ajaxOrgRoleList() {
    }
    def private ajaxOrgRoleAdd() {
    }
    def private ajaxOrgRoleDelete() {
    }
}
