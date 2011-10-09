package tof.cv.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class MyIndexAdapter<T> extends ArrayAdapter<T> implements
		SectionIndexer {

	private ArrayList<String> myElements;
	private HashMap<String, Integer> alphaIndexer;

	private String[] sections;

	public MyIndexAdapter(Context context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
		myElements = extracted(objects);
		// here is the tricky stuff
		alphaIndexer = new HashMap<String, Integer>();
		// in this hashmap we will store here the positions for
		// the sections

		// int size = elements.size();
		int size = myElements.size();
		for (int i = size - 1; i >= 0; i--) {
			String element = myElements.get(i);
			alphaIndexer.put(element.substring(0, 1), i);
			// We store the first letter of the word, and its index.
			// The Hashmap will replace the value for identical keys are putted
			// in
		}

		// now we have an hashmap containing for each first-letter
		// sections(key), the index(value) in where this sections begins

		// we have now to build the sections(letters to be displayed)
		// array .it must contains the keys, and must (I do so...) be
		// ordered alphabetically

		Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
		// cannot be sorted...

		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>(); // list can be
		// sorted

		while (it.hasNext()) {
			String key = it.next();
			keyList.add(key);
		}

		Collections.sort(keyList);

		sections = new String[keyList.size()]; // simple conversion to an
		// array of object
		keyList.toArray(sections);

	}

	@SuppressWarnings("unchecked")
	private ArrayList<String> extracted(List<T> objects) {
		return (ArrayList<String>) objects;
	}

	public int getPositionForSection(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSectionForPosition(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object[] getSections() {
		// TODO Auto-generated method stub
		return null;
	}

}
