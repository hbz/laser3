<%@ page import="com.k_int.kbplus.RefdataValue" %>
<semui:modal id="ciecModal" message="costItemElementConfiguration.create_new.label">

    <g:form class="ui form" url="${formUrl}" method="POST">

        <div class="ui grid">
            <%
                def signPreset = institution.costConfigurationPreset ? institution.costConfigurationPreset : RefdataValue.getByValueAndCategory('positive','Cost configuration')
                def considerationPreset = institution.considerationPreset ? institution.considerationPreset : RefdataValue.getByValueAndCategory('Yes','YN')
            %>
            <div class="sixteen wide column">
                <label>${message(code:'financials.costItemElement')}</label>
                <g:select name="cie" from="${costItemElements}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}"/>
            </div>
            <div class="eight wide column">
                <label>${message(code:'financials.costItemConfiguration')}</label>
                <g:select name="sign" from="${elementSigns}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}" value="${signPreset}"/>
            </div>
            <div class="eight wide column">
                <label>${message(code:'financials.consider')}</label>
                <g:select name="consider" from="${yn}" optionKey="${{it.class.name+":"+it.id}}" optionValue="${{it.getI10n('value')}}" value="${considerationPreset}"/>
            </div><!-- .row -->
        </div><!-- .grid -->
    </g:form>
</semui:modal>