package tof.cv.mpp.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class AlphabeticalAdapter extends ArrayAdapter<String> implements
		SectionIndexer {

	HashMap<String, Integer> alphaIndexer;
	String[] sections;

	public AlphabeticalAdapter(Context context, String[] items) {
		super(context, android.R.layout.simple_list_item_1, items);

		alphaIndexer = new HashMap<String, Integer>();
		int size = items.length;

		for (int x = 0; x < size; x++) {
			String s = items[x];

			// get the first letter of the store
			String ch = s.substring(0, 1);
			// convert to uppercase otherwise lowercase a -z will be sorted
			// after upper A-Z
			ch = ch.toUpperCase();

			// HashMap will prevent duplicates
			alphaIndexer.put(ch, x);
		}

		Set<String> sectionLetters = alphaIndexer.keySet();

		// create a list from the set to sort
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

		Collections.sort(sectionList);

		sections = new String[sectionList.size()];

		sectionList.toArray(sections);
	}

	public int getPositionForSection(int section) {
		return alphaIndexer.get(sections[section]);
	}

	public int getSectionForPosition(int position) {
		return 1;
	}

	public Object[] getSections() {
		return sections;
	}
}
