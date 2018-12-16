
package com.mywings.dictionary.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class SQLiteAssetHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteAssetHelper.class.getSimpleName();

    private final Context mContext;
    private final String mName;
    private final CursorFactory mFactory;
    private final int mNewVersion;

    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;

    private String mDatabasePath;
    private String mArchivePath;
    private String mUpgradePathFormat;

    private int mForcedUpgradeVersion = 0;


    public SQLiteAssetHelper(Context context, String name,
                             String storageDirectory, CursorFactory factory, int version) {
        super(context, name, factory, version);

        if (version < 1)
            throw new IllegalArgumentException("Version must be >= 1, was "
                    + version);
        if (name == null)
            throw new IllegalArgumentException("Databse name cannot be null");

        mContext = context;
        mName = name;
        mFactory = factory;
        mNewVersion = version;

        mArchivePath = name + ".zip";

        if (storageDirectory != null) {
            mDatabasePath = storageDirectory;
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 4.2) {
                mDatabasePath = context.getApplicationInfo().dataDir + "/databases/";
            } else {
                mDatabasePath = "/data/data/" + context.getPackageName() + "/databases/";
            }
            //mDatabasePath = context.getApplicationInfo().dataDir + "/databases";
        }
        mUpgradePathFormat = name + "_upgrade_%s-%s.sql";
    }


    public SQLiteAssetHelper(Context context, String name,
                             CursorFactory factory, int version) {
        this(context, name, null, factory, version);
    }


    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getWritableDatabase called recursively");
        }


        boolean success = false;
        SQLiteDatabase db = null;
        // if (mDatabase != null) mDatabase.lock();
        try {
            mIsInitializing = true;
            // if (mName == null) {
            // db = SQLiteDatabase.create(null);
            // } else {
            // db = mContext.openOrCreateDatabase(mName, 0, mFactory);
            // }
            db = createOrOpenDatabase(false);

            int version = db.getVersion();

            // do force upgrade
            if (version != 0 && version < mForcedUpgradeVersion) {
                db = createOrOpenDatabase(true);
                db.setVersion(mNewVersion);
                version = db.getVersion();
            }

            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            Log.w(TAG,
                                    "Can't downgrade read-only database from version "
                                            + version + " to " + mNewVersion
                                            + ": " + db.getPath());
                        }
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            success = true;
            return db;
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // mDatabase.unlock();
                }
                mDatabase = db;
            } else {
                // if (mDatabase != null) mDatabase.unlock();
                if (db != null)
                    db.close();
            }
        }

    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (mName == null)
                throw e; // Can't open a temp database read-only!
            Log.e(TAG, "Couldn't open " + mName
                    + " for writing (will try read-only):", e);
        }

        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            String path = mContext.getDatabasePath(mName).getPath();
            db = SQLiteDatabase.openDatabase(path, mFactory,
                    SQLiteDatabase.OPEN_READONLY);
            if (db.getVersion() != mNewVersion) {
                throw new SQLiteException(
                        "Can't upgrade read-only database from version "
                                + db.getVersion() + " to " + mNewVersion + ": "
                                + path);
            }

            onOpen(db);
            Log.w(TAG, "Opened " + mName + " in read-only mode");
            mDatabase = db;
            return mDatabase;
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase)
                db.close();
        }
    }


    @Override
    public synchronized void close() {
        if (mIsInitializing)
            throw new IllegalStateException("Closed during initialization");

        if (mDatabase != null && mDatabase.isOpen()) {
            mDatabase.close();
            mDatabase = null;
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

       /* Log.w(TAG, "Upgrading database " + mName + " from version "
                + oldVersion + " to " + newVersion + "...");

        ArrayList<String> paths = new ArrayList<String>();

        getUpgradeFilePaths(oldVersion, newVersion - 1, newVersion, paths);

        if (paths.isEmpty()) {
            Log.e(TAG, "no upgrade script path from " + oldVersion + " to "
                    + newVersion);
            throw new SQLiteAssetException("no upgrade script path from "
                    + oldVersion + " to " + newVersion);
        }

        Collections.sort(paths, new VersionComparator());
        for (String path : paths) {
            try {
                Log.w(TAG, "processing upgrade: " + path);
                InputStream is = mContext.getAssets().open(path);
                String sql = convertStreamToString(is);
                if (sql != null) {
                    String[] cmds = sql.split(";");
                    for (String cmd : cmds) {
                        // Log.d(TAG, "cmd=" + cmd);
                        if (cmd.trim().length() > 0) {
                            db.execSQL(cmd);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.w(TAG, "Successfully upgraded database " + mName + " from version "
                + oldVersion + " to " + newVersion);*/

       if(newVersion > oldVersion){
           delete();
           db = createOrOpenDatabase(true);
           //db.setVersion(newVersion);
           //version = db.getVersion();
       }

    }

    public void setForcedUpgradeVersion(int version) {
        mForcedUpgradeVersion = version;
    }

    private SQLiteDatabase createOrOpenDatabase(boolean force)
            throws SQLiteAssetException {
        SQLiteDatabase db = returnDatabase();
        if (db != null) {
            // database already exists
            if (force) {
                Log.w(TAG, "forcing database upgrade!");
                copyDatabaseFromAssets();
                db = returnDatabase();
            }
            return db;
        } else {
            // database does not exist, copy it from assets and return it
            copyDatabaseFromAssets();
            db = returnDatabase();
            return db;
        }
    }

    private void delete()
    {
        File file = new File(mDatabasePath  + "/" + mName);
        if(file.exists())
        {
            file.delete();
            System.out.println("delete database file.");
        }
    }

    private SQLiteDatabase returnDatabase() {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(mDatabasePath + "/"
                    + mName, mFactory, SQLiteDatabase.OPEN_READWRITE);
            Log.i(TAG, "successfully opened database " + mName);
            return db;
        } catch (SQLiteException e) {
            Log.w(TAG,
                    "could not open database " + mName + " - " + e.getMessage());
            return null;
        }
    }

    private void copyDatabaseFromAssets() throws SQLiteAssetException {
        Log.w(TAG, "copying database from assets...");

        try {
            InputStream zipFileStream = mContext.getAssets().open(mArchivePath);


            File f = new File(mDatabasePath + "/");

            if (!f.exists()) {
                f.mkdir();
            }

            ZipInputStream zis = getFileFromZip(zipFileStream);

            if (zis == null) {
                throw new SQLiteAssetException(
                        "Archive is missing a SQLite database file");
            }

            writeExtractedFileToDisk(zis, new FileOutputStream(mDatabasePath
                    + "/" + mName));

            Log.w(TAG, "database copy complete");

        } catch (FileNotFoundException fe) {
            SQLiteAssetException se = new SQLiteAssetException("Missing "
                    + mArchivePath
                    + " file in assets or target folder not writable");
            se.setStackTrace(fe.getStackTrace());
            throw se;
        } catch (IOException e) {
            SQLiteAssetException se = new SQLiteAssetException(
                    "Unable to extract " + mArchivePath + " to data directory");
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
    }

    private InputStream getUpgradeSQLStream(int oldVersion, int newVersion) {
        String path = String.format(mUpgradePathFormat, oldVersion, newVersion);
        try {
            InputStream is = mContext.getAssets().open(path);
            return is;
        } catch (IOException e) {
            Log.w(TAG, "missing database upgrade script: " + path);
            return null;
        }
    }

    private void getUpgradeFilePaths(int baseVersion, int start, int end,
                                     ArrayList<String> paths) {

        int a;
        int b;

        InputStream is = getUpgradeSQLStream(start, end);
        if (is != null) {
            String path = String.format(mUpgradePathFormat, start, end);
            paths.add(path);

            a = start - 1;
            b = start;
            is = null;
        } else {
            a = start - 1;
            b = end;
        }

        if (a < baseVersion) {
            return;
        } else {
            getUpgradeFilePaths(baseVersion, a, b, paths); // recursive call
        }

    }

    private void writeExtractedFileToDisk(ZipInputStream zin, OutputStream outs)
            throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = zin.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        zin.close();
    }

    private ZipInputStream getFileFromZip(InputStream zipFileStream)
            throws FileNotFoundException, IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze = null;

        while ((ze = zis.getNextEntry()) != null) {
            Log.w(TAG, "extracting file: '" + ze.getName() + "'...");
            return zis;
        }
        return null;
    }

    private String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }


    private class VersionComparator implements Comparator<String> {
        private Pattern pattern = Pattern
                .compile(".*_upgrade_([0-9]+)-([0-9]+).*");


        @Override
        public int compare(String file0, String file1) {
            Matcher m0 = pattern.matcher(file0);
            Matcher m1 = pattern.matcher(file1);

            if (!m0.matches()) {
                Log.w(TAG, "could not parse upgrade script file: " + file0);
                throw new SQLiteAssetException("Invalid upgrade script file");
            }

            if (!m1.matches()) {
                Log.w(TAG, "could not parse upgrade script file: " + file1);
                throw new SQLiteAssetException("Invalid upgrade script file");
            }

            int v0_from = Integer.valueOf(m0.group(1));
            int v1_from = Integer.valueOf(m1.group(1));
            int v0_to = Integer.valueOf(m0.group(2));
            int v1_to = Integer.valueOf(m1.group(2));

            if (v0_from == v1_from) {


                if (v0_to == v1_to) {
                    return 0;
                }

                return v0_to < v1_to ? -1 : 1;
            }

            return v0_from < v1_from ? -1 : 1;
        }
    }

}
