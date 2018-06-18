<%@ page import="com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:each in="${roleLinks}" var="role">
    <g:if test="${role.org}">
        <g:set var="cssId" value="prsLinksModal-${role.org.id}" />

        <table class="ui la-selectable table">
            <colgroup>
                <col width="130" />
                <col width="300" />
                <col width="130"/>
                <col width="200"/>
            </colgroup>
            <tr>
                <th scope="row">${role?.roleType?.getI10n("value")}</th>
                <td>
                    <g:link controller="Organisations" action="show" id="${role.org.id}">${role?.org?.name}</g:link>
                    <g:if test="${editmode}">
                </td>
                <td>
                        <div class="ui mini icon buttons">
                            <g:link class="ui mini icon button" controller="ajax" action="delOrgRole" id="${role.id}" onclick="return confirm(${message(code:'template.orgLinks.delete.warn')})" >
                                <i class="times icon red"></i>${message(code:'default.button.unlink.label')}
                            </g:link>
                        </div>
                </td>
                <td>
                    <div class="ui mini icon buttons">
                        <button class="ui button" data-semui="modal" href="#${cssId}" style="margin-left:1rem">
                                    <i class="address plus icon"></i> ${modalPrsLinkRole.getI10n("value")} hinzufügen
                        </button>
                    </div>

                    </g:if>
                </td>
            </tr>
            <g:if test="${  Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                            Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg()) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())
            }">
            <tr>
                <td></td>
                <td>
                    <%-- public --%>
                    <g:if test="${  Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                            Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)             }">
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
                    </g:if>
                    <%-- private --%>
                    <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg()) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())
                    }">
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
                    </g:if>
                </td>
                <td></td>
                <td></td>
            </tr>
            </g:if>
        </table>

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
    <br />
    <dl>
        <dt></dt>
        <dd>
            <a class="ui button" data-semui="modal" href="#${tmplmodalID}">${tmplButtonText}</a>
        </dd>
    </dl>
</g:if>
