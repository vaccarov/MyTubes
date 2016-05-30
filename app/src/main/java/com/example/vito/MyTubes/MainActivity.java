package com.example.vito.MyTubes;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.vito.MyTubes.fragments.DownloadFragment;
import com.example.vito.MyTubes.fragments.ListFragment;
import com.example.vito.MyTubes.fragments.LyricsFragment;

import java.util.ArrayList;
import java.util.List;

// Toast.makeText(getApplicationContext(),"texte",Toast.LENGTH_SHORT).show();
// Log.i(TAG,"texte");
public class MainActivity  extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private static final String TAG = "debug mainActivity"; // pour identifier les logs
    private static final int PERMISSIONS_READ = 13; // pour identifier la permission
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private GlobalState gs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gs = (GlobalState) getApplication();
        allowPermission();

        Intent in = getIntent();
        String receivedAction = in.getAction();
        if (receivedAction.equals(Intent.ACTION_SEND)) {
            Log.i("ok", "action send"); // lancement depuis intent
            String shortLink = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
            String idLink = shortLink.substring(shortLink.length() - 11);
            String correctLink = "https://www.youtube.com/watch?v=" + idLink;
            Log.i("ok", correctLink);
//            DownloadFragment df = new DownloadFragment();
//            df.telecharge(correctLink);
            new tacheDeFond().execute();
        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            Log.i("ok", "action main"); // lancement normal
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private class tacheDeFond extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Log.i("okokok","doitinbg");
            gs.requete("https://www.youtube.com/watch?v=xXkjfnhqRGI");
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("okokok","postexec");
        }
    }
    //connect to the service
    public ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            gs.musicSrv = binder.getService();
            //pass list
            gs.musicSrv.setList(gs.songList);
            gs.musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            gs.musicBound = false;
        }
    };

    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(gs.playIntent==null){
            gs.playIntent = new Intent(this, MusicService.class);
            bindService(gs.playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(gs.playIntent);
        }
    }

    private void setUpToolbars() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ListFragment(), "List");
        adapter.addFragment(new DownloadFragment(), "Download");
        adapter.addFragment(new LyricsFragment(), "Lyrics");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                gs.musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(gs.playIntent);
                gs.musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // demande de droits pour lire la carte sd
    private void allowPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ);
        } else setUpToolbars();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpToolbars();
                } else {
                    Toast.makeText(getApplicationContext(),"We need access for reading memory",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_READ);
                }
                return;
            }
        }
    }
}
