package com.wdj.mankai.ui.main;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.wdj.mankai.R;
import com.wdj.mankai.data.model.AppHelper;
import com.wdj.mankai.ui.mypage.ViewPagerAdapter;
import com.wdj.mankai.ui.mypage.toolbar.FragMyMemoExceptToolbar;
import com.wdj.mankai.ui.mypage.toolbar.FragMyMemoToolbar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageFragment extends Fragment {
    private View view;
    private ViewPagerAdapter viewPagerAdapter;
    String userName;
    String userDescription;
    String userProfile;
    String userId;
    boolean first = true;
    String url;




    public static MyPageFragment newInstance(){
        MyPageFragment fragMyPageHead = new MyPageFragment();

        return fragMyPageHead;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_my_page,container,false);


        url = "https://api.mankai.shop/api/";
        AppHelper.requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        TextView userNameView = (TextView) view.findViewById(R.id.userName);
        TextView userDescriptionView = (TextView) view.findViewById(R.id.userDescription);
        CircleImageView userProfileView = (CircleImageView) view.findViewById(R.id.userProfile);

        if(getArguments() != null){

            userName = getArguments().getString("userName");
            userDescription = getArguments().getString("userDescription");
            userProfile = getArguments().getString("userProfile");
            userId = getArguments().getString("userId");

            userNameView.setText(userName);
            userDescriptionView.setText(userDescription);
            Glide.with(this).load(userProfile).placeholder(R.drawable.ic_launcher_foreground).dontAnimate().into(userProfileView);
        }

//      ???????????? ??????
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(userId,getActivity().getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);





        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if(position == 0 && first){
                    onPageSelected(0);
                    first = false;
                }
                /* ????????? 1?????? ??????(????????? ????????? ?????? ????????? onPageSelected?????? ???????????????
                *  ????????? ?????? ?????????????????? onPageSelected??? ???????????? ?????? ????????? ?????????
                *  onPageScrolled??? ???????????? ???????????? ?????? ??????*/

            }
//            onPageScrolled??? ???????????? ?????? ????????? ??? ??? ???????????? ?????????????????? ????????? ?????? ????????????.


            /*?????? ????????? ????????? ?????????*/
            @Override
            public void onPageSelected(int position) {
                if(position == 0||position == 1||position == 2||position ==3){
                    FragMyMemoExceptToolbar fragMyMemoExceptToolbar = new FragMyMemoExceptToolbar();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.my_page_head,fragMyMemoExceptToolbar).commit();
                }else{
                    FragMyMemoToolbar fragMyMemoToolbar = new FragMyMemoToolbar();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.my_page_head,fragMyMemoToolbar).commit();
                }
            }


            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i("onPageScrollStateChanged","onPageScrollStateChanged"+state);
            }
        });

        return view;
    }


}