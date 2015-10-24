package com.example.ar.moviesproject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ar on 24/10/2015.
 *
 * Basic representation of a Movie object from the MovieDB
 * Implements parcelable using http://shri.blog.kraya.co.uk/2010/04/26/android-parcel-data-to-pass-between-activities-using-parcelable-classes/
 */
public class Movie implements Parcelable{
    private static final String POSTER_URL = "http://image.tmdb.org/t/p/";
    private static final String SMALL_IMAGE = "w342/";
    private static final String LARGE_IMAGE = "w780/";

    int id;
    String title;
    String poster;
    String thumb;
    String synopsis;
    String release;
    double vote;

    public Movie(int id, String title, String poster, String overview, double vote, String release){
        this.id = id;
        this.title = title;
        this.synopsis = overview;
        this.release = release;
        this.vote = vote;
        this.poster = POSTER_URL+LARGE_IMAGE+poster;
        this.thumb = POSTER_URL+SMALL_IMAGE+poster;
    }

    public Movie(Parcel p){
        readFromParcel(p);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(thumb);
        dest.writeString(synopsis);
        dest.writeString(release);
        dest.writeDouble(vote);
    }

    public void readFromParcel(Parcel p){
        id = p.readInt();
        title = p.readString();
        poster = p.readString();
        thumb = p.readString();
        synopsis = p.readString();
        release = p.readString();
        vote = p.readDouble();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator(){
                public Movie createFromParcel(Parcel in){
                    return new Movie(in);
                }
                public Movie[] newArray(int size){
                    return new Movie[size];
                }
            };
}
