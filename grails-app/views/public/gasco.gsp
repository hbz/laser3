<%@ page import="de.laser.Org; de.laser.PersonRole; de.laser.OrgRole; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Contact; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'gasco.title')}</title>
</head>

<body>

<h1 class="ui icon header la-clear-before">
    ${message(code: 'menu.public.gasco_monitor')}: ${message(code: 'gasco.licenceSearch')}
</h1>
    <div class="ui grid">
        <div class="eleven wide column">
            <div class="ui la-search segment">
                <g:form action="gasco" controller="public" method="get" class="ui small form">

                    <div class="field">
                        <label for="search">${message(code: 'default.search.label')}</label>

                        <div class="ui input">
                            <input type="text" id="search" name="q"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.q}"/>
                        </div>
                    </div>
                    <div class="field">
                        <fieldset id="subscritionKind">
                            <legend>${message(code: 'myinst.currentSubscriptions.subscription_kind')}</legend>
                            <div class="inline fields la-filter-inline">

                                <%
                                    List subkinds = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)
                                    subkinds -= RDStore.SUBSCRIPTION_KIND_LOCAL
                                %>

                                <g:each in="${subkinds}" var="subKind">

                                        <g:if test="${subKind.value == RDStore.SUBSCRIPTION_KIND_NATIONAL.value}">
                                            <div class="inline field js-nationallicence">
                                        </g:if>
                                        <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_ALLIANCE.value}">
                                            <div class="inline field js-alliancelicence">
                                        </g:elseif>
                                        <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_CONSORTIAL.value}">
                                            <div class="inline field js-consortiallicence">
                                        </g:elseif>
                                        <g:else>
                                            <div class="inline field">
                                        </g:else>

                                            <div class="ui checkbox">
                                                <label for="checkSubType-${subKind.id}">${subKind.getI10n('value')}</label>
                                                <input id="checkSubType-${subKind.id}" name="subKinds" type="checkbox" value="${subKind.id}"
                                                    <g:if test="${params.list('subKinds').contains(subKind.id.toString())}"> checked="" </g:if>
                                                    <g:if test="${initQuery}"> checked="" </g:if>
                                                       tabindex="0">

                                            </div>
                                        </div>

                                </g:each>

                            </div>
                        </fieldset>
                    </div>
                    <div class="field" id="js-consotial-authority">
                        <fieldset>
                            <label for="consortia" id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</label>

                            <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                optionKey="${{ Org.class.name + ':' + it.id }}"
                                optionValue="${{ it.getName() }}"
                                name="consortia"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                value="${params.consortia}"/>
                        </fieldset>

                    </div>

                    <div class="field la-field-right-aligned ">
                        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label')}">
                    </div>

                </g:form>
            </div>
        </div>
        <div class="five wide column">
            <img class="ui fluid image" alt="Logo GASCO" src="${resource(dir: 'images', file: 'gasco/GASCO-Logo-2_klein.jpg')}"/>
        </div>
    </div>
    <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.toggleFilterPart = function () {
                if ($('.js-consortiallicence input').prop('checked')) {
                    $('#js-consotial-authority .dropdown').removeClass('disabled')
                    $('#js-consotial-authority select').removeAttr('disabled')
                } else {
                    $('#js-consotial-authority .dropdown').addClass('disabled')
                    $('#js-consotial-authority select').attr('disabled', 'disabled')
                }
            }
            JSPC.app.toggleTableHeading = function () {
                if ($('.js-nationallicence input').prop('checked') || $('.js-alliancelicence input').prop('checked')) {
                    $('#js-negotiator-header').show()
                    $('#js-consortium-header').hide()
                } else {
                    $('#js-negotiator-header').hide()
                    $('#js-consortium-header').show()
                }
            }
            JSPC.app.toggleFilterPart()
            $('.js-nationallicence').on('click', JSPC.app.toggleFilterPart)
            $('.js-alliancelicence').on('click', JSPC.app.toggleFilterPart)
            $('.js-consortiallicence').on('click', JSPC.app.toggleFilterPart)
            JSPC.app.toggleTableHeading()
            $('.ui secondary button').on('click', JSPC.app.toggleTableHeading)

    </laser:script>

    <g:if test="${subscriptions}">

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'gasco.table.product')}</th>
            <th>${message(code:'gasco.table.provider')}</th>
            %{--Task ERMS-587: Temporäres Ausblenden dieser Spalte--}%
            %{--<th>${message(code:'gasco.licenceType')}</th>--}%
            <th>
                <div id="js-consortium-header">
                    ${message(code:'gasco.table.consortium')}</div>
                <div id="js-negotiator-header">
                    ${message(code:'gasco.table.negotiator')}</div>
            </th>
        </tr>
        </thead>
        <tbody>
        <g:set var="GASCO_INFORMATION_LINK" value="${PropertyDefinition.getByNameAndDescr('GASCO information link', PropertyDefinition.SUB_PROP)}" />
        <g:set var="GASCO_ANZEIGENAME" value="${PropertyDefinition.getByNameAndDescr('GASCO display name', PropertyDefinition.SUB_PROP)}" />
        <g:set var="GASCO_VERHANDLERNAME" value="${PropertyDefinition.getByNameAndDescr('GASCO negotiator name', PropertyDefinition.SUB_PROP)}" />
            <g:each in="${subscriptions}" var="sub" status="i">
                <g:set var="gasco_infolink" value="${sub.propertySet.find{ it.type == GASCO_INFORMATION_LINK}?.urlValue}" />
                <g:set var="gasco_anzeigename" value="${sub.propertySet.find{ it.type == GASCO_ANZEIGENAME}?.stringValue}" />
                <g:set var="gasco_verhandlername" value="${sub.propertySet.find{ it.type == GASCO_VERHANDLERNAME}?.stringValue}" />
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>

                        <g:if test="${gasco_infolink}">
                            <span  class="la-popup-tooltip la-delay" data-position="right center" data-content="Diese URL aufrufen:  ${gasco_infolink}">
                                <a class="la-break-all" href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                            </span>
                        </g:if>
                        <g:else>
                            ${gasco_anzeigename ?: sub}
                        </g:else>

                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">${subPkg.pkg}</g:link>
                                %{--<g:link controller="public" action="gascoDetails" id="${subPkg.id}">${subPkg.pkg}</g:link>--}%
                            </div>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RDStore.OR_PROVIDER)}" var="role">
                            ${role.org?.name}<br />
                        </g:each>
                    </td>
                    %{--Task ERMS-587: Temporäres Ausblenden dieser Spalte--}%
                    %{--<td>--}%
                        %{--${sub.type?.getI10n('value')}--}%
                    %{--</td>--}%
                    <td>

                    ${gasco_verhandlername ?: sub.getConsortia()?.name}
                    <br />
                    <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, sub.getConsortia())}" var="personRole">
                        <g:set var="person" value="${personRole.getPrs()}" />
                        <g:if test="${person.isPublic}">
                            <div class="ui list">
                                <div class="item">
                                    <div class="content">
                                        <div class="header">
                                            ${person?.getFirst_name()} ${person?.getLast_name()}
                                        </div>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person,
                                                RDStore.CCT_URL
                                        )}" var="prsContact">
                                            <div class="description">
                                                <i class="icon globe la-list-icon"></i>
                                                <span  class="la-popup-tooltip la-delay " data-position="right center" data-content="Diese URL aufrufen:  ${prsContact?.content}">
                                                    <a class="la-break-all" href="${prsContact?.content}" target="_blank">${prsContact?.content}</a>
                                                </span>

                                            </div>
                                        </g:each>
                                        <g:each in ="${Contact.findAllByPrsAndContentType(
                                                person,
                                                RDStore.CCT_EMAIL
                                        )}" var="prsContact">
                                            <div class="description js-copyTriggerParent">
                                                <i class="ui icon envelope outline la-list-icon js-copyTrigger"></i>
                                                <span  class="la-popup-tooltip la-delay" data-position="right center " data-content="Mail senden an ${person?.getFirst_name()} ${person?.getLast_name()}">
                                                    <a class="la-break-all js-copyTopic" href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
                                                </span>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    </g:if>
<style>
.ui.table thead tr:first-child>th {
    top: 48px!important;
}
</style>
<sec:ifAnyGranted roles="ROLE_USER">
    <style>
        .ui.table thead tr:first-child>th {
            top: 90px!important;
        }
    </style>
</sec:ifAnyGranted>
</body>
</html>