package de.laser.interfaces

import de.laser.traits.ShareableTrait

interface ShareSupport {

    boolean checkSharePreconditions(ShareableTrait sharedObject)

    boolean showUIShareButton()

    def updateShare(ShareableTrait sharedObject)

    def syncAllShares(List<ShareSupport> targets)
}
