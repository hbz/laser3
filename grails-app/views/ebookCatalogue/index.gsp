<%@ page import="de.laser.Org; de.laser.PersonRole; de.laser.OrgRole; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Contact; de.laser.helper.RDStore; de.laser.RefdataValue; de.laser.helper.RDConstants;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="eBookCatalogue">
</head>

<body>
    <g:if test="${initQuery}">
        <g:set var="status" value="is-bold" />
    </g:if>
    <g:else>
        <g:if test="${subscriptions.size() == 0}">
            <g:set var="status" value="is-warning" />
        </g:if>
        <g:else>
            <g:set var="status" value="is-success" />
        </g:else>
    </g:else>

<section class="section custom-section-form pt-6 pb-6">
    <div class="container">
        <g:form method="get" autocomplete="off">

            <div class="field">
                <div class="control is-expanded">
                    <input id="q" name="q" value="${params.q}"
                           class="input is-medium" autofocus="autofocus" onfocus="this.select()"
                           type="search" placeholder="Suche nach EBooks .." />
                </div>
            </div>

            <div class="field">
                <label class="label">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <%
                    List<RefdataValue> subkinds = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)
                    subkinds -= RDStore.SUBSCRIPTION_KIND_LOCAL
                %>
                    <div class="field is-grouped">
                        <g:each in="${subkinds}" var="subKind">

                            <g:if test="${subKind.value == 'National Licence'}">
                                <div class="control js-natLic">
                            </g:if>
                            <g:elseif test="${subKind.value == 'Alliance Licence'}">
                                <div class="control js-allLic">
                            </g:elseif>
                            <g:elseif test="${subKind.value == 'Consortial Licence'}">
                                <div class="control js-consLic">
                            </g:elseif>
                            <g:else>
                                <div class="control">
                            </g:else>
                                    <label class="checkbox" for="checkSubType-${subKind.id}">
                                        <input id="checkSubType-${subKind.id}" name="subKinds" type="checkbox" value="${subKind.id}"
                                            <g:if test="${params.list('subKinds').contains(subKind.id.toString())}"> checked="" </g:if>
                                            <g:if test="${initQuery}"> checked="" </g:if>
                                               tabindex="0">
                                        ${subKind.getI10n('value')}
                                    </label>
                                </div>
                        </g:each>
                        </div>
            </div>

            <div class="field" id="js-consAuth">
                <label class="label">${message(code: 'gasco.filter.consortialAuthority')}</label>
                <div class="control is-expanded">
                    <div class="select is-fullwidth">
                        <g:select from="${allConsortia}" id="consortial"
                                  optionKey="${{ Org.class.name + ':' + it.id }}"
                                  optionValue="${{ it.getName() }}"
                                  name="consortia"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                                  value="${params.consortia}"/>
                    </div>
                </div>
            </div>

            <div class="field is-grouped">
                <div class="control">
                    <a href="${request.forwardURI}" class="button is-light is-link">${message(code:'default.button.reset.label')}</a>
                </div>
                <div class="control">
                    <input type="submit" class="button is-info is-link" value="${message(code:'default.button.search.label')}">
                </div>
            </div>

        </g:form>
    </div>
</section>

    <laser:script>

            function toggleFilterPart() {
                console.log('! 1')
                if ($('.js-consLic input').prop('checked')) {
                    $('#js-consAuth select').removeAttr('disabled')
                } else {
                    $('#js-consAuth select').attr('disabled', 'disabled')
                }
            }
            function toggleTableHeading() {
                console.log('! 2')
                if ($('.js-natLic input').prop('checked') || $('.js-allLic input').prop('checked')) {
                    $('#js-negotiator-header').show()
                    $('#js-consortium-header').hide()
                } else {
                    $('#js-negotiator-header').hide()
                    $('#js-consortium-header').show()
                }
            }
            toggleFilterPart()
            $('.js-natLic').on('change', toggleFilterPart)
            $('.js-allLic').on('change', toggleFilterPart)
            $('.js-consLic').on('change', toggleFilterPart)
            toggleTableHeading()
            $('form .button.is-info').on('click', toggleTableHeading)

    </laser:script>

<g:if test="${!initQuery}">

<section class="section custom-section-result">
    <div class="container is-widescreen">

    <g:if test="${subscriptions.size() == 0}">
        <div class="level">
            <div class="level-item"><span class="tag is-medium is-warning">Leider keine Treffer</span></div>
        </div>TitleInstancePackagePlatform
    </g:if>
    <g:else>
        <div class="level mb-6">
            <div class="level-item"><span class="tag is-medium is-success">${subscriptions.size()} Treffer</span></div>
        </div>

        <table class="table is-striped is-fullwidth">
            <thead>
            <tr>
                <th>${message(code:'sidewide.number')}</th>
                <th>${message(code:'gasco.table.product')}</th>
                <th>${message(code:'gasco.table.provider')}</th>
                <th>
                    <div id="js-consortium-header">${message(code:'gasco.table.consortium')}</div>
                    <div id="js-negotiator-header">${message(code:'gasco.table.negotiator')}</div>
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
                                <a class="la-break-all" href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                            </g:if>
                            <g:else>
                                ${gasco_anzeigename ?: sub}
                            </g:else>

                            <g:each in="${sub.packages}" var="subPkg" status="j">
                                <div class="la-flexbox">
                                    <i class="fas fa-gift"></i>
                                    <span class="tag">&#128269;</span>
                                    <g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">${subPkg.pkg}</g:link>
                                </div>
                            </g:each>
                        </td>
                        <td>
                            <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RDStore.OR_PROVIDER)}" var="role">
                                ${role.org?.name}<br />
                            </g:each>
                        </td>
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
                                                    <g:if test="${person && (person.getFirst_name() != 'Kontakt' && person.getLast_name() != 'Kontakt')}">
                                                        ${person.getFirst_name()} ${person.getLast_name()}
                                                    </g:if>
                                                </div>
                                                <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_URL)}" var="prsContact">
                                                    <div class="description">
                                                        <i class="fas fa-globe"></i>
                                                        <span class="tag">&#127760;</span>
                                                        <a class="la-break-all" href="${prsContact?.content}" target="_blank">${prsContact?.content}</a>
                                                    </div>
                                                </g:each>
                                                <g:each in ="${Contact.findAllByPrsAndContentType(person, RDStore.CCT_EMAIL)}" var="prsContact">
                                                    <div class="description js-copyTriggerParent">
                                                        <i class="fas fa-envelope js-copyTrigger"></i>
                                                        <span class="tag">&#128236;</span>
                                                        <a class="la-break-all js-copyTopic" href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
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
    </g:else>

    </div>
</section>

</g:if>

</body>
</html>