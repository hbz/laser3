<%@ page import="de.laser.storage.RDStore; de.laser.convenience.Marker; de.laser.Org;de.laser.Package;de.laser.Platform" %>

<laser:htmlStart message="menu.my.markers" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.markers" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.markers" type="Marker" floated="true" />

<ui:filter simple="true">
    <form id="markerFilterForm" class="ui form">
        <div class="two fields">
            <div class="field">
                <label>${message(code:'marker.label')}</label>
                <g:select class="ui dropdown la-not-clearable" name="filterMarkerType"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${[Marker.TYPE.WEKB_CHANGES]}"
                           value="${filterMarkerType}"
                           optionValue="${{message(code: 'marker.' + it.value)}}"
                           optionKey="${{it.value}}" />

            </div>
            <div class="field la-field-right-aligned">
%{--                <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: 'reset']}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</g:link>--}%
                <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </form>
</ui:filter>

<g:each in="${myMarkedObjects}" var="objCat">
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
                                <g:each in="${obj.orgType}" var="ot">
                                    ${ot.getI10n('value')}
                                </g:each>
                            </td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentProviderIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.providers')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:markerSwitch org="${obj}"/>
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
                            <td>
                                <ui:markerSwitch package="${obj}"/>
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
                            <td>
                                <ui:markerSwitch platform="${obj}"/>
                            </td>
                        </g:elseif>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
</g:each>
    
<laser:htmlEnd />
