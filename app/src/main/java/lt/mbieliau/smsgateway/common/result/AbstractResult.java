/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common.result;

public abstract class AbstractResult {
    public boolean success;
    public String error_message;
    public long timestamp;
}
