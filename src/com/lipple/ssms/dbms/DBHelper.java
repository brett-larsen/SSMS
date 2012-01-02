package com.lipple.ssms.dbms;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.lipple.ssms.entities.Contact;
import com.lipple.ssms.entities.Message;

public class DBHelper extends OrmLiteSqliteOpenHelper {
	
	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "helloAndroid.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;
	
	//Store some state to pass between activities
	private static Contact selectedContact = null;

	// the DAO object we use to access the SimpleData table
	//private Dao<SimpleData, Integer> simpleDao = null;
	//private RuntimeExceptionDao<SimpleData, Integer> simpleRuntimeDao = null;
	
	// the DAO object we use to access the SimpleData table
	private Dao<Contact, Integer> contactDao = null;
	private Dao<Message, Integer> messageDao = null;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			
			Log.i(DBHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Contact.class);
			TableUtils.createTable(connectionSource, Message.class);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}


	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
	{
		try {
			Log.i(DBHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Contact.class, true);
			TableUtils.dropTable(connectionSource, Message.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DBHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Contact, Integer> getContactDao() throws SQLException {
		if (contactDao == null) {
			contactDao = getDao(Contact.class);
		}
		return contactDao;
	}

	public Dao<Message, Integer> getMessageDao() throws SQLException {
		if (messageDao == null) {
			messageDao = getDao(Message.class);
		}
		return messageDao;
	}

	public Contact getSelectedContact() {
		return selectedContact;
	}

	public void setSelectedContact(Contact selectedContact) {
		this.selectedContact = selectedContact;
	}
	
	
}
