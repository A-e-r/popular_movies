package com.example.ar.moviesproject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

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
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Movie>> {

    MovieArrayAdapter mMovieAdapter;
    private ArrayList<Movie> movieList;
    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;
    private static final int LOADER_ID = 0;
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_KEY = "movies";

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie m);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_KEY)){
            movieList = new ArrayList<Movie>();
        } else {
            movieList = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, movieList);

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovieAdapter = new MovieArrayAdapter(getActivity(), movieList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(mMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie m = mMovieAdapter.getItem(position);

                if (m != null) {
                    ((Callback) getActivity()).onItemSelected(m);
                    Toast.makeText(getContext(), m.title, Toast.LENGTH_SHORT).show();
                }
                mPosition = position;
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra(Movie.class.getName(), m);
//                startActivity(intent);
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovies(){
        Loader<List<Movie>> loader = getLoaderManager().getLoader(LOADER_ID);
        if (loader != null){
            loader.forceLoad();
        }
//        FetchMoviesTask moviesTask = new FetchMoviesTask();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String sort_by = prefs.getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_popularity));
//        moviesTask.execute(sort_by);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new MovieLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mMovieAdapter.setData(data);
        if (mPosition != GridView.INVALID_POSITION){
            mGridView.smoothScrollToPosition(mPosition);
        }

        //show data?
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mMovieAdapter.setData(null);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]>{
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Fetch movie data from server, parse and return objects
         * @param params params[0] is sort_by value
         * @return array of Movie objects
         */
        @Override
        protected Movie[] doInBackground(String... params) {

            if (params.length == 0){
                return null;
            }


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviewJsonString = null;

            try{
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(API_PARAM, getString(R.string.movie_api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviewJsonString = buffer.toString();
                Log.v(LOG_TAG, moviewJsonString);
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR", e);
                return null;
            }

            try {
                return getMovieDataFromJSON(moviewJsonString);
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return null;
        }

        @Override
        /**
         * If there is new data, delete old and repoulate
         */
        protected void onPostExecute(Movie[] results) {
            if (results != null){
                mMovieAdapter.clear();
                for (Movie movie : results){
                    mMovieAdapter.add(movie);
                }
            }
        }

        /**
         * Parses JSON data from MovieDB into Movie objects
         *
         * @param movieJsonString JSON string
         * @return Movie[] array of Movie objects with parsed JSON data
         * @throws JSONException
         */
        private Movie[] getMovieDataFromJSON(String movieJsonString) throws JSONException{
            final String MDB_RESULTS = "results";
            final String MDB_ID = "id";
            final String MDB_POSTER = "poster_path";
            final String MDB_TITLE = "original_title";
            final String MDB_OVERVIEW = "overview";
            final String MDB_VOTE = "vote_average";
            final String MDB_RELEASE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonString);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            Movie[] moviesArray = new Movie[20]; // Movie DB pages contain 20 elements. Consider making dynamic?
            for (int i = 0; i < movieArray.length(); i++){

                JSONObject movie = movieArray.getJSONObject(i);
                int id = movie.getInt(MDB_ID);
                String title = movie.getString(MDB_TITLE);
                String poster = movie.getString(MDB_POSTER);
                String overview = movie.getString(MDB_OVERVIEW);
                double vote = movie.getDouble(MDB_VOTE);
                String release = movie.getString(MDB_RELEASE);


                moviesArray[i] = new Movie(id, title, poster, overview, vote, release);
            }

            for (Movie m : moviesArray) {
                if (m != null) {
                    Log.v(LOG_TAG, m.toString());
                }
            }
            return moviesArray;
        }
    }
}
