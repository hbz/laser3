<g:if test="${wrapper == 'altname'}">
    <li data-objId="${ownObj.id}"><ui:xEditable owner="${ownObj}" field="${field}" overwriteEditable="${overwriteEditable}"/>
        <button name="removeAltname" class="ui small icon negative button la-modern-button removeAltname" data-objId="${ownObj.id}"><i class="ui small trash alternate outline icon"></i></button>
    </li>
</g:if>
