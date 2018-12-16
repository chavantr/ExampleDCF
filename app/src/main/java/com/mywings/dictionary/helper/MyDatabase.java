
package com.mywings.dictionary.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import com.mywings.dictionary.model.Word;
import com.mywings.dictionary.model.WordDetail;

import static com.mywings.dictionary.helper.Constants.*;

import java.util.ArrayList;


public class MyDatabase extends SQLiteAssetHelper {

    public MyDatabase(Context context, String name, CursorFactory factory,
                      int version) {
        super(context, name, factory, version);
    }

    public ArrayList<Object> getWords() {
        ArrayList<Object> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(WORD_TABLE, null, null, null, null, null, "word ASC");
        if (cursor.moveToFirst()) {
            do {
                Word word = new Word(cursor.getInt(cursor.getColumnIndex(WORD_ID)),
                        cursor.getString(cursor.getColumnIndex(WORD)),
                        cursor.getString(cursor.getColumnIndex(KEY_WORDS)));
                words.add(word);
            } while (cursor.moveToNext());
        }
        return words;
    }

    public ArrayList<Object> getFavoriteWords() {
        ArrayList<Object> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor fcursor = db.query(FAVORITE_TABLE, null, null, null, null, null, null);

        if (fcursor.moveToFirst()) {
            do {
                int id = fcursor.getInt(fcursor.getColumnIndex("qid"));
                String query = "SELECT * FROM dic_words where word_id=" + id;
                Cursor cursor = db.rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Word word = new Word(cursor.getInt(cursor.getColumnIndex(WORD_ID)),
                                cursor.getString(cursor.getColumnIndex(WORD)),
                                cursor.getString(cursor.getColumnIndex(KEY_WORDS)));
                        words.add(word);
                    } while (cursor.moveToNext());
                }
            } while (fcursor.moveToNext());
        }
        return words;
    }


    public ArrayList<Object> getRecentWords() {
        ArrayList<Object> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor fcursor = db.query(RECENT_TABLE, null, null, null, null, null, null);
        if (fcursor.moveToFirst()) {
            do {
                int id = fcursor.getInt(fcursor.getColumnIndex("qid"));
                String query = "SELECT * FROM dic_words where word_id=" + id;
                Cursor cursor = db.rawQuery(query, null);
                if (cursor.moveToFirst()) {
                    do {
                        Word word = new Word(cursor.getInt(cursor.getColumnIndex(WORD_ID)),
                                cursor.getString(cursor.getColumnIndex(WORD)),
                                cursor.getString(cursor.getColumnIndex(KEY_WORDS)));
                        words.add(word);
                    } while (cursor.moveToNext());
                }
            } while (fcursor.moveToNext());
        }
        return words;
    }


    public WordDetail getWordDetail(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] args = {String.valueOf(id)};
        WordDetail wordDetail = new WordDetail();
        Cursor cursor = db.query(MEANING_TABLE, null, "word_id=?", args, null, null, null);
        if (cursor.moveToFirst()) {
            wordDetail.setMeaning(cursor.getString(cursor.getColumnIndex(MEANING)));
            wordDetail.setWordType(cursor.getString(cursor.getColumnIndex(WORD_TYPE)));
        }
        return wordDetail;
    }

    public int saveFavorite(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {String.valueOf(id)};
        Cursor cursor = db.query(FAVORITE_TABLE, null, "qid=?", args, null, null, null, null);
        if (null != null && cursor.getCount() > 0) return 100;
        ContentValues contentValues = new ContentValues();
        contentValues.put("qid", id);
        int inserted = (int) db.insert(FAVORITE_TABLE, null, contentValues);
        return inserted;
    }

    public int saveRecent(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] args = {String.valueOf(id)};
        Cursor cursor = db.query(RECENT_TABLE, null, "qid=?", args, null, null, null, null);
        if (null != null && cursor.getCount() > 0) return 100;
        ContentValues contentValues = new ContentValues();
        contentValues.put("qid", id);
        int inserted = (int) db.insert(RECENT_TABLE, null, contentValues);
        return inserted;
    }
}
