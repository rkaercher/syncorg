package com.coste.syncorg.dao;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.orgdata.OrgDatabase;
import com.coste.syncorg.orgdata.OrgFileNew;
import com.coste.syncorg.orgdata.SyncOrgApplication;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,application = SyncOrgApplication.class)
public class OrgFileDaoTest {
    @Test
    public void save() throws Exception {
        OrgDatabase odb = new OrgDatabase(RuntimeEnvironment.application);
        OrgFileDao dao = new OrgFileDao(odb);
        OrgFileNew orgFile = new OrgFileNew();
        orgFile.setCreated(DateTime.now());
        orgFile.setComment("a comment").setLastModified(DateTime.now());
        dao.save(orgFile);

        String s = odb.getDatabasePath();
        assertTrue(odb.isOpen());
    }

}