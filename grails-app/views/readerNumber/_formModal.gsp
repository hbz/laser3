<%@ page import="de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDConstants;de.laser.Org;de.laser.I10nTranslation; java.text.SimpleDateFormat; de.laser.storage.RDStore" %>
<laser:serviceInjection />
<%
    Calendar pastYear = GregorianCalendar.getInstance(),
            twoYearsAgo = GregorianCalendar.getInstance(),
            currentYear = GregorianCalendar.getInstance(),
            nextYear = GregorianCalendar.getInstance(),
            dateSwitch = GregorianCalendar.getInstance()
    pastYear.add(Calendar.YEAR, -1)
    twoYearsAgo.add(Calendar.YEAR, -2)
    nextYear.add(Calendar.YEAR, 1)
    SimpleDateFormat sdf = DateUtils.getSDF_yy()
    dateSwitch.set(Calendar.MONTH, 8)
    dateSwitch.set(Calendar.DAY_OF_MONTH, 1)
    RefdataValue pastTerm, currTerm
    if(currentYear < dateSwitch) {
        pastTerm = RefdataValue.getByValueAndCategory("w${sdf.format(twoYearsAgo.getTime())}/${sdf.format(pastYear.getTime())}", RDConstants.SEMESTER)
        currTerm = RefdataValue.getByValueAndCategory("w${sdf.format(pastYear.getTime())}/${sdf.format(currentYear.getTime())}", RDConstants.SEMESTER)
    }
    else {
        pastTerm = RefdataValue.getByValueAndCategory("w${sdf.format(pastYear.getTime())}/${sdf.format(currentYear.getTime())}", RDConstants.SEMESTER)
        currTerm = RefdataValue.getByValueAndCategory("w${sdf.format(currentYear.getTime())}/${sdf.format(nextYear.getTime())}", RDConstants.SEMESTER)
    }
    Set<RefdataValue> preloadGroups
    switch(formId) {
        case 'newForUni': preloadGroups = [RDStore.READER_NUMBER_STUDENTS, RDStore.READER_NUMBER_SCIENTIFIC_STAFF, RDStore.READER_NUMBER_FTE]
            break
        case 'newForPublic': preloadGroups = [RDStore.READER_NUMBER_PEOPLE]
            break
        case 'newForState': preloadGroups = [RDStore.READER_NUMBER_USER]
            break
        case 'newForResearchInstitute': preloadGroups = [RDStore.READER_NUMBER_FTE, RDStore.READER_NUMBER_FTE_TOTAL, RDStore.READER_NUMBER_USER]
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
<ui:modal id="${formId}" text="${title}" isEditModal="${!formId.contains('new') ? formId : null}">

    <g:form class="ui form create_number" url="[controller: 'readerNumber', action: !formId.contains('new') ? 'edit' : 'create', id: numbersInstance ? numbersInstance.id : null]" method="POST">
        <g:hiddenField name="orgid" value="${params.id}"/>
        <g:hiddenField name="tableA" value="${params.tableA}"/>
        <g:hiddenField name="tableB" value="${params.tableB}"/>
        <g:hiddenField name="sort" value="${params.sort}"/>
        <g:hiddenField name="order" value="${params.order}"/>
            <div class="three fields">
                <div class="field ten wide required">
                    <label for="referenceGroup">
                        <g:message code="readerNumber.referenceGroup.label" />
                    </label>
                    <ui:dropdownWithI18nExplanations name="referenceGroup" class="referenceGroup search"
                                                        from="${referenceGroups}"
                                                        optionKey="id" optionValue="value" optionExpl="expl" noSelection="${message(code:'default.select.choose.label')}"
                                                        value="${numbersInstance?.referenceGroup}"
                    />
                </div>
                <div class="field four wide">
                    <%--
                    as of ERMS-6179, time point should be set to the past winter term or year
                    --%>
                    <g:if test="${withSemester}">
                        <label for="semester"><g:message code="readerNumber.semester.label"/></label>
                        <ui:select class="ui selection dropdown la-full-width" label="readerNumber.semester.label" id="semester" name="semester"
                                      from="${[pastTerm, currTerm]}"
                                      optionKey="id" optionValue="value" required=""
                                      value="${currTerm.id}"/>
                    </g:if>
                    <%--
                    <g:elseif test="${withYear}">
                        <ui:datepicker type="year" label="readerNumber.year.label" id="year" name="year"
                                          placeholder="default.date.label" value="${numbersInstance?.year}" required=""
                                          bean="${numbersInstance}"/>
                    </g:elseif>
                    <g:if test="${withSemester}">
                        <label for="semester"><g:message code="readerNumber.semester.label"/></label>
                        <g:hiddenField name="semester" value="${pastTerm.id}"/>
                        ${pastTerm.getI10n('value')}
                    </g:if>
                    --%>
                    <g:elseif test="${withYear}">
                        <label for="year"><g:message code="readerNumber.year.label"/></label>
                        <g:hiddenField name="year" value="${pastYear.get(Calendar.YEAR)}"/>
                        ${pastYear.get(Calendar.YEAR)}
                    </g:elseif>
                </div>
                <div class="field two wide required">
                    <label for="value">
                        <g:message code="readerNumber.number.label"/>
                    </label>
                    <input id="value" name="value" value="${numbersInstance?.value}"/>
                </div>
            </div>

    </g:form>

</ui:modal>