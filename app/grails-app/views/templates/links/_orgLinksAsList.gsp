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
                    </div>
                </g:if>
                <div class="ui list">
                    <g:each in="${Person.getByOrgAndFunction(role.org, 'General contact person')}" var="gcp">
                        <div class="item">
                            <i class="address card icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                ,
                                ${(RefdataValue.findByValue('General contact person')).getI10n('value')}
                            </div>
                        </div>
                    </g:each>
                    <g:each in="${Person.getByOrgAndFunctionFromAddressbook(role.org, 'General contact person', contextService.getOrg())}" var="gcp">
                        <div class="item">
                            <i class="address card outline icon"></i>
                            <div class="content">
                                <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                ,
                                ${(RefdataValue.findByValue('General contact person')).getI10n('value')}
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
