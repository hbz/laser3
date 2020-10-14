package de.laser.interfaces

import de.laser.traits.ShareableTrait

interface ShareSupport {

    boolean checkSharePreconditions(ShareableTrait sharedObject)

    boolean showUIShareButton()

    void updateShare(ShareableTrait sharedObject)

    void syncAllShares(List<ShareSupport> targets)
}
