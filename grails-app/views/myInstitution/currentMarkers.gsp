<%@ page import="de.laser.Org;de.laser.Package;de.laser.Platform" %>

<laser:htmlStart message="menu.my.markers" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.markers" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.markers" type="Marker" floated="true" />

<g:each in="${myMarkerObjList}" var="objCat">
    <g:if test="${objCat.value}">
        <table class="ui celled table la-js-responsive-table la-table">
            <thead>
                <tr>
                    <th class="one wide">${message(code:'sidewide.number')}</th>
                    <th class="nine wide"></th>
                    <th class="three wide"></th>
                    <th class="one wide center aligned"><ui:myXIcon /></th>
                    <th class="two wide">${message(code:'default.actions.label')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${objCat.value}" var="obj" status="oi">
                    <tr>
                        <td>${oi+1}</td>

                        <g:if test="${obj instanceof Org}">
                            <td>
                                <g:link controller="org" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td>
                                <i class="icon grey university"></i>
                                ${message(code:'default.provider.label')} /
                                ${message(code:'default.agency.label')}
                            </td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentProviderIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.providers')}" color="yellow"/>
                                </g:if>
                            </td>
                        </g:if>
                        <g:elseif test="${obj instanceof Package}">
                            <td>
                                <g:link controller="package" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td>
                                <i class="icon grey gift"></i> ${message(code:'package.label')}
                            </td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentPackageIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.packages')}" color="yellow"/>
                                </g:if>
                            </td>
                        </g:elseif>
                        <g:elseif test="${obj instanceof Platform}">
                            <td>
                                <g:link controller="platform" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td>
                                <i class="icon grey cloud"></i> ${message(code:'platform.label')}
                            </td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentPlatformIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.platforms')}" color="yellow"/>
                                </g:if>
                            </td>
                        </g:elseif>

                        <td></td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
</g:each>
    
<laser:htmlEnd />
