<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.storage.RDStore; de.laser.convenience.Marker; de.laser.Org;de.laser.Vendor;de.laser.Package;de.laser.Platform;de.laser.Provider;de.laser.TitleInstancePackagePlatform" %>

<laser:htmlStart message="menu.my.markers" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.markers" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.markers" type="Marker" floated="true" />

<g:set var="markerTypeList" value="${SpringSecurityUtils.ifAnyGranted('ROLE_YODA') ? [Marker.TYPE.WEKB_CHANGES, Marker.TYPE.TIPP_CHANGES, Marker.TYPE.UNKOWN] : [Marker.TYPE.WEKB_CHANGES]}" />

<ui:filter simple="true">
    <form id="markerFilterForm" class="ui form">
        <div class="two fields">
            <div class="field">
                <label>${message(code:'marker.label')}</label>
                <g:select class="ui dropdown la-not-clearable" name="filterMarkerType"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${markerTypeList}"
                           value="${markerType.value}"
                           optionValue="${{message(code: 'marker.' + it.value)}}"
                           optionKey="${{it.value}}" />

            </div>
            <div class="field la-field-right-aligned">
%{--                <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: 'reset']}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</g:link>--}%
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </form>
</ui:filter>

<ui:msg class="info" hideClose="true">
    <i class="ui icon hand point right"></i> <strong>Hinweis:</strong> Ihre persönlichen Beobachtungslisten sind für andere Nutzer Ihrer Einrichtung nicht sichtbar.
</ui:msg>

<g:each in="${myMarkedObjects}" var="objCat">
    <g:if test="${objCat.value}">
        <table class="ui celled table la-js-responsive-table la-table">
            <thead>
                <tr>
                    <th class="one wide">${message(code:'sidewide.number')}</th>
                    <th class="ten wide">
                        <g:if test="${objCat.value.first() instanceof Org}">
                            <i class="${Icon.ORG} grey la-list-icon"></i> ???
                        </g:if>
                        <g:elseif test="${objCat.value.first() instanceof Package}">
                            <i class="${Icon.PACKAGE} grey la-list-icon"></i> ${message(code:'package.label')}
                        </g:elseif>
                        <g:elseif test="${objCat.value.first() instanceof Platform}">
                            <i class="${Icon.PLATFORM} grey la-list-icon"></i> ${message(code:'platform.label')}
                        </g:elseif>
                        <g:if test="${objCat.value.first() instanceof Provider}">
                            <i class="${Icon.PROVIDER} grey la-list-icon"></i> ${message(code:'provider.label')}
                        </g:if>
                        <g:elseif test="${objCat.value.first() instanceof Vendor}">
                            <i class="${Icon.VENDOR} grey la-list-icon"></i> ${message(code:'vendor')}
                        </g:elseif>
                        <g:elseif test="${objCat.value.first() instanceof TitleInstancePackagePlatform}">
                            <i class="${Icon.TIPP} grey la-list-icon"></i> ${message(code:'title')}
                        </g:elseif>
                    </th>
                    <th class="three wide"></th>
                    <th class="one wide center aligned"><ui:myXIcon /></th>
                    <th class="one wide">${message(code:'default.actions.label')}</th>
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
                                <g:each in="${obj.orgType}" var="ot">
                                    ${ot.getI10n('value')}
                                </g:each>
                            </td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentOrgIdList}">
                                    <ui:myXIcon tooltip="???" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction org="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:if>
                        <g:elseif test="${obj instanceof Package}">
                            <td>
                                <g:link controller="package" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td></td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentPackageIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.packages')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction package="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:elseif>
                        <g:elseif test="${obj instanceof Platform}">
                            <td>
                                <g:link controller="platform" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td></td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentPlatformIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.platforms')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction platform="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:elseif>
                        <g:if test="${obj instanceof Provider}">
                            <td>
                                <g:link controller="provider" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td></td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentProviderIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.providers')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction provider="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:if>
                        <g:elseif test="${obj instanceof Vendor}">
                            <td>
                                <g:link controller="vendor" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td></td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentVendorIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.vendors')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction vendor="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:elseif>
                        <g:elseif test="${obj instanceof TitleInstancePackagePlatform}">
                            <td>
                                <g:link controller="tipp" action="show" id="${obj.id}" target="_blank">${obj.name}</g:link>
                            </td>
                            <td></td>
                            <td class="center aligned">
                                <g:if test="${obj.id in myXMap.currentTippIdList}">
                                    <ui:myXIcon tooltip="${message(code: 'menu.my.titles')}" color="yellow"/>
                                </g:if>
                            </td>
                            <td>
                                <ui:cbItemMarkerAction tipp="${obj}" type="${markerType}" simple="true"/>
                            </td>
                        </g:elseif>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:if>
</g:each>
    
<laser:htmlEnd />
