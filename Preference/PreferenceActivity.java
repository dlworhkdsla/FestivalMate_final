package com.festival.tacademy.festivalmate.Preference;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.festival.tacademy.festivalmate.Data.Artist;
import com.festival.tacademy.festivalmate.Data.MySignUpResult;
import com.festival.tacademy.festivalmate.Data.PreferenceArtist;
import com.festival.tacademy.festivalmate.Data.ShowArtistSurveyResult;
import com.festival.tacademy.festivalmate.HomeActivity;
import com.festival.tacademy.festivalmate.Manager.NetworkManager;
import com.festival.tacademy.festivalmate.Manager.PropertyManager;
import com.festival.tacademy.festivalmate.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class PreferenceActivity extends AppCompatActivity {

    RecyclerView listView;
    PreferenceAdapter mAdapter;
    Toolbar toolbar;
    EditText editSearch;
    List<Artist> artistList;
    List<Artist> selectedArtist;
    Button btn_complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        btn_complete = (Button)findViewById(R.id.btn_complete);

        mAdapter = new PreferenceAdapter();
        artistList = new ArrayList<Artist>();
        selectedArtist = new ArrayList<Artist>();

        mAdapter.setOnItemClickListener(new PreferenceViewHolder.OnItemClickListener() {

            @Override
            public void onItemClick(View view, Artist artist) {

                selectedArtist.clear();
                if(artist.isCheck()==1) {
                    for (int i = 0; i < artistList.size(); i++) {
                        if (artist.getArtist_no() == artistList.get(i).getArtist_no())
                            artistList.get(i).setCheck(0);
                    }
                    artist.setCheck(0);
                }
                else
                    artist.setCheck(1);

               for(int i = 0; i < artistList.size(); i++){
                   if(artistList.get(i).isCheck() == 1){
                       selectedArtist.add(artistList.get(i));
                   }
               }

                if(selectedArtist.size() >= 10){
                    btn_complete.setVisibility(View.VISIBLE);
                }
                else if(selectedArtist.size() < 10 ) {
                    btn_complete.setVisibility(View.GONE);
                }

            }
        });


        listView = (RecyclerView)findViewById(R.id.rv_list);
        listView.setAdapter(mAdapter);
        listView.setLayoutManager(new GridLayoutManager(this,3));

        editSearch= (EditText)findViewById(R.id.edit_search);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        setData();

        Button btn = (Button)findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() { // 검색
            @Override
            public void onClick(View v) {
                String name =editSearch.getText().toString();
                int memNo = PropertyManager.getInstance().getNo();

                    NetworkManager.getInstance().searchArtistSurvey(PreferenceActivity.this, memNo, name, new NetworkManager.OnResultListener<ShowArtistSurveyResult>() {
                        @Override
                        public void onSuccess(Request request, ShowArtistSurveyResult result) {
                          //  Toast.makeText(PreferenceActivity.this,"성공",Toast.LENGTH_SHORT).show();
                            mAdapter.clear();
                            artistList.addAll(result.result);
                            mAdapter.addAll(result.result);

                            // artistList=result.result;
                        }

                        @Override
                        public void onFail(Request request, IOException exception) {
                    //        Toast.makeText(PreferenceActivity.this,"실패"+exception.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

               }
        });

        btn_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int memNo = PropertyManager.getInstance().getNo();
                NetworkManager.getInstance().saveArtistSurvey(PreferenceActivity.this, memNo, selectedArtist, new NetworkManager.OnResultListener<MySignUpResult>() {
                    @Override
                    public void onSuccess(Request request, MySignUpResult result) {
                      //  Toast.makeText(PreferenceActivity.this,"성공",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PreferenceActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFail(Request request, IOException exception) {

                       // Toast.makeText(PreferenceActivity.this,"실패"+exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void setData() {
        int memNo = PropertyManager.getInstance().getNo();

        NetworkManager.getInstance().showArtistSurvey(PreferenceActivity.this, memNo, new NetworkManager.OnResultListener<ShowArtistSurveyResult>() {
            @Override
            public void onSuccess(Request request, ShowArtistSurveyResult result) {
                mAdapter.clear();
                mAdapter.addAll(result.result);

                artistList = result.result;

            }

            @Override
            public void onFail(Request request, IOException exception) {
            }
        });
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        setData();

        if(mAdapter.getItemCount() >= 10){
            btn_complete.setVisibility(View.VISIBLE);
        }
        else if(mAdapter.getItemCount() < 10 ) {
            btn_complete.setVisibility(View.GONE);
        }
    }
}
