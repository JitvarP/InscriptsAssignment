package com.example.jitvar.inscriptsassignment.UI;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jitvar.inscriptsassignment.Databases.DBHelper;
import com.example.jitvar.inscriptsassignment.Databases.MessageDAO;
import com.example.jitvar.inscriptsassignment.Model.MessageVO;
import com.example.jitvar.inscriptsassignment.R;
import com.example.jitvar.inscriptsassignment.WebEntities.WeBaseObject;
import com.example.jitvar.inscriptsassignment.WebEntities.WeMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private MessageDAO messageDAO;
    private MessagesAdapter messagesAdapter;
    private ListView mListView;
    private EditText mEditTextMessage;
    private Button mSentBtn;
    private ArrayList<MessageVO> mMessagesList;
    private static final int MESSAGE_LOADER = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] MESSAGE_FROM_FIELDS = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_MESSAGE,
            DBHelper.COLUMN_ROLE,
            DBHelper.COLUMN_TIMESTAMP
    };

    private static final int[] MESSAGE_TO_FIELDS = {
            R.id.message,
            R.id.message2,
            R.id.date1,
            R.id.date2,
            R.id.layout_receiver,
            R.id.layout_sender,
            R.id.root
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageDAO = new MessageDAO(this);
        messageDAO.open();
        mListView = (ListView) findViewById(R.id.messages_list);
        mEditTextMessage = (EditText) findViewById(R.id.new_message);
        mSentBtn = (Button) findViewById(R.id.send_message);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setStackFromBottom(true);

        mMessagesList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(mMessagesList);
        mListView.setAdapter(messagesAdapter);


        mSentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mEditTextMessage.getText().toString();
                if(msg.isEmpty())
                    Toast.makeText(MainActivity.this,"Enter Message to send ",Toast.LENGTH_SHORT).show();
                else{
                    MessageVO messageVO = new MessageVO();
                    messageVO.setRole("sender");
                    messageVO.setMessage(msg);

                    Calendar calendar = Calendar.getInstance();
                    messageVO.setTimestamp(calendar.getTimeInMillis());
                    messageDAO.persistMessage(messageVO);
                    mEditTextMessage.setText("");
                }
                new FetchDataFromDataBase().execute();
            }
        });



        new FetchDataFromDataBase().execute();
        new FetchDataFromServer().execute();
    }

    @Override
    protected void onResume() {
        messageDAO.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        messageDAO.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void persistDataToDB(WeBaseObject weBaseObject) {
        List<WeMessage> messages = weBaseObject.getMessages();
        for (Iterator iterator = messages.iterator(); iterator.hasNext(); ) {
            WeMessage weMessage = (WeMessage) iterator.next();

            MessageVO messageVO = messageDAO.getMessageByTimestamp(weMessage.getTimestamp());

            if(messageVO == null) {
                messageVO = new MessageVO();

                messageVO.setMessage(weMessage.getMessage());
                messageVO.setRole(weMessage.getRole());
                messageVO.setTimestamp(weMessage.getTimestamp());
                messageDAO.persistMessage(messageVO);
            }
        }

        new FetchDataFromDataBase().execute();
    }

    private class FetchDataFromDataBase extends AsyncTask<Void, Void, List<MessageVO>>{

        @Override
        protected List<MessageVO> doInBackground(Void... params) {
            List<MessageVO> messageVOList = messageDAO.getAllMessages();
            Log.i(TAG,"size of message list from db = "+messageVOList.size());
            return messageVOList;
        }

        @Override
        protected void onPostExecute(List<MessageVO> list) {

            mMessagesList.clear();
            for(Iterator iterator = list.iterator();iterator.hasNext();){
                MessageVO messageVO = (MessageVO) iterator.next();
                mMessagesList.add(messageVO);
            }
            messagesAdapter.notifyDataSetChanged();
        }
    }


    private class FetchDataFromServer extends AsyncTask<Void, Void, WeBaseObject> {
        private final String TAG = FetchDataFromServer.class.getSimpleName();
        private HttpURLConnection urlConnection = null;
        private BufferedReader reader = null;
        WeBaseObject responce;

        @Override
        protected WeBaseObject doInBackground(Void... params) {
            try {

                // Url of the server use builder to built url
                URL url = new URL("https://demo7677878.mockable.io/getmessages");

                // Create request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                Reader reader = new InputStreamReader(inputStream);
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                responce = gson.fromJson(reader, WeBaseObject.class);

                Log.i(TAG, "responce size of message list  =" + responce.getMessages().size());
                inputStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return responce;
        }

        @Override
        protected void onPostExecute(WeBaseObject weBaseObject) {
            persistDataToDB(weBaseObject);
        }
    }

    private class MessagesAdapter extends ArrayAdapter<MessageVO> {
        MessagesAdapter(ArrayList<MessageVO> messages) {
            super(MainActivity.this, R.layout.message, R.id.message, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            MessageVO message = getItem(position);

            TextView nameView = (TextView) convertView.findViewById(R.id.message);
            TextView nameView2 = (TextView) convertView.findViewById(R.id.message2);
            TextView time1 = (TextView) convertView.findViewById(R.id.date1);
            TextView time2 = (TextView) convertView.findViewById(R.id.date2);

            nameView.setText(message.getMessage());
            nameView2.setText(message.getMessage());


            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(message.getTimestamp());

            String time = c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE);

            if(c.get(Calendar.AM_PM) == Calendar.AM)
                time = time + " AM";
            else
                time = time +" PM";

            time1.setText(time);
            time2.setText(time);


            RelativeLayout layoutReceiver = (RelativeLayout) convertView.findViewById(R.id.layout_receiver);
            RelativeLayout layoutSender = (RelativeLayout) convertView.findViewById(R.id.layout_sender);
            if (message.getRole().equals("sender")) {
                layoutReceiver.setVisibility(View.GONE);
                layoutSender.setVisibility(View.VISIBLE);
            } else {
                layoutReceiver.setVisibility(View.VISIBLE);
                layoutSender.setVisibility(View.GONE);
            }

            return convertView;
        }
    }


}
