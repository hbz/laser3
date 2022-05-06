<%@ page import="de.laser.helper.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDConstants;de.laser.Org;de.laser.I10nTranslation; java.text.SimpleDateFormat; de.laser.storage.RDStore" %>
<laser:serviceInjection />
<%
    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
    Date startOfYear = DateUtils.getSDF_ymd().parse(Calendar.getInstance().get(Calendar.YEAR)+'-01-01')
    Set<RefdataValue> preloadGroups
    switch(formId) {
        case 'newForUni': preloadGroups = [RDStore.READER_NUMBER_STUDENTS, RDStore.READER_NUMBER_SCIENTIFIC_STAFF, RDStore.READER_NUMBER_FTE]
            break
        case 'newForPublic': preloadGroups = [RDStore.READER_NUMBER_PEOPLE]
            break
        case 'newForState': preloadGroups = [RDStore.READER_NUMBER_USER]
            break
        case 'newForResearchInstitute': preloadGroups = [RDStore.READER_NUMBER_FTE]
            break
        case 'newForScientificLibrary': preloadGroups = [RDStore.READER_NUMBER_FTE, RDStore.READER_NUMBER_USER]
            break
    }
    if(formId.contains("newForSemester"))
        preloadGroups = [RDStore.READER_NUMBER_STUDENTS, RDStore.READER_NUMBER_SCIENTIFIC_STAFF, RDStore.READER_NUMBER_FTE]
    List<Map<String,Object>> referenceGroups = []
    if(preloadGroups) {
        preloadGroups.each { RefdataValue group ->
            referenceGroups << [id:group.id,value:group.getI10n("value"),expl:group.getI10n("expl")]
        }
    }
%>
<semui:modal id="${formId}" text="${title}" isEditModal="${!formId.contains('new') ? formId : null}">

    <g:form class="ui form create_number" url="[controller: 'readerNumber', action: !formId.contains('new') ? 'edit' : 'create', id: numbersInstance ? numbersInstance.id : null]" method="POST">
        <g:hiddenField name="orgid" value="${params.id}"/>
        <g:hiddenField name="tableA" value="${params.tableA}"/>
        <g:hiddenField name="tableB" value="${params.tableB}"/>
        <g:hiddenField name="sort" value="${params.sort}"/>
        <g:hiddenField name="order" value="${params.order}"/>
            <div class="three fields">
                <div class="field ten wide">
                    <label for="referenceGroup">
                        <g:message code="readerNumber.referenceGroup.label" />
                    </label>
                    <semui:dropdownWithI18nExplanations name="referenceGroup" class="referenceGroup search"
                                                        from="${referenceGroups}"
                                                        optionKey="id" optionValue="value" optionExpl="expl" noSelection="${message(code:'default.select.choose.label')}"
                                                        value="${numbersInstance?.referenceGroup}"
                    />
                </div>
                <div class="field four wide">
                    <g:if test="${withSemester}">
                        <label for="semester"><g:message code="readerNumber.semester.label"/></label>
                        <laser:select class="ui selection dropdown la-full-width" label="readerNumber.semester.label" id="semester" name="semester"
                                      from="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SEMESTER)}"
                                      optionKey="id" optionValue="value" required=""
                                      value="${semester}"/>
                    </g:if>
                    <g:elseif test="${withDueDate}">
                        <semui:datepicker label="readerNumber.dueDate.label" id="dueDate" name="dueDate"
                                          placeholder="default.date.label" value="${numbersInstance?.dueDate ?: sdf.format(startOfYear)}" required=""
                                          bean="${numbersInstance}"/>
                    </g:elseif>
                </div>
                <div class="field two wide required">
                    <label for="value">
                        <g:message code="readerNumber.number.label"/>
                    </label>
                    <input type="number" id="value" name="value" step=".01" value="${numbersInstance?.value}"/>
                </div>
            </div>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
            $(".referenceGroup").dropdown({
                clearable: true
            });

            $('.create_number').form({
                on: 'blur',
                inline: true,
                fields: {
                    value: {
                        identifier  : 'value',
                        rules: [
                            {
                                type   : 'empty',
                                prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                            }
                        ]
                    },
                    dueDate: {
                        identifier : 'dueDate',
                        rules: [
                            {
                                type : 'regExp',
                                value: /\d{2}\.\d{2}\.\d{4}/,
                                prompt: '<g:message code="validation.validDate"/>'
                            }
                        ]
                    },
                    semester: {
                        identifier: 'value',
                        rules: [
                            {
                                type   : 'empty',
                                prompt : '{name} <g:message code="validation.needsToBeFilledOut" />'
                            }
                        ]
                    }
                 }
            });
    </laser:script>

</semui:modal>