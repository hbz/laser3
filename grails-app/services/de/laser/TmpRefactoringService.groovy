package de.laser

import de.laser.annotations.UnstableFeature
@UnstableFeature
class TmpRefactoringService {

    ContextService contextService
    UserService userService

    boolean hasAccessToDoc() {
        return true
    }

    boolean hasAccessToDocNote() {
        return true
    }

    boolean hasAccessToTask() {
        return true
    }

}
