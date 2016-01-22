package com.lange.twitterexercise.operations;

import com.lange.common.dbc.DesignByContract;
import org.junit.Before;

/**
 * Created by lange on 23/1/16.
 */
public class BaseOperationsTest {

    @Before
    public void setUp() {
        DesignByContract.setEnableDesignByContract(true);

        System.setProperty("customer.key", "<<customer key>>");
        System.setProperty("customer.secret", "<<customer secret>>");

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
    }

}
