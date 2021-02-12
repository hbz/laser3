<g:render template="/templates/filter/javascript"/>
<semui:filter showFilterButton="true">
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>

        <div class="two fields">
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
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
            %{--<div class="field">
                <label for="provider">${message(code: 'package.index.filter.resource.typ')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'package.index.filter.resource.typ.tooltipp')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="resourceTyp" name="resourceTyp"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.resourceTyp}"/>
                </div>
            </div>--}%

            <div class="field">
                <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}
                </label>

                <g:select class="ui fluid dropdown" name="curatoryGroup"
                              from="${curatoryGroups.sort{it.name.toLowerCase()}}"
                              optionKey="name"
                              optionValue="name"
                              value="${params.curatoryGroup}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

        </div>


        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}"
               class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
            <button type="submit" name="search" value="yes"
                    class="ui secondary button">${message(code: 'default.button.filter.label')}</button>
        </div>
    </g:form>
</semui:filter>