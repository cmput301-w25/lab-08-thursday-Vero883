package com.example.androidcicd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;

import com.example.androidcicd.movie.Movie;
import com.example.androidcicd.movie.MovieProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

public class MovieProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockMovieCollection;

    @Mock
    private DocumentReference mockDocRef;

    private MovieProvider movieProvider;

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("movies")).thenReturn(mockMovieCollection);
        when(mockMovieCollection.document()).thenReturn(mockDocRef);
        when(mockMovieCollection.document(anyString())).thenReturn(mockDocRef);
        // Setup the movie provider
        MovieProvider.setInstanceForTesting(mockFirestore);
        movieProvider = MovieProvider.getInstance(mockFirestore);
    }

    @Test
    public void testAddMovieSetsId() {
        // Movie to add
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);

        // Define the ID we want to set for the movie
        when(mockDocRef.getId()).thenReturn("123");


        // Add movie and check that we update our movie with the generated id
        movieProvider.addMovie(movie);
        assertEquals("Movie was not updated with correct id.", "123", movie.getId());

        verify(mockDocRef).set(movie);
    }

    @Test
    public void testDeleteMovie() {
        // Create movie and set our id
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");

        // Call the delete movie and verify the firebase delete method was called.
        movieProvider.deleteMovie(movie);
        verify(mockDocRef).delete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForDifferentIds() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        // Set our ID to 1
        movie.setId("1");

        // Make sure the doc ref has a different ID
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error
        movieProvider.updateMovie(movie, "Another Title", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateMovieShouldThrowErrorForEmptyName() {
        Movie movie = new Movie("Oppenheimer", "Thriller/Historical Drama", 2023);
        movie.setId("123");
        when(mockDocRef.getId()).thenReturn("123");

        // Call update movie, which should throw an error due to having an empty name
        movieProvider.updateMovie(movie, "", "Another Genre", 2026);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUniqueTitle() {
        ArrayList<Movie> list = new ArrayList<>();
        list.add(new Movie("Barbie", "Thriller/Historical Drama", 2023));
        movieProvider.setMovies(list);
        Movie movie = new Movie("Barbie", "Thriller/Historical Drama", 2023);
        when(mockDocRef.getId()).thenReturn("12345");
        movieProvider.addMovie(movie);
    }
}