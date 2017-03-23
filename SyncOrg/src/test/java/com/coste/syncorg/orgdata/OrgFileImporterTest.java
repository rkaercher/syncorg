package com.coste.syncorg.orgdata;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.dao.OrgFileDao;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class OrgFileImporterTest {

    @Mock
    private OrgFileDao orgFileDao;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void parseFile() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org-testfile.org");
        OrgFileImporter sut = new OrgFileImporter(RuntimeEnvironment.application);
        sut.orgFileDao = this.orgFileDao;
        sut.parseFile("/tmp/test.org", new BufferedReader(new InputStreamReader(inputStream)), RuntimeEnvironment.application);

    }

}