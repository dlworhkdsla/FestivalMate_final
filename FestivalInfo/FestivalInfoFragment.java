package com.festival.tacademy.festivalmate.FestivalInfo;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.festival.tacademy.festivalmate.Data.Artist;
import com.festival.tacademy.festivalmate.Data.CheckGoingResult;
import com.festival.tacademy.festivalmate.Data.Festival;
import com.festival.tacademy.festivalmate.Data.FestivalResultResult;
import com.festival.tacademy.festivalmate.Data.Lineup;
import com.festival.tacademy.festivalmate.Data.User;
import com.festival.tacademy.festivalmate.Manager.NetworkManager;
import com.festival.tacademy.festivalmate.Manager.PropertyManager;
import com.festival.tacademy.festivalmate.MateMatching.MateMatchingStartActivity;
import com.festival.tacademy.festivalmate.MyPage.FestibalSearchActivity;
import com.festival.tacademy.festivalmate.R;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * A simple {@link Fragment} subclass.
 */
public class FestivalInfoFragment extends Fragment {


    TextView titleView;
    ViewPager pager;
    FestivalPagerAdapter mAdapter;

    RecyclerView listView;
    FestivalAdapter mAdapter2;

    CirclePageIndicator mIndicator;

    int res;
    public FestivalInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new FestivalPagerAdapter(getChildFragmentManager());
        mIndicator = new CirclePageIndicator(getContext());

        mAdapter2 = new FestivalAdapter();

        mAdapter2.setOnItemClickListener(new FestivalViewHolder.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Festival festival) {

                final  Festival festival1 = festival;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // Toast.makeText(getContext(),festival.getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), FestivalDetailActivity.class);
                        intent.putExtra("festival", festival1);
                        startActivity(intent);
                    }
                }, 250);
            }

            @Override
            public void onItemCheck(final View view, final Festival festival) {
          //     Toast.makeText(getContext(),festival.getMem_going_check()+festival.getFestival_name(), Toast.LENGTH_SHORT).show();
                int memNo = PropertyManager.getInstance().getNo();
                int fesNo = festival.getFestival_no();
                int goingCheck = festival.getMem_going_check();

                NetworkManager.getInstance().check_going(getContext(), memNo, fesNo, goingCheck, new NetworkManager.OnResultListener<CheckGoingResult>() {
                    @Override
                    public void onSuccess(Request request, CheckGoingResult result) {
                        CheckBox checkBox = (CheckBox) view;
                        festival.setMem_going_check(result.result.getMem_going_check());
                    }

                    @Override
                    public void onFail(Request request, IOException exception) {

                    }
                });
            }
        });


        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_festival_info, container, false);
        titleView = (TextView) getActivity().findViewById(R.id.toolbar_title);
        titleView.setText(getResources().getString(R.string.festival_info));
        pager = (ViewPager)view.findViewById(R.id.pager);
        pager.setAdapter(mAdapter);
        listView = (RecyclerView)view.findViewById(R.id.rv_list);
        listView.setAdapter(mAdapter2);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               // Toast.makeText(getContext(), "position : " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mIndicator = (CirclePageIndicator) view.findViewById(R.id.page_indicator);
        mIndicator.setViewPager(pager);
        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        int memNo = PropertyManager.getInstance().getNo();

        NetworkManager.getInstance().show_festival_list(getContext(), memNo, new NetworkManager.OnResultListener<FestivalResultResult>() {
            @Override
            public void onSuccess(Request request, FestivalResultResult result) {
                mAdapter2.clear();
                mAdapter.clear();

                mAdapter.addAll(result.result_promotion);
                mAdapter2.addAll(result.result);
            }

            @Override
            public void onFail(Request request, IOException exception) {
            }
        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.festibal_search){
            startActivity(new Intent(getContext(), FestibalSearchActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
