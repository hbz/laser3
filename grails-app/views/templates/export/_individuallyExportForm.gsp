<div class="scrolling content">
    <div class="ui form">
        <div class="field">
            <label><g:message code="exportClickMe.fieldsToExport"/></label>
        </div>

        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <g:each in="${formFields}" var="fields" status="i">
                <a class="${("tab-${i}" == "tab-0") ? 'active' : ''}  item"
                   data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label}</a>
            </g:each>
        </div>

        <g:each in="${formFields}" var="fields" status="i">
            <div class="ui bottom attached ${("tab-${i}" == "tab-0") ? 'active' : ''} tab segment"
                 data-tab="tab-${i}">
                <div class="ui form">
                <div class="fields">
                    <g:each in="${fields.value.fields}" var="field" status="fc">

                        <g:if test="${field.key == 'costItemsElements'}">
                            <g:set var="newFieldsDiv" value="${false}"/>
                            </div>

                            <label for="${field.key}"><g:message
                                    code="exportClickMe.costItemsElements.selectCostItems"/></label>
                            <div class="fields">
                            <g:each in="${field.value}" var="costItemsElements" status="gc">
                                <div class="wide eight field">

                                    <div class="ui checkbox ">
                                        <input type="checkbox" name="iex:${costItemsElements.key}"
                                               id="iex:${costItemsElements.key}" ${costItemsElements.value.defaultChecked ? 'checked="checked"' : ''}>
                                        <label for="iex:${costItemsElements.key}">${costItemsElements.value.message ? message(code: costItemsElements.value.message) : costItemsElements.value.label}</label>
                                    </div>

                                </div><!-- .field -->

                                <g:if test="${gc % 2 == 1}">
                                    <g:set var="newFieldsDiv" value="${true}"/>
                                    </div>
                                    <div class="fields">
                                </g:if>
                                <g:else>
                                    <g:set var="newFieldsDiv" value="${false}"/>
                                </g:else>
                            </g:each>
                            <g:if test="${!newFieldsDiv}">
                                </div>
                                <div class="ui divider"></div>
                                <div class="fields">
                            </g:if>
                            <g:else>
                                <div class="ui divider"></div>
                            </g:else>

                        </g:if>
                        <g:else>
                            <div class="wide eight field">
                                <div class="ui checkbox">
                                    <input type="checkbox" name="iex:${field.key}"
                                           id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                                    <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                                </div>
                            </div><!-- .field -->
                        </g:else>



                        <g:if test="${fc % 2 == 1}">
                            </div>
                            <div class="fields">
                        </g:if>

                    </g:each>
                </div>
                </div>
            </div>
        </g:each>

        <br/>

        <div class="fields">

            <div class="wide eight field">
                <label for="filename"><g:message code="default.fileName.label"/></label>
                <input name="filename" id="filename" value="${exportFileName}"/>
            </div>

            <div class="wide eight field">
                <br>
                <button class="ui button positive right floated"
                        id="export-as-excel">${exportButtonName ?: 'Export Excel'}</button>
            </div>

        </div><!-- .fields -->
    </div><!-- .form -->
</div>