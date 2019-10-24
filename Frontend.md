## Frontend

### Confirmation Modal to confirm Delete / Unlink / Cancel

#### Set Terms by HTML 5 data attributes
- data-confirm-term-what
- data-confirm-term-where
#### Set Concrete Objects
- data-confirm-term-what-detail
- data-confirm-term-where-detail
#### Set Delete / Unlink / Cancel Mode
- data-confirm-term-how="delete"
- data-confirm-term-how="unlink"
- data-confirm-term-how="cancel"
#### Used in this context
- Button is **Link** calls action to delete / unlink / cancel
- Button is inside a **Form**
- Button has onclick with **ajax** call


#### Examples
#### Set Terms

```
data-confirm-term-what="contact"
data-confirm-term-where="addressbook"
```
There need to be a case for that **keyword** "contact" in `app/web-app/js/application.js.gsp`

#### Set Concrete Objects

```
data-confirm-term-what-detail="${person?.toString()}"
```
#### Set Delete / Unlink / Cancel Mode

```
data-confirm-term-how="delete"
```
```
data-confirm-term-how="unlink"
```
```
data-confirm-term-how="cancel"
```
#### Use in Link

##### no content 

```
<g:link class="ui icon negative button js-open-confirm-modal"
        data-confirm-term-what="subscription"
        data-confirm-term-what-detail="${s.name}"
        data-confirm-term-how="delete"
        controller="myInstitution" action="actionCurrentSubscriptions"
        params="${[curInst: institution.id, basesubscription: s.id]}">
    <i class="trash alternate icon"></i>
</g:link>
```
##### with content
```
<g:link  class="item js-open-confirm-modal"
        data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
        data-confirm-term-how="ok"
        action="index"
        id="${params.id}"
        params="${[format:'xml', transformId:transkey, mode: params.mode, filter: params.filter, asAt: params.asAt]}">${transval.name}
</g:link>
```
#### Use in Form

```
<g:form controller="person" action="delete" data-confirm-id="${person?.id?.toString()+ '_form'}">
    <g:hiddenField name="id" value="${person?.id}" />
        <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
            <i class="write icon"></i>
        </g:link>
        <div class="ui icon negative button js-open-confirm-modal"
             data-confirm-term-what="contact"
             data-confirm-term-what-detail="${person?.toString()}"
             data-confirm-term-where="addressbook"
             data-confirm-term-how="delete"
             data-confirm-id="${person?.id}" >
            <i class="trash alternate icon"></i>
        </div>
</g:form>
```
#### Use in Link with AJAX Call

```
<button class="ui icon negative button js-open-confirm-modal-copycat">
    <i class="trash alternate icon"></i>
</button>
<g:remoteLink class="js-gost"
    style="visibility: hidden"
    data-confirm-term-what="property"
    data-confirm-term-what-detail="${prop.type.getI10n('name')}"
    data-confirm-term-how="delete"
    controller="ajax" action="deletePrivateProperty"
    params='[propClass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]' id="${prop.id}"
    onComplete="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id}), c3po.loadJsAfterAjax()"
    update="${custom_props_div}" >
</g:remoteLink>
```

### Function 'deckSaver'

#### Functions

Improve usability at special views while toggle the hide/show button in upper right corner

##### Show Bottons
- build a clone fom icons in icon button
- delete button style hidden
- 'turn on' x-editable

##### Hide Buttons:
- delete clone
- delete tooltip popup from the clone
- set buttons on hidden - add button style hidden
- 'turn off' x-editable

#### How to invoke it

1.) the outer HML Element (around the button) need the class <b>la-js-editmode-container</b>

2.) Icon in the button need to have the class <b>la-js-editmode-icon</b>

3.) the whole area affected from show/hide the buttons need to have the class <b>la-show-context-orgMenu</b>

### Function 'tooltip'

#### Functions

1.) create a random talken like "<b>wcag_f735ng3k7</b>"
- add this token as id to inner div in the tooltip popup
- add this token to the button markup like "<b>aria-labelledby="wcag_f735ng3k7</b>"

2.) Access via keys

#### How to invoke it

1.) Element (Button / Icon) need to have the css class <b>la-popup-tooltip</b>

2.) Element (Button / Icon) need to have the data attribut <b>data-content="lorem ipsum"</b>

