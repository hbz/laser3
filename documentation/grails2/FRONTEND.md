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
<g:link class="ui icon la-modern-button negative button js-open-confirm-modal"
        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItemElementConfiguration", args: [ciec.costItemElement.getI10n("value")])}"
        data-confirm-term-how="delete"
        controller="costConfiguration" action="deleteCostConfiguration"
        params="${[ciec: ciec.id]}"
        role="button">
    <i class="trash alternate outline icon"></i>
</g:link>

```

#### Use in Form

#### Important

Mind the convention of data-confirm-id in form element and inner button element

#### The possible pattern

1.) The form sending trigger element (e.g. button) **HAS NO** value/name. This is set in hidden field via ***Markup*** (***=from you***)

```
<g:form controller="person" action="delete" data-confirm-id="${person?.id?.toString()+ '_form'}">
    <g:hiddenField name="id" value="${person?.id}" />
        <g:link class="ui icon button blue la-modern-button" controller="person" action="show" id="${person?.id}">
            <i aria-hidden="true" class="write icon"></i>
        </g:link>
        <div class="ui icon negative button la-modern-button js-open-confirm-modal"
             data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.contact.addressbook", args: [person?.toString()])}"
             data-confirm-term-how="delete"
             data-confirm-id="${person?.id}" >
            <i class="trash alternate outline icon"></i>
        </div>
</g:form>
```
2.) The form sending trigger element (e.g. button) **HAS** value/name. The system makes a copy in hidden field via ***Javascript***.

```
    <g:form action="processLinkLicenseMembers" method="post" class="ui form" data-confirm-id="deleteLicenses_form">
                <div class="ui buttons">
                    <button class="ui button negative js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteLicenses.button.confirm')}"
                            data-confirm-term-how="ok"
                            name="processOption"
                            data-confirm-id="deleteLicenses"
                            value="unlinkLicense">${message(code: 'subscriptionsManagement.deleteLicenses.button')}</button>
                </div>
    </g:form>
```

#### Form with Ajax Update

```
<div id="${wrapper}">
    <ui:remoteForm   url="[controller: 'ajax', action: 'somethingWithProperties']"
                        name="demo" 
                        class="ui form"
                        data-update="${wrapper}"
                        data-before="alert('before')"
                        data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${wrapper}')">

                            <input type="hidden" name="blah" value="blubb"/>
                            <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
    </ui:remoteForm>
</div>
```

#### Use in Link with AJAX Call

```
<ui:remoteLink class="ui icon negative button la-modern-button js-open-confirm-modal"
                  controller="ajax"
                  action="deletePrivateProperty"
                  params='[propClass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]'
                  id="${prop.id}"
                  data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [prop.type.getI10n('name')])}"
                  data-confirm-term-how="delete"
                  data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})"
                  data-update="${custom_props_div}"
                  role="button"
                  ariaLabel="Löschen"
>
    <i class="trash alternate outline icon"></i>
</ui:remoteLink>

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

