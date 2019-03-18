<%@ page import="com.k_int.kbplus.RefdataCategory;" %>
<laser:serviceInjection/>

<semui:form>
    <g:form action="emptySurvey" controller="survey" method="post" class="ui form">

        <g:hiddenField name="steps" value="${params.steps+1}"/>

        <div class="field required">
            <label>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</label>
            <input type="text" name="newEmptySurveyName" placeholder="" required/>
        </div>

        <div class="two fields">
            <semui:datepicker label="surveyInfo.startDate.label" name="valid_from" value=""/>

            <semui:datepicker label="surveyInfo.endDate.label" name="valid_to" value=""/>
        </div>


        <div class="field required">
            <label>${message(code: 'surveyInfo.type.label')}</label>
            <laser:select class="ui dropdown" name="surveyType"
                          from="${RefdataCategory.getAllRefdataValues('Survey Type')}"
                          optionKey="id"
                          optionValue="value"
                          value="${params.type}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}" required="true"/>
        </div>
        <br/>

        <input type="submit" class="ui button"
               value="${message(code: 'emptySurvey.nextStep', default: 'Next Step')}"/>
    </g:form>
</semui:form>

<r:script language="JavaScript">

                    $('.newLicence')
                            .form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            newEmptySubName: {
                                identifier  : 'newEmptySubName',
                                rules: [
                                    {
                                        type   : 'empty',
                                        prompt : '{name} <g:message code="validation.needsToBeFilledOut"
                                                                    default=" muss ausgefüllt werden"/>'
                                    }
                                ]
                            }
                         }
                    });
</r:script>