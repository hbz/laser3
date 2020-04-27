package de.laser.interfaces

interface CalculatedType {

    final static CALCULATED_TYPE_LOCAL            = 'Local'
    final static CALCULATED_TYPE_CONSORTIAL       = 'Consortial'
    final static CALCULATED_TYPE_COLLECTIVE       = 'Collective'
    final static CALCULATED_TYPE_ADMINISTRATIVE   = 'Administrative'
    final static CALCULATED_TYPE_PARTICIPATION    = 'Participation'
    final static CALCULATED_TYPE_UNKOWN           = 'Unknown'
    final static CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE = 'Participation as Collective'

    String getCalculatedType()
}
