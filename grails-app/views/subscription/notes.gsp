<laser:htmlStart message="default.notes.label" />

        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
                <laser:render template="actions" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon>
            <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
                <laser:render template="iconSubscriptionIsChild"/>
            </g:if>
            <ui:xEditable owner="${subscription}" field="name" />
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="notes" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


        <laser:render template="nav" />

        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <laser:render template="message" />
        </g:if>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/notes/table" model="${[instance: subscription, redirect: 'notes']}"/>

<laser:htmlEnd />
