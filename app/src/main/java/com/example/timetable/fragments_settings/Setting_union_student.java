package com.example.timetable.fragments_settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetable.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Setting_union_student extends Fragment
{
    private View view;
    private RecyclerView recyclerview_unions;
    private RecyclerViewAdapterUnion recyclerViewAdapterUnion;
    private LinearLayout no_internet_setting_union, small_no_internet_setting_union;
    private SpinKitView progressBar;
    private Toolbar toolbar_setting_union;
    private final String URL = "https://eios.s-vfu.ru/WebApp/#/Rasp/List";
    private List<String> savedUnionList;
    private WebView webview_setting_union;
    private final String all_groups_option = "Все";
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private static final String LAST_PARSE_TIME_KEY = "last_parse_time_";
    private static final String SETTING_UNION_STUDENT_PREFS = "Setting-union-student";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm, dd MMMM", new Locale("ru"));
    private static int lastTimePars;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        setupNetworkCallback();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.setting_union, container, false);
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupWebView();
        loadData(savedInstanceState);

        return view;
    }
    private void initViews()
    {
        recyclerview_unions = view.findViewById(R.id.recyclerview_setting_union);
        no_internet_setting_union = view.findViewById(R.id.no_internet_setting_union);
        small_no_internet_setting_union = view.findViewById(R.id.small_no_internet_setting_union);
        progressBar = view.findViewById(R.id.progress_bar_setting_union);
        toolbar_setting_union = view.findViewById(R.id.toolbar_setting_union);
        webview_setting_union = view.findViewById(R.id.webview_setting_union);
    }

    private void setupToolbar()
    {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar_setting_union);

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar_setting_union.setNavigationOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView()
    {
        recyclerview_unions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAdapterUnion = new RecyclerViewAdapterUnion();
        recyclerview_unions.setAdapter(recyclerViewAdapterUnion);
    }
    private void loadData(Bundle savedInstanceState)
    {
        savedUnionList = loadUnionList();
        if (savedInstanceState != null && savedInstanceState.containsKey("webview_state"))
        {
            webview_setting_union.restoreState(savedInstanceState.getBundle("webview_state"));

            if (savedUnionList != null && !savedUnionList.isEmpty())
            {
                recyclerViewAdapterUnion.RecyclerViewAdapterUnion(savedUnionList);
            }
        }
        else  if (savedUnionList != null && !savedUnionList.isEmpty())
        {
            recyclerViewAdapterUnion.RecyclerViewAdapterUnion(savedUnionList);

            if (isInternetAvailable())
            {
                checkAndLoadWebPage();
            }
            else
            {
                showSmallNoInternetView();
            }
        }
        else
        {
            if (!isInternetAvailable())
            {
                showNoInternetView();
            }
            else
            {
                showProgressBar();
                checkAndLoadWebPage();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Bundle webviewState = new Bundle();
        webview_setting_union.saveState(webviewState);
        outState.putBundle("webview_state", webviewState);
    }

    private void setupNetworkCallback()
    {
        networkCallback = new ConnectivityManager.NetworkCallback()
        {
            @Override
            public void onAvailable(@NonNull Network network)
            {
                super.onAvailable(network);

                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() ->
                    {
                        hideNoInternetViews();
                        checkAndLoadWebPage();
                    });
                }
            }
            @Override
            public void onLost(@NonNull Network network)
            {
                super.onLost(network);
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() ->
                    {
                        if (savedUnionList == null || savedUnionList.isEmpty())
                        {
                            showNoInternetView();
                        }
                        else
                        {
                            showSmallNoInternetView();
                        }
                    });
                }
            }
        };
    }
    private void checkAndLoadWebPage()
    {
        if (isParseNeeded())
        {
            loadWebPage();
        }
    }
    private boolean isParseNeeded()
    {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SETTING_UNION_STUDENT_PREFS, Context.MODE_PRIVATE);
        long lastParseTime = sharedPreferences.getLong(LAST_PARSE_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastParseTime) < TimeUnit.HOURS.toMillis(24);
    }

    private void setupWebView()
    {
        WebSettings webSettings = webview_setting_union.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webview_setting_union.addJavascriptInterface(new JavaScriptInterface(), "Android");
        webview_setting_union.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon)
            {
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() -> showProgressBar());
                }

                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                if (getActivity() != null)
                {
                    getActivity().runOnUiThread(() ->
                    {
                        hideProgressBar();
                        showNoInternetView();
                    });
                }
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:(function() { " +
                        "   var checkDropdown = setInterval(function() {" +
                        "       var dropdownSlots = document.querySelectorAll('.v-select__slot');" +
                        "       console.log('Number of dropdown slots found: ' + dropdownSlots.length);" +
                        "       Android.logDropdownSlotsCount(dropdownSlots.length);" +
                        "      if (dropdownSlots.length > 1) {" +
                        "          clearInterval(checkDropdown);" +
                        "           var dropdownSlot = dropdownSlots[1];" +
                        "            console.log('Dropdown slot found: ' + (dropdownSlot != null));" +
                        "           Android.logDropdownFound(dropdownSlot != null);" +
                        "        if (dropdownSlot) {" +
                        "            var dropdownInputSlot = dropdownSlot.closest('.v-input__control').querySelector('.v-input__slot');" +
                        "            console.log('Dropdown input slot found: ' + (dropdownInputSlot != null));" +
                        "            Android.logDropdownInputSlotFound(dropdownInputSlot != null);" +
                        "            if (dropdownInputSlot) {" +
                        "                dropdownInputSlot.click();" +
                        "            console.log('Clicked on the v-input__slot.');" +
                        "            Android.dropdownClicked('Clicked on v-input__slot successfully');" +
                        "           var checkMenu = setInterval(function() {" +
                        "            var menuContent = document.querySelector('.v-menu__content.menuable__content__active');" +
                        "           if(menuContent){" +
                        "            clearInterval(checkMenu);" +
                        "           console.log('Menu Content found successfully');" +
                        "           Android.logMenuContentFound('Menu Content found successfully');" +
                        "            var listBox = menuContent.querySelector('div[role=\"listbox\"]');" +
                        "           if(listBox){" +
                        "            clearInterval(checkMenu);" +
                        "            console.log('Menu ListBox found successfully');" +
                        "            Android.logListBoxFound('Menu ListBox found successfully');" +
                        "            var checkExist = setInterval(function() {" +
                        "            var allOption = Array.from(listBox.querySelectorAll('.v-list-item')).find(opt => " +
                        "            opt.querySelector('.v-list-item__title').innerText.trim() === '" + all_groups_option + "');" +
                        "           if (allOption) {" +
                        "            clearInterval(checkExist);" +
                        "             allOption.click();" +
                        "              console.log('Selected option ВСЕ');" +
                        "             Android.dropdownClicked('Successfully selected ВСЕ');" +
                        "              setTimeout(function() {" +
                        "              Android.processHTML(document.documentElement.outerHTML);" +
                        "              }, 5000);" +
                        "           } else {" +
                        "              Android.dropdownClicked('ВСЕ option not found');" +
                        "           }" +
                        "         }, 200);" +
                        "       }else{" +
                        "             Android.logListBoxFound('Menu ListBox not found');" +
                        "      }" +
                        "          }else{" +
                        "           Android.logMenuContentFound('Menu Content not found');" +
                        "          }" +
                        "         }, 100);" +
                        "    } else {" +
                        "       Android.dropdownClicked('v-input__slot not found');" +
                        "    }" +
                        "   } else {" +
                        "        Android.dropdownClicked('v-select__slot not found');" +
                        "    }" +
                        " }else{" +
                        "          Android.dropdownClicked('Not enough dropdowns');" +
                        "   }" +
                        "   }, 100);" +
                        "})();");
            }
        });
    }

    private void loadWebPage()
    {
        webview_setting_union.loadUrl(URL);
    }

    private class JavaScriptInterface
    {
        @JavascriptInterface
        public void processHTML(String html)
        {

            List<String> unionItemList = parseHTML(html);

            if (getActivity() != null)
            {
                getActivity().runOnUiThread(() ->
                {
                    hideProgressBar();

                    if (unionItemList != null && !unionItemList.isEmpty())
                    {

                        saveUnionList(unionItemList);
                        recyclerViewAdapterUnion.RecyclerViewAdapterUnion(unionItemList);
                        saveLastParseTime();
                    }
                });
            }
        }
        @JavascriptInterface
        public void logDropdownSlotsCount(int count) {}
        @JavascriptInterface
        public void logDropdownFound(boolean found) {}
        @JavascriptInterface
        public void logDropdownInputSlotFound(boolean found) {}
        @JavascriptInterface
        public void logDropdownSelectionFound(boolean found) {}
        @JavascriptInterface
        public void logDropdownControlFound(boolean found) {}
        @JavascriptInterface
        public void logDropdownInnerFound(boolean found) {}
        @JavascriptInterface
        public void logListBoxFound(String status) {}
        @JavascriptInterface
        public void logMenuContentFound(String status) {}
        @JavascriptInterface
        public void logDropdownCount(int count) {}
        @JavascriptInterface
        public void dropdownClicked(String status) {}
    }

    private List<String> parseHTML(String html)
    {
        List<String> unionItemList = new ArrayList<>();

        if (html != null)
        {
            Document document = Jsoup.parse(html);
            Elements groupRows = document.select("tr");

            for (Element row : groupRows)
            {
                Elements cells = row.select("td");

                if (cells.size() >= 3)
                {
                    Calendar calendar = Calendar.getInstance();
                    String formattedDate = "Обновлено: " + SDF.format(calendar.getTime());
                    String institute = cells.get(1).text();
                    String course = cells.get(2).text();
                    String group = cells.get(0).text();
                    String link = "https://eios.s-vfu.ru/WebApp/" + cells.get(0).select("a").attr("href");
                    unionItemList.add(institute + " | " + course + " | " + group + " | " + link + " | " + formattedDate);
                }
            }
        }
        return unionItemList;
    }

    private void saveUnionList(List<String> unionList)
    {
        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SETTING_UNION_STUDENT_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String jsonList = new Gson().toJson(unionList);
            editor.putString("group-list", jsonList);
            editor.apply();
        }
    }

    private void saveLastParseTime()
    {
        lastTimePars = (int) System.currentTimeMillis();

        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SETTING_UNION_STUDENT_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(LAST_PARSE_TIME_KEY, lastTimePars);
            editor.apply();
        }
    }

    private List<String> loadUnionList()
    {
        if (isAdded() && getContext() != null)
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SETTING_UNION_STUDENT_PREFS, Context.MODE_PRIVATE);
            String jsonList = sharedPreferences.getString("group-list", null);

            if (jsonList != null)
            {
                try
                {
                    TypeToken<List<String>> token = new TypeToken<List<String>>()
                    {};
                    List<String> list = new Gson().fromJson(jsonList, token.getType());

                    return list != null ? list : new ArrayList<>();
                }
                catch (Exception e)
                {
                    return new ArrayList<>();
                }
            }
            return new ArrayList<>();
        }
        return null;
    }
    private void hideNoInternetViews()
    {
        no_internet_setting_union.setVisibility(View.GONE);
        small_no_internet_setting_union.setVisibility(View.GONE);
    }

    private void showNoInternetView()
    {
        no_internet_setting_union.setVisibility(View.VISIBLE);
        small_no_internet_setting_union.setVisibility(View.GONE);

    }

    private void showSmallNoInternetView()
    {
        no_internet_setting_union.setVisibility(View.GONE);
        small_no_internet_setting_union.setVisibility(View.VISIBLE);
    }

    private void showProgressBar()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar()
    {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        webview_setting_union.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        webview_setting_union.onResume();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        registerNetworkCallback();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        unregisterNetworkCallback();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterNetworkCallback();
    }

    private void registerNetworkCallback()
    {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void unregisterNetworkCallback()
    {
        if (connectivityManager != null)
        {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private boolean isInternetAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}