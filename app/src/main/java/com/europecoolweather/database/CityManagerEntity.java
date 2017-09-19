package com.europecoolweather.database;

/**
 * 数据库存储的城市天气信息对应类
 *
 * @author GuangKai
 * @version 版本1.0
 */

public class CityManagerEntity {

    //城市
    private String cityName;

    //天气图片
    private String weatherImage;

    //温度
    private String temperature;

    //天气
    private String weather;

    //描述
    private String weatherDescription;

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getWeatherImage() {
        return weatherImage;
    }

    public void setWeatherImage(String weatherImage) {
        this.weatherImage = weatherImage;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }
}
