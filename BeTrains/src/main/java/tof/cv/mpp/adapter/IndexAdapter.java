package tof.cv.mpp.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import tof.cv.mpp.Utils.ConnectionMaker;

/**
 * Created by versieuxchristophe on 24/10/15.
 */
public class IndexAdapter extends ArrayAdapter<String> implements SectionIndexer, Filterable {
    private HashMap<String, Integer> alphaIndexer;
    private ArrayList<String> sections = new ArrayList<>();
    ArrayList<String> list;
    ArrayList<String> data = new ArrayList<>();
    ContainsFilter myFilter;
    boolean filtered;

    @Override
    public Filter getFilter() {
        if (myFilter == null)
            myFilter = new ContainsFilter();
        return myFilter;
    }

    public IndexAdapter(Context c, int resource, ArrayList<String> pData) {
        super(c, resource, pData);
        this.data = pData;
        this.list = (ArrayList<String>) pData.clone();
        alphaIndexer = new HashMap<>();
        sections.clear();
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i).substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s)) {
                alphaIndexer.put(s, i);
                sections.add(s);
            }
        }
/*
        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        //Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        for (int i = 0; i < sectionList.size(); i++){
            Log.e("CVE",sectionList.get(i));
            sections[i] = sectionList.get(i);
        }*/

    }

    public int getPositionForSection(int section) {
        try {
            return alphaIndexer.get(sections.get(section));
        } catch (Exception e) {
            return 0;
        }
    }

    public int getSectionForPosition(int position) {
        return 1;
    }

    public Object[] getSections() {
        String[] array = new String[sections.size()];
        sections.toArray(array);
        return array;
    }
    public String unaccent(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "");
    }
    private class ContainsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String toFilter = constraint.toString();

            FilterResults filterResults = new FilterResults();
            ArrayList<String> tempList = new ArrayList<>();
            int length = list.size();
            int add = 0;
            filtered = false;
            for (int i = 0; i < length; i++) {
                String obj = list.get(i);
                Log.e("CVE", obj);
                if (unaccent(obj.toLowerCase()).contains(unaccent(toFilter.toLowerCase()))) {
                    if (toFilter.length() > 0 && obj.toLowerCase().startsWith(toFilter.toLowerCase())) {
                        tempList.add(add, obj);
                        add++;
                        filtered = true;
                    } else
                        tempList.add(obj);
                }

            }
            Collections.sort(tempList);
            filterResults.values = tempList;
            filterResults.count = tempList.size();

            return filterResults;

        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.count > 0) {

                alphaIndexer = new HashMap<>();
                ArrayList<String> items = (ArrayList<String>) results.values;

                for (int i = 0; i < items.size(); i++) {
                    String s = items.get(i).substring(0, 1).toUpperCase();
                    if (constraint.length() > 0 && items.get(i).toUpperCase().startsWith(constraint.toString().toUpperCase()))
                        s = constraint.toString().toUpperCase();
                    sections.clear();
                    if (!alphaIndexer.containsKey(s)) {
                        alphaIndexer.put(s, i);
                        sections.add(s);
                    }
                }
                addAll((ArrayList<String>) results.values);

            } else {
                //Log.println(Log.INFO, "Results !!", "NONE");

            }

            notifyDataSetChanged();
        }

    }

    public class CustomComparator implements Comparator<String> {

        String s;

        public CustomComparator(String s) {
            this.s = s.toUpperCase();
        }

        @Override
        public int compare(String o1, String o2) {
            if (s.length() > 0 && o1.toUpperCase().startsWith(s) && !o2.toUpperCase().startsWith(s)) {
                // Log.e("CVE", "START o1 " + o1);
                return -1;

            }


            if (s.length() > 0 && o2.toUpperCase().startsWith(s) && !o1.toUpperCase().startsWith(s)) {
                // Log.e("CVE", "START o2 " + o2);
                return 1;
            }

            //Log.e("CVE", "COMPARE " + o2.compareTo(o2));
            return o1.compareTo(o2);
        }
    }
}