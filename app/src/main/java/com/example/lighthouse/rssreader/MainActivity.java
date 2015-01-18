package com.example.lighthouse.rssreader;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    static final String RSS_URL="http://rss.weather.yahoo.co.jp/rss/days/14.xml";

    ArrayList<Map<String,String>> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void refreshListView(){

        String[] keys ={"title","description"};
        int[] ids = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter =
                new SimpleAdapter(this,list,android.R.layout.simple_list_item_2,keys,ids);

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

    }

    public void show(View v){



        AsyncTask<String,Integer,Integer> task = new AsyncTask<String, Integer, Integer>() {

            ProgressDialog pd ;

            @Override
            protected void onPreExecute() {
                pd = new ProgressDialog(MainActivity.this);
                pd.setTitle("しばらくお待ちください");
                pd.setMessage("通信中...");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(true);


                pd.show();
            }

            @Override
            protected Integer doInBackground(String... params) {

                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet();
                try{
                    get.setURI(new URI(RSS_URL));

                    HttpResponse res = client.execute(get);

                    InputStream in = res.getEntity().getContent();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    XmlPullParserFactory xpf = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xpf.newPullParser();

                    parser.setInput(br);

                    int type = parser.getEventType();

                    HashMap<String,String> map = null;
                    String title="";
                    String description="";
                    boolean isItem = false;

                    while(type != XmlPullParser.END_DOCUMENT){
                        if( type == XmlPullParser.START_TAG){

                            if( parser.getName().equals(("item"))){
                                map = new HashMap<String,String>();
                                isItem = true;

                            }else if(parser.getName().equals("title")){
                                if( isItem){
                                    title = parser.nextText();
                                    map.put("title",title);
                                }

                            }else if(parser.getName().equals("description")){
                                if( isItem){
                                    description = parser.nextText();
                                    map.put("description",description);


                                }
                            }


                        }else if(type == XmlPullParser.END_TAG){
                            if(parser.getName().equals("item")){
                                list.add(map);
                                isItem = false;
                            }
                        }
                        type = parser.next();

                    }



                }catch(Exception e){
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                refreshListView();
                pd.dismiss();
            }
        };

        task.execute();

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
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
