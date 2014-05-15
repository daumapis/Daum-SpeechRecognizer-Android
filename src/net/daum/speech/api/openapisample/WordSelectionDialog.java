package net.daum.speech.api.openapisample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class WordSelectionDialog extends Dialog implements View.OnClickListener,
        AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private WordListAdapter mAdapter;
    private String selectedWord;

    public WordSelectionDialog(Context context) {
        super(context, R.style.Theme_NoTitle_Dialog);
    }

    public void setWordList(List<String> list) {
        if (mAdapter == null) {
            mAdapter = new WordListAdapter();
        }

        mAdapter.setWordList(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_word_selection);

        ListView listView = (ListView) findViewById(R.id.word_list);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        listView.setAdapter(mAdapter);

        findViewById(android.R.id.button1).setOnClickListener(this);
        findViewById(android.R.id.button2).setOnClickListener(this);
    }

    private static class WordListAdapter extends BaseAdapter {
        private List<String> wordList = new ArrayList<String>();
        private int selectedIndex = -1;

        public void setWordList(List<String> list) {
            wordList = list;
        }

        public void setSelectedIndex(int i) {
            selectedIndex = i;
        }

        @Override
        public int getCount() {
            return wordList.size();
        }

        @Override
        public Object getItem(int position) {
            return wordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String word = wordList.get(position);

            if (convertView == null) {
                Context context = parent.getContext();
                convertView = LayoutInflater.from(context).inflate(R.layout.word_selection_item, null);
            }

            TextView wordText = (TextView)convertView.findViewById(R.id.wordText);
            View backPanel = convertView.findViewById(R.id.selected_panel);

            wordText.setText(word);
            backPanel.setSelected(selectedIndex == position);

            return convertView;
        }
    }

    public String getSelectedWord() {
        return selectedWord;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == android.R.id.button1) {
            dismiss();
        }
        else if (v.getId() == android.R.id.button2) {
            selectedWord = null;
            dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        selectedWord = (String) mAdapter.getItem(i);

        findViewById(android.R.id.button1).setEnabled(true);

        mAdapter.setSelectedIndex(i);
        mAdapter.notifyDataSetChanged();
    }

   @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // do nothing.
    }
}
