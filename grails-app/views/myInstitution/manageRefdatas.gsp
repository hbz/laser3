<%@ page import="de.laser.ui.Icon; de.laser.I10nTranslation; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="refdata.plural" />

    <ui:breadcrumbs>
        <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
        <ui:crumb message="menu.institutions.manage_props" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.institutions.manage_props" type="${contextService.getOrg().getCustomerType()}" />

    <laser:render template="nav" />

    <div class="ui styled fluid accordion">
        <g:each in="${rdCategories}" var="rdc">
            <div class="title">
                <i class="dropdown icon"></i> ${rdc.getI10n('desc')}
            </div>
            <div class="content">
                <table class="ui celled la-js-responsive-table la-table very compact table">
                    <thead>
                    <tr>
                        <th></th>
                        <th>DE</th>
                        <th>EN</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td> </td>
                        <td><strong>${rdc.getI10n('desc', 'de')}</strong></td>
                        <td><strong>${rdc.getI10n('desc', 'en')}</strong></td>
                    </tr>

                    <g:each in="${RefdataCategory.getAllRefdataValues(rdc.desc)}" var="rdv">
                        <tr>
                            <td>
                                <g:if test="${!rdv.isHardData}">
                                    <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.hardData.not.tooltip')}">
                                        <i class="${Icon.PROP.HARDDATA_NOT}"></i>
                                    </span>
                                </g:if>
                            </td>
                            <td>${rdv.getI10n('value', 'de')}</td>
                            <td>${rdv.getI10n('value', 'en')}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:each>
    </div>

<laser:htmlEnd />
