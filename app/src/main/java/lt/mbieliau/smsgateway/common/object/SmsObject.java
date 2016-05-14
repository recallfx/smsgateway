/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common.object;

import java.io.Serializable;

public class SmsObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public long timestamp = 0;

    public  SmsObject(){

    }

    public SmsObject(long paramLong)
    {
        this.timestamp = paramLong;
    }
}
