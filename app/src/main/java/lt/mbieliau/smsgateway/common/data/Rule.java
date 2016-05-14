/*
 * Copyright (c) 2016.  All Rights Reserved
 * Marius Bieliauskas
 */

package lt.mbieliau.smsgateway.common.data;

import java.io.Serializable;

public class Rule implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;

    protected long id;
    protected String address;
    protected int status;

    public Rule(){
        this.id = 0;
        this.address = "";
        this.status = 0;
    }

    public Rule(long id, String address, int status){
        this.id = id;
        this.address = address;
        this.status = status;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return address + " " + status;
    }
}