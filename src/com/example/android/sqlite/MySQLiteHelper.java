package com.example.android.sqlite;
 
import java.util.LinkedList;
import java.util.List;
 

import com.example.android.model.User;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
public class MySQLiteHelper extends SQLiteOpenHelper {
 
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "fbprueba.db";
 
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXIST users ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "fb_user_id TEXT, "+
                "name TEXT )";
 
        // create books table
        db.execSQL(CREATE_USER_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS users");
 
        // create fresh books table
        this.onCreate(db);
    }
    
    public void onReset(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("TRUNCATE TABLE IF EXISTS users");
 
        // create fresh books table
        this.onCreate(db);
    }
    
    public void closeDatabase() {
    	this.close();
    }
    //---------------------------------------------------------------------
 
    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */
 
    // Books table name
    private static final String TABLE_USERS = "users";
 
    // Books Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FB_USER_ID = "fb_user_id";
    private static final String KEY_NAME = "name";
 
    private static final String[] COLUMNS = {KEY_ID,KEY_FB_USER_ID,KEY_NAME};
 
    public void addUser(User user){
       // Log.d("addUser", user.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_FB_USER_ID, user.getFbUserId()); // get title 
        values.put(KEY_NAME, user.getName()); // get author
 
        // 3. insert
        db.insert(TABLE_USERS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
 
        // 4. close
        db.close(); 
    }
 
    public User getUser(int id){
 
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
 
        // 2. build query
        Cursor cursor = 
                db.query(TABLE_USERS, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
 
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
 
        // 4. build book object
        User user = new User();
        user.setId(Integer.parseInt(cursor.getString(0)));
        user.setFbUserId(cursor.getString(1));
        user.setName(cursor.getString(2));
 
       // Log.d("getUser("+id+")", user.toString());
 
        db.close();
        
        // 5. return book
        return user;
    }
    
    public User getUser(String fbUserId){
    	 
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
 
        // 2. build query
        Cursor cursor = 
                db.query(TABLE_USERS, // a. table
                COLUMNS, // b. column names
                " fb_user_id = ?", // c. selections 
                new String[] { fbUserId }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
 
        
        Integer c =  cursor.getCount();
      //  Log.i("contador", c.toString());
        // 3. if we got results get the first one
        if ((cursor != null) && (cursor.getCount() > 0)){
           //	Log.i("existe?","si");
            cursor.moveToFirst();
            // 4. build book object
            User user = new User();
            user.setId(Integer.parseInt(cursor.getString(0)));
            user.setFbUserId(cursor.getString(1));
            user.setName(cursor.getString(2));
          //  Log.d("getUser("+fbUserId+")", user.toString());
            cursor.close();
            db.close();
            
            return user;
        }else{
        //	Log.i("existe?","no");
        	 cursor.close();
        	  db.close();
        	 return null;
        }
     
      
    }
    
//    public Boolean existUser(String fbUserId){
//    	// 1. get reference to readable DB
//        SQLiteDatabase db = this.getReadableDatabase();
//        Boolean result = false;
//        // 2. build query
//        Cursor cursor = 
//                db.query(TABLE_USERS, // a. table
//                COLUMNS, // b. column names
//                " fb_user_id = ?", // c. selections 
//                new String[] { fbUserId }, // d. selections args
//                null, // e. group by
//                null, // f. having
//                null, // g. order by
//                null); // h. limit
// 
//        // 3. if we got results get the first one
//        if (cursor != null){
//            cursor.moveToFirst();
//        	result = true;
//        }
//        // 5. return book
//        
//       
//        db.close();
//        return result;
//    }
 
    // Get All Books
    public List<User> getAllUsers() {
        List<User> users = new LinkedList<User>();
 
        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_USERS;
 
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        // 3. go over each row, build book and add it to list
        User user = null;
        if (cursor.moveToFirst()) {
            do {
            	user = new User();
            	user.setId(Integer.parseInt(cursor.getString(0)));
            	user.setFbUserId(cursor.getString(1));
            	user.setName(cursor.getString(2));
 
                // Add book to books
                users.add(user);
            } while (cursor.moveToNext());
        }
 
      //  Log.d("getAllUsers()", users.toString());
        
        db.close();
 
        // return books
        return users;
    }
 
     // Updating single book
    public int updateUser(User user) {
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("fb_user_id", user.getFbUserId()); // get title 
        values.put("name", user.getName()); // get author
 
        // 3. updating row
        int i = db.update(TABLE_USERS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(user.getId()) }); //selection args
 
        // 4. close
        db.close();
 
        return i;
 
    }
 
    // Deleting single book
    public void deleteUser(User user) {
 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_USERS,
                KEY_ID+" = ?",
                new String[] { String.valueOf(user.getId()) });
 
        // 3. close
        db.close();
 
       // Log.d("deleteUser", user.toString());
 
    }
}