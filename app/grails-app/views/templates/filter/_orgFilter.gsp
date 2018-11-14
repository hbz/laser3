<%@ page import="com.k_int.kbplus.*" %>


<g:each in="${tmplConfigShow}" var="row">

    <g:set var="numberOfFields" value="${row.size()}" />

    <% if (row.contains('property')) { numberOfFields++ } %>

    <g:if test="${numberOfFields > 1}">
        <div class="${numberOfFields==4 ? 'four fields' : numberOfFields==3 ? 'three fields' : numberOfFields==2 ? 'two fields' : ''}">
    </g:if>

        <g:each in="${row}" var="field" status="fieldCounter">

            <g:if test="${field.equalsIgnoreCase('name')}">
                <div class="field">
                    <label>${message(code: 'org.search.contains')}</label>
                    <input type="text" name="orgNameContains" value="${params.orgNameContains}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('property')}">
                <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('type')}">
                <div class="field">
                    <label>${message(code: 'org.orgRoleType.label')}</label>
                    <g:if test="${orgRoleTypes == null || orgRoleTypes.isEmpty()}">
                        <g:set var="orgRoleTypes" value="${RefdataCategory.getAllRefdataValues('OrgRoleType')}"/>
                    </g:if>
                    <laser:select class="ui dropdown" name="orgRoleType"
                                  from="${orgRoleTypes}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgRoleType}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('role')}">
                <div class="field">
                    <label>${message(code: 'org.orgRole.label')}</label>
                    <g:if test="${orgRoles == null || orgRoles.isEmpty()}">
                        <g:set var="orgRoles" value="${RefdataCategory.getAllRefdataValues('Organisational Role')}"/>
                    </g:if>
                    <laser:select class="ui dropdown" name="orgRole"
                                  from="${orgRoles}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgRole}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('sector')}">
                <div class="field">
                    <label>${message(code: 'org.sector.label')}</label>
                    <laser:select class="ui dropdown" name="orgSector"
                                  from="${RefdataCategory.getAllRefdataValues('OrgSector')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgSector}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('federalState')}">
                <div class="field">
                    <label>${message(code: 'org.federalState.label')}</label>
                    <laser:select class="ui dropdown" name="federalState"
                                  from="${RefdataCategory.getAllRefdataValues('Federal State')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.federalState}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('libraryNetwork')}">
                <div class="field">
                    <label>${message(code: 'org.libraryNetwork.label')}</label>
                    <laser:select class="ui dropdown" name="libraryNetwork"
                                  from="${RefdataCategory.getAllRefdataValues('Library Network')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.libraryNetwork}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('libraryType')}">
                <div class="field">
                    <label>${message(code: 'org.libraryType.label')}</label>
                    <laser:select class="ui dropdown" name="libraryType"
                                  from="${RefdataCategory.getAllRefdataValues('Library Type')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.libraryType}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('country')}">
                <div class="field">
                    <label>${message(code: 'org.country.label')}</label>
                    <laser:select class="ui dropdown" name="country"
                                  from="${RefdataCategory.getAllRefdataValues('Country')}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.country}"
                                  noSelection="${['':message(code:'default.select.choose.label', default:'Please Choose...')]}"/>
                </div>
            </g:if>

        </g:each>

    <g:if test="${row.size() > 1}">
        </div><!-- .fields -->
    </g:if>
</g:each>

<g:set var="allFields" value="${tmplConfigShow.flatten()}" />

<g:if test="${! allFields.contains('orgRoleType')}">
    <input type="hidden" name="orgRoleType" value="${params.orgRoleType}" />
</g:if>

<g:if test="${! allFields.contains('orgRoles')}">
    <input type="hidden" name="orgRoles" value="${params.orgRoles}" />
</g:if>

<g:if test="${! allFields.contains('orgSector')}">
    <input type="hidden" name="orgSector" value="${params.orgSector}" />
</g:if>

<g:if test="${! allFields.contains('libraryNetwork')}">
    <input type="hidden" name="federalState" value="${params.federalState}" />
</g:if>

<g:if test="${! allFields.contains('libraryNetwork')}">
    <input type="hidden" name="libraryNetwork" value="${params.libraryNetwork}" />
</g:if>

<g:if test="${! allFields.contains('libraryType')}">
    <input type="hidden" name="libraryType" value="${params.libraryType}" />
</g:if>

<g:if test="${! allFields.contains('country')}">
    <input type="hidden" name="country" value="${params.country}" />
</g:if>


<div class="field la-field-right-aligned">

        <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>

        <g:if test="${tmplConfigFormFilter}">
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui secondary button" onclick="formFilter(event)" />
            <r:script>
                formFilter = function(e) {
                    e.preventDefault()

                    var form = $(e.target).parents('form')
                    $(form).find(':input').filter(function () {
                        return !this.value
                    }).attr('disabled', 'disabled')

                    form.submit()
                }
            </r:script>
        </g:if>
        <g:else>
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui secondary button"/>
        </g:else>

</div>

