package io.github.the_dagger.movies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    MovieAdapter adapter;

    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater Inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MovieDetails weather = new MovieDetails();
            weather.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    // Will contain the raw JSON response as a string.
    String movieinfo = null;
    private final String LOG_TAG = MovieDetails.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SingleMovie[] movieList = {};
        adapter = new MovieAdapter(getActivity(), Arrays.asList(movieList));
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridView);
        gridview.setAdapter(adapter);

        return rootView;
    }

    public class MovieDetails extends AsyncTask<Void, Void, SingleMovie[]> {
        @Override
        protected void onPostExecute(SingleMovie[] singleMovies) {
            if (singleMovies != null) {
                adapter.clear();
                for (int i = 0; i < singleMovies.length; i++) {
                    SingleMovie oneMovie = singleMovies[i];
                    adapter.add(oneMovie);
                }
            }
            super.onPostExecute(singleMovies);
        }

        private SingleMovie[] getmovieData(String movieInfo)
                throws JSONException {
            final String MDB_RESULT = "results";
            final String MDB_TITLE = "title";
            final String MDB_POSTER = "poster_path";
            JSONObject moviejson = new JSONObject(movieInfo);
            JSONArray movieArray = moviejson.getJSONArray(MDB_RESULT);
          //  Log.e(LOG_TAG, String.valueOf(movieArray));
            String baseURL = "http://image.tmdb.org/t/p/w185/";
            SingleMovie[] movieDetails = new SingleMovie[5];
            for (int i = 0; i < 5; i++) {
                JSONObject currentMovie = movieArray.getJSONObject(i);
              //  Log.e(LOG_TAG, String.valueOf(currentMovie));
                String movietitle = currentMovie.getString(MDB_TITLE);
                String moviePosterendURL = currentMovie.getString(MDB_POSTER);
                String moviePosterURL = baseURL + moviePosterendURL;
             //   Log.e(LOG_TAG,moviePosterURL);
                //String actualTitle = movietitle.getString(MDB_TITLE);
                movieDetails[i] = new SingleMovie(moviePosterURL, movietitle);
            }
            return movieDetails;
        }

        @Override
        protected SingleMovie[] doInBackground(Void... params) {
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=9ee088a6d3ed11d3c10ee27466d39427");
//                Uri.Builder builder = new Uri.Builder();
//                builder.scheme("http")
//                        .authority("api.openweathermap.org")
//                        .appendPath("data")
//                        .appendPath("2.5")
//                        .appendPath("forecast")
//                        .appendPath("daily")
//                        .appendQueryParameter("q", String.valueOf(params[0]))
//                        .appendQueryParameter("mode", "json")
//                        .appendQueryParameter("units", "metric")
//                        .appendQueryParameter("cnt", "7")
//                        .appendQueryParameter("appid", "232730d9c646236b0cf445becaaf2240");
                String movieDbUrl = url.toString();
                Log.v(LOG_TAG, movieDbUrl);
                // Create the request to OpenWeatherMap, and open the connection
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieinfo = buffer.toString();
                Log.v(LOG_TAG, movieinfo);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getmovieData(movieinfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}