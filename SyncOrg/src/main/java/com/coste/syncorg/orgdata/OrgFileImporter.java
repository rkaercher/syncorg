package com.coste.syncorg.orgdata;

import android.content.Context;
import android.content.Intent;

import com.coste.syncorg.dao.OrgFileDao;
import com.coste.syncorg.dao.OrgNodeDao;
import com.coste.syncorg.dao.TagDao;
import com.coste.syncorg.dao.TimestampDao;
import com.coste.syncorg.dao.TodoDao;
import com.coste.syncorg.gui.FileDecryptionActivity;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.coste.syncorg.orgdata.table.OrgNodeEntity;
import com.coste.syncorg.orgdata.table.TimestampEntity;
import com.coste.syncorg.util.FileUtils;
import com.coste.syncorg.util.PreferenceUtils;

import org.cowboyprogrammer.org.*;
import org.cowboyprogrammer.org.OrgNode;
import org.cowboyprogrammer.org.parser.RegexParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static junit.framework.Assert.assertTrue;

@Singleton
public class OrgFileImporter {
    private HashSet<String> excludedTags;


    @Inject
    OrgFileDao orgFileDao;

    @Inject
    OrgNodeDao orgNodeDao;

    @Inject
    TimestampDao timestampDao;

    @Inject
    TagDao tagDao;

    @Inject
    TodoDao todoDao;

    private  Context context;

    public OrgFileImporter(Context context) {
        this.context = context;
    }

    private void decryptAndParseFile(String orgFilePath, BufferedReader reader, Context context) {
        try {
            Intent intent = new Intent(context, FileDecryptionActivity.class);
            intent.putExtra("data", FileUtils.read(reader).getBytes());
            intent.putExtra("filename", orgFilePath);
            intent.putExtra("filenameAlias", getFileNameFromPath(orgFilePath));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (IOException e) {
        }
    }

    private String getFileNameFromPath(String path) {
        return new File(path).getName();
    }

    private boolean isEncrypted(String filename) {
        return filename.endsWith(".gpg") || filename.endsWith(".pgp")
                || filename.endsWith(".enc") || filename.endsWith(".asc");
    }

    /**
     * Remove old file from DB, decrypt file if encrypted and then parse
     */
    public boolean parseFile(String orgFilePath, BufferedReader breader, Context context) {
        if (isEncrypted(orgFilePath)) {
            decryptAndParseFile(orgFilePath, breader, context);
            return true;
        } else {
            try {
                parse(orgFilePath, breader);
                return true;
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void parse(String orgFilePath, BufferedReader breader) throws IOException, ParseException {
        this.excludedTags = PreferenceUtils.getExcludedTags(context);

        OrgFile orgFile = OrgFile.createFromBufferedReader(new RegexParser("TODO", "DONE"), getFileNameFromPath(orgFilePath), breader);

        FileEntity orgFileEntity = createOrgFileEntity(orgFile);
        FileEntity saved = orgFileDao.save(orgFileEntity);

        Map<String, Long> todoMappings = todoDao.getTodoIdMappings();
        saveNodesRecursive(orgFile.getSubNodes(), saved.getId(), null, todoMappings);

    }

    private void saveNodesRecursive(List<OrgNode> nodes, Long fileId, Long parentId, Map<String, Long> todoMappings) {
        for (int idx = 0;idx < nodes.size(); idx++) {
            OrgNode node = nodes.get(idx);
            Long todoId = getTodoId(node, todoMappings);
            OrgNodeEntity savedEntity = saveNode(fileId, parentId, idx, node, todoId);
            if (!node.getTimestamps().isEmpty()) {
                saveTimestamps(node.getTimestamps(), savedEntity.getId());
            }
            if (!node.getTags().isEmpty()) {
                saveTags(savedEntity.getId(), node.getTags(), false);
            }

            if (!node.getSubNodes().isEmpty()) {
                saveNodesRecursive(node.getSubNodes(), fileId, savedEntity.getId(), todoMappings);
            }
        }
    }

    private Long getTodoId(OrgNode node, Map<String, Long> todoMappings) {
        Long result = null;
        if (node.getTodo() != null && !node.getTodo().isEmpty()) {
            result = todoMappings.get(node.getTodo());
        }
        return result;
    }

    private void saveTags(long nodeId, List<String> tags, boolean inherited) {
        for (String tag : tags) {
            tagDao.tagNodeWith(nodeId, tag, inherited);
        }
    }

    private void saveTimestamps(List<OrgTimestamp> timestamps, long nodeId) {
        for (OrgTimestamp timestamp : timestamps) {
            TimestampEntity entity = new TimestampEntity();
            entity.setNodeId(nodeId);
            entity.setStartDate(timestamp.getDate().toLocalDate());
            if (timestamp.hasTime()) {
                entity.setStartTime(timestamp.getDate().toLocalTime());
            }
            if (timestamp.getEndTime() != null) {
                entity.setEndDate(entity.getStartDate());
                entity.setEndTime(timestamp.getEndTime());
            }
            entity.setType(getTimestampType(timestamp));
            entity.setIsInActive(timestamp.isInactive());
            timestampDao.save(entity);
        }
    }

    private TimestampType getTimestampType(OrgTimestamp timestamp) {
        switch (timestamp.getType()) {
            case PLAIN:
                return TimestampType.PLAIN;
            case SCHEDULED:
                return TimestampType.SCHEDULED;
            case DEADLINE:
                return TimestampType.DEADLINE;
        }
        return TimestampType.PLAIN;
    }

    private OrgNodeEntity saveNode(Long fileId, Long parentId, int idx, OrgNode node, Long todoId) {
        OrgNodeEntity entity = new OrgNodeEntity();
        entity.setDisplayName(node.getTitle().trim());
        entity.setLevel(node.getLevel());
        entity.setPayload(node.getBody());
        entity.setPositionInParent(idx);
        entity.setFileId(fileId);
        entity.setParentId(parentId);
        entity.setComment(node.getComments());
        entity.setTodoId(todoId);
        return orgNodeDao.save(entity);
    }

    private FileEntity createOrgFileEntity(OrgFile orgFile) {
        FileEntity result = new FileEntity();
        result.setComment(orgFile.getComments());
        result.setFilePath(orgFile.getFilename());
        result.setDisplayName(getFileNameFromPath(orgFile.getFilename()));

        return result;
    }

}
