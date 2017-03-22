package com.coste.syncorg.synchronizers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.coste.syncorg.MainActivity;
import com.coste.syncorg.R;
import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.orgdata.OrgFileImporter;
import com.coste.syncorg.orgdata.OrgProviderUtils;
import com.coste.syncorg.synchronizers.SshSessionFactory.ConnectionType;
import com.coste.syncorg.util.FileUtils;
import com.coste.syncorg.util.OrgFileNotFoundException;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JGitWrapper {
    final static String CONFLICT_FILES = "conflict_files";
    // The git dir inside the Context.getFilesDir() folder
    static String GIT_DIR = "git_dir";

    private OrgFileImporter importer;

    @Inject
    JGitWrapper(OrgFileImporter importer) {
        this.importer = importer;
    }


    public void add(String filename, Context context, String repoDir) {
        new AddTask(context, repoDir).execute(filename);
    }

    /**
     * Perform a Git pull in the given folder
     */
    SyncResult pull(final Context context, String folder) {
        File repoDir = new File(folder + "/.git");
        SyncResult result = new SyncResult();
        AuthData authData = AuthData.getInstance(context);
        Git git = null;
        try {
            git = Git.open(repoDir);
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }


        try {

            Repository repository = git.getRepository();

            ObjectId oldHead = repository.resolve("HEAD^{tree}");

            //
            //            git.commit()
            //                    .setAll(true)
            //                    .setMessage("Commit before pulling")
            //                    .call();

            PullResult pullResult =
                    git
                            .pull()
                            .setCredentialsProvider(new CredentialsProviderAllowHost(authData.getUser(), authData.getPassword()))
                            .setTransportConfigCallback(new CustomTransportConfigCallback(context))
                            .call();

            Status status = git.status().call();
            //            Iterator<RevCommit>refs =  git.log().call().iterator();
            //            while(refs.hasNext()){
            //            }
            ObjectId head = repository.resolve("HEAD^{tree}");

            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();


            for (DiffEntry entry : diffs) {
                String newpath = entry.getNewPath();
                String oldpath = entry.getOldPath();
                if (newpath.equals("/dev/null")) {
                    result.deletedFiles.add(oldpath);
                } else if (oldpath.equals("/dev/null")) {
                    result.newFiles.add(entry.getNewPath());
                } else {
                    result.changedFiles.add(entry.getNewPath());
                }
            }
            result.setState(SyncResult.State.kSuccess);
            return result;
        } catch (WrongRepositoryStateException e) {
            e.printStackTrace();
            handleMergeConflict(git, context);
        } catch (IOException
                | DetachedHeadException
                | TransportException
                | InvalidConfigurationException
                | NoHeadException
                | RefNotFoundException
                | InvalidRemoteException
                | CanceledException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void handleMergeConflict(Git git, Context context) {
        Status status = null;

        try {
            status = git.status().call();
            ContentResolver resolver = context.getContentResolver();
            for (String file : status.getConflicting()) {
                OrgFile f = new OrgFile(file, resolver);
                ContentValues values = new ContentValues();
                values.put("comment", "conflict");
                f.updateFileInDB(resolver, values);
            }

        } catch (GitAPIException e1) {
            e1.printStackTrace();
            return;
        } catch (OrgFileNotFoundException e) {
            e.printStackTrace();
        }
    }


    static public String getUrl(Context context) {
        StringBuilder REMOTE_URL = new StringBuilder();
        AuthData authData = AuthData.getInstance(context);

        if (SshSessionFactory.getConnectionType(context) == ConnectionType.kHttp) {
            // http connection
            REMOTE_URL.append(authData.getHost())
                    .append("/")
                    .append(authData.getPath());
            return REMOTE_URL.toString();
        } else {
            // ssh connection
            REMOTE_URL.append("ssh://")
                    .append(authData.getUser())
                    .append("@")
                    .append(authData.getHost())
                    .append("/")
                    .append(authData.getPath());

            return REMOTE_URL.toString();
        }
    }

    public AsyncTask<String, Void, Object> createCloneTask(Context context, String folderActual) {
        return new CloneGitRepoTask(context, folderActual, importer);
    }

    AsyncTask<String, Void, Void> createPushTask(Context context, String repoDir) {
        return new PushTask(context, repoDir);
    }

    public AsyncTask<String, Void, Void> createMergeTask(Context context, String filename, String repoPath) {
        return new MergeTask(context, filename, repoPath);
    }


    /**
     * SSH configuration
     * In charge of specifying the PubKey or the password depending on the connection type
     */
    private static class CustomTransportConfigCallback implements TransportConfigCallback {
        Context context;

        CustomTransportConfigCallback(Context context) {
            this.context = context;
        }

        @Override
        public void configure(Transport transport) {
            try {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(new SshSessionFactory(context));
            } catch (ClassCastException ignored) { /* If connection is HTTP */ }
        }
    }


//    static public class StatusTask extends AsyncTask<Void, Void, Status> {
//        Context context;
//
//        public StatusTask(Context context) {
//            this.context = context;
//        }
//
//        @Override
//        protected org.eclipse.jgit.api.Status doInBackground(Void... voids) {
//            File repoDir = new File(Synchronizer.getSynchronizer(context).getAbsoluteFilesDir() + "/.git");
//            Git git = null;
//
//            try {
//                git = Git.open(repoDir);
//                return git.status().call();
//            } catch (IOException | GitAPIException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }

    private class CloneGitRepoTask extends AsyncTask<String, Void, Object> {
        Context context;
        ProgressDialog progress;
        String repoPath;
        private OrgFileImporter importer;

        CloneGitRepoTask(Context context, String repoPath, OrgFileImporter importer) {
            this.context = context;
            this.repoPath = repoPath;
            this.importer = importer;
        }

        protected Object doInBackground(String... params) {
            final AuthData authData = AuthData.getInstance(context);

            final ConnectionType connection = SshSessionFactory.getConnectionType(context);

            File localPath = new File(repoPath);
            FileUtils.deleteFile(localPath);

            CloneCommand cloneCommand = Git.cloneRepository();

            String url = getUrl(context);


            if (connection != ConnectionType.kHttp)
                cloneCommand.setTransportConfigCallback(new CustomTransportConfigCallback(context));


            System.setProperty("user.home", repoPath);

            try {
                cloneCommand
                        .setURI(url)
                        .setDirectory(localPath)
                        .setCredentialsProvider(new CredentialsProviderAllowHost(authData.getUser(), authData.getPassword()))
                        .setTransportConfigCallback(new CustomTransportConfigCallback(context))
                        .setBare(false)
                        .call();
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage(context.getString(R.string.please_wait));
            progress.setTitle(context.getString(R.string.signing_in));
            progress.show();
        }

        @Override
        protected void onPostExecute(Object exception) {

            progress.dismiss();
            if (exception == null) {
                OrgProviderUtils
                        .clearDB(context.getContentResolver());
                parseAll();

                Toast.makeText(context, "Synchronization successful !", Toast.LENGTH_LONG).show();
                ((Activity) context).finish();

                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                return;
            }

            if (exception instanceof InvalidRemoteException) {
                Toast.makeText(context, "Path does not exist or is not a valid repository", Toast.LENGTH_LONG).show();
            } else if (exception instanceof UnableToPushException) {
                //				git config receive.denyCurrentBranch ignore
                Toast.makeText(context, "Push test failed. Make sure the repository is bare.", Toast.LENGTH_LONG).show();
            } else if (exception instanceof TransportException) {
                Toast.makeText(context, context.getString(R.string.error_transport_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, exception.toString(), Toast.LENGTH_LONG).show();
                ((Exception) exception).printStackTrace();
            }
        }


        void parseAll() {
            String fileDir = repoPath;
            File f = new File(fileDir);
            File file[] = f.listFiles();
            if (file == null) return;
            for (File aFile : file) {
                String filename = aFile.getName();
                if (filename.equals(".git")) continue;
                OrgFile orgFile = new OrgFile(filename, filename);
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(fileDir + "/" + filename);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    importer.parseFile(orgFile, bufferedReader, context);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            }
        }
    }


    private class AddTask extends AsyncTask<String, Void, Void> {
        Context context;
        private String repoPath;

        AddTask(Context context, String repoPath) {
            this.context = context;
            this.repoPath = repoPath;
        }

        protected Void doInBackground(String... params) {
            File repoDir = new File(repoPath + "/.git");

            try {
                Git git = Git.open(repoDir);
                git.add()
                        .addFilepattern(params[0])
                        .call();

//                String addMsg = context.getResources().getString(R.string.sync_git_file_added);
//                git.commit()
//                        .setMessage(addMsg + params[0])
//                        .call();

            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new PushTask(context, repoPath).execute();
        }
    }

    private class PushTask extends AsyncTask<String, Void, Void> {
        Context context;
        private String repoPath;

        PushTask(Context context, String repoPath) {
            this.context = context;
            this.repoPath = repoPath;
        }

        protected Void doInBackground(String... params) {

            File repoDir = new File(repoPath + "/.git");

            Git git = null;
            try {
                git = Git.open(repoDir);

                if (git.status().call().isClean()) {
                    return null;
                }
                // Stage all changed files, omitting new files, and commit with one command

//                org.eclipse.jgit.api.Status status = git.status().call();
//                System.out.println("Added: " + status.getAdded());
//                System.out.println("Changed: " + status.getChanged());
//                System.out.println("Conflicting: " + status.getConflicting());
//                System.out.println("Missing: " + status.getMissing());
//                System.out.println("Modified: " + status.getModified());
//                System.out.println("Removed: " + status.getRemoved());
//                System.out.println("Untracked: " + status.getUntracked());

                git.commit()
                        .setAll(true)
                        .setMessage("Commit changes to all files")
                        .call();

                AuthData authData = AuthData.getInstance(context);
                //TODO reenable
//                git.push()
//                        .setCredentialsProvider(new CredentialsProviderAllowHost(authData.getUser(), authData.getPassword()))
//                        .setTransportConfigCallback(new CustomTransportConfigCallback(context))
//                        .call();
                System.out.println("Committed all changes to repository at ");
            } catch (IOException | UnmergedPathsException e) {
                e.printStackTrace();
            } catch (WrongRepositoryStateException e) {
                e.printStackTrace();
                handleMergeConflict(git, context);
            } catch (ConcurrentRefUpdateException e) {
                e.printStackTrace();
            } catch (NoHeadException e) {
                e.printStackTrace();
            } catch (NoMessageException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            } catch (JGitInternalException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class MergeTask extends AsyncTask<String, Void, Void> {
        Context context;
        String filename;
        private String repoPath;

        public MergeTask(Context context, String filename, String repoPath) {
            this.context = context;
            this.filename = filename;
            this.repoPath = repoPath;
        }

        protected Void doInBackground(String... params) {

            File repoDir = new File(repoPath + "/.git");
            Git git = null;
            try {
                git = Git.open(repoDir);

                CheckoutCommand coCmd = git.checkout();
                // Commands are part of the api module, which include git-like calls
                coCmd.setName("master");
                coCmd.setCreateBranch(false); // probably not needed, just to make sure
                Ref ref = coCmd.call();

                // Stage all changed files, omitting new files, and commit with one command
                git.merge()
                        .setStrategy(MergeStrategy.OURS)
                        .include(ref)
                        .call();

                git.add()
                        .addFilepattern(filename).call();

                org.eclipse.jgit.api.Status status = git.status().call();
                System.out.println("Added: " + status.getAdded());
                System.out.println("Changed: " + status.getChanged());
                System.out.println("Conflicting: " + status.getConflicting());
                System.out.println("Missing: " + status.getMissing());
                System.out.println("Modified: " + status.getModified());
                System.out.println("Removed: " + status.getRemoved());
                System.out.println("Untracked: " + status.getUntracked());

                //                AuthData authData = AuthData.getInstance(context);
                //                git.push()
                //                        .setCredentialsProvider(new CredentialsProviderAllowHost(authData.getUser(), authData.getPassword()))
                //                        .call();
                //                System.out.println("Committed all changes to repository at ");
            } catch (IOException | UnmergedPathsException e) {
                e.printStackTrace();
            } catch (WrongRepositoryStateException e) {
                e.printStackTrace();
                handleMergeConflict(git, context);
            } catch (ConcurrentRefUpdateException e) {
                e.printStackTrace();
            } catch (NoHeadException e) {
                e.printStackTrace();
            } catch (NoMessageException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static class UnableToPushException extends Exception {

    }
}
