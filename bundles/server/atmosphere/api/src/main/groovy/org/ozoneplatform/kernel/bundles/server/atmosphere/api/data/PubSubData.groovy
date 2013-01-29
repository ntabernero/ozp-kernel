package org.ozoneplatform.kernel.bundles.server.atmosphere.api.data;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Sean T Booker
 * Date: 1/29/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PubSubData {

    String getMessage();

    public String getAuthor();

    public void setAuthor(String author);

    public void setMessage(String message);

    public long getTime();

    public void setTime(long time);
}
