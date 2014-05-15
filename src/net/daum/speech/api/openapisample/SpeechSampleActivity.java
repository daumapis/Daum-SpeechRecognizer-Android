package net.daum.speech.api.openapisample;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.daum.mf.speech.api.SpeechRecognizeListener;
import net.daum.mf.speech.api.SpeechRecognizerManager;
import net.daum.mf.speech.api.SpeechRecognizerClient;
import net.daum.mf.speech.api.SpeechRecognizerActivity;

import java.util.ArrayList;

public class SpeechSampleActivity extends ListActivity implements View.OnClickListener, SpeechRecognizeListener {
    private SpeechRecognizerClient client;

    private final static String apikey = "2b268b18991386c80c9054ab1aee8ce709b3085c";
    private View recordButton;
    private Button nextButton;
    private SampleCursorAdapter listAdapter;

    private final static String COLUMN_WORD = "word";
    private final static int COLUMN_INDEX_WORD = 1;

    private final static int STATUS_READY = 0;
    private final static int STATUS_INITIALIZING = 1;
    private final static int STATUS_RECORDING = 2;

    private final static int MINIMUM_WORD_LIST_SIZE = 2;

    private ArrayList<Object[]> itemList = new ArrayList<Object[]>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // library를 초기화 합니다.
        // API를 사용할 시점이 되었을 때 initializeLibrary(Context)를 호출한다.
        // 사용을 마치면 finalizeLibrary()를 호출해야 한다.
        /*
        SpeechRecognizerManager.getInstance().initializeLibrary(this);
        /**/

        recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(this);
        nextButton = (Button)findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);

        setButtonStatus(STATUS_READY);

        listAdapter = new SampleCursorAdapter(this, null, false);
        getListView().setAdapter(listAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // API를 더이상 사용하지 않을 때 finalizeLibrary()를 호출한다.
        /*
        SpeechRecognizerManager.getInstance().finalizeLibrary();
        /**/
    }

    private static class SampleCursorAdapter extends CursorAdapter {
        public SampleCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.main_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView wordView = (TextView)view.findViewById(R.id.wordText);

            wordView.setText(cursor.getString(COLUMN_INDEX_WORD));
        }
    }

    private void rebuildCursor() {
        String columns[] = {
                BaseColumns._ID,
                COLUMN_WORD,
        };

        MatrixCursor cursor = new MatrixCursor(columns);
        for (Object[] row : itemList) {
            cursor.addRow(row);
        }

        listAdapter.swapCursor(cursor);
        listAdapter.notifyDataSetChanged();
    }

    private void addColumn(String word) {
        itemList.add(new Object[]{itemList.size(), word});
    }

    private void startWordRecording() {
        if (client != null) {
            return;
        }

        // Builder 를 통해 client 를 생성한다.
        /*
        SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder()
                .setApiKey(apikey)
                .setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB);

        client = builder.build();

        client.setSpeechRecognizeListener(this);
        client.startRecording(true);
        /**/
    }

    private void stopWordRecording() {
        if (client == null) {
            return;
        }

        // stopRecording()을 호출하면 음성인식을 멈추고 인식된 음성에 대한 분석을 시작한다.
        /*
        client.stopRecording();
        /**/
    }

    private void setButtonStatus(int status) {
        if (status == STATUS_INITIALIZING) {
            recordButton.setSelected(true);
            recordButton.setEnabled(false);

            nextButton.setEnabled(false);
            nextButton.setText("");
        }
        else if (status == STATUS_RECORDING) {
            recordButton.setSelected(true);
            recordButton.setEnabled(true);
        }
        else { // if (status == STATUS_READY) {
            recordButton.setSelected(false);
            recordButton.setEnabled(true);

            nextButton.setEnabled(true);
            nextButton.setText(R.string.next_button);

            View backPanel = findViewById(R.id.record_button_panel);
            backPanel.setAlpha(1.f);
        }
    }

    private String buildWordList() {
        StringBuilder wordList = new StringBuilder();

        for (Object[] item : itemList) {
            wordList.append(item[COLUMN_INDEX_WORD].toString());
            wordList.append('\n');
        }

        return wordList.toString();
    }

    private void openVoiceRecoActivityWithWordList(String wordList) {
        // 기본으로 제공되는 음성인식 activity를 통해 단어인식 음성인식을 호출한다.
        // VoiceRecoActivity는 sample 로 제공되는 class와 동일
        /*
        Intent i = new Intent(getApplicationContext(), VoiceRecoActivity.class);

        i.putExtra(SpeechRecognizerActivity.EXTRA_KEY_SERVICE_TYPE, SpeechRecognizerClient.SERVICE_TYPE_WORD);
        i.putExtra(SpeechRecognizerActivity.EXTRA_KEY_USER_DICTIONARY, wordList);

        startActivityForResult(i, 0);
        /**/
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this).
                setMessage(message).
                setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).
                show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(VoiceRecoActivity.EXTRA_KEY_RESULT_ARRAY);

            // 받은 결과를 dialog 로 모두 표시
            // 첫번째 값이 가장 신뢰도가 높은 값이다.
            StringBuilder builder = new StringBuilder();
            if (results != null) {
                for (String result : results) {
                    builder.append(result);
                    builder.append("\n");
                }
            }

            showAlertDialog(builder.toString());
        }
        else if (requestCode == RESULT_CANCELED) {
            // 음성인식의 오류 등이 아니라 activity의 취소가 발생했을 때.
            if (data == null) {
                return;
            }

            int errorCode = data.getIntExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_CODE, -1);
            String errorMsg = data.getStringExtra(VoiceRecoActivity.EXTRA_KEY_ERROR_MESSAGE);

            if (errorCode != -1 && !TextUtils.isEmpty(errorMsg)) {
                showAlertDialog(errorMsg);
            }
        }
    }

    @Override
    public void onReady() {
        setButtonStatus(STATUS_RECORDING);
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int errorCode, final String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SpeechSampleActivity.this, errorMsg, Toast.LENGTH_SHORT).show();

                setButtonStatus(STATUS_READY);
            }
        });

        // startWordRecording() 에서 client 의 null 여부로 인식 중인 것을 체크
        client = null;
    }

    @Override
    public void onPartialResult(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nextButton.setText(text);
            }
        });
    }

    @Override
    public void onResults(final Bundle results) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);

                if (texts == null) {
                    showAlertDialog("empty result");
                    return;
                }

                if (results.getBoolean(SpeechRecognizerClient.KEY_IS_MARKED_RESULT)) {
                    addColumn(texts.get(0));
                    rebuildCursor();
                }
                else {
                    WordSelectionDialog dialog = new WordSelectionDialog(SpeechSampleActivity.this);
                    dialog.setWordList(texts);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            addColumn(((WordSelectionDialog)dialog).getSelectedWord());
                            rebuildCursor();
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onAudioLevel(final float v) {
        View backPanel = findViewById(R.id.record_button_panel);
        backPanel.setAlpha(v);
    }

	@Override
	public void onFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setButtonStatus(STATUS_READY);
            }
        });

        // startWordRecording() 에서 client 의 null 여부로 인식 중인 것을 체크
        client = null;
	}

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.record_button) {
            // 인식 대기중
            if (!recordButton.isSelected()) {
                startWordRecording();

                setButtonStatus(STATUS_INITIALIZING);
            }
            // 인식중
            else {
                stopWordRecording();
            }
        }
        else if (id == R.id.next_button) {
            if (itemList.size() < MINIMUM_WORD_LIST_SIZE) {
                String message = getResources().getString(R.string.word_list_size_message, MINIMUM_WORD_LIST_SIZE);

                showAlertDialog(message);
                return;
            }

            // 단어목록 중 인식 기능
            openVoiceRecoActivityWithWordList(buildWordList());
        }
    }
}
