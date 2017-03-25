package com.coste.syncorg.orgdata;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.dao.OrgFileDao;
import com.coste.syncorg.orgdata.table.FileEntity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class OrgFileImporterTest {

    @Mock
    private OrgFileDao orgFileDao;

    @Before
    public  void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void parseFile() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org-testfile.org");
        OrgFileImporter sut = new OrgFileImporter(RuntimeEnvironment.application);
        sut.orgFileDao = this.orgFileDao;

        ArgumentCaptor<FileEntity> fileCaptor = ArgumentCaptor.forClass(FileEntity.class);

        when(orgFileDao.save(argThat(argument ->
                topFirstNode().equals(argument))))
                .thenReturn(new FileEntity());


        sut.parseFile("/tmp/test.org", new BufferedReader(new InputStreamReader(inputStream)), RuntimeEnvironment.application);


        verify(orgFileDao,times(1)).save(argThat(argument ->
                topFirstNode().equals(argument)));

        assertEquals(topFirstNode(), fileCaptor.getValue());

    }

    private FileEntity topFirstNode() {
        return new FileEntity()
                .setComment("# These should2 be\n" +
                        "# placed in the\n" +
                        "# orgfile root\n").setDisplayName("test.org")
                .setFileName("test.org")
                ;
    }

}