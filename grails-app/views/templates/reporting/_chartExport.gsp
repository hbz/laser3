<laser:serviceInjection />

<!-- _chartExport.gsp -->

<semui:modal id="${modalID}" text="${query}" hideSubmitButton="true">

    <div class="ui form">

        <p>${orgList}</p>
        <p>${subList}</p>
        <p>${licList}</p>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        /* .. */
    </laser:script>

</semui:modal>
<!-- _chartExport.gsp -->


