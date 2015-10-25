package com.example.ar.moviesproject;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ar on 25/10/2015.
 * AsyncTaskLoader for drawing data from TheMovieDB.org and parsing it into suitable Movie objects
 */
public class MovieLoader extends AsyncTaskLoader<List<Movie>> {
    private static final String LOG_TAG = MovieLoader.class.getSimpleName();
    private static final boolean DEBUG = true;

    private List<Movie> mMovies;

    public MovieLoader(Context c ){
        super(c);
    }

    @Override
    public List<Movie> loadInBackground() {
        if (DEBUG) Log.v(LOG_TAG, "loadInBackground() Called");

        HttpURLConnection urlConnection;
        BufferedReader reader;
        String movieJsonString;

        try{
            // Get the data from DB using sort_by from settings
            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String API_PARAM = "api_key";
            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, Utility.getSortBy(getContext()))
                    .appendQueryParameter(API_PARAM, getContext().getString(R.string.movie_api_key))
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonString = buffer.toString();
            Log.v(LOG_TAG, movieJsonString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR", e);
            return null;
        }

        try {
            return getMovieDataFromJSON(movieJsonString);
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void deliverResult(List<Movie> data) {
        if (isReset()) {
            if (DEBUG) Log.w(LOG_TAG, "+++ Warning! An async query came in while the Loader was reset! +++");

            if (data != null) {
                releaseResources(data);
                return;
            }
        }

        List<Movie> old = mMovies;
        mMovies = data;

        if (isStarted()){
            if (DEBUG) Log.v(LOG_TAG, "Delivering results to LoaderManager");
            super.deliverResult(data);
        }

        if (old != null && old != data){
            releaseResources(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (DEBUG) Log.v(LOG_TAG, "onStartLoading()");

        if (mMovies != null){
            deliverResult(mMovies);
        }

        if (takeContentChanged()){
            forceLoad();
        } else if (mMovies == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mMovies != null){
            releaseResources(mMovies);
            mMovies = null;
        }
    }

    @Override
    public void onCanceled(List<Movie> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    private void releaseResources(List<Movie> data){
        // Place holder for Part 2
    }

    /**
     * Parses JSON data from MovieDB into Movie objects
     *
     * @param movieJsonString JSON string
     * @return Movie[] array of Movie objects with parsed JSON data
     * @throws JSONException
     */
    private List<Movie> getMovieDataFromJSON(String movieJsonString) throws JSONException{
        final String MDB_RESULTS = "results";
        final String MDB_ID = "id";
        final String MDB_POSTER = "poster_path";
        final String MDB_TITLE = "original_title";
        final String MDB_OVERVIEW = "overview";
        final String MDB_VOTE = "vote_average";
        final String MDB_RELEASE = "release_date";

        JSONObject movieJson = new JSONObject(movieJsonString);
        JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

        List<Movie> moviesArray = new ArrayList<>(); // Movie DB pages contain 20 elements. Consider making dynamic?
        for (int i = 0; i < movieArray.length(); i++){

            JSONObject movie = movieArray.getJSONObject(i);
            int id = movie.getInt(MDB_ID);
            String title = movie.getString(MDB_TITLE);
            String poster = movie.getString(MDB_POSTER);
            String overview = movie.getString(MDB_OVERVIEW);
            double vote = movie.getDouble(MDB_VOTE);
            String release = movie.getString(MDB_RELEASE);


            moviesArray.add(new Movie(id, title, poster, overview, vote, release));
        }

        for (Movie m : moviesArray) {
            if (m != null) {
                Log.v(LOG_TAG, m.toString());
            }
        }
        return moviesArray;
    }
}
