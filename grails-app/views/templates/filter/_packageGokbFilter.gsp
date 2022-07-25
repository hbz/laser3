<ui:filter showFilterButton="true" addFilterJs="true">
    <g:form action="${actionName}" controller="${controllerName}" params="${params}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>

        <div class="two fields">
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.package')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>

            <div class="field">
                <label for="provider">${message(code: 'default.provider.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="provider" name="provider"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.provider}"/>
                </div>
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="ddc">${message(code: 'package.ddc.label')}</label>

                <select name="ddc" id="ddc" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${ddcs}" var="ddc">
                        <option <%=(params.list('ddc')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                value="${ddc.id}">
                            ${ddc.value} - ${ddc.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <g:if test="${curatoryGroups}">
                <div class="field">
                    <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}
                    </label>

                    <g:select class="ui fluid search select dropdown" name="curatoryGroup"
                              from="${curatoryGroups.sort{it.name.toLowerCase()}}"
                              optionKey="name"
                              optionValue="name"
                              value="${params.curatoryGroup}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>
        </div>

        <div class="two fields">
            <div class="field">
                <label>${message(code: 'package.curatoryGroup.type')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="curatoryGroupProvider">${message(code: 'package.curatoryGroup.provider')}</label>
                            <input id="curatoryGroupProvider" name="curatoryGroupProvider" type="checkbox" <g:if test="${params.curatoryGroupProvider}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="curatoryGroupOther">${message(code: 'package.curatoryGroup.other')}</label>
                            <input id="curatoryGroupOther" name="curatoryGroupOther" type="checkbox" <g:if test="${params.curatoryGroupOther}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <button type="submit" name="search" value="yes"
                        class="ui secondary button">${message(code: 'default.button.filter.label')}</button>
            </div>
        </div>
    </g:form>
</ui:filter>