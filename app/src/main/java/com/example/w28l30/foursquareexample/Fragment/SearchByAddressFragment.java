package com.example.w28l30.foursquareexample.Fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.lib.WheelView;
import com.example.w28l30.foursquareexample.Activity.SetDinnerActivity;
import com.example.w28l30.foursquareexample.Adapter.NearByPlacesAdapter;
import com.example.w28l30.foursquareexample.AppController;
import com.example.w28l30.foursquareexample.Model.FoursquareVenue;
import com.example.w28l30.foursquareexample.R;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchByAddressFragment extends android.support.v4.app.Fragment implements View.OnClickListener {
    private ArrayList<String> options1Items = new ArrayList<String>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<ArrayList<String>>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<ArrayList<ArrayList<String>>>();

    private ArrayList<String> optionsSection = new ArrayList<String>();

    private ArrayList<String> optionsSortByPrice = new ArrayList<String>();

    private ProgressDialog pDialog;

    private SearchBox search;
    private Toolbar toolbar;

    private Button btnLocation;
    private Button btnSortByPrice;
    private Button btnSection;

    private String city = "New%20Brunswick";
    private String state = "NJ";
    private String section = null;
    private String priceNum = "1,2,3,4";

    View vMasker;

    OptionsPickerView popupWindowSection;
    OptionsPickerView popupWindowLocation;
    OptionsPickerView popupWindowSortbyPrice;

    private static String TAG = SearchByAddressFragment.class.getSimpleName();

    private final String CLIENT_ID = "U3VRM3SJ1S3MXCZL0EM0GDFUHK0W2GOTUJVWBIDH3QRVOTHS";
    private final String CLIENT_SECRET = "AFRE3BAJT40TIAJQGG0GDVJUYUG1KVDMAJM2SXJTXB2KYKA3";

    private NearByPlacesAdapter mAdapter;
    private SwipeMenuListView mListView;
    private ArrayList<FoursquareVenue> mVenueList;


    public SearchByAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_by_address, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firstCall() {
        String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                + CLIENT_ID
                + "&client_secret="
                + CLIENT_SECRET
                + "&v=20150815&near="
                + city
                + ","
                + state
                + "&section="
                + section
                + "&venuePhotos=1"
                + "&price="
                + priceNum;
        urlJsonObj = urlJsonObj.replaceAll(" ", "%20");
        // Showing progress dialog before making http request
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Loading...");
        pDialog.show();
        mVenueList.clear();
        makeJsonObjectRequest(urlJsonObj);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        mListView = (SwipeMenuListView) view.findViewById(R.id.lvSpecificAddress);
        mVenueList = new ArrayList<FoursquareVenue>();
        mAdapter = new NearByPlacesAdapter(getContext(), mVenueList);
        mListView.setAdapter(mAdapter);


        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        search = (SearchBox) view.findViewById(R.id.searchbox);
        search.enableVoiceRecognition(this);

        vMasker = getActivity().findViewById(R.id.vMasker);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openSearch();
                return true;
            }
        });


        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu swipeMenu) {
                SwipeMenuItem addItem = new SwipeMenuItem(
                        getContext());
                // set item background
                addItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                addItem.setWidth(dp2px(90));
                addItem.setTitleColor(Color.WHITE);
                addItem.setTitleSize(18);
                addItem.setTitle("Add");
                // add to menu
                swipeMenu.addMenuItem(addItem);
            }
        };

        initializeButton(view);
        initializePopupWindowLocation();
        initializePopupWindowSection();
        initializeinitializePopupWindowSortByPrice();

        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                FoursquareVenue item = mVenueList.get(position);
                switch (index) {
                    case 0:
                        // add
                        Intent intent = new Intent(getActivity(), SetDinnerActivity.class);
                        intent.putExtra("restaurant", item.getName());
                        intent.putExtra("address", item.getAddress());
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        firstCall();
    }

    private void initializeButton(View view) {
        btnLocation = (Button) view.findViewById(R.id.btnLocation);
        btnSortByPrice = (Button) view.findViewById(R.id.btnSortByPrice);
        btnSection = (Button) view.findViewById(R.id.btnSection);

        btnLocation.setOnClickListener(this);
        btnSortByPrice.setOnClickListener(this);
        btnSection.setOnClickListener(this);

    }

    private void initializeinitializePopupWindowSortByPrice() {
        popupWindowSortbyPrice = new OptionsPickerView(getActivity());

        optionsSortByPrice.add(" Any ");
        optionsSortByPrice.add(" < $10 ");
        optionsSortByPrice.add("$10 - $20 ");
        optionsSortByPrice.add("$20 - $30 ");
        optionsSortByPrice.add(" > $30 ");

        popupWindowSortbyPrice.setTitle("choose a range");

        popupWindowSortbyPrice.setPicker(optionsSortByPrice);
        popupWindowSortbyPrice.setSelectOptions(0);
        popupWindowSortbyPrice.setCyclic(false);
        btnSortByPrice.setText(optionsSortByPrice.get(0));

        popupWindowSortbyPrice.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                priceNum = String.valueOf(options1);
                String price = optionsSortByPrice.get(options1);
                btnSortByPrice.setText(price);
                vMasker.setVisibility(View.GONE);
                String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                        + CLIENT_ID
                        + "&client_secret="
                        + CLIENT_SECRET
                        + "&v=20150815&near="
                        + city
                        + ","
                        + state
                        + "&section="
                        + section
                        + "&venuePhotos=1"
                        + "&price="
                        + priceNum;
                urlJsonObj = urlJsonObj.replaceAll(" ", "%20");
                // Showing progress dialog before making http request
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Loading...");
                pDialog.show();
                mVenueList.clear();
                makeJsonObjectRequest(urlJsonObj);

                System.out.println(urlJsonObj);
            }
        });

    }


    private void initializePopupWindowSection() {
        popupWindowSection = new OptionsPickerView(getActivity());

        optionsSection.add("any");
        optionsSection.add("food");
        optionsSection.add("drinks");
        optionsSection.add("coffee");
        optionsSection.add("shops");
        optionsSection.add("topPicks");
        optionsSection.add("arts");
        optionsSection.add("outdoors");
        optionsSection.add("sights");
        optionsSection.add("specials");

        popupWindowSection.setTitle("Choose a section");


        //三级联动效果
        popupWindowSection.setPicker(optionsSection);
        popupWindowSection.setCyclic(false);
        //设置默认选中的三级项目
        popupWindowSection.setSelectOptions(0);
        btnSection.setText(optionsSection.get(0));
        popupWindowSection.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                section = optionsSection.get(options1);
                btnSection.setText(section);
                vMasker.setVisibility(View.GONE);
                String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                        + CLIENT_ID
                        + "&v=20150815&client_secret="
                        + CLIENT_SECRET
                        + "&near="
                        + city
                        + ","
                        + state
                        + "&section="
                        + section
                        + "&venuePhotos=1"
                        + "&price="
                        + priceNum;
                urlJsonObj = urlJsonObj.replaceAll(" ", "%20");
                // Showing progress dialog before making http request
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Loading...");
                pDialog.show();
                mVenueList.clear();
                makeJsonObjectRequest(urlJsonObj);

                System.out.println(urlJsonObj);
            }
        });
    }


    private void initializePopupWindowLocation() {
        popupWindowLocation = new OptionsPickerView(getActivity());

        options1Items.add("NJ");
        options1Items.add("NY");

        //选项2
        ArrayList<String> options2Items_01 = new ArrayList<String>();
        options2Items_01.add("Middlesex");
        options2Items_01.add("Mercer");
        options2Items_01.add("Somerset");
        ArrayList<String> options2Items_02 = new ArrayList<String>();
        options2Items_02.add("New York");
        options2Items_02.add("Orange County");
        options2Items.add(options2Items_01);
        options2Items.add(options2Items_02);

        //选项3
        ArrayList<ArrayList<String>> options3Items_01 = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> options3Items_02 = new ArrayList<ArrayList<String>>();
        ArrayList<String> options3Items_01_01 = new ArrayList<String>();
        options3Items_01_01.add("New Brunswick");
        options3Items_01_01.add("Edison");
        options3Items_01_01.add("Highland Park");
        options3Items_01_01.add("Piscataway");
        options3Items_01.add(options3Items_01_01);
        ArrayList<String> options3Items_01_02 = new ArrayList<String>();
        options3Items_01_02.add("Lawrence");
        options3Items_01_02.add("Trenton");
        options3Items_01.add(options3Items_01_02);
        ArrayList<String> options3Items_01_03 = new ArrayList<String>();
        options3Items_01_03.add("Franklin");
        options3Items_01_03.add("Bound Brook");
        options3Items_01.add(options3Items_01_03);

        ArrayList<String> options3Items_02_01 = new ArrayList<String>();
        options3Items_02_01.add("Manhattan");
        options3Items_02_01.add("Brooklyn");
        options3Items_02.add(options3Items_02_01);
        ArrayList<String> options3Items_02_02 = new ArrayList<String>();
        options3Items_02_02.add("Middletown");
        options3Items_02_02.add("Newburgh");
        options3Items_02.add(options3Items_02_02);

        options3Items.add(options3Items_01);
        options3Items.add(options3Items_02);


        //三级联动效果
        popupWindowLocation.setTitle("Choose a city");
        popupWindowLocation.setPicker(options1Items, options2Items, options3Items, true);
        //设置选择的三级单位
//        popupWindowLocation.setLabels("State", "County", "City");
        //设置默认选中的三级项目
        popupWindowLocation.setSelectOptions(0, 0, 0);
        popupWindowLocation.setCyclic(false, false, false);
        btnLocation.setText(options3Items.get(0).get(0).get(0));
        //监听确定选择按钮
        popupWindowLocation.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                city = options3Items.get(options1).get(option2).get(options3);
                btnLocation.setText(city);
                vMasker.setVisibility(View.GONE);
                state = options1Items.get(options1);

                String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                        + CLIENT_ID
                        + "&client_secret="
                        + CLIENT_SECRET
                        + "&v=20150815&near="
                        + city
                        + ","
                        + state
                        + "&section="
                        + section
                        + "&venuePhotos=1"
                        + "&price="
                        + priceNum;
                urlJsonObj = urlJsonObj.replaceAll(" ", "%20");
                // Showing progress dialog before making http request
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Loading...");
                pDialog.show();
                mVenueList.clear();
                makeJsonObjectRequest(urlJsonObj);

//                System.out.println(urlJsonObj);
            }
        });
    }

    private void makeJsonObjectRequest(String urlJsonObj) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                urlJsonObj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                hidePDialog();
                try {
                    // make an jsonObject in order to parse the response
                    JSONObject jsonObject = response;

                    // make an jsonObject in order to parse the response
                    if (jsonObject.has("response")) {
                        if (jsonObject.getJSONObject("response").has("groups")) {
                            JSONArray jsonArrayGroups = jsonObject.getJSONObject("response").getJSONArray("groups");

                            for (int i = 0; i < jsonArrayGroups.length(); i++) {
                                JSONObject jsonObject1 = jsonArrayGroups.getJSONObject(i);
                                if (jsonObject1.has("items")) {
                                    JSONArray jsonArray1 = jsonObject1.getJSONArray("items");
                                    for (int j = 0; j < jsonArray1.length(); j++) {
                                        FoursquareVenue poi = new FoursquareVenue();
                                        if (jsonArray1.getJSONObject(j).has("venue")) {
                                            poi.setName(jsonArray1.getJSONObject(j).getJSONObject("venue").getString("name"));

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("location")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").has("address")) {
                                                    if (jsonArray1.getJSONObject(j)
                                                            .getJSONObject("venue").getJSONObject("location")
                                                            .has("lat")) {
                                                        poi.setLatitude(jsonArray1
                                                                .getJSONObject(j)
                                                                .getJSONObject("venue").getJSONObject("location")
                                                                .getDouble("lat"));
                                                    }
                                                    if (jsonArray1.getJSONObject(j)
                                                            .getJSONObject("venue").getJSONObject("location")
                                                            .has("lng")) {
                                                        poi.setlongitude(jsonArray1
                                                                .getJSONObject(j)
                                                                .getJSONObject("venue").getJSONObject("location")
                                                                .getDouble("lng"));
                                                    }
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").has("address")) {
                                                        poi.setAddress(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("location").getString("address"));
                                                    }
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("categories")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").length() > 0) {
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).has("icon")) {
                                                        poi.setCategory(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONArray("categories").getJSONObject(0).getString("name"));
                                                    }
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("price")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").has("currency")) {
                                                    poi.setPrice(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("price").getString("message"));
                                                }
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("rating")) {
                                                poi.setRating(jsonArray1.getJSONObject(j).getJSONObject("venue").getDouble("rating"));
                                            }

                                            if (jsonArray1.getJSONObject(j).getJSONObject("venue").has("featuredPhotos")) {
                                                if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").has("items")) {
                                                    if (jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).has("prefix")) {
                                                        poi.setThumbnailUrl(jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).getString("prefix") + "original" + jsonArray1.getJSONObject(j).getJSONObject("venue").getJSONObject("featuredPhotos").getJSONArray("items").getJSONObject(0).getString("suffix"));
                                                    }
                                                }
                                            }


                                            mVenueList.add(poi);
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hidePDialog();
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLocation:
                popupWindowLocation.show();
                WheelView wheelView1 = (WheelView) getActivity().findViewById(R.id.options1);
                WheelView wheelView2 = (WheelView) getActivity().findViewById(R.id.options2);
                WheelView wheelView3 = (WheelView) getActivity().findViewById(R.id.options3);

                wheelView1.setTextSize(20);
                wheelView2.setTextSize(20);
                wheelView3.setTextSize(20);

                wheelView1.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f));
                wheelView2.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));
                wheelView3.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
                break;
            case R.id.btnSortByPrice:
                popupWindowSortbyPrice.show();
                break;
            case R.id.btnSection:
                popupWindowSection.show();
                break;
            default:
                break;
        }
    }

    public void openSearch() {
        getActivity().findViewById(R.id.hide).setVisibility(LinearLayout.INVISIBLE);
        toolbar.setTitle("");
        search.revealFromMenuItem(R.id.action_search, getActivity());
//        for (int x = 0; x < 10; x++) {
//            SearchResult option = new SearchResult("Result "
//                    + Integer.toString(x), getResources().getDrawable(
//                    R.drawable.ic_history));
//            search.addSearchable(option);
//        }
        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                // Hamburger has been clicked
                Toast.makeText(getActivity(), "Menu click",
                        Toast.LENGTH_LONG).show();

            }

        });
        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                // Use this to tint the screen

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged(String term) {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(getActivity(), searchTerm + " Searched",
                        Toast.LENGTH_LONG).show();
                toolbar.setTitle(searchTerm);
                SearchResult option = new SearchResult(searchTerm, getResources().getDrawable(R.drawable.ic_history));
                search.addSearchable(option);
                String urlJsonObj = "https://api.foursquare.com/v2/venues/explore?client_id="
                        + CLIENT_ID
                        + "&client_secret="
                        + CLIENT_SECRET
                        + "&v=20150815&near="
                        + city
                        + ","
                        + state
                        + "&query="
                        + searchTerm
                        + "&venuePhotos=1"
                        + "&price="
                        + priceNum;
                urlJsonObj = urlJsonObj.replaceAll(" ", "%20");
                // Showing progress dialog before making http request
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Loading...");
                pDialog.show();
                mVenueList.clear();
                makeJsonObjectRequest(urlJsonObj);

                System.out.println(urlJsonObj);
            }

            @Override
            public void onResultClick(SearchResult result) {
                //React to result being clicked
            }

            @Override
            public void onSearchCleared() {

            }

        });
    }

    protected void closeSearch() {
        getActivity().findViewById(R.id.hide).setVisibility(LinearLayout.VISIBLE);
        search.hideCircularly(getActivity());
        if (search.getSearchText().isEmpty()) toolbar.setTitle("");
    }
}
