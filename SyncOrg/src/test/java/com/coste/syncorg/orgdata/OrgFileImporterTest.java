package com.coste.syncorg.orgdata;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.dao.OrgFileDao;
import com.coste.syncorg.dao.OrgNodeDao;
import com.coste.syncorg.dao.TagDao;
import com.coste.syncorg.dao.TimestampDao;
import com.coste.syncorg.dao.TodoDao;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.coste.syncorg.orgdata.table.OrgNodeEntity;
import com.coste.syncorg.orgdata.table.TagEntity;
import com.coste.syncorg.orgdata.table.TimestampEntity;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class OrgFileImporterTest {

    private static final int NUM_NODES = 8;
    private static final long FILE_ID = 99L;

    private static final long TODO_ID_TODO = 11;
    private static final long TODO_ID_DONE = 15;

    @Mock
    private OrgFileDao orgFileDao;

    @Mock
    private OrgNodeDao orgNodeDao;

    @Mock
    private TimestampDao timestampDao;

    @Mock
    private TagDao tagDao;

    @Mock
    private TodoDao todoDao;

    @Before

    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void parseFile() throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org-testfile.org");
        OrgFileImporter sut = new OrgFileImporter(RuntimeEnvironment.application);
        sut.orgFileDao = this.orgFileDao;
        sut.orgNodeDao = this.orgNodeDao;
        sut.timestampDao = this.timestampDao;
        sut.tagDao = this.tagDao;
        sut.todoDao = this.todoDao;

        when(orgFileDao.save(argThat(argument ->
                fileEntity().equals(argument))))
                .thenReturn(fileEntity().setId(FILE_ID));

        OrgNodeEntity[] orgNodeEntities = nodeEntities();
        for (int idx = 0; idx < NUM_NODES; idx++) {
            doReturn(orgNodeEntities[idx].clone().setId((long) idx)).when(orgNodeDao).save(orgNodeEntities[idx]);
        }

        when(todoDao.getTodoIdMappings()).thenReturn(getTodoMap());

        sut.parseFile("/tmp/test.org", new BufferedReader(new InputStreamReader(inputStream)), RuntimeEnvironment.application);


        verify(orgFileDao, times(1)).save(argThat(argument ->
                fileEntity().equals(argument)));

        for (int idx = 0; idx < NUM_NODES; idx++) {
            verify(orgNodeDao).save(orgNodeEntities[idx]);
        }

        for (TimestampEntity entity : timestampEntities()) {
            verify(timestampDao).save(entity);
        }

        verifyNoMoreInteractions(timestampDao);

        verify(tagDao).tagNodeWith(0L, "o", false);
        verify(tagDao).tagNodeWith(1L, "oo", false);
        verify(tagDao).tagNodeWith(2L, "ooo", false);
        verify(tagDao).tagNodeWith(3L, "ot", false);
        verify(tagDao).tagNodeWith(4L, "oT", false);
        verify(tagDao).tagNodeWith(5L, "t", false);
        verify(tagDao).tagNodeWith(6L, "TTT", false);
        verify(tagDao).tagNodeWith(6L, "bla", false);

        verifyNoMoreInteractions(tagDao);

        verify(todoDao).getTodoIdMappings();

        verifyNoMoreInteractions(todoDao);

    }

    private Map<String, Long> getTodoMap() {
        Map<String, Long> result = new HashMap<>();
        result.put("TODO", TODO_ID_TODO);
        result.put("DONE", TODO_ID_DONE);
        return result;
    }


    private TimestampEntity[] timestampEntities() {
        return new TimestampEntity[]{
                timestampEntity("2013-02-21", TimestampType.PLAIN, 0L, false),
                timestampEntity("2014-02-26", TimestampType.DEADLINE, 0L, false),
                timestampEntity("2014-02-26", TimestampType.SCHEDULED, 0L, true)
        };
    }

    private TimestampEntity timestampEntity(String startDate, TimestampType timestampType, long nodeId, boolean inActive) {
        return new TimestampEntity().
                setStartDate(LocalDate.parse(startDate)).
                setType(timestampType).
                setNodeId(nodeId).
                setIsInActive(inActive);
    }

    private OrgNodeEntity[] nodeEntities() {
        return new OrgNodeEntity[]{
                nodeEntity("one", 1, null, "<2014-02-26 Wed 17:00 -3w +2d>\n" +
                        "<2014-02-26 Wed 17:00-19:30 -3w +2d>\n" +
                        "\n" +
                        "One little piggy went to the bank\n", 0, ""),
                nodeEntity("one one", 2, 0L, "A second little piggy\n" +
                        "went to another\n" +
                        "bank\n", 0, ""),
                nodeEntity("one one one", 3, 1L, "A third little\n" +
                        "piggy wasn't\n" +
                        "that little\n", 0, ""),
                nodeEntity("one two", 2, 0L, "A boxing combination\n", 1, ""),
                nodeEntity("[#B] one three", 2, 0L, "\n" +
                        "Leading newline on me\n", 2, ""),
                nodeEntity("two", 1, null, "- Number\n" +
                        "- After\n" +
                        "- One\n", 1, ""),
                nodeEntity("two skip one", 3, 5L, "Goes straight\n" +
                        "/to/ *one*.\n", 0, ""),
                nodeEntity("Commented test", 1, null, "\n" +
                        "Bla bla bla\n", 2, "# NONSENSEID: 02DS2G\n")};
    }

    private OrgNodeEntity nodeEntity(String displayName, int level, Long parentId, String payload, int positionInParent, String comment) {
        return new OrgNodeEntity().
                setFileId(FILE_ID).
                setDisplayName(displayName).
                setLevel(level).
                setParentId(parentId).
                setPayload(payload).
                setPositionInParent(positionInParent).
                setComment(comment);
    }

    private FileEntity fileEntity() {
        return new FileEntity()
                .setComment("# These should be\n" +
                        "# placed in the\n" +
                        "# orgfile root\n")
                .setDisplayName("test.org")
                .setFilePath("test.org");
    }

}