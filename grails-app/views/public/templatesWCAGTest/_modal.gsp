<p class="la-clear-before">
    <g:link controller="public"
            id="trigger-lock"
            action="wcagTest"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate icon"></i>
    </g:link>
</p>

<a role="button" id="meinTest" class="ui button icon la-selectable-button" data-semui="modal" href="#sub_edit_link_3535">

    <i class="write icon"></i>

</a>


<div role="dialog" class="ui modal" id="sub_edit_link_3535" aria-label="Modal" >



            <select class="ui dropdown">
                <option value="">Bitte auswählen</option>
                <option value="de.laser.RefdataValue:921§0">... ist Bedingung für</option>
                <option value="de.laser.RefdataValue:921§1">... ist bedingt durch</option>
                <option value="de.laser.RefdataValue:919§0" selected="selected">... ist Nachfolger von</option>
                <option value="de.laser.RefdataValue:919§1">... ist Vorgänger von</option>
                <option value="de.laser.RefdataValue:920§0">... referenziert</option>
                <option value="de.laser.RefdataValue:920§1">... wird referenziert durch</option>
            </select>




            <textarea class="ui" name="linkComment_3535" id="linkComment_3535"></textarea>



    <div class="actions"><a href="#" class="ui button sub_edit_link_3535" onclick="$('#sub_edit_link_3535').modal('hide')">Schließen</a><input type="submit" class="ui button green" name="save" value="Anlegen" onclick="event.preventDefault(); $('#sub_edit_link_3535').find('form').submit()"></div>
</div>
<asset:script type="text/javascript">

</asset:script>