<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<ui:filter>
    <g:form action="${actionName}" controller="${controllerName}" params="${params}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <g:if test="${participant}">
            <input type="hidden" name="participant" value="${participant.id}"/>
            <g:hiddenField name="participant" value="${participant.id}"/>
        </g:if>
        <g:if test="${params.viewTab}">
            <g:hiddenField name="viewTab" value="${params.viewTab}"/>
        </g:if>
        <g:if test="${params.subTab}">
            <g:hiddenField name="subTab" value="${params.subTab}"/>
        </g:if>
        <g:each in="${tmplConfigShow}" var="row">
            <g:set var="numberOfFields" value="${row.size()}"/>
            <g:if test="${numberOfFields > 1}">
                <%
                    String fieldCount
                    switch(numberOfFields) {
                        case 2: fieldCount = 'two fields'
                            break
                        case 3: fieldCount = 'three fields'
                            break
                        case 4: fieldCount = 'four fields'
                            break
                        case 5: fieldCount = 'five fields'
                            break
                    }
                %>
                <div class="${fieldCount}">
            </g:if>
            <g:each in="${row}" var="field" status="fieldCounter">
                <g:if test="${field == 'q'}">
                    <div class="field">
                        <label for="search-title">${message(code: 'default.search.text')}
                            <span data-position="right center" class="la-popup-tooltip"
                                  data-content="${message(code: 'default.search.tooltip.package')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </label>

                        <div class="ui input">
                            <input type="text" id="search-title" name="q"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.q}"/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field == 'singleTitle'}">
                    <div class="field">
                        <label for="search-single-title">${message(code: 'myinst.currentPackages.filter.singleTitle')}</label>

                        <div class="ui input">
                            <input type="text" id="search-single-title" name="singleTitle"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.singleTitle}"/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field == 'pkgStatus'}">
                    <div class="field">
                        <label for="status">${message(code: 'package.status.label')}</label>
                        <select name="pkgStatus" id="pkgStatus" multiple="multiple" class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>
                            <g:set var="excludes" value="${[RDStore.PACKAGE_STATUS_REMOVED]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.PACKAGE_STATUS)-excludes}" var="pkgStatus">
                                <option <%=(params.list('pkgStatus').contains(pkgStatus.value)) ? 'selected="selected"' : ''%> value="${pkgStatus.value}">
                                    ${pkgStatus.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                <g:if test="${field == 'status'}">
                    <div class="field">
                        <label>${message(code: 'myinst.currentPackages.filter.subStatus.label')}</label>
                        <select name="status" id="status" multiple="multiple" class="ui search selection dropdown">
                            <option value="">${message(code:'default.select.choose.label')}</option>
                            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                                <option <%=(Params.getLongList(params, 'status').contains(status.id)) ? 'selected=selected"' : ''%> value="${status.id}">
                                    ${status.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                <g:if test="${field == 'hasPerpetualAccess'}">
                    <div class="field">
                        <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                        <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                                   from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${params.hasPerpetualAccess}"
                                   noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                    </div>
                </g:if>
                <g:if test="${field == 'provider'}">
                    <div class="field">
                        <label for="provider">${message(code: 'provider.label')}</label>
                        <div class="ui input">
                            <input type="text" id="provider" name="provider"
                                   placeholder="${message(code: 'default.search.ph')}"
                                   value="${params.provider}"/>
                        </div>
                    </div>
                </g:if>
                <g:if test="${field == 'ddc'}">
                    <div class="field">
                        <label for="ddc">${message(code: 'package.ddc.label')}</label>
                        <select name="ddc" id="ddc" multiple="" class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>
                            <g:each in="${ddcs}" var="ddc">
                                <option <%=Params.getLongList(params, 'ddc').contains(ddc.id) ? 'selected="selected"' : ''%>
                                        value="${ddc.id}">
                                    ${ddc.value} - ${ddc.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                </g:if>
                <g:if test="${field == 'curatoryGroup'}">
                    <div class="field">
                        <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}</label>
                        <g:select class="ui fluid search select dropdown" name="curatoryGroup"
                                  from="${curatoryGroups}"
                                  optionKey="name"
                                  optionValue="name"
                                  value="${params.curatoryGroup}"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                    </div>
                </g:if>
                <g:if test="${field == 'curatoryGroupType'}">
                    <div class="field">
                        <label for="curatoryGroupType">${message(code: 'package.curatoryGroup.type')}</label>
                        <g:select class="ui fluid search select dropdown" name="curatoryGroupType"
                                  from="${curatoryGroupTypes}"
                                  optionKey="value"
                                  optionValue="name"
                                  value="${params.curatoryGroupType}"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                        />
                    </div>
                </g:if>
                <g:if test="${field == 'automaticUpdates'}">
                    <div class="field">
                        <label for="automaticUpdates">${message(code: 'package.source.automaticUpdates')}</label>
                        <g:select class="ui fluid search select dropdown" name="automaticUpdates"
                                  from="${automaticUpdates}"
                                  optionKey="value"
                                  optionValue="name"
                                  value="${params.automaticUpdates}"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                        />
                    </div>
                </g:if>
                <g:if test="${field == ''}">
                    <div class="field"></div>
                </g:if>
            </g:each>
            <g:if test="${numberOfFields > 1}">
                </div><!-- .fields -->
            </g:if>
        </g:each>
        <div class="field la-field-right-aligned">
            <g:if test="${surveyConfig && participant && controllerName == 'survey'}">
                <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: params.viewTab, tabStat: params.tabStat]}"/>
                <g:set var="participant" value="${participant}"/>
            </g:if>
            <g:elseif test="${surveyConfig}">
                <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, viewTab: params.viewTab, tabStat: params.tabStat]}"/>
            </g:elseif>
            <g:else>
                <g:set var="parame" value="${[tab: params.tab, tabStat: params.tabStat]}"/>
            </g:else>

            <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="${parame}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</g:link>
            <button type="submit" name="search" value="yes"
                    class="${Btn.PRIMARY}">${message(code: 'default.button.filter.label')}</button>
        </div>
    </g:form>
</ui:filter>