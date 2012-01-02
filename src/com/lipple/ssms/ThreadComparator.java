package com.lipple.ssms;

import java.util.Comparator;

import com.lipple.ssms.entities.Message;

public class ThreadComparator implements Comparator<Message> {

	public int compare(Message object1, Message object2) {
		return object1.getDate().compareTo(object2.getDate());
	}

}
