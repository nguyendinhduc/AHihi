package com.phongbm.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

public class SharedpreferenceAccount {
    private static final String NAME_FILE = "ListID";
    private static final String SIZE_LIST  = "zize_list";
    private static final String ID_USER = "ID_User";
    private Context context;
    public SharedpreferenceAccount(Context context) {
        this.context = context;
        createSharedpreferenceAcount();
    }
    private void createSharedpreferenceAcount() {
        SharedPreferences sf = this.context.getSharedPreferences(NAME_FILE,
                Context.MODE_PRIVATE);
        int size = sf.getInt(SIZE_LIST, -1);
        if ( size == -1 ) {
            SharedPreferences.Editor editor = sf.edit();
            editor.putInt(SIZE_LIST, 0);
            editor.commit();
        }
    }

    public ArrayList<String> readListID() {
        int size = getSizeLitID();
        ArrayList<String> listID = new ArrayList<>();
        SharedPreferences sf = this.context.getSharedPreferences(NAME_FILE,
                Context.MODE_PRIVATE);
        for ( int i = 0; i < size; i++ ) {
            String idUser = sf.getString(ID_USER + i, "");
            listID.add(idUser);
        }
        return listID;
    }

    public void writeUserID( String userID) {
        int size = getSizeLitID();
        SharedPreferences sp = this.context.getSharedPreferences(NAME_FILE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(ID_USER + size, userID);
        edit.putInt(SIZE_LIST, size + 1);
        edit.commit();
    }

    private int getSizeLitID() {
        return this.context.getSharedPreferences(NAME_FILE,
                Context.MODE_PRIVATE).getInt(SIZE_LIST, 0);
    }
}
