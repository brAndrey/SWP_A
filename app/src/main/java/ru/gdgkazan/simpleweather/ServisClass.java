package ru.gdgkazan.simpleweather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.gdgkazan.simpleweather.data.model.City;
import ru.gdgkazan.simpleweather.data.model.WeatherCity;

public class ServisClass {

    public static String ArreyToString(String[] arrey){
        String rez = "";
        for (String num : arrey) {
            rez = rez+", "+num;
        }
        return rez;
    }

    public static String ArreyToString(ArrayList<Object > arrayList){
        String rez = "";
        int i;
        for ( i=0; i<=arrayList.size();i++) {
          Object  num=arrayList.get(i);
            rez = rez+", "+num.toString();
        }
        return rez;
    }

    public static String ArreyToString(List<Integer> List){
        String rez = "";
        if (List.size() > 0) {
            for (int i = 0; i <= List.size(); i++) {
                Integer num = List.get(i);
                rez = rez + ", " + num.toString();
            }
            return rez;
        } else return null;
    }
    public static String[] CityToMasName(List<City> arrey){
        String[] rez = new String[arrey.size()];
        int k=0;
        for (City num : arrey) {
            rez[k] = num.getName();
            k++;
        }
        return rez;
    }

    public static String[] WeaCityToMasName(List<WeatherCity> arrey){
        String[] rez = new String[arrey.size()];
        int k=0;
        for (WeatherCity num : arrey) {
            rez[k] = num.getCityName();
            k++;
        }
        return rez;
    }

    public static String[] WeaCityToMasId(List<WeatherCity> arrey){
        String[] rez = new String[arrey.size()];
        int k=0;
        for (WeatherCity num : arrey) {
            rez[k] = String.valueOf(num.getCityId());
            k++;
        }
        return rez;
    }


    public static String currentDateandTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime;
    }
    /*
    long time= System.currentTimeMillis();
     Log.i(" loadWeather ",cityName+ " Считали за "+String.valueOf(System.currentTimeMillis()-time)+" миисекунд");


      String[] daysArray = days.toArray(new String[days.size()]);

     */
}
