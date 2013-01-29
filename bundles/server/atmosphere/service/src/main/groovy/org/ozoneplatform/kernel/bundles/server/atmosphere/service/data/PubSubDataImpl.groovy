package org.ozoneplatform.kernel.bundles.server.atmosphere.service.data;

import org.ozoneplatform.kernel.bundles.server.atmosphere.api.data.PubSubData;

import java.util.Date;

public class PubSubDataImpl implements PubSubData {
    private String message;
    private String author;
    private long time;

    public PubSubDataImpl() {
        this("", "");
    }

    public PubSubDataImpl(String author, String message) {
        this.author = author;
        this.message = message;
        this.time = new Date().getTime();
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
