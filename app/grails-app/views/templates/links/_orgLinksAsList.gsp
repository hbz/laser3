<%@ page import="com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:each in="${roleLinks}" var="role">
    <g:if test="${role.org}">
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

                        <a class="ui button" data-semui="modal" href="#prsLinksModal">
                            ${message(code: 'default.add.label', args: [message(code: 'person.label', default: 'Person')])}
                        </a>
                    </div>
                </g:if>
                <%-- public --%>
                <div class="ui list">
                    <g:each in="${Person.getByOrgFunc(role.org, 'General contact person')}" var="func">
                        <div class="item">
                            <i class="address card icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${func.id}">${func}</g:link>,
                                ${(RefdataValue.findByValue('General contact person')).getI10n('value')}
                            </div>
                        </div>
                    </g:each>
                    <g:each in="${Person.getByOrgObjectResp(role.org, roleObject, roleRespValue)}" var="resp">
                        <div class="item">
                            <i class="address card icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>,
                                ${(RefdataValue.findByValue(roleRespValue)).getI10n('value')}
                            </div>
                        </div>
                    </g:each>
                </div>
                <%-- private --%>
                <div class="ui list">
                    <g:each in="${Person.getByOrgFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg())}" var="func">
                        <div class="item">
                            <i class="address card outline icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${func.id}">${func}</g:link>,
                                ${(RefdataValue.findByValue('General contact person')).getI10n('value')}
                            </div>
                        </div>
                    </g:each>
                    <g:each in="${Person.getByOrgObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())}" var="resp">
                        <div class="item">
                            <i class="address card outline icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>,
                                ${(RefdataValue.findByValue(roleRespValue)).getI10n('value')}
                            </div>
                        </div>
                    </g:each>
                </div>

            </dd>
        </dl>

    </g:if>
</g:each>

<g:if test="${editmode}">
    <dl>
        <dt></dt>
        <dd>
            <a class="ui button" data-semui="modal" href="#osel_add_modal">${tmplButtonText}</a>
        </dd>
    </dl>
</g:if>
