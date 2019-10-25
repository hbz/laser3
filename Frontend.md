## Frontend

### Confirmation Modal to confirm Delete / Unlink / Cancel

#### Set Terms by HTML 5 data attributes
- data-confirm-tokenMsg

#### Set Delete / Unlink / Cancel Mode
- data-confirm-term-how="delete"
- data-confirm-term-how="unlink"
- data-confirm-term-how="share":
- data-confirm-term-how="inherit":
- data-confirm-term-how="ok":
- data-confirm-term-how="concludeBinding":
#### Used in this context
- Button is **Link** calls action to delete / unlink / cancel
- Button is inside a **Form**
- Button has onclick with **ajax** call


#### Examples
#### Setting Message Terms

```
data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform')}"
```


#### Setting Concrete Objects

```
data-confirm-tokenMsg="${message(code: 'confirm.dialog.unlink.accessPoint.platform', args: [accessPoint.name, linkedPlatform.platform.name])}"
```
#### Setting the modes of delete / unlink / share/ inherit / ok /conclude binding

```
data-confirm-term-how="delete"
```
```
data-confirm-term-how="unlink"
```
```
data-confirm-term-how="share":
```
```
data-confirm-term-how="inherit":
```
```
data-confirm-term-how="ok":
```
```
data-confirm-term-how="concludeBinding":
```


#### Use in Link

```
<g:link class="ui icon negative button js-open-confirm-modal"
        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItemElementConfiguration", args: [ciec.costItemElement.getI10n("value")])}"
        data-confirm-term-how="delete"
        controller="costConfiguration" action="deleteCostConfiguration"
        params="${[ciec: ciec.id]}">
    <i class="trash alternate icon"></i>
</g:link>

```

#### Use in Form

```
<g:form controller="person" action="_delete" data-confirm-id="${person?.id?.toString()+ '_form'}">
    <g:hiddenField name="id" value="${person?.id}" />
        <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
            <i class="write icon"></i>
        </g:link>
        <div class="ui icon negative button js-open-confirm-modal"
             data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact.addressbook", args: [person?.toString()])}"
             data-confirm-term-how="delete"
             data-confirm-id="${person?.id}" >
            <i class="trash alternate icon"></i>
        </div>
</g:form>
```
#### Use in Link with AJAX Call

```
<button class="ui mini icon button js-open-confirm-modal-copycat js-no-wait-wheel">
    <i class="la-share slash icon"></i>
</button>
<g:remoteLink class="js-gost la-popup-tooltip la-delay"
              controller="ajax" action="toggleShare"
              params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
              onSuccess=""
              onComplete=""
              update="container-documents"
              data-position="top right"
              data-content="${message(code: 'property.share.tooltip.off')}"

              data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
              data-confirm-term-how="share">
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

