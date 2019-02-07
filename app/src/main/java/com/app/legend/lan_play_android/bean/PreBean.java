package com.app.legend.lan_play_android.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class PreBean implements Parcelable {

    private int id;
    private String url;
    private String port;
    private int select=-1;
    private String name;
    private int number=-1;

    private int ss=1;

    public PreBean() {
    }


    protected PreBean(Parcel in) {
        id = in.readInt();
        url = in.readString();
        port = in.readString();
        select = in.readInt();
        name = in.readString();
        number = in.readInt();
        ss = in.readInt();
    }

    public static final Creator<PreBean> CREATOR = new Creator<PreBean>() {
        @Override
        public PreBean createFromParcel(Parcel in) {
            return new PreBean(in);
        }

        @Override
        public PreBean[] newArray(int size) {
            return new PreBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public int getSs() {
        return ss;
    }

    public void setSs(int ss) {
        this.ss = ss;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(port);
        dest.writeInt(select);
        dest.writeString(name);
        dest.writeInt(number);
        dest.writeInt(ss);
    }
}