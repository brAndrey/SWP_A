package ru.gdgkazan.simpleweather.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ru.arturvasilov.sqlite.core.SQLite;
import ru.arturvasilov.sqlite.core.Where;
import ru.gdgkazan.simpleweather.ServisClass;
import ru.gdgkazan.simpleweather.data.GsonHolder;
import ru.gdgkazan.simpleweather.data.model.City;
import ru.gdgkazan.simpleweather.data.model.Weather;
import ru.gdgkazan.simpleweather.data.model.WeatherArray;
import ru.gdgkazan.simpleweather.data.model.WeatherCity;
import ru.gdgkazan.simpleweather.data.tables.CityTable;
import ru.gdgkazan.simpleweather.data.tables.RequestTable;
import ru.gdgkazan.simpleweather.data.tables.WeatherCityTable;
import ru.gdgkazan.simpleweather.network.model.NetworkRequest;
import ru.gdgkazan.simpleweather.network.model.Request;
import ru.gdgkazan.simpleweather.network.model.RequestStatus;
import ru.gdgkazan.simpleweather.screen.weatherlist.WeatherListActivity;

/**
 * @author Artur Vasilov
 */
public class NetworkService extends IntentService {

    private static final String LOG= NetworkService.class.getName();
    private static final String REQUEST_KEY = "request";
    private static final String CITY_NAME_KEY = "city_name";

    public static void start(@NonNull Context context,
                             @NonNull Request request,
                             @NonNull String[] citiesNames) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(REQUEST_KEY, GsonHolder.getGson().toJson(request));
        intent.putExtra(CITY_NAME_KEY, citiesNames);

        context.startService(intent);
    }

    @SuppressWarnings("unused")
    public NetworkService() {
        super(NetworkService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Request request = GsonHolder.getGson().fromJson(intent.getStringExtra(REQUEST_KEY), Request.class);
        Request savedRequest = SQLite.get().querySingle(RequestTable.TABLE,
                Where.create().equalTo(RequestTable.REQUEST, request.getRequest()));

        if (savedRequest != null && request.getStatus() == RequestStatus.IN_PROGRESS) {
            return;
        }
        request.setStatus(RequestStatus.IN_PROGRESS);
        SQLite.get().insert(RequestTable.TABLE, request);
        SQLite.get().notifyTableChanged(RequestTable.TABLE);

        if (TextUtils.equals(NetworkRequest.CITY_WEATHER, request.getRequest())) {
            String[] citiesNames = intent.getStringArrayExtra(CITY_NAME_KEY);
            executeCityRequest(request, citiesNames);
        }
    }

    private void executeCityRequest(@NonNull Request request, @NonNull String cityName) {
        try {

            City city = ApiFactory.getWeatherService()
                    .getWeather(cityName)
                    .execute()
                    .body();
            SQLite.get().delete(CityTable.TABLE);
            SQLite.get().insert(CityTable.TABLE, city);
            Log.i("ID sity",city.getName()+"  "+city.getId());
            request.setStatus(RequestStatus.SUCCESS);
        } catch (IOException e) {
            request.setStatus(RequestStatus.ERROR);
            request.setError(e.getMessage());
        } finally {
            SQLite.get().insert(RequestTable.TABLE, request);
            SQLite.get().notifyTableChanged(RequestTable.TABLE);
        }
    }

    private void executeCityRequest(@NonNull Request request, @NonNull String[] citiesName) {
        try {
            ensureSupportedCitiesLoaded(citiesName);
            String citiesIds = getCitiesIds(citiesName);

            List<WeatherArray> weatherArrays = new ArrayList<>();
            String weatherString = weatherArrays.toString();

            WeatherArray city = ApiFactory.getWeatherService()
                    .getWeatherList(citiesIds)
                    .execute()
                    .body();

            if (city!=null){
            SQLite.get().delete(CityTable.TABLE);
            SQLite.get().insert(CityTable.TABLE, city.getCitiesForecasts());}

            //Log.i("ID sity",city.getName()+"  "+city.getId());
            request.setStatus(RequestStatus.SUCCESS);
        } catch (IOException e) {
            request.setStatus(RequestStatus.ERROR);
            request.setError(e.getMessage());
        } finally {
            SQLite.get().insert(RequestTable.TABLE, request);
            SQLite.get().notifyTableChanged(RequestTable.TABLE);
        }
    }

    private String getCitiesIds(String[] citiesNames) {
        // new
        // от сюда мы должны вернуть строку со списком id городов через "," - ю
        Where where = Where.create();
        for (int i = 0; i < citiesNames.length; ++i) {
            if (i != 0)
                where = where.or();
            where = where.equalTo(WeatherCityTable.CITY_NAME, citiesNames[i]);
        }

        Log.i("getCitiesIds ", ServisClass.ArreyToString(citiesNames));
        Log.i("getCitiesIds ", where.toString());
/*
Показывает ошибку в чем причина пока не знаю надо разбираться с "Similar way for RxSQLite" (https://github.com/ArturVasilov/SQLite ) :
        List<Integer> ids = SQLite.get().query(WeatherCityTable.TABLE, where)
                .stream()
                .collect(Collectors.groupingBy(WeatherCity::getCityName))
                .entrySet()
                .stream()
                .map(x -> x.getValue().get(0).getCityId())
                .collect(Collectors.toList());
*/
        List<WeatherCity> citiesQuery =SQLite.get().query(WeatherCityTable.TABLE,where);

        List<Integer> ids = new ArrayList();

        Log.i("getCitiesIds ", "citiesQuery "+ServisClass.ArreyToString(ServisClass.WeaCityToMasName(citiesQuery)));

        Log.i("getCitiesIds ", "citiesQuery "+ServisClass.ArreyToString(ServisClass.WeaCityToMasId(citiesQuery)));

        for (WeatherCity elem : citiesQuery) {
//            for (int i = 0; i < citiesNames.length; ++i) {
//                if (i != 0) {
//                    if (elem.getCityName().equals(citiesNames[i]))
                        ids.add(elem.getCityId());
               // }
           // }
        }
        Log.i("getCitiesIds ", " ids.size() " +ids.size());
        //Log.i("getCitiesIds ", " ids " +ServisClass.ArreyToString(ids));

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ids.size(); ++i) {
            stringBuilder.append(ids.get(i));
            if (i != ids.size() - 1)
                stringBuilder.append(',');
        }
        return stringBuilder.toString();
    }

    private void ensureSupportedCitiesLoaded(String[] citiesName) throws IOException {
        // new тут берем список городов

        WeatherCity weatherCity = SQLite.get().querySingle(WeatherCityTable.TABLE);

        if (weatherCity == null) {

            // более не работает т.к. ссылка не рабочая
//            List<WeatherCity> cities = ApiFactoryCitiesList.getCitiesListHelpService()
//                    .getCitiesList()
//                    .execute()
//                    .body();
            List<WeatherCity> cities = new ArrayList<>();
            for (String cityName : citiesName) {

                City city = ApiFactory.getWeatherService()
                        .getWeather(cityName)
                        .execute()
                        .body();

                Log.i(LOG, "ensureSupportedCitiesLoaded " + city.getName()+"  "+city.getId());

                cities.add(new WeatherCity(city.getId(),city.getName()));

            }

               SQLite.get().insert(WeatherCityTable.TABLE, cities);
        }else Log.i(LOG, "ensureSupportedCitiesLoaded weatherCity != null");

    }
}

