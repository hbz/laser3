<%@ page import="com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:each in="${roleLinks}" var="role">
    <g:if test="${role.org}">
        <g:set var="cssId" value="prsLinksModal-${role.org.id}" />

        <dl>
             <dt><label class="control-label">${role?.roleType?.getI10n("value")}</label></dt>
            <dd>
                <g:link controller="Organisations" action="show" id="${role.org.id}">${role?.org?.name}</g:link>
                <g:if test="${editmode}">
                    <div class="ui mini icon buttons">
                        <g:link class="ui button" controller="ajax" action="delOrgRole" id="${role.id}" onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                            <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                        </g:link>

                        &nbsp;

                        <button class="ui button" data-semui="modal" href="#${cssId}" style="margin-left:1rem">
                            <i class="address plus icon"></i> ${modalPrsLinkRole.getI10n("value")} hinzufügen
                        </button>
                    </div>
                </g:if>
                <%-- public --%>
                <div class="ui list">
                    <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
                        <div class="item">
                            <i class="address card icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${func.id}">${func}</g:link>
                                (${(RefdataValue.findByValue('General contact person')).getI10n('value')})
                            </div>
                        </div>
                    </g:each>
                    <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)}" var="resp">
                        <div class="item">
                            <i class="address card icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>
                                (${(RefdataValue.findByValue(roleRespValue)).getI10n('value')})

                                <g:if test="${editmode}">
                                    <g:set var="prsRole" value="${com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
                                    <div class="ui mini icon buttons">
                                        <g:link class="ui button" controller="ajax" action="delPrsRole" id="${prsRole?.id}" onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                                            <i class="times icon red"></i>
                                        </g:link>
                                    </div>
                                </g:if>
                            </div>
                        </div>
                    </g:each>
                </div>
                <%-- private --%>
                <div class="ui list">
                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg())}" var="func">
                        <div class="item">
                            <i class="address card outline icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${func.id}">${func}</g:link>
                                (${(RefdataValue.findByValue('General contact person')).getI10n('value')})
                            </div>
                        </div>
                    </g:each>
                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())}" var="resp">
                        <div class="item">
                            <i class="address card outline icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>
                                (${(RefdataValue.findByValue(roleRespValue)).getI10n('value')})

                                <g:if test="${editmode}">
                                    <g:set var="prsRole" value="${com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
                                    <div class="ui mini icon buttons">
                                        <g:link class="ui button" controller="ajax" action="delPrsRole" id="${prsRole?.id}" onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                                            <i class="times icon red"></i>
                                        </g:link>
                                    </div>
                                </g:if>
                            </div>
                        </div>
                    </g:each>
                </div>

            </dd>
        </dl>

        <g:render template="/templates/links/orgLinksAsListAddPrsModal"
                  model="['cssId': cssId,
                          'orgRole': role,
                          'roleObject': roleObject,
                          parent: roleObject.class.name + ':' + roleObject.id,
                          role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id
                  ]"/>
    </g:if>
</g:each>

<g:if test="${editmode}">
    <dl>
        <dt></dt>
        <dd>
            <a class="ui button" data-semui="modal" href="#${tmplmodalID}">${tmplButtonText}</a>
        </dd>
    </dl>
</g:if>
