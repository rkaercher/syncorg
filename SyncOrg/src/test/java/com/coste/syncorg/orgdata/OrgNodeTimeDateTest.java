package com.coste.syncorg.orgdata;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.junit.Assert.*;


public class OrgNodeTimeDateTest {

    @Test
    public void thatConstructionByEpochSecondsReturnsTheCorrectValues() {
        LocalDateTime now = LocalDateTime.now();
        OrgNodeTimeDate sut = new OrgNodeTimeDate(now.toDate().getTime());

        assertEquals(OrgNodeTimeDate.TYPE.Timestamp, sut.getType());
    }

    @Test
    public void getTimestampMatcher() throws Exception {

    }

    @Test
    public void deleteTimestamp() throws Exception {

    }

    @Test
    public void isEmpty() throws Exception {

    }

    @Test
    public void set() throws Exception {

    }

    @Test
    public void getDate() throws Exception {

    }

    @Test
    public void getTime() throws Exception {

    }

    @Test
    public void getStartTime() throws Exception {

    }

    @Test
    public void getEpochTime() throws Exception {

    }

    @Test
    public void isAllDay() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void toFormatedString() throws Exception {

    }

    @Test
    public void update() throws Exception {

    }

    @Test
    public void isBetween() throws Exception {

    }

}