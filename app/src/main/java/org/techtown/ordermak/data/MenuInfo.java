package org.techtown.ordermak.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuInfo implements Parcelable {

    String menu_name;
    String menu_price;
    String menu_desc;

    public MenuInfo(String name, String price, String desc){
        this.menu_name=name;
        this.menu_price=price;
        this.menu_desc=desc;
    }

    protected MenuInfo(Parcel in) {
        menu_name = in.readString();
        menu_price = in.readString();
        menu_desc = in.readString();
    }

    public static final Creator<MenuInfo> CREATOR = new Creator<MenuInfo>() {
        @Override
        public MenuInfo createFromParcel(Parcel in) {
            return new MenuInfo(in);
        }

        @Override
        public MenuInfo[] newArray(int size) {
            return new MenuInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(menu_name);
        dest.writeString(menu_price);
        dest.writeString(menu_desc);
    }
}
