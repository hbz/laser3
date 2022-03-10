package de.laser.interfaces

interface CalculatedType {

    final static TYPE_LOCAL          = 'Local'
    final static TYPE_CONSORTIAL     = 'Consortial'
    final static TYPE_ADMINISTRATIVE = 'Administrative'
    final static TYPE_PARTICIPATION  = 'Participation'
    final static TYPE_UNKOWN         = 'Unknown'

    String _getCalculatedType()
}
