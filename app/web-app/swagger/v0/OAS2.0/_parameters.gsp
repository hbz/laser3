
### parameters ###

parameters:
  q:
    in: query
    name: q
    required: true
    type: string
    description: Identifier for this query
#    enum:
#      - id
#      - identifier
#      - impId
#      - uuid
#      - name
#      - namespace:identifier
#      - shortcode
  v:
    in: query
    name: v
    required: true
    type: string
    description: Value for this query
  context:
    in: query
    name: context
    required: false
    type: string
    description: Concrete globalUID, if user has memberships in multiple organisations
  authorization:
    in: header
    name: Authorization
    required: true
    type: string
    description: hmac-sha256 generated auth header
