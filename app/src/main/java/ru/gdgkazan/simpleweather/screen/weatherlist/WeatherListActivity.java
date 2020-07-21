package ru.gdgkazan.simpleweather.screen.weatherlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

//import butterknife.BindView;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.gdgkazan.simpleweather.R;
import ru.gdgkazan.simpleweather.ServisClass;
import ru.gdgkazan.simpleweather.data.model.City;
import ru.gdgkazan.simpleweather.network.NetworkService;
import ru.gdgkazan.simpleweather.network.model.NetworkRequest;
import ru.gdgkazan.simpleweather.network.model.Request;
import ru.gdgkazan.simpleweather.screen.general.LoadingDialog;
import ru.gdgkazan.simpleweather.screen.general.LoadingView;
import ru.gdgkazan.simpleweather.screen.general.SimpleDividerItemDecoration;
import ru.gdgkazan.simpleweather.screen.weather.WeatherActivity;

/**
 * @author Artur Vasilov
 */
public class WeatherListActivity extends AppCompatActivity implements CitiesAdapter.OnItemClick {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty)
    View mEmptyView;

    private static final String LOG=WeatherListActivity.class.getName();

    private CitiesAdapter mAdapter;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_list);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, false));

        mAdapter = new CitiesAdapter(getInitialCities(), this);
        mRecyclerView.setAdapter(mAdapter);
        mLoadingView = LoadingDialog.view(getSupportFragmentManager());

        List<City> cities = getInitialCities();

        loadWeather(cities);

        /**
         * TODO : task
         *
         * 1) Load all cities (pair id-name) if WeatherCityTable is empty from http://openweathermap.org/help/city_list.txt
         * and save them to database
         * 2) Using the information from the first step find id for each current city
         * 3) Load forecast in all cities using one single request http://openweathermap.org/current#severalid
         * 4) Do all this work in NetworkService (you need to modify it to better support multiple requests)
         * 5) Use SQLite API to manage communication
         * 6) Handle configuration changes
         * 7) Modify all the network layer to create a universal way for managing queries with pattern A
         */
    }

    @Override
    public void onItemClick(@NonNull City city) {
        startActivity(WeatherActivity.makeIntent(this, city.getName()));
    }

    @NonNull
    private List<City> getInitialCities() {
        List<City> cities = new ArrayList<>();
        String[] initialCities = getResources().getStringArray(R.array.initial_cities);
        for (String city : initialCities) {
            cities.add(new City(city));
        }
        return cities;
    }


    private void loadWeather ( List<City> citiesNames){

        long time= System.currentTimeMillis();
        //mLoadingView.showLoadingIndicator();

        Request request = new Request(NetworkRequest.CITY_WEATHER);

        //Log.i(LOG+" loadWeather ",ServisClass.ArreyToString(ServisClass.CityToMasName(citiesNames)));

        NetworkService.start(this, request, ServisClass.CityToMasName(citiesNames));

//        if (restart){
//            getSupportLoaderManager().restartLoader(id, Bundle.EMPTY, callbacks);
//        } else {
//            getSupportLoaderManager().initLoader(id, Bundle.EMPTY, callbacks);
//        }

        Log.i(" loadWeather "," Считали за "+String.valueOf(System.currentTimeMillis()-time)+" миисекунд");
    }
}
