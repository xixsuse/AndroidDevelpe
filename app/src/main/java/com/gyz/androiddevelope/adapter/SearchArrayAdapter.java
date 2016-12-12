package com.gyz.androiddevelope.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @FileName: com.gyz.androiddevelope.adapter.SearchArrayAdapter.java
 * @author: ZhaoHao
 * @date: 2016-06-30 17:19
 */
public class SearchArrayAdapter extends ArrayAdapter<String> {
    private static final String TAG = "SearchArrayAdapter";

    private Filter mFilter;
    private List<String> mObjects;

    public SearchArrayAdapter(Context context, int simple_spinner_dropdown_item, List<String> mListHttpHint) {
        super(context, simple_spinner_dropdown_item, mListHttpHint);
        this.mObjects = mListHttpHint;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new HintFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     * 重写过滤类 自定义一个不会过滤任何数的Filter
     */
    private class HintFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {


            ArrayList<Object> suggestions = new ArrayList<Object>();
            for (String s : mObjects) {
                suggestions.add(s);
//                Logger.d(s);
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
//            Logger.d("filterResults.count=" + filterResults.count);
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<String>) results.values;
//            Logger.d("results.count=" + results.count);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
