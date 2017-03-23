package com.coste.syncorg.orgdata;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.coste.syncorg.dao.OrgFileDao;
import com.coste.syncorg.gui.FileDecryptionActivity;
import com.coste.syncorg.orgdata.table.FileEntity;
import com.coste.syncorg.orgdata.table.OrgNodeEntity;
import com.coste.syncorg.util.FileUtils;
import com.coste.syncorg.util.PreferenceUtils;

import org.cowboyprogrammer.org.*;
import org.cowboyprogrammer.org.OrgNode;
import org.cowboyprogrammer.org.parser.RegexParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import static junit.framework.Assert.assertTrue;

@Singleton
public class OrgFileImporter {
    private static final Pattern starPattern = Pattern.compile("^(\\**)\\s");
    private static final Pattern getTodos = Pattern
            .compile("#\\+TODO:([^\\|]+)(\\| (.*))*");

    private OrgFileOld orgFile;
    private HashSet<String> excludedTags;


    @Inject
    OrgFileDao orgFileDao;
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

        orgFileDao.save(createOrgFileEntity(orgFile));

        saveNodesRecursive(orgFile.getSubNodes());

        List<String> s = orgFile.getTags();
        assertTrue(true);
    }

    private void saveNodesRecursive(List<org.cowboyprogrammer.org.OrgNode> nodes) {
        OrgNodeEntity entity = new OrgNodeEntity();
        for (int idx = 0;idx < nodes.size(); idx++) {
            entity.clear();
            OrgNode node = nodes.get(idx);
            entity.setDisplayName(node.getTitle());
            entity.setLevel(node.getLevel());
            entity.setPayload(node.getBody());
            entity.setPositionInFile(idx);
            // TODO tags
        }
    }

    private FileEntity createOrgFileEntity(OrgFile orgFile) {
        FileEntity result = new FileEntity();
        result.setComment(orgFile.getComments());
        result.setFileName(orgFile.getFilename());
        result.setDisplayName(getFileNameFromPath(orgFile.getFilename()));

        return result;
    }

}
