package org.superbiz.moviefun.albums;

import org.joda.time.DateTime;

public class AlbumSchedulerTask {

    public DateTime getStarted_at() {
        return started_at;
    }

    public void setStarted_at(DateTime started_at) {
        this.started_at = started_at;
    }

    private DateTime started_at;
}
