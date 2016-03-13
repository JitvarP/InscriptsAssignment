package com.example.jitvar.inscriptsassignment.UI;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MessageDAO messageDAO;
    private MessagesAdapter messagesAdapter;
    private ListView mListView;
    private ArrayList<MessageVO> mMessagesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageDAO = new MessageDAO(this);
        messageDAO.open();
        mListView = (ListView)findViewById(R.id.messages_list);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mListView.setStackFromBottom(true);

        mMessagesList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(mMessagesList);
        mListView.setAdapter(messagesAdapter);


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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void persistDataToDB(WeBaseObject weBaseObject){
        List<WeMessage> messages = weBaseObject.getMessages();
        for(Iterator iterator = messages.iterator();iterator.hasNext();){
            WeMessage weMessage = (WeMessage) iterator.next();
            MessageVO messageVO = new MessageVO();
            messageVO.setMessage(weMessage.getMessage());
            messageVO.setRole(weMessage.getRole());
            messageVO.setTimestamp(weMessage.getTimestamp());
            messageDAO.persistMessage(messageVO);
            mMessagesList.add(messageVO);
        }

        messagesAdapter.notifyDataSetChanged();
    }


    private class FetchDataFromServer extends AsyncTask<Void,Void,WeBaseObject> {
        private final String TAG = FetchDataFromServer.class.getSimpleName();
        private HttpURLConnection urlConnection = null;
        private BufferedReader reader = null;
        WeBaseObject responce ;

        @Override
        protected WeBaseObject doInBackground(Void... params) {
            try {

                // Url of the server use builder to built url
                URL url = new URL(" https://demo7677878.mockable.io/getmessages ");

                // Create request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                Reader reader = new InputStreamReader(inputStream);
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                responce = gson.fromJson(reader,WeBaseObject.class);

                Log.i(TAG, "responce size of message list  =" + responce.getMessages().size());
                inputStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }if (reader != null) {
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
        MessagesAdapter(ArrayList<MessageVO> messages){
            super(MainActivity.this, R.layout.message, R.id.message, messages);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);
            MessageVO message = getItem(position);

            TextView nameView = (TextView)convertView.findViewById(R.id.message);
            nameView.setText(message.getMessage());

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)nameView.getLayoutParams();

            if (message.getRole().equals("sender")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        nameView.setBackground(getResources().getDrawable(R.drawable.in_message_bg));
                    }
                else{
                    nameView.setBackgroundDrawable(getResources().getDrawable(R.drawable.in_message_bg));
                }
                layoutParams.gravity = Gravity.RIGHT;
            }else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        nameView.setBackground(getResources().getDrawable(R.drawable.out_message_bg));
                    } else{
                    nameView.setBackgroundDrawable(getResources().getDrawable(R.drawable.out_message_bg));
                }
                layoutParams.gravity = Gravity.LEFT;
            }

            nameView.setLayoutParams(layoutParams);


            return convertView;
        }
    }
}
