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
    data-confirm-term-what-detail="${prop.type.name}"
    data-confirm-term-how="delete"
    controller="ajax" action="deletePrivateProperty"
    params='[propClass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]' id="${prop.id}"
    onComplete="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id}), c3po.loadJsAfterAjax()"
    update="${custom_props_div}" >
</g:remoteLink>
```
